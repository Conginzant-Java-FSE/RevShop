package com.revature.revshop.service;

import com.revature.revshop.exception.InvalidInputException;
import com.revature.revshop.exception.UserNotFoundException;
import com.revature.revshop.model.Role;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "passwordEncoder", passwordEncoder);
        sampleUser = new User();
        sampleUser.setUserId(1L);
        sampleUser.setName("John Doe");
        sampleUser.setEmail("john@example.com");
        sampleUser.setPassword("password123");
        sampleUser.setPhone("1234567890");
        sampleUser.setAge(30);
        sampleUser.setRole(Role.BUYER);
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(sampleUser));
        List<User> users = userService.getAllUsers();
        assertEquals(1, users.size());
        assertEquals(sampleUser.getName(), users.get(0).getName());
    }

    @Test
    void testGetUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        Optional<User> user = userService.getUserById(1L);
        assertTrue(user.isPresent());
        assertEquals("John Doe", user.get().getName());
    }

    @Test
    void testGetUserByEmail_Success() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));
        Optional<User> user = userService.getUserByEmail("john@example.com");
        assertTrue(user.isPresent());
        assertEquals("john@example.com", user.get().getEmail());
    }

    @Test
    void testUpdateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        User updatedDetails = new User();
        updatedDetails.setName("John Updated");
        updatedDetails.setPhone("0987654321");

        sampleUser.setName("John Updated");
        sampleUser.setPhone("0987654321");

        User result = userService.updateUser(1L, updatedDetails);
        assertEquals("John Updated", result.getName());
        assertEquals("0987654321", result.getPhone());
    }

    @Test
    void testUpdateUser_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(1L, new User()));
    }

    @Test
    void testUpdateUser_EmailAlreadyExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        User updatedDetails = new User();
        updatedDetails.setEmail("jane@example.com");

        User existingUserWithEmail = new User();
        existingUserWithEmail.setUserId(2L);
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(existingUserWithEmail));

        assertThrows(InvalidInputException.class, () -> userService.updateUser(1L, updatedDetails));
    }

    @Test
    void testUpdateName_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        userService.updateName(1L, "John Smith");
        assertEquals("John Smith", sampleUser.getName());
    }

    @Test
    void testUpdateEmail_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        userService.updateEmail(1L, "new@example.com");
        assertEquals("new@example.com", sampleUser.getEmail());
    }

    @Test
    void testUpdatePassword_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("password123", "password123")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        userService.updatePassword(1L, "password123", "newPassword");
        assertEquals("encodedNewPassword", sampleUser.getPassword());
    }

    @Test
    void testUpdatePassword_InvalidOldPassword() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongPassword", "password123")).thenReturn(false);

        assertThrows(InvalidInputException.class, () -> userService.updatePassword(1L, "wrongPassword", "newPassword"));
    }
}
