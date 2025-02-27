package org.grnet.cat.exceptions;


import jakarta.validation.ValidationException;

public class CustomValidationException extends ValidationException {

    private int code;

    public CustomValidationException(String message, int status){
        super(message);
        this.code = status;
    }

    public int getCode() {
        return code;
    }
}
