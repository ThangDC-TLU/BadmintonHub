package com.badmintonhub.authservice.service.impl;

import com.badmintonhub.authservice.dto.mapper.AddressMapper;
import com.badmintonhub.authservice.dto.model.AddressDTO;
import com.badmintonhub.authservice.entity.Address;
import com.badmintonhub.authservice.entity.User;
import com.badmintonhub.authservice.exception.APIException;
import com.badmintonhub.authservice.repository.AddressRepository;
import com.badmintonhub.authservice.repository.UserRepository;
import com.badmintonhub.authservice.service.AddressService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;
    private final UserRepository userRepository;

    public AddressServiceImpl(AddressRepository addressRepository, AddressMapper addressMapper, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.addressMapper = addressMapper;
        this.userRepository = userRepository;
    }

    @Override
    public AddressDTO createAddress(long userId, AddressDTO addressDto) throws APIException {
        Address address = addressMapper.mapToEntity(addressDto);

        User user = userRepository.findById(userId).orElseThrow(
                () -> new APIException("Not found by userId = " + userId));

        address.setUser(user);

        Address newAddress = addressRepository.save(address);
        return addressMapper.mapToDto(newAddress);
    }

    @Override
    public AddressDTO getAddressById(long id) throws APIException {
        Address address = addressRepository.findById(id).orElseThrow(()
                -> new APIException("Not found address with id = " + id));
        return addressMapper.mapToDto(address);
    }

    @Override
    public List<AddressDTO> getAddressByUserId(long userId){
        List<Address> addresses = addressRepository.findByUserId(userId);
        List<AddressDTO> addressDtoList = addresses.stream()
                .map(address -> addressMapper.mapToDto(address))
                .toList();
        return addressDtoList;
    }

    @Override
    public AddressDTO updateAddress(long userId, AddressDTO addressDto, long id) throws APIException {
        Address address = addressMapper.mapToEntity(addressDto);


        User user = userRepository.findById(userId).orElseThrow(
                () -> new APIException("Not found user by id = " + userId));

        address.setUser(user);
        address.setId(id);

        Address resultAddress = addressRepository.save(address);
        return addressMapper.mapToDto(resultAddress);
    }

    @Override
    public void deleteAddress(long id) {
        addressRepository.deleteById(id);
    }
}
