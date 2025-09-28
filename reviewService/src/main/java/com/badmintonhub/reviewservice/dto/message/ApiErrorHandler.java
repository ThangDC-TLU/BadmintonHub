package com.badmintonhub.reviewservice.dto.message;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiErrorHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ProblemDetail> duplicate(DuplicateKeyException ex){
        var pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Duplicate Review");
        pd.setDetail("User already reviewed this product.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> rse(ResponseStatusException ex){
        var pd = ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(pd);
    }
}
