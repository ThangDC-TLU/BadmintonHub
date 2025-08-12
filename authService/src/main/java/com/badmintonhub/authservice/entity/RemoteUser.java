package com.badmintonhub.authservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemoteUser {
    private String username;
    private String password;
    private Set<String> roles; // ví dụ: ["ROLE_ADMIN", "ROLE_USER"]
}
