package com.revature.revshop.service;

import com.revature.revshop.model.Role;
import com.revature.revshop.model.Seller;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.SellerRepository;
import com.revature.revshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private SellerService sellerService;

    private User sampleUser;
    private Seller sampleSeller;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setUserId(1L);
        sampleUser.setName("Seller Doe");
        sampleUser.setEmail("seller@example.com");
        sampleUser.setPassword("password");
        sampleUser.setRole(Role.SELLER);
        sampleUser.setAddresses(new ArrayList<>());

        sampleSeller = new Seller();
        sampleSeller.setUserId(1L);
        sampleSeller.setBusinessName("Test Business");
        sampleSeller.setBusinessDescription("A test business");
        sampleSeller.setTaxId("TAX12345");
        sampleSeller.setUser(sampleUser);
        sampleUser.setSellerProfile(sampleSeller);
    }

    @Test
    void testRegisterSeller_Success() {
        User newUser = new User();
        newUser.setUserId(2L);
        newUser.setEmail("new_seller@example.com");

        when(userRepository.save(any(User.class))).thenReturn(newUser);

        Seller sellerResult = sellerService.registerSeller(newUser, "New Business", "Desc", "TAX6789");

        assertNotNull(sellerResult);
        verify(notificationService).createNotification(eq(2L), anyString(), anyString());
        assertEquals(Role.SELLER, newUser.getRole());
        assertEquals("New Business", sellerResult.getBusinessName());
    }

    @Test
    void testLoginSeller_Success() {
        when(userRepository.findByEmail("seller@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("password", "password")).thenReturn(true);

        Optional<Seller> result = sellerService.loginSeller("seller@example.com", "password");

        assertTrue(result.isPresent());
        assertEquals(sampleSeller.getUserId(), result.get().getUserId());
    }

    @Test
    void testLoginSeller_InvalidPassword() {
        when(userRepository.findByEmail("seller@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongpassword", "password")).thenReturn(false);

        Optional<Seller> result = sellerService.loginSeller("seller@example.com", "wrongpassword");

        assertFalse(result.isPresent());
    }

    @Test
    void testLoginSeller_NotASeller() {
        sampleUser.setRole(Role.BUYER);
        when(userRepository.findByEmail("seller@example.com")).thenReturn(Optional.of(sampleUser));

        Optional<Seller> result = sellerService.loginSeller("seller@example.com", "password");

        assertFalse(result.isPresent());
    }

    @Test
    void testGetSellerById_Success() {
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(sampleSeller));

        Optional<Seller> result = sellerService.getSellerById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Business", result.get().getBusinessName());
    }
}
