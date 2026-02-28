package com.revature.revshop.service;

import com.revature.revshop.exception.ResourceNotFoundException;
import com.revature.revshop.exception.UserNotFoundException;
import com.revature.revshop.model.Address;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.AddressRepository;
import com.revature.revshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressService addressService;

    private User sampleUser;
    private Address sampleAddress;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setUserId(1L);

        sampleAddress = new Address();
        sampleAddress.setAddressId(1L);
        sampleAddress.setAddressLine("123 Main St");
        sampleAddress.setCity("Anytown");
        sampleAddress.setState("CA");
        sampleAddress.setZipCode("12345");
        sampleAddress.setCountry("USA");
        sampleAddress.setIsDefault(true);
        sampleAddress.setUser(sampleUser);

        sampleUser.setAddresses(Arrays.asList(sampleAddress));
    }

    @Test
    void testAddAddress_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(addressRepository.save(any(Address.class))).thenReturn(sampleAddress);

        Address newAddress = new Address();
        newAddress.setAddressLine("123 Main St");

        Address result = addressService.addAddress(newAddress, 1L);

        assertNotNull(result);
        assertEquals(sampleUser, newAddress.getUser());
        verify(addressRepository).save(newAddress);
    }

    @Test
    void testAddAddress_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> addressService.addAddress(new Address(), 1L));
    }

    @Test
    void testGetAddressById_Success() {
        when(addressRepository.findById(1L)).thenReturn(Optional.of(sampleAddress));

        Optional<Address> result = addressService.getAddressById(1L);

        assertTrue(result.isPresent());
        assertEquals(sampleAddress.getAddressLine(), result.get().getAddressLine());
    }

    @Test
    void testGetAddressesByUserId_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        List<Address> addresses = addressService.getAddressesByUserId(1L);

        assertEquals(1, addresses.size());
        assertEquals("123 Main St", addresses.get(0).getAddressLine());
    }

    @Test
    void testGetAddressesByUserId_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> addressService.getAddressesByUserId(1L));
    }

    @Test
    void testUpdateAddress_Success() {
        when(addressRepository.findById(1L)).thenReturn(Optional.of(sampleAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(sampleAddress);

        Address updatedData = new Address();
        updatedData.setAddressLine("456 Elm St");
        updatedData.setCity("Othertown");

        Address result = addressService.updateAddress(1L, updatedData);

        assertEquals("456 Elm St", sampleAddress.getAddressLine());
        assertEquals("Othertown", sampleAddress.getCity());
        verify(addressRepository).save(sampleAddress);
    }

    @Test
    void testUpdateAddress_NotFound() {
        when(addressRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> addressService.updateAddress(1L, new Address()));
    }

    @Test
    void testDeleteAddress_Success() {
        when(addressRepository.existsById(1L)).thenReturn(true);
        doNothing().when(addressRepository).deleteById(1L);

        assertDoesNotThrow(() -> addressService.deleteAddress(1L));
        verify(addressRepository).deleteById(1L);
    }

    @Test
    void testDeleteAddress_NotFound() {
        when(addressRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> addressService.deleteAddress(1L));
    }
}
