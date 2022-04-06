package com.mx.privilege.validator;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mengxu
 * @date 2022/4/6 22:38
 */
public class ValidatorFactory {

    private static final ConcurrentHashMap<Class<?>, Validator> FACTORY = new ConcurrentHashMap<>();

    private ValidatorFactory() {

    }

    public static void addValidator(Class<?> cl, Validator validator) {
        FACTORY.put(cl, validator);
    }

    public static Validator getValidator(Class<?> cl) {
        Validator validator = FACTORY.get(cl);
        return validator == null? FACTORY.get(ValidatorFactory.class):validator;
    }

}
