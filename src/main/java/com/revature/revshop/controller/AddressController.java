package com.revature.revshop.controller;

import com.revature.revshop.dto.AddressDTO;
import com.revature.revshop.exception.ResourceNotFoundException;
import com.revature.revshop.model.Address;
import com.revature.revshop.service.AddressService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private static final Logger log = LoggerFactory.getLogger(AddressController.class);

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<AddressDTO> addAddress(@PathVariable Long userId, @Valid @RequestBody AddressDTO addressDTO) {
        log.info("POST /api/addresses/{}", userId);
        Address address = convertToEntity(addressDTO);
        Address savedAddress = addressService.addAddress(address, userId);
        return new ResponseEntity<>(convertToDTO(savedAddress), HttpStatus.CREATED);
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long addressId) {
        log.info("GET /api/addresses/{}", addressId);
        Address address = addressService.getAddressById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        return ResponseEntity.ok(convertToDTO(address));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AddressDTO>> getAddressesByUserId(@PathVariable Long userId) {
        log.info("GET /api/addresses/user/{}", userId);
        List<Address> addresses = addressService.getAddressesByUserId(userId);
        List<AddressDTO> addressDTOs = addresses.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(addressDTOs);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressDTO> updateAddress(@PathVariable Long addressId,
            @Valid @RequestBody AddressDTO addressDTO) {
        log.info("PUT /api/addresses/{}", addressId);
        Address address = convertToEntity(addressDTO);
        Address updatedAddress = addressService.updateAddress(addressId, address);
        return ResponseEntity.ok(convertToDTO(updatedAddress));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<String> deleteAddress(@PathVariable Long addressId) {
        log.info("DELETE /api/addresses/{}", addressId);
        addressService.deleteAddress(addressId);
        return ResponseEntity.ok("Address deleted successfully");
    }

    private Address convertToEntity(AddressDTO dto) {
        Address address = new Address();
        address.setAddressLine(dto.getAddressLine());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setCountry(dto.getCountry());
        address.setZipCode(dto.getZipCode());
        address.setIsDefault(dto.getIsDefault());
        address.setAddressType(dto.getAddressType());
        return address;
    }

    private AddressDTO convertToDTO(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setAddressLine(address.getAddressLine());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setCountry(address.getCountry());
        dto.setZipCode(address.getZipCode());
        dto.setIsDefault(address.getIsDefault());
        dto.setAddressType(address.getAddressType());
        if (address.getUser() != null) {
            dto.setUserId(address.getUser().getUserId());
        }
        return dto;
    }
}
