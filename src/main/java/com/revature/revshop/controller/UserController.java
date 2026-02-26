package com.revature.revshop.controller;

import com.revature.revshop.dto.*;
import com.revature.revshop.exception.InvalidInputException;
import com.revature.revshop.exception.UserNotFoundException;
import com.revature.revshop.model.User;
import com.revature.revshop.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {

        List<UserDTO> list = userService.getAllUsers()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                new ApiResponse<>("Users fetched successfully", list)
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(
            @PathVariable Long id) {

        User user = userService.getUserById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        return ResponseEntity.ok(
                new ApiResponse<>("User fetched successfully",
                        convertToDTO(user))
        );
    }


    @PutMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            @PathVariable Long id,
            @RequestBody UserDTO userDTO) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        String loggedInEmail = userDetails.getUsername();

        User loggedInUser = userService.getUserByEmail(loggedInEmail)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        if (!loggedInUser.getUserId().equals(id)) {
            throw new InvalidInputException(
                    "You cannot update another user's profile");
        }

        User updatedUser =
                userService.updateUser(id, convertToEntity(userDTO));

        return ResponseEntity.ok(
                new ApiResponse<>("Profile updated successfully",
                        convertToDTO(updatedUser))
        );
    }


    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @PathVariable Long id,
            @RequestBody PasswordUpdateRequest request) {

        userService.updatePassword(
                id,
                request.getOldPassword(),
                request.getNewPassword()
        );

        return ResponseEntity.ok(
                new ApiResponse<>("Password updated successfully", null)
        );
    }


    @PatchMapping("/{id}/name")
    public ResponseEntity<ApiResponse<UserDTO>> updateName(
            @PathVariable Long id,
            @RequestParam String name) {

        User updatedUser = userService.updateName(id, name);

        return ResponseEntity.ok(
                new ApiResponse<>("Name updated successfully",
                        convertToDTO(updatedUser))
        );
    }


    @PatchMapping("/{id}/email")
    public ResponseEntity<ApiResponse<UserDTO>> updateEmail(
            @PathVariable Long id,
            @RequestParam String email) {

        User updatedUser = userService.updateEmail(id, email);

        return ResponseEntity.ok(
                new ApiResponse<>("Email updated successfully",
                        convertToDTO(updatedUser))
        );
    }


    @PatchMapping("/{id}/phone")
    public ResponseEntity<ApiResponse<UserDTO>> updatePhone(
            @PathVariable Long id,
            @RequestParam String phone) {

        User updatedUser = userService.updatePhone(id, phone);

        return ResponseEntity.ok(
                new ApiResponse<>("Phone updated successfully",
                        convertToDTO(updatedUser))
        );
    }


    @PatchMapping("/{id}/age")
    public ResponseEntity<ApiResponse<UserDTO>> updateAge(
            @PathVariable Long id,
            @RequestParam Integer age) {

        User updatedUser = userService.updateAge(id, age);

        return ResponseEntity.ok(
                new ApiResponse<>("Age updated successfully",
                        convertToDTO(updatedUser))
        );
    }


    private User convertToEntity(UserDTO dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setAge(dto.getAge());
        return user;
    }

    private UserDTO convertToDTO(User user) {

        UserDTO dto = new UserDTO();

        dto.setUserId(user.getUserId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAge(user.getAge());

        if (user.getRole() != null)
            dto.setRole(user.getRole().name());

        return dto;
    }
}