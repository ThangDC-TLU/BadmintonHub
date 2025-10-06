package com.badmintonhub.inventoryservice.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> notFound(NotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("timestamp", Instant.now(), "error", ex.getMessage()));
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> badReq(BadRequestException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("timestamp", Instant.now(), "error", ex.getMessage()));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> generic(Exception ex){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("timestamp", Instant.now(), "error", ex.getMessage()));
    }
}
