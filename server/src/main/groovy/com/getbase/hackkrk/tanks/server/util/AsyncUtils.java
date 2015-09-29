package com.getbase.hackkrk.tanks.server.util;

import lombok.experimental.UtilityClass;
import org.springframework.web.context.request.async.DeferredResult;

@UtilityClass
public class AsyncUtils {

    public static <T> DeferredResult<T> immediate(T result) {
        final DeferredResult<T> deferred = new DeferredResult<>();
        deferred.setResult(result);
        return deferred;
    }

    public static <T> DeferredResult<T> immediateError(Object error) {
        final DeferredResult<T> deferred = new DeferredResult<>();
        deferred.setErrorResult(error);
        return deferred;
    }

}
