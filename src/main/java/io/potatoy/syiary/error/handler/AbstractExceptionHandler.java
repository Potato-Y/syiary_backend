package io.potatoy.syiary.error.handler;

import io.potatoy.syiary.error.dto.ErrorResponse;
import io.potatoy.syiary.util.EnvProperties;

public abstract class AbstractExceptionHandler<T extends Exception> {

    public static final String PROD = "prod";

    protected final EnvProperties envProperties;

    public AbstractExceptionHandler(EnvProperties envProperties) {
        this.envProperties = envProperties;
    }

    public abstract ErrorResponse handleException(T exception);
}
