package com.revature.revshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.revshop.dto.PasswordUpdateRequest;
import com.revature.revshop.dto.UserDTO;
import com.revature.revshop.model.Role;
import com.revature.revshop.model.User;
import com.revature.revshop.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.revature.revshop.security.JwtUtil;
import com.revature.revshop.security.JwtAuthenticationFilter;
import com.revature.revshop.security.CustomUserDetailsService;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false) // Add this to bypass security filters for unit tests if needed, or use
                                          // @WithMockUser
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private User sampleUser;
    private UserDTO sampleUserDTO;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setUserId(1L);
        sampleUser.setName("John Doe");
        sampleUser.setEmail("john@example.com");
        sampleUser.setPhone("1234567890");
        sampleUser.setAge(30);
        sampleUser.setRole(Role.BUYER);

        sampleUserDTO = new UserDTO();
        sampleUserDTO.setUserId(1L);
        sampleUserDTO.setName("John Doe");
        sampleUserDTO.setEmail("john@example.com");
        sampleUserDTO.setPhone("1234567890");
        sampleUserDTO.setAge(30);
        sampleUserDTO.setRole("BUYER");
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void testGetAllUsers() throws Exception {
        Mockito.when(userService.getAllUsers()).thenReturn(Arrays.asList(sampleUser));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].email").value("john@example.com"));
    }

    @Test
    @WithMockUser
    void testGetUserById_Success() throws Exception {
        Mockito.when(userService.getUserById(1L)).thenReturn(Optional.of(sampleUser));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @WithMockUser
    void testGetUserById_NotFound() throws Exception {
        Mockito.when(userService.getUserById(2L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/2"))
                .andExpect(status().isNotFound()); // Assuming a global exception handler returns 404
        // Note: The actual controller throws RuntimeException, might result in 500 if
        // not handled.
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void testUpdateProfile_Success() throws Exception {
        Mockito.when(userService.getUserByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));
        Mockito.when(userService.updateUser(eq(1L), any(User.class))).thenReturn(sampleUser);

        mockMvc.perform(put("/api/users/1/profile")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @WithMockUser(username = "other@example.com")
    void testUpdateProfile_Unauthorized() throws Exception {
        User otherUser = new User();
        otherUser.setUserId(2L);
        otherUser.setEmail("other@example.com");

        Mockito.when(userService.getUserByEmail("other@example.com")).thenReturn(Optional.of(otherUser));

        mockMvc.perform(put("/api/users/1/profile")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleUserDTO)))
                .andExpect(status().isBadRequest()); // InvalidInputException typically maps to 400 Bad Request
    }

    @Test
    @WithMockUser
    void testUpdatePassword_Success() throws Exception {
        PasswordUpdateRequest request = new PasswordUpdateRequest();
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass");

        Mockito.when(userService.updatePassword(eq(1L), eq("oldPass"), eq("newPass"))).thenReturn(sampleUser);

        mockMvc.perform(put("/api/users/1/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password updated successfully"));
    }

    @Test
    @WithMockUser
    void testUpdateName_Success() throws Exception {
        Mockito.when(userService.updateName(1L, "New Name")).thenReturn(sampleUser);
        sampleUser.setName("New Name");

        mockMvc.perform(patch("/api/users/1/name")
                .with(csrf())
                .param("name", "New Name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }
}
