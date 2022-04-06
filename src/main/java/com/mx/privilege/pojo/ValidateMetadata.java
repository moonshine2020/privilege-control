package com.mx.privilege.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author mengxu
 * @date 2022/4/6 23:10
 */
@Data
public class ValidateMetadata {
    /**
     * 需要被校验的对象
     */
    private Object target;
    /**
     * target对象对应的权限集合
     */
    private List<String> privilegeList;
    /**
     * privilegeList对应的用户id
     */
    private String userId;
    /**
     * target对象的变量名称
     */
    private String fieldName;
}
