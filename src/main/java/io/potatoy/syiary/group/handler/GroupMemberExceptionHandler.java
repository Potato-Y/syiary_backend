package io.potatoy.syiary.group.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.potatoy.syiary.error.dto.ErrorResponse;
import io.potatoy.syiary.error.handler.AbstractExceptionHandler;
import io.potatoy.syiary.group.exception.GroupMemberException;
import io.potatoy.syiary.util.EnvProperties;

@RestControllerAdvice
public class GroupMemberExceptionHandler extends AbstractExceptionHandler<GroupMemberException> {

    public GroupMemberExceptionHandler(EnvProperties envProperties) {
        super(envProperties);
    }

    @Override
    @ExceptionHandler(GroupMemberException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleException(GroupMemberException exception) {
        if (envProperties.getMode().equals(PROD)) { // 운영 환경에서는 상세 내용을 반환하지 않도록 설정
            return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), null);
        }

        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), exception.toString());
    }
}
