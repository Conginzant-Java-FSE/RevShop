package com.revature.revshop.service;

import com.revature.revshop.model.User;
import com.revature.revshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    public User updateUser(Long userId, User updatedUser) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // update all fields except password
        if (updatedUser.getName() != null) {
            existingUser.setName(updatedUser.getName());
        }
        if (updatedUser.getEmail() != null) {

            // check if email is already taken by another user
            Optional<User> userWithEmail = userRepository.findByEmail(updatedUser.getEmail());
            if (userWithEmail.isPresent() && !userWithEmail.get().getUserId().equals(userId)) {
                throw new RuntimeException("Email already exists");
            }
            existingUser.setEmail(updatedUser.getEmail());

        }
        if (updatedUser.getPhone() != null) {
            existingUser.setPhone(updatedUser.getPhone());
        }
        if (updatedUser.getAge() != null) {
            existingUser.setAge(updatedUser.getAge());
        }

        return userRepository.save(existingUser);
    }


    public User updateName(Long userId, String name) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setName(name);
        return userRepository.save(user);
    }


    public User updateEmail(Long userId, String email) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // check if email is already taken
        Optional<User> userWithEmail = userRepository.findByEmail(email);
        if (userWithEmail.isPresent() && !userWithEmail.get().getUserId().equals(userId)) {
            throw new RuntimeException("Email already exists");
        }

        user.setEmail(email);
        return userRepository.save(user);

    }


    public User updatePhone(Long userId, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPhone(phone);
        return userRepository.save(user);
    }


    public User updateAge(Long userId, Integer age) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setAge(age);
        return userRepository.save(user);
    }


    public User updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // verify old password
        if (!user.getPassword().equals(oldPassword)) {
            throw new RuntimeException("Invalid old password");
        }

        // update to new password
        user.setPassword(newPassword);
        return userRepository.save(user);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
