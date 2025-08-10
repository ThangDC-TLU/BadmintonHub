package com.badmintonhub.authservice.controller;



import com.badmintonhub.authservice.dto.message.ObjectResponse;
import com.badmintonhub.authservice.dto.model.UserDTO;
import com.badmintonhub.authservice.dto.model.UserUpdateDTO;
import com.badmintonhub.authservice.entity.User;
import com.badmintonhub.authservice.exception.APIException;
import com.badmintonhub.authservice.service.UserService;
import com.badmintonhub.authservice.utils.CustomHeaders;
import com.badmintonhub.authservice.utils.anotation.ApiMessage;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ApiMessage("Create user")
    public ResponseEntity<UserDTO> createUser(@RequestBody @Valid UserDTO userDTO) throws APIException {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.createUser(userDTO));
    }

    @GetMapping
    @ApiMessage("Get all users")
    public ResponseEntity<ObjectResponse> getAllUser(
            @Filter Specification<User> specification,
            Pageable pageable
    ){
        return ResponseEntity.ok(this.userService.getAllUser(specification, pageable));
    }

    @GetMapping("/{userId}")
    @ApiMessage("Get user by id")
    public ResponseEntity<UserDTO> getUserById(@PathVariable(value = "userId") Long id) throws APIException {
        return ResponseEntity.ok(this.userService.getUserById(id));
    }

    @PutMapping("/{userId}")
    @ApiMessage("Update user")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable(value = "userId") Long id,
            @RequestBody UserUpdateDTO userDto) throws APIException {
        return ResponseEntity.ok(this.userService.updateUser(userDto, id));
    }

    @DeleteMapping("/{userId}")
    @ApiMessage("Delete a user")
    public ResponseEntity<String> deleteUser(@PathVariable(value = "userId") Long id) throws APIException {
        this.userService.deleteUser(id);
        return ResponseEntity.ok("Delete user successfully");
    }

    @GetMapping("/profile")
    @ApiMessage("Get profile")
    public ResponseEntity<UserDTO> getUserProfile(@RequestHeader(CustomHeaders.X_AUTH_USER_ID) Long id) throws APIException {
        return ResponseEntity.ok(this.userService.getUserProfile(id));
    }
}