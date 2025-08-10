package com.badmintonhub.authservice.dto.mapper;

import com.badmintonhub.authservice.dto.model.UserDTO;
import com.badmintonhub.authservice.entity.User;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private final ModelMapper mapper;

    public UserMapper(ModelMapper mapper) {
        this.mapper = mapper;
    }

    public UserDTO mapToDto(User user){
        UserDTO userDto = mapper.map(user, UserDTO.class);
        return userDto;
    }

    public User mapToEntity(UserDTO userDto){
        User user = mapper.map(userDto, User.class);
        return user;
    }
}
