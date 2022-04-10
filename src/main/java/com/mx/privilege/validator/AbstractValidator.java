package com.mx.privilege.validator;

import com.mx.privilege.exception.NoRowPrivilegeException;
import com.mx.privilege.pojo.ValidateMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * @author mengxu
 * @date 2022/4/10 21:46
 */
public abstract class AbstractValidator implements Validator {

    private static Logger log = LoggerFactory.getLogger(CollectionTypeValidator.class);

    /**
     * 参数校验
     * @param validateMetadata
     * @return
     */
    @Override
    public boolean check(ValidateMetadata validateMetadata) {
        if (CollectionUtils.isEmpty(validateMetadata.getPrivilegeList())) {
            log.error("RowPrivilegeAspect.checkIfParamNull permission deny, privilegeList is null");
            throw new NoRowPrivilegeException();
        }

        this.handle(validateMetadata);
        return true;
    }

    /**
     * 处理各个类型的校验方法
     *
     * @param validateMetadata
     * @return
     */
    public abstract boolean handle(ValidateMetadata validateMetadata);
}
