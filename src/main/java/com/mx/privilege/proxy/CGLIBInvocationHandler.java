package com.mx.privilege.proxy;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.InvocationHandler;

import java.lang.reflect.Method;

/**
 * @Author: Xu.Meng1
 * @Date: 2022/1/24 16:54
 */
public class CGLIBInvocationHandler<T> implements InvocationHandler {
    private T target;

    public CGLIBInvocationHandler(T o) {
        this.target = o;
    }
    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        System.out.println("CGLIBInvocationHandler Before invoke ...");
        Object result = method.invoke(target, objects);
        System.out.println("CGLIBInvocationHandler After invoke ...");
        return result;
    }

    public T newDynamicProxy() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(target.getClass());
        enhancer.setCallback(this);
        return (T) enhancer.create();
    }
}
