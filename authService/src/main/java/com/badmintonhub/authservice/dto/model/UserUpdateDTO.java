package com.badmintonhub.authservice.dto.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDTO {
    private Long id;

    @NotEmpty(message = "Tên không được bỏ trống")
    @Size(min = 2, message = "Tên phải có độ dài tối thiểu là 2 ký tự")
    private String name;

    @NotEmpty(message = "Số điện thoại không được bỏ trống")
    @Size(min = 9, message = "Số điện thoại phải có độ dài tối thiểu là 9 ký tự")
    private String phone;

    private String gender;
    private LocalDate dateOfBirth;
}