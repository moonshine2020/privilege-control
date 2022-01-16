package com.mx.privilege.pojo;

import lombok.Data;
import lombok.ToString;

/**
 * @Author: Xu.Meng1
 * @Date: 2022/1/11 18:38
 */
@Data
@ToString
public class PermissionDto {
    private Long roleId;
    private String name;
    private String code;
}
