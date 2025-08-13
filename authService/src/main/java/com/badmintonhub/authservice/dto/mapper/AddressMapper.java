package com.badmintonhub.authservice.dto.mapper;

import com.badmintonhub.authservice.dto.model.AddressDTO;
import com.badmintonhub.authservice.entity.Address;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {
    private final ModelMapper mapper;

    public AddressMapper(ModelMapper mapper) {
        this.mapper = mapper;
    }

    public AddressDTO mapToDto(Address address){
        AddressDTO addressDto = mapper.map(address, AddressDTO.class);
        return addressDto;
    }

    public Address mapToEntity(AddressDTO addressDto){
        Address address = mapper.map(addressDto, Address.class);
        return address;
    }
}
