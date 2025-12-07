package com.jewellery.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception when a service is unavailable (for circuit breaker fallbacks)
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String serviceName) {
        super(String.format("Service '%s' is currently unavailable. Please try again later.", serviceName));
    }

    public ServiceUnavailableException(String serviceName, Throwable cause) {
        super(String.format("Service '%s' is currently unavailable. Please try again later.", serviceName), cause);
    }
}
