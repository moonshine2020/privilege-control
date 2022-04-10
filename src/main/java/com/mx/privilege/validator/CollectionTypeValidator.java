package com.mx.privilege.validator;

import com.mx.privilege.exception.NoRowPrivilegeException;
import com.mx.privilege.exception.NotSupportTypeRowPrivilegeException;
import com.mx.privilege.pojo.ValidateMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mengxu
 * @date 2022/4/10 21:32
 */
public class CollectionTypeValidator extends AbstractValidator {

    private static Logger log = LoggerFactory.getLogger(CollectionTypeValidator.class);


    @Override
    public boolean handle(ValidateMetadata validateMetadata) {
        if (validateMetadata.getParentObject() == null) {
            throw new NotSupportTypeRowPrivilegeException("Type not support");
        }

        Object parent = validateMetadata.getParentObject();
        Object target = validateMetadata.getTarget();
        try {
            // 将用户所有应有的权限赋值过去
            if (validateMetadata.getTarget() == null || ((List<String>) target).isEmpty()) {
                Method setMethod = validateMetadata.getTargetSetMethod();
                setMethod.invoke(parent, validateMetadata.getPrivilegeList());
            } else {
                checkPrivilege(validateMetadata);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("CollectionTypeValidator.check error, validateMetadata = {}", validateMetadata, e);
        }
        return true;
    }

    /**
     * 比较用户请求的权限与用户拥有权限的差异
     *
     * @param validateMetadata
     * @return
     */
    private boolean checkPrivilege(ValidateMetadata validateMetadata) {
        // 通过请求中的参数范围与用户所拥有的权限范围取交集, 判断剩余结果与原结果的差异
        // 相等则拥有权限 不等则无权限
        List<String> sourceList = (List<String>)validateMetadata.getTarget();
        List<String> originList = new ArrayList<>(sourceList);
        sourceList.retainAll(validateMetadata.getPrivilegeList());

        if (originList.size() != sourceList.size()) {
            log.error("CollectionTypeValidator.checkPrivilege user has no privilege, validateMetadata = {}", validateMetadata);
            throw new NoRowPrivilegeException();
        }
        return true;
    }
}
