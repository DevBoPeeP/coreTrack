package com.notificationservice.exception;

import lombok.Getter;

@Getter
public class EmailDeliveryException extends RuntimeException {

    private final String recipient;

    public EmailDeliveryException(String recipient, String message) {
        super(message);
        this.recipient = recipient;
    }

    public EmailDeliveryException(String recipient, String message, Throwable cause) {
        super(message, cause);
        this.recipient = recipient;
    }
}