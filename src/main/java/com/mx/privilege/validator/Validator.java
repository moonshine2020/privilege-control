package com.mx.privilege.validator;

import com.mx.privilege.pojo.ValidateMetadata;

/**
 * @author mengxu
 * @date 2022/4/6 22:07
 *
 * 目前的权限控制器有多种方案:
 * 1. 单纯具有校验的作用, 仅验证入参是否有权限, 当入参为空或者入参多了不应有的权限, 抛出异常
 * 2. 权限校验器具有部分纠正权限的功能, 当入参为null时会将用户应有的权限赋给入参, 此方案对入参时基本数据类型时无法进行赋值, 仅支持对类实例中的成员变量
 * 进行赋值, 当入参不为空且匹配发现多了一些不应有的权限, 会抛出异常
 * 3. 权限校验器具有全部权限纠正能力, 如果用户权限多了, 会删除多余权限, 仅让用户访问应访问的数据, 该方案也有方案2的弊端, 无法处理基本数据类型参数
 *
 * ps: 目前只实现了2, 可以将三个方案都实现
 */
public interface Validator {
    /**
     * 校验参数是否符合行级数据权限
     * @param validateMetadata
     * @return
     */
    boolean check(ValidateMetadata validateMetadata);
}
