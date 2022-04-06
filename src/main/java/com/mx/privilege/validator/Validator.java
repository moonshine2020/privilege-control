package com.mx.privilege.validator;

import com.mx.privilege.pojo.ValidateMetadata;

/**
 * @author mengxu
 * @date 2022/4/6 22:07
 */
public interface Validator {
    boolean check(ValidateMetadata validateMetadata);
}
