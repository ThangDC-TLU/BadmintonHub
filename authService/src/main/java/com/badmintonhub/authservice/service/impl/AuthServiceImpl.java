package com.badmintonhub.authservice.service.impl;

import com.badmintonhub.authservice.dto.model.RegisterDTO;
import com.badmintonhub.authservice.entity.Role;
import com.badmintonhub.authservice.entity.User;
import com.badmintonhub.authservice.exception.APIException;
import com.badmintonhub.authservice.repository.RoleRepository;
import com.badmintonhub.authservice.repository.UserRepository;
import com.badmintonhub.authservice.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String register(RegisterDTO registerDTO) throws APIException {

        // add check for username exists in database
        if(this.userRepository.existsByUsername(registerDTO.getUsername())){
            throw new APIException("Username is already exists!");
        }

        // add check for email exists in database
        if(this.userRepository.existsByEmail(registerDTO.getEmail())){
            throw new APIException("Email is already exists!");
        }

        User user = new User();
        user.setName(registerDTO.getName());
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setPhone(registerDTO.getPhone());
        user.setGender(registerDTO.getGender());
        user.setDateOfBirth(registerDTO.getDateOfBirth());

        Set<Role> roles = new HashSet<>();
        Role userRole = this.roleRepository.findByName("USER").get();
        roles.add(userRole);
        user.setRoles(roles);

        userRepository.save(user);

        return "User register successfully!";
    }
}

