package com.badmintonhub.authservice.controller;

import com.badmintonhub.authservice.dto.model.RegisterDTO;
import com.badmintonhub.authservice.exception.APIException;
import com.badmintonhub.authservice.service.AuthService;
import com.badmintonhub.authservice.utils.anotation.ApiMessage;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @ApiMessage("Register account")
    @PostMapping(value = {"/register", "/signup"})
    public ResponseEntity<String> register(@RequestBody @Valid RegisterDTO registerDTO) throws APIException {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.authService.register(registerDTO));
    }

}
