package com.mx.privilege.validator;

import com.mx.privilege.annotation.RowPrivilegeProperty;
import com.mx.privilege.exception.NoRowPrivilegeException;
import com.mx.privilege.pojo.ValidateMetadata;
import com.mx.privilege.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author mengxu
 * @date 2022/4/6 22:58
 * 复杂对象类型校验器
 */
public class ComplexTypeValidator extends AbstractValidator {

    private static Logger log = LoggerFactory.getLogger(ComplexTypeValidator.class);

    static {
        ComplexTypeValidator complexTypeValidator = new ComplexTypeValidator();
        ValidatorFactory.addValidator(ValidatorFactory.class, complexTypeValidator);
    }

    @Override
    public boolean handle(ValidateMetadata validateMetadata) {

        Object param = validateMetadata.getTarget();
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
                    Method getMethod = param.getClass().getDeclaredMethod("get" + StringUtil.firstToUpper(fieldName));
                    Method setMethod = param.getClass().getDeclaredMethod("set" + StringUtil.firstToUpper(fieldName));
                    Object result = getMethod.invoke(param);

                    Validator validator = ValidatorFactory.getValidator(field.getType());

                    ValidateMetadata childValidateMetadata = new ValidateMetadata();
                    childValidateMetadata.setFieldName(fieldName);
                    childValidateMetadata.setPrivilegeList(Arrays.asList(rowPrivilegeProperty.value()));
                    childValidateMetadata.setTarget(result);
                    childValidateMetadata.setUserId(validateMetadata.getUserId());
                    childValidateMetadata.setParentObject(param);
                    childValidateMetadata.setTargetSetMethod(setMethod);
                    childValidateMetadata.setField(field);
                    return validator.check(childValidateMetadata);
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
}
