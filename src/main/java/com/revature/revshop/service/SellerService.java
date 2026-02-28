package com.revature.revshop.service;

import com.revature.revshop.model.Seller;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.SellerRepository;
import com.revature.revshop.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SellerService {

    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public SellerService(SellerRepository sellerRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            NotificationService notificationService) {

        this.sellerRepository = sellerRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    public Seller registerSeller(User user,
            String businessName,
            String businessDescription,
            String taxId) {

        user.setRole(com.revature.revshop.model.Role.SELLER);

        Seller sellerProfile = new Seller();
        sellerProfile.setBusinessName(businessName);
        sellerProfile.setBusinessDescription(businessDescription);
        sellerProfile.setTaxId(taxId);
        sellerProfile.setUser(user);
        user.setSellerProfile(sellerProfile);

        if (user.getAddresses() != null) {
            user.getAddresses().forEach(address -> address.setUser(user));
        }

        User savedUser = userRepository.save(user);

        notificationService.createNotification(
                savedUser.getUserId(),
                "Welcome to RevShop",
                "Your seller account has been created successfully.");

        return savedUser.getSellerProfile();
    }

    public Optional<Seller> loginSeller(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> com.revature.revshop.model.Role.SELLER.equals(user.getRole()))
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .map(User::getSellerProfile);
    }

    public Optional<Seller> getSellerById(Long sellerId) {
        return sellerRepository.findById(sellerId);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}