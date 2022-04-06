package com.mx.privilege.service.impl;

import com.mx.privilege.exception.NoRowPrivilegeException;
import com.mx.privilege.pojo.PermissionDto;
import com.mx.privilege.service.RowPrivilegeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author mengxu
 * @date 2022/3/26 12:26
 */
@Slf4j
@Service
public class RowPrivilegeServiceImpl implements RowPrivilegeService {

    @Override
    public List<PermissionDto> listPrivilege(Long userId, String dateType) {
        try {

            return Collections.emptyList();

        } catch (Exception e) {
            log.error("RowPrivilegeService.listPrivilege error, userId = {}, dateType = {}", userId, dateType, e);
            throw new NoRowPrivilegeException();
        }
    }

    @Override
    public List<String> listPrivilegeCode(Long userId, String dataType) {
        List<PermissionDto> permissionDtoList = this.listPrivilege(userId, dataType);
        return permissionDtoList.stream().map(PermissionDto::getCode).collect(Collectors.toList());
    }
}
