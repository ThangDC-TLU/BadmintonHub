package com.badmintonhub.authservice.dto.model;

import lombok.Data;

@Data
public class AddressDTO {
    private long id;
    private String name;
    private String phone;
    private String company;
    private String province;
    private String district;
    private String ward;
    private String address;
    private String addressType;

}
