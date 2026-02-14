package com.revature.revshop.controller;

import com.revature.revshop.dto.PasswordUpdateRequest;
import com.revature.revshop.dto.UserDTO;
import com.revature.revshop.model.User;
import com.revature.revshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {


    private UserService userService;

    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<UserDTO> updateProfile(@PathVariable Long id, @RequestBody UserDTO userDTO){
        User user = convertToEntity(userDTO);
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(convertToDTO(updatedUser));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<String> updatePassword(@PathVariable Long id, @RequestBody PasswordUpdateRequest request){
        userService.updatePassword(id, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok("Password updated successfully");
    }

    @PatchMapping("/{id}/name")
    public ResponseEntity<UserDTO> updateName(@PathVariable Long id, @RequestParam String name) {
        User updatedUser = userService.updateName(id, name);
        return ResponseEntity.ok(convertToDTO(updatedUser));
    }

    @PatchMapping("/{id}/email")
    public ResponseEntity<UserDTO> updateEmail(@PathVariable Long id, @RequestParam String email) {
        User updatedUser = userService.updateEmail(id, email);
        return ResponseEntity.ok(convertToDTO(updatedUser));
    }

    @PatchMapping("/{id}/phone")
    public ResponseEntity<UserDTO> updatePhone(@PathVariable Long id, @RequestParam String phone) {
        User updatedUser = userService.updatePhone(id, phone);
        return ResponseEntity.ok(convertToDTO(updatedUser));
    }

    @PatchMapping("/{id}/age")
    public ResponseEntity<UserDTO> updateAge(@PathVariable Long id, @RequestParam Integer age) {
        User updatedUser = userService.updateAge(id, age);
        return ResponseEntity.ok(convertToDTO(updatedUser));
    }


    // DTO Conversion Methods - instead of the repeating same code everytime (to reduce BoilerPlate Code)
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
        if (user.getRole() != null) {
            dto.setRole(user.getRole().name());
        }
        return dto;
    }



}
