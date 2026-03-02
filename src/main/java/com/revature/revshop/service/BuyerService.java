package com.revature.revshop.service;

import com.revature.revshop.exception.UserNotFoundException;
import com.revature.revshop.model.Buyer;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.BuyerRepository;
import com.revature.revshop.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BuyerService {

    private static final Logger log = LoggerFactory.getLogger(BuyerService.class);

    private final BuyerRepository buyerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public BuyerService(BuyerRepository buyerRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            NotificationService notificationService) {

        this.buyerRepository = buyerRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    public Buyer registerBuyer(User user) {
        log.info("Registering buyer email={}", user.getEmail());

        user.setRole(com.revature.revshop.model.Role.BUYER);

        Buyer buyerProfile = new Buyer();
        buyerProfile.setUser(user);
        user.setBuyerProfile(buyerProfile);

        if (user.getAddresses() != null) {
            user.getAddresses().forEach(address -> address.setUser(user));
        }

        User savedUser = userRepository.save(user);

        notificationService.createNotification(
                savedUser.getUserId(),
                "Welcome to RevShop",
                "Your buyer account has been created successfully.");

        return savedUser.getBuyerProfile();
    }

    public Optional<Buyer> loginBuyer(String email, String password) {
        log.info("Buyer login attempt email={}", email);
        return userRepository.findByEmail(email)
                .filter(user -> com.revature.revshop.model.Role.BUYER.equals(user.getRole()))
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .map(User::getBuyerProfile);
    }

    public Buyer updateBuyerProfile(Long buyerId, User updatedUserData) {
        log.info("Updating buyer profile id={}", buyerId);
        User existingUser = userRepository.findById(buyerId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        existingUser.setName(updatedUserData.getName());
        existingUser.setPhone(updatedUserData.getPhone());

        if (updatedUserData.getAddresses() != null) {
            existingUser.getAddresses().clear();
            updatedUserData.getAddresses().forEach(address -> {
                address.setUser(existingUser);
                existingUser.getAddresses().add(address);
            });
        }

        User savedUser = userRepository.save(existingUser);
        return savedUser.getBuyerProfile();
    }

    public Optional<Buyer> getBuyerById(Long buyerId) {
        return buyerRepository.findById(buyerId);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
