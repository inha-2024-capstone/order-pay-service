package com.project.yogerOrder.global.exception;

import com.project.yogerOrder.global.exception.specific.NotHandledException;
import com.project.yogerOrder.product.exception.ProductNotFoundException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;

public enum CustomExceptionEnum {

    ERROR(0, NotHandledException.class),

    PRODUCT_NOT_FOUND(300, ProductNotFoundException.class);
    //PRODUCT_SERVER_ERROR(302, ProductServerStateException.class); // 응답을 못하기에 등록하지 않음


    private final Integer CUSTOM_CODE_LENGTH = 3;

    @Getter
    private final Integer code;

    @Getter
    private final Class<? extends CustomRuntimeException> exceptionClass;

    CustomExceptionEnum(Integer code, Class<? extends CustomRuntimeException> exceptionClass) {
        this.exceptionClass = exceptionClass;

        try {
            validateCode(code);
            this.code = getCustomCode(exceptionClass.getDeclaredConstructor().newInstance().getHttpStatus(), code);
        } catch (IllegalArgumentException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        HashSet<Integer> codes = new HashSet<>();
        HashSet<Class<? extends CustomRuntimeException>> exceptions = new HashSet<>();
        for (CustomExceptionEnum customExceptionEnum : CustomExceptionEnum.values()) {
            if (codes.contains(customExceptionEnum.code))
                throw new IllegalArgumentException("Duplicated code: " + customExceptionEnum.code);

            if (exceptions.contains(customExceptionEnum.exceptionClass))
                throw new IllegalArgumentException("Duplicated class: " + customExceptionEnum.exceptionClass.getName());

            codes.add(customExceptionEnum.getCode());
            exceptions.add(customExceptionEnum.exceptionClass);
        }
    }

    private void validateCode(Integer code) throws IllegalArgumentException {
        if (code < 0 || Math.pow(10, CUSTOM_CODE_LENGTH) <= code)
            throw new IllegalArgumentException("Invalid code: " + code);
    }

    private Integer getCustomCode(HttpStatus httpStatus, Integer code) {
        return (int) (httpStatus.value() * Math.pow(10, CUSTOM_CODE_LENGTH) + code);
    }

    public static CustomExceptionEnum getByExceptionClass(Class<? extends CustomRuntimeException> clazz) {
        for (CustomExceptionEnum customExceptionEnum : values()) {
            if (customExceptionEnum.exceptionClass.equals(clazz)) return customExceptionEnum;
        }

        throw new IllegalArgumentException("Unregistered exception: " + clazz);
    }

    public static CustomExceptionEnum getByCode(Integer code) {
        for (CustomExceptionEnum customExceptionEnum : values()) {
            if (customExceptionEnum.code.equals(code)) return customExceptionEnum;
        }

        throw new IllegalArgumentException("Unregistered exception code: " + code);
    }
}
