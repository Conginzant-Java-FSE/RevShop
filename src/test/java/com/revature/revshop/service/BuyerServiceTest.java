package com.revature.revshop.service;

import com.revature.revshop.exception.UserNotFoundException;
import com.revature.revshop.model.Buyer;
import com.revature.revshop.model.Role;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.BuyerRepository;
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
class BuyerServiceTest {

    @Mock
    private BuyerRepository buyerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BuyerService buyerService;

    private User sampleUser;
    private Buyer sampleBuyer;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setUserId(1L);
        sampleUser.setName("Buyer Doe");
        sampleUser.setEmail("buyer@example.com");
        sampleUser.setPassword("password");
        sampleUser.setRole(Role.BUYER);
        sampleUser.setAddresses(new ArrayList<>());

        sampleBuyer = new Buyer();
        sampleBuyer.setUserId(1L);
        sampleBuyer.setUser(sampleUser);
        sampleUser.setBuyerProfile(sampleBuyer);
    }

    @Test
    void testRegisterBuyer_Success() {
        // Ensure user is created without a buyer context yet to simulate real case
        User newUser = new User();
        newUser.setUserId(2L);
        newUser.setEmail("new@example.com");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        Buyer buyerResult = buyerService.registerBuyer(newUser);

        assertNotNull(buyerResult);
        verify(notificationService).createNotification(eq(2L), anyString(), anyString());
        assertEquals(Role.BUYER, newUser.getRole());
    }

    @Test
    void testLoginBuyer_Success() {
        when(userRepository.findByEmail("buyer@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("password", "password")).thenReturn(true);

        Optional<Buyer> result = buyerService.loginBuyer("buyer@example.com", "password");

        assertTrue(result.isPresent());
        assertEquals(sampleBuyer.getUserId(), result.get().getUserId());
    }

    @Test
    void testLoginBuyer_InvalidPassword() {
        when(userRepository.findByEmail("buyer@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongpassword", "password")).thenReturn(false);

        Optional<Buyer> result = buyerService.loginBuyer("buyer@example.com", "wrongpassword");

        assertFalse(result.isPresent());
    }

    @Test
    void testLoginBuyer_NotABuyer() {
        sampleUser.setRole(Role.SELLER);
        when(userRepository.findByEmail("buyer@example.com")).thenReturn(Optional.of(sampleUser));

        Optional<Buyer> result = buyerService.loginBuyer("buyer@example.com", "password");

        assertFalse(result.isPresent());
    }

    @Test
    void testUpdateBuyerProfile_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        User updatedData = new User();
        updatedData.setName("New Name");
        updatedData.setPhone("1231231234");
        updatedData.setAddresses(new ArrayList<>());

        Buyer result = buyerService.updateBuyerProfile(1L, updatedData);

        assertEquals(sampleBuyer, result);
        assertEquals("New Name", sampleUser.getName());
    }

    @Test
    void testUpdateBuyerProfile_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> buyerService.updateBuyerProfile(1L, new User()));
    }

    @Test
    void testGetBuyerById_Success() {
        when(buyerRepository.findById(1L)).thenReturn(Optional.of(sampleBuyer));

        Optional<Buyer> result = buyerService.getBuyerById(1L);

        assertTrue(result.isPresent());
        assertEquals(sampleBuyer, result.get());
    }
}
