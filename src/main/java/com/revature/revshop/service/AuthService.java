package com.revature.revshop.service;

import com.revature.revshop.model.User;
import com.revature.revshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String getSecurityQuestion(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            return user.get().getSecurityQuestion();
        }
        throw new com.revature.revshop.exception.UserNotFoundException("User not found with email: " + email);
    }

    public boolean resetPassword(String email, String answer, String newPassword) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Case-insensitive check for the answer
            if (user.getSecurityAnswer() != null && user.getSecurityAnswer().equalsIgnoreCase(answer)) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }
}
