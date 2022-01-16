package com.mx.privilege.aspect;

import com.mx.privilege.exception.NoRowPrivilegeException;
import com.mx.privilege.pojo.PermissionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: Xu.Meng1
 * @Date: 2022/1/10 19:43
 */
@Slf4j
@Component
public class RowPrivilegeService {


    public List<PermissionDto> listPrivilege(Long userId, String dateType) {
        try {

            return Collections.emptyList();

        } catch (Exception e) {
            log.error("RowPrivilegeService.listPrivilege error, userId = {}, dateType = {}", userId, dateType, e);
            throw new NoRowPrivilegeException();
        }
    }
    public List<String> listPrivilegeCode(Long userId, String dataType) {
        List<PermissionDto> permissionDtoList = this.listPrivilege(userId, dataType);
        return permissionDtoList.stream().map(PermissionDto::getCode).collect(Collectors.toList());
    }
}
