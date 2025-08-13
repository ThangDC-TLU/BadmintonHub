package com.badmintonhub.authservice.service;

import com.badmintonhub.authservice.dto.message.ObjectResponse;
import com.badmintonhub.authservice.dto.model.UserDTO;
import com.badmintonhub.authservice.dto.model.UserUpdateDTO;
import com.badmintonhub.authservice.entity.User;
import com.badmintonhub.authservice.exception.APIException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface UserService {
    UserDTO createUser(UserDTO userDTO) throws APIException;

    ObjectResponse getAllUser(Specification<User> specification, Pageable pageable);

    UserDTO getUserById(Long id) throws APIException;

    UserDTO updateUser(UserUpdateDTO userDto, Long id) throws APIException;

    void deleteUser(Long id) throws APIException;

    UserDTO getUserProfile(Long id) throws APIException;

    UserDTO getUserByUsername(String username) throws APIException;
}
