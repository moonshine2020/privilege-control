package com.mx.privilege.validator;

import com.mx.privilege.exception.NoRowPrivilegeException;
import com.mx.privilege.pojo.ValidateMetadata;
import com.mx.privilege.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mengxu
 * @date 2022/4/6 22:07
 */
public class NullValueValidator implements Validator {

    private static Logger log = LoggerFactory.getLogger(NullValueValidator.class);

    static {
        NullValueValidator nullValueValidator = new NullValueValidator();
        ValidatorFactory.addValidator(null, nullValueValidator);
    }

    @Override
    public boolean check(ValidateMetadata validateMetadata) {

        List<String> privilegeList = validateMetadata.getPrivilegeList();
        Object param = validateMetadata.getParentObject();
        String fieldName = validateMetadata.getFieldName();

        if (privilegeList.isEmpty()) {
            log.error("RowPrivilegeAspect.checkIfParamNull permission deny, privilegeList is null");
            throw new NoRowPrivilegeException();
        }

        try {
            // 将用户所有应有的权限赋值过去
            Field field = param.getClass().getDeclaredField(fieldName);
            Method setMethod = param.getClass().getDeclaredMethod("set" + StringUtil.firstToUpper(fieldName), field.getType());

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
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return true;
    }
}
