package com.badmintonhub.inventoryservice.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String msg){ super(msg); }
}
