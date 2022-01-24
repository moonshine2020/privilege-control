package com.mx.privilege.proxy;

import com.mx.privilege.pojo.User;
import com.mx.privilege.pojo.UserDto;
import com.mx.privilege.pojo.UserInterface;

/**
 * @Author: Xu.Meng1
 * @Date: 2022/1/21 16:36
 *
 * Spring AOP 目前使用两种方式生成动态代理类
 * 1. Java自带库, 只有被代理类实现了某些接口才可生成其相应的代理类, 如mybatis的mapper
 * 2. cglib生成动态代理类, 没有实现接口的被代理类也可以生成新的动态代理类, 如Controller层与Service层
 */
public class TestInvocationHandler {

    public static void main(String[] args) {

        JavaInvocationHandler<UserInterface> javaInvocationHandler = new JavaInvocationHandler<>(new User());
        UserInterface userInterface = javaInvocationHandler.newDynamicProxy();
        System.out.println(userInterface.getName());

        CGLIBInvocationHandler<UserDto> cglibInvocationHandler = new CGLIBInvocationHandler<>(new UserDto());
        UserDto userDto = cglibInvocationHandler.newDynamicProxy();
        System.out.println(userDto.getName());
    }
}
