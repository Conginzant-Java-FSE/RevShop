package com.revature.revshop.service;

import com.revature.revshop.exception.InvalidInputException;
import com.revature.revshop.exception.UserNotFoundException;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.revature.revshop.model.PasswordResetToken;
import com.revature.revshop.repository.PasswordResetTokenRepository;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final String USER_NOT_FOUND = "User not found";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    @Autowired
    public UserService(UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User updateUser(Long userId, User updatedUser) {
        log.info("Updating user id={}", userId);
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

        // update all fields except password
        if (updatedUser.getName() != null) {
            existingUser.setName(updatedUser.getName());
        }
        if (updatedUser.getEmail() != null) {

            // check if email is already taken by another user
            Optional<User> userWithEmail = userRepository.findByEmail(updatedUser.getEmail());
            if (userWithEmail.isPresent() && !userWithEmail.get().getUserId().equals(userId)) {
                throw new InvalidInputException("Email already exists");
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
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        user.setName(name);
        return userRepository.save(user);
    }

    public User updateEmail(Long userId, String email) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

        // check if email is already taken
        Optional<User> userWithEmail = userRepository.findByEmail(email);
        if (userWithEmail.isPresent() && !userWithEmail.get().getUserId().equals(userId)) {
            throw new InvalidInputException("Email already exists");
        }

        user.setEmail(email);
        return userRepository.save(user);

    }

    public User updatePhone(Long userId, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        user.setPhone(phone);
        return userRepository.save(user);
    }

    public User updateAge(Long userId, Integer age) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        user.setAge(age);
        return userRepository.save(user);
    }

    public User updatePassword(Long userId, String oldPassword, String newPassword) {
        log.info("Updating password for user id={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidInputException("Invalid old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    public User deactivateUser(Long userId) {
        log.info("Deactivating user id={}", userId);
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        existingUser.setActive(false);
        return userRepository.save(existingUser);
    }

    public User reactivateUser(Long userId) {
        log.info("Reactivating user id={}", userId);
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        existingUser.setActive(true);
        return userRepository.save(existingUser);
    }

    public User deleteUser(Long userId) {
        log.info("Deleting user id={}", userId);
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        existingUser.setActive(false);
        existingUser.setName("Deleted User");
        existingUser.setEmail("deleted_" + UUID.randomUUID().toString() + "@revshop.com");
        existingUser.setPhone(null);
        return userRepository.save(existingUser);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public String getSecurityQuestionByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return user.getSecurityQuestion();
    }

    public void resetPasswordBySecurity(String email, String securityAnswer, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (user.getSecurityAnswer() == null || !user.getSecurityAnswer().equalsIgnoreCase(securityAnswer)) {
            throw new InvalidInputException("Incorrect security answer");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void generatePasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        // Delete any existing token
        tokenRepository.deleteByUser(user);

        // Create new token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user, LocalDateTime.now().plusMinutes(15));
        tokenRepository.save(resetToken);

        // Send Email
        emailService.sendPasswordResetLinkEmail(email, token);
    }

    public void resetPasswordWithToken(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidInputException("Invalid or expired token"));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new InvalidInputException("Token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete token after successful reset
        tokenRepository.delete(resetToken);
    }
}
