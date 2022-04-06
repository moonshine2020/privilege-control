package com.mx.privilege.validator;

import com.mx.privilege.pojo.ValidateMetadata;

/**
 * @author mengxu
 * @date 2022/4/6 22:33
 *
 * 基本类型校验器
 */
public class PrimitiveTypeValidator implements Validator {

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
    public boolean check(ValidateMetadata validateMetadata) {
        return false;
    }
}
