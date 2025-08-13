package com.badmintonhub.authservice.service;

import com.badmintonhub.authservice.dto.model.RegisterDTO;
import com.badmintonhub.authservice.exception.APIException;
import jakarta.validation.Valid;

public interface AuthService {
    String register(@Valid RegisterDTO registerDTO) throws APIException;
}
