package com.mx.privilege.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @Author: Xu.Meng1
 * @Date: 2022/1/24 16:53
 */
public class JavaInvocationHandler<T> implements InvocationHandler {
    private T target;

    public JavaInvocationHandler(T o) {
        this.target = o;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("JavaInvocationHandler Before invoke ...");
        Object result = method.invoke(target, args);
        System.out.println("JavaInvocationHandler invoke()");
        System.out.println("JavaInvocationHandler After invoke ...");
        return result;
    }

    public T newDynamicProxy() {
        return (T)Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), this);
    }
}
