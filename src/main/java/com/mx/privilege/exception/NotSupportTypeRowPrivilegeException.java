package com.mx.privilege.exception;

/**
 * @author mengxu
 * @date 2022/4/10 21:22
 */
public class NotSupportTypeRowPrivilegeException extends RuntimeException {

    private String message;

    public NotSupportTypeRowPrivilegeException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
