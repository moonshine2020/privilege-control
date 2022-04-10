package com.mx.privilege.validator;

import com.mx.privilege.pojo.ValidateMetadata;

/**
 * @author mengxu
 * @date 2022/4/6 22:07
 */
public interface Validator {
    /**
     * 校验参数是否符合行级数据权限
     * @param validateMetadata
     * @return
     */
    boolean check(ValidateMetadata validateMetadata);
}
