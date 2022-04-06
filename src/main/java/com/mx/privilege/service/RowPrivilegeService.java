package com.mx.privilege.service;

import com.mx.privilege.pojo.PermissionDto;

import java.util.List;

/**
 * @Author: Xu.Meng1
 * @Date: 2022/1/10 19:43
 */
public interface RowPrivilegeService {

    /**
     * 获取用户权限
     * @param userId
     * @param dateType
     * @return
     */
     List<PermissionDto> listPrivilege(Long userId, String dateType) ;

    /**
     * 获取用户权限code
     * @param userId
     * @param dataType
     * @return
     */
     List<String> listPrivilegeCode(Long userId, String dataType);
}
