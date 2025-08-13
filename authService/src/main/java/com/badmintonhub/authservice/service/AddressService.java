package com.badmintonhub.authservice.service;

import com.badmintonhub.authservice.dto.model.AddressDTO;
import com.badmintonhub.authservice.exception.APIException;

import java.util.List;

public interface AddressService {
    AddressDTO createAddress(long userId, AddressDTO addressDto) throws APIException;

    List<AddressDTO> getAddressByUserId(long userId);

    AddressDTO getAddressById(long id) throws APIException;

    AddressDTO updateAddress(long userId, AddressDTO addressDto, long id) throws APIException;

    void deleteAddress(long id);
}
