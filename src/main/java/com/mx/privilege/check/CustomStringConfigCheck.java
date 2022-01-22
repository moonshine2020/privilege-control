package com.mx.privilege.check;

import com.alibaba.fastjson.JSON;
import com.mx.privilege.annotation.RowPrivilege;
import com.mx.privilege.annotation.RowPrivilegeProperty;
import com.mx.privilege.aspect.RowPrivilegeService;
import com.mx.privilege.constant.Constant;
import com.mx.privilege.exception.NoRowPrivilegeException;
import com.mx.privilege.pojo.UserDto;
import com.mx.privilege.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author mengxu
 * @date 2022/1/22 11:31
 */
@Slf4j
@Component
public class CustomStringConfigCheck {

    @Value("#{${row.privilege.field}}")
    private Map<String, String> fieldNameMap;


    @Resource
    private RowPrivilegeService rowPrivilegeService;

    @Resource
    private RedisUtil redisUtil;

    public boolean check(JoinPoint joinPoint) {

        if (ArrayUtils.isEmpty(joinPoint.getArgs())) {
            log.error("RowPrivilegeAspect.invoke request have not any Parameter, Please check request");
            throw new NoRowPrivilegeException();
        }

        MethodSignature ms = (MethodSignature) joinPoint.getSignature();
        Method method = ms.getMethod();
        RowPrivilege rowPrivilege = method.getAnnotation(RowPrivilege.class);
        if(rowPrivilege == null) {
            log.error("RowPrivilegeAspect.invoke no have RowPrivilege annotation!");
            throw new NoRowPrivilegeException();
        }

        String[] value =  rowPrivilege.value();
        List<String> checkField = Arrays.asList(value);
        try {

            // TODO 判断是否为超级管理员, 管理员直接放行, 待开发, 目前不支持,无法判断用户是否为超级管理员
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            String token = request.getHeader(Constant.TOKEN);

            if(StringUtils.isBlank(token)) {
                log.error("token is bland, Please check token!");
                throw new NoRowPrivilegeException();
            }

            // 校验token的有效性并获取用户信息
            Long userId;
            Object info = redisUtil.get(Constant.USE_TOKEN_KEY_PREFIX + token);
            if (info != null) {
                UserDto userInfoVO = JSON.parseObject(info.toString(), UserDto.class);
                userId = userInfoVO.getId();
            } else {
                log.warn("RowPrivilegeService.listPrivilege token invalid");
                throw new NoRowPrivilegeException();
            }

            if (userId == null) {
                log.error("RowPrivilegeAspect userId is null !");
                throw new NoRowPrivilegeException();
            }

            for (Object arg : joinPoint.getArgs()) {
                if (check(userId, arg, checkField)) {
                    break;
                }
            }
        } catch (NoRowPrivilegeException e) {
            throw e;
        } catch (Exception e) {
            log.error("RowPrivilegeAspect.invoke error!", e);
            throw new NoRowPrivilegeException();
        }
        return true;
    }

    /**
     * 用户权限校验
     *
     * @param userId
     * @param param
     * @return
     */
    private boolean check(Long userId, Object param, List<String> checkField) {

        RowPrivilegeProperty rowPrivilegeProperty = param.getClass().getAnnotation(RowPrivilegeProperty.class);

        if(rowPrivilegeProperty == null) {
            return true;
        }

        Field[] fields = param.getClass().getDeclaredFields();
        for (Field field : fields) {

            // 成员变量在fieldNameMap配置之中且在RowPrivilege注解校验范围, RowPrivilege校验范围为空则进行允许全部类型进行校验
            boolean checkable = field != null && field.getAnnotation(RowPrivilegeProperty.class) != null;
            // 遍历输入参数, 找到需要进行权限控制的成员变量, 通过反射获取该成员变量的值
            // 将此值与用户应有的权限值进行比较
            if (checkable) {
                String fieldName = field.getName();
                try {
                    Method getMethod = param.getClass().getDeclaredMethod("get" + this.firstToUpper(fieldName));
                    Object result = getMethod.invoke(param);

                    if (result == null) {
                        List<String> privilegeList = rowPrivilegeService.listPrivilegeCode(userId, fieldNameMap.get(fieldName));
                        checkIfParamNull(param, fieldName, privilegeList);
                    } else if (result instanceof List) {
                        List<String> sourceList = (List<String>) result;
                        List<String> privilegeList = rowPrivilegeService.listPrivilegeCode(userId, fieldNameMap.get(fieldName));

                        if (sourceList.isEmpty() && checkIfParamNull(param, fieldName, privilegeList)) {
                            return true;
                        }

                        checkPrivilege(sourceList, privilegeList, userId, fieldName);
                    } else if (result instanceof String) {
                        List<String> sourceList = new ArrayList<>();
                        sourceList.add(result.toString());
                        List<String> privilegeList = rowPrivilegeService.listPrivilegeCode(userId, fieldNameMap.get(fieldName));
                        checkPrivilege(sourceList, privilegeList, userId, fieldName);
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
     * @param privilegeList
     * @return
     */
    private boolean checkPrivilege(List<String> sourceList, List<String> privilegeList, Long userId, String fieldName) {
        // 通过请求中的参数范围与用户所拥有的权限范围取交集, 判断剩余结果与原结果的差异
        // 相等则拥有权限 不等则无权限
        List<String> originList = new ArrayList<>(sourceList);
        sourceList.retainAll(privilegeList);

        if (originList.size() != sourceList.size()) {
            log.error("RowPrivilegeAspect.invoke user has no privilege, userId = {}, fieldName = {}, originList = {}, sourceList = {}", userId, fieldName, originList, sourceList);
            throw new NoRowPrivilegeException();
        }
        return true;
    }
}
