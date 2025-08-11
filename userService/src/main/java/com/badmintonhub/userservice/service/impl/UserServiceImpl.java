package com.badmintonhub.userservice.service.impl;

import com.badmintonhub.userservice.dto.mapper.UserMapper;
import com.badmintonhub.userservice.dto.message.ObjectResponse;
import com.badmintonhub.userservice.dto.model.UserDTO;
import com.badmintonhub.userservice.dto.model.UserUpdateDTO;
import com.badmintonhub.userservice.entity.Role;
import com.badmintonhub.userservice.entity.User;
import com.badmintonhub.userservice.exception.APIException;
import com.badmintonhub.userservice.repository.RoleRepository;
import com.badmintonhub.userservice.repository.UserRepository;
import com.badmintonhub.userservice.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
//    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
//        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) throws APIException {
        // add check for username exists in database
        if(this.userRepository.existsByUsername(userDTO.getUsername())){
            throw new APIException("Username is already exists!");
        }

        // add check for email exists in database
        if(this.userRepository.existsByEmail(userDTO.getEmail())){
            throw new APIException("Email is already exists!");
        }

        User newUser = this.userMapper.mapToEntity(userDTO);

//        newUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        newUser.setPassword(userDTO.getPassword());


//        Set<Role> roles = new HashSet<>();
//        Role userRole = this.roleRepository.findByName("USER").get();
//        roles.add(userRole);
//        newUser.setRoles(roles);

        User userResponse = this.userRepository.save(newUser);
        return this.userMapper.mapToDto(userResponse);
    }

    @Override
    public ObjectResponse getAllUser(Specification<User> specification, Pageable pageable) {
        Page<User> page = userRepository.findAll(specification, pageable);
        ObjectResponse objectResponse = new ObjectResponse();
        ObjectResponse.Meta meta = new ObjectResponse.Meta();

        meta.setTotal(page.getTotalElements());
        meta.setPages(page.getTotalPages());

        meta.setPageSize(pageable.getPageSize());
        meta.setPage(pageable.getPageNumber() + 1);

        objectResponse.setMeta(meta);
        List<UserDTO> productResponseDTOList = page
                .getContent()
                .stream()
                .map(userMapper::mapToDto)
                .toList();

        objectResponse.setResult(productResponseDTOList);
        return objectResponse;


    }

    @Override
    public UserDTO getUserById(Long id) throws APIException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new APIException("Không tìm thấy user với id = " + id));
        return this.userMapper.mapToDto(user);
    }

    @Override
    public UserDTO updateUser(UserUpdateDTO userUpdateDto, Long id) throws APIException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new APIException("Không tìm thấy user với id = " + id));

        user.setName(userUpdateDto.getName());
        user.setPhone(userUpdateDto.getPhone());
        user.setGender(userUpdateDto.getGender());
        user.setDateOfBirth(userUpdateDto.getDateOfBirth());

        User updated = userRepository.save(user);
        return userMapper.mapToDto(updated);
    }

    @Override
    public void deleteUser(Long id) throws APIException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new APIException("Không tìm thấy user với id = " + id));
        userRepository.delete(user);
    }

    @Override
    public UserDTO getUserProfile(Long id) throws APIException {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new APIException("Không tìm thấy user với id = " + id));
        return this.userMapper.mapToDto(user);
    }
}
