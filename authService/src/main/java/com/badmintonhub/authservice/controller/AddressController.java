package com.badmintonhub.authservice.controller;

import com.badmintonhub.authservice.dto.model.AddressDTO;
import com.badmintonhub.authservice.exception.APIException;
import com.badmintonhub.authservice.service.AddressService;
import com.badmintonhub.authservice.utils.CustomHeaders;
import com.badmintonhub.authservice.utils.anotation.ApiMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/address")
public class AddressController {
    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    @ApiMessage("Create address")
    public ResponseEntity<AddressDTO> createAddress(@RequestHeader(CustomHeaders.X_AUTH_USER_ID) long userId,
                                                    @RequestBody AddressDTO addressDto) throws APIException {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.createAddress(userId, addressDto));
    }

    @GetMapping
    @ApiMessage("Get all address by userId")
    public ResponseEntity<List<AddressDTO>> getAllAddressByUserId(@RequestHeader(CustomHeaders.X_AUTH_USER_ID) long userId){
        return ResponseEntity.ok(this.addressService.getAddressByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable(name = "id") long id) throws APIException {
        return ResponseEntity.ok(this.addressService.getAddressById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressDTO> updateAddress(@RequestHeader(CustomHeaders.X_AUTH_USER_ID) long userId,
                                                    @PathVariable(name = "id") long id,
                                                    @RequestBody AddressDTO addressDto) throws APIException {
        return ResponseEntity.ok(addressService.updateAddress(userId, addressDto, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAddress(@PathVariable(name = "id") long id) {
        this.addressService.deleteAddress(id);
        return ResponseEntity.ok("Address deleted successfully");
    }

}

