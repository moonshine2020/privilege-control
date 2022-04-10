package com.mx.privilege.validator;

import com.mx.privilege.exception.NoRowPrivilegeException;
import com.mx.privilege.exception.NotSupportTypeRowPrivilegeException;
import com.mx.privilege.pojo.ValidateMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author mengxu
 * @date 2022/4/6 22:33
 * <p>
 * 基本类型校验器
 */
public class PrimitiveTypeValidator extends AbstractValidator {

    private static Logger log = LoggerFactory.getLogger(PrimitiveTypeValidator.class);

    static {
        PrimitiveTypeValidator primitiveTypeValidator = new PrimitiveTypeValidator();
        ValidatorFactory.addValidator(Integer.class, primitiveTypeValidator);
        ValidatorFactory.addValidator(Long.class, primitiveTypeValidator);
        ValidatorFactory.addValidator(String.class, primitiveTypeValidator);
        ValidatorFactory.addValidator(Short.class, primitiveTypeValidator);
        ValidatorFactory.addValidator(Double.class, primitiveTypeValidator);
        ValidatorFactory.addValidator(Float.class, primitiveTypeValidator);
        ValidatorFactory.addValidator(Boolean.class, primitiveTypeValidator);
        ValidatorFactory.addValidator(Character.class, primitiveTypeValidator);
    }

    @Override
    public boolean handle(ValidateMetadata validateMetadata) {
        if (validateMetadata.getParentObject() == null) {
            throw new NotSupportTypeRowPrivilegeException("Type not support");
        }

        Object param = validateMetadata.getParentObject();
        // 暂不支持
        if (validateMetadata.getParentObject() == null) {
            throw new NotSupportTypeRowPrivilegeException("Type not support");
        }

        try {
            // 将用户所有应有的权限赋值过去
            if (validateMetadata.getTarget() == null) {
                Method setMethod = validateMetadata.getTargetSetMethod();
                setMethod.invoke(param, validateMetadata.getPrivilegeList().get(0));
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("PrimitiveTypeValidator.check error, validateMetadata = {}", validateMetadata, e);
        }
        return true;
    }
}
