package com.mx.privilege.aspect;

import com.alibaba.fastjson.JSON;
import com.mx.privilege.annotation.RowPrivilegeProperty;
import com.mx.privilege.constant.Constant;
import com.mx.privilege.exception.NoRowPrivilegeException;
import com.mx.privilege.pojo.UserDto;
import com.mx.privilege.service.RowPrivilegeService;
import com.mx.privilege.util.RedisUtil;
import com.mx.privilege.validator.Validator;
import com.mx.privilege.validator.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 行级数据权限校验
 *
 * @Author: Xu.Meng1
 * @Date: 2022/1/7 15:11
 */
@Slf4j
@Aspect
@Component
public class RowPrivilegeAspect {

    @Value("#{${row.privilege.field}}")
    private Map<String, String> fieldNameMap;

    @Resource
    private RowPrivilegeService rowPrivilegeService;

    @Resource
    private RedisUtil redisUtil;

    /**
     * @Description: 定义需要拦截的切面
     * @Return: void
     *
     * 拦截被注解的方法和被注解的类中的所有方法
     **/
    @Pointcut("@annotation(com.mx.privilege.annotation.RowPrivilege) || @target(com.mx.privilege.annotation.RowPrivilege)")
    public void methodArgs() {

    }

    @Before("methodArgs()")
    public void invoke(JoinPoint joinPoint) {
        try {

            // TODO 判断是否为超级管理员, 管理员直接放行, 待开发, 目前不支持,无法判断用户是否为超级管理员
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            String token = request.getHeader(Constant.TOKEN);

            if(StringUtils.isBlank(token)) {
                log.error("RowPrivilegeAspect.invoke token is blank, Please check token!");
                throw new NoRowPrivilegeException();
            }

            // 校验token的有效性并获取用户信息
            Long userId;
            Object info = redisUtil.get(Constant.USE_TOKEN_KEY_PREFIX + token);
            if (info != null) {
                UserDto userInfoVO = JSON.parseObject(info.toString(), UserDto.class);
                userId = userInfoVO.getId();
            } else {
                log.warn("RowPrivilegeAspect.invoke token invalid");
                throw new NoRowPrivilegeException();
            }

            if (userId == null) {
                log.error("RowPrivilegeAspect.invoke userId is null !");
                throw new NoRowPrivilegeException();
            }

            for (Object arg : joinPoint.getArgs()) {
                    this.check(userId, arg);
            }

        } catch (NoRowPrivilegeException e) {
            throw e;
        } catch (Exception e) {
            log.error("RowPrivilegeAspect.invoke error!", e);
            throw new NoRowPrivilegeException();
        }
    }

    /**
     * 用户权限校验
     *
     * @param userId
     * @param param
     * @return
     */
    private boolean check(Long userId, Object param) {

        RowPrivilegeProperty rowPrivilegeProperty = param.getClass().getAnnotation(RowPrivilegeProperty.class);

        if(rowPrivilegeProperty == null) {
            return true;
        }

        Validator validator = ValidatorFactory.getValidator(param.getClass());
        List<String> privilegeList = Arrays.asList(rowPrivilegeProperty.value());
        return validator.check(validateMetadata);
    }


    private String firstToUpper(String fieldName) {
        // 进行字母的ascii编码前移，效率要高于截取字符串进行转换的操作
        char[] cs = fieldName.toCharArray();
        cs[0] -= 32;
        return String.valueOf(cs);
    }

    /**
     * 比较用户请求的权限与用户拥有权限的差异
     *
     * @param sourceList
     * @param privilegeList
     * @return
     */
    private boolean checkPrivilege(List<String> sourceList, List<String> privilegeList, Long userId, String fieldName) {
        // 通过请求中的参数范围与用户所拥有的权限范围取交集, 判断剩余结果与原结果的差异
        // 相等则拥有权限 不等则无权限
        List<String> originList = new ArrayList<>(sourceList);
        sourceList.retainAll(privilegeList);

        if (originList.size() != sourceList.size()) {
            log.error("RowPrivilegeAspect.invoke user has no privilege, userId = {}, fieldName = {}, originList = {}, sourceList = {}", userId, fieldName, originList, sourceList);
            throw new NoRowPrivilegeException();
        }
        return true;
    }
}
