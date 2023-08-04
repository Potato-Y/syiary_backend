package io.potatoy.syiary.user.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.potatoy.syiary.error.dto.ErrorResponse;
import io.potatoy.syiary.error.handler.AbstractExceptionHandler;
import io.potatoy.syiary.user.exception.NotFoundUserException;
import io.potatoy.syiary.util.EnvProperties;

@RestControllerAdvice
public class NotFoundUserExceptionHandler extends AbstractExceptionHandler<NotFoundUserException> {

    public NotFoundUserExceptionHandler(EnvProperties envProperties) {
        super(envProperties);
    }

    @Override
    @ExceptionHandler(NotFoundUserException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleException(NotFoundUserException exception) {
        if (envProperties.getMode().equals(PROD)) { // 운영 환경에서는 상세 내용을 반환하지 않도록 설정
            return new ErrorResponse(HttpStatus.NOT_FOUND.value(), null);
        }

        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), exception.toString());
    }

}
