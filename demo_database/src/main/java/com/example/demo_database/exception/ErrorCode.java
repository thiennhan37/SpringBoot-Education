package com.example.demo_database.exception;

public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized Exception"),
    KEY_INVALID(1001, "ErrorKey is invalid"),
    USER_EXISTED(1002,"User has been existed"),
    USERNAME_INVALID(1003, "Username must be at least 3 character"),
    PASSWORD_INVALID(1004, "Password must be at least 6 character"),
    USER_NOT_EXISTED(1005,"User not be existed"),
    UNAUTHENTICATED(1005, "unauthenticated")
    ;

    private int code;
    private String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
