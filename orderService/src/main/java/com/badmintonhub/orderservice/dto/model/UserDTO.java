package com.badmintonhub.orderservice.dto.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDTO {
    private String name;
    private String email;
    private String username;
}
