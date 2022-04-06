package com.mx.privilege.validator;

import com.alibaba.fastjson.JSON;
import com.mx.privilege.annotation.RowPrivilegeProperty;
import com.mx.privilege.exception.NoRowPrivilegeException;
import com.mx.privilege.pojo.ValidateMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author mengxu
 * @date 2022/4/6 22:58
 * 复杂对象类型校验器
 */
public class ComplexTypeValidator implements Validator {

    private static Logger log = LoggerFactory.getLogger(ComplexTypeValidator.class);

    static {
        ComplexTypeValidator complexTypeValidator = new ComplexTypeValidator();
        ValidatorFactory.addValidator(ValidatorFactory.class, complexTypeValidator);
    }

    @Override
    public boolean check(ValidateMetadata validateMetadata) {

        Object param = validateMetadata.getTarget();
        List<String> privilegeList = validateMetadata.getPrivilegeList();
        String userId = validateMetadata.getUserId();

        Field[] fields = param.getClass().getDeclaredFields();
        for (Field field : fields) {

            // 成员变量在fieldNameMap配置之中且在RowPrivilege注解校验范围, RowPrivilege校验范围为空则进行允许全部类型进行校验
            boolean checkable = field != null && field.getAnnotation(RowPrivilegeProperty.class) != null;
            // 遍历输入参数, 找到需要进行权限控制的成员变量, 通过反射获取该成员变量的值
            // 将此值与用户应有的权限值进行比较
            if (checkable) {
                String fieldName = field.getName();
                RowPrivilegeProperty rowPrivilegeProperty = field.getAnnotation(RowPrivilegeProperty.class);
                try {
                    Method getMethod = param.getClass().getDeclaredMethod("get" + this.firstToUpper(fieldName));
                    Object result = getMethod.invoke(param);

                    Validator validator = ValidatorFactory.getValidator(result.getClass());
                    if (validator instanceof ComplexTypeValidator) {
                        ValidateMetadata complexValidateMetadata = new ValidateMetadata();
                        complexValidateMetadata.setFieldName(fieldName);
                        complexValidateMetadata.setPrivilegeList(Arrays.asList(rowPrivilegeProperty.value()));
                        complexValidateMetadata.setTarget(result);
                        complexValidateMetadata.setUserId(validateMetadata.getUserId());
                        if (!validator.check(complexValidateMetadata)) {
                            return false;
                        }
                    }

                    if (result == null) {
                        checkIfParamNull(param, fieldName, privilegeList);
                    } else if (result instanceof List) {
                        List<String> sourceList = (List<String>) result;
                        if (sourceList.isEmpty() && checkIfParamNull(param, fieldName, privilegeList)) {
                            return true;
                        }

                        checkPrivilege(sourceList, validateMetadata);
                    } else if (result instanceof String) {
                        List<String> sourceList = new ArrayList<>();
                        sourceList.add(result.toString());
                        checkPrivilege(sourceList, validateMetadata);
                    } else {
                        log.error("RowPrivilegeAspect.check result is error, result = {}", JSON.toJSONString(result));
                        throw new NoRowPrivilegeException();
                    }
                } catch (NoRowPrivilegeException e) {
                    throw e;
                } catch (Exception e) {
                    log.error("RowPrivilegeAspect.check check error, fieldName = {}", fieldName, e);
                    throw new NoRowPrivilegeException();
                }
            } else if (log.isDebugEnabled()) {
                log.debug("field [{}] no need row privilege check!", field != null? field.getName():null);
            }
        }
        return true;
    }

    private String firstToUpper(String fieldName) {
        // 进行字母的ascii编码前移，效率要高于截取字符串进行转换的操作
        char[] cs = fieldName.toCharArray();
        cs[0] -= 32;
        return String.valueOf(cs);
    }

    /**
     * 比较用户请求的权限与用户拥有权限的差异
     *
     * @param sourceList
     * @param validateMetadata
     * @return
     */
    private boolean checkPrivilege(List<String> sourceList, ValidateMetadata validateMetadata) {
        // 通过请求中的参数范围与用户所拥有的权限范围取交集, 判断剩余结果与原结果的差异
        // 相等则拥有权限 不等则无权限
        List<String> originList = new ArrayList<>(sourceList);
        sourceList.retainAll(validateMetadata.getPrivilegeList());

        if (originList.size() != sourceList.size()) {
            log.error("RowPrivilegeAspect.invoke user has no privilege, userId = {}, fieldName = {}, originList = {}, privilegeList = {}, sourceList = {}"
                    , validateMetadata.getUserId(), validateMetadata.getFieldName(), originList, validateMetadata.getPrivilegeList(), sourceList);
            throw new NoRowPrivilegeException();
        }
        return true;
    }

    /**
     * //TODO 如果参数为空, 则为查询权限内全部 此为特殊处理, 以后修改
     * 如果请求参数中需要校验的属性为空, 此时特殊处理, 将该用户的该属性所有权限赋值给属性字段
     *
     * @param param
     * @param fieldName
     * @param privilegeList
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private boolean checkIfParamNull(Object param, String fieldName, List<String> privilegeList) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        if (privilegeList.isEmpty()) {
            log.error("RowPrivilegeAspect.checkIfParamNull permission deny, privilegeList is null");
            throw new NoRowPrivilegeException();
        }
        // 将用户所有应有的权限赋值过去
        Field field = param.getClass().getDeclaredField(fieldName);
        Method setMethod = param.getClass().getDeclaredMethod("set" + this.firstToUpper(fieldName), field.getType());

        // 当权限中包含ALL权限时仅进行全部查询
        // 权宜之计
        if (field.getType() == List.class) {
            setMethod.invoke(param, new ArrayList<>());
            return true;
        } else if (field.getType() == String.class) {
            setMethod.invoke(param, "");
            return true;
        }

        if (field.getType() == List.class) {
            setMethod.invoke(param, privilegeList);
        } else if (field.getType() == String.class) {
            setMethod.invoke(param, privilegeList.get(0));
        } else {
            log.error("RowPrivilegeAspect.checkIfParamNull row privilege not support [{}]", field.getType());
            throw new NoRowPrivilegeException();
        }
        return true;
    }

}
