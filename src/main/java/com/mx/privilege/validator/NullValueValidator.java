package com.mx.privilege.validator;

import com.mx.privilege.pojo.ValidateMetadata;

/**
 * @author mengxu
 * @date 2022/4/6 22:07
 */
public class NullValueValidator implements Validator {

    static {
        NullValueValidator nullValueValidator = new NullValueValidator();
        ValidatorFactory.addValidator(null, nullValueValidator);
    }

    @Override
    public boolean check(ValidateMetadata validateMetadata) {
        return false;
    }
}
