package com.revature.revshop.service;

import com.revature.revshop.exception.UserNotFoundException;
import com.revature.revshop.model.Seller;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.SellerRepository;
import com.revature.revshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SellerService {

    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;

    @Autowired
    public SellerService(SellerRepository sellerRepository, UserRepository userRepository) {
        this.sellerRepository = sellerRepository;
        this.userRepository = userRepository;
    }

    public Seller registerSeller(User user, String businessName, String businessDescription, String taxId) {
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
        return savedUser.getSellerProfile();
    }

    public Optional<Seller> loginSeller(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> com.revature.revshop.model.Role.SELLER.equals(user.getRole()))
                .filter(user -> user.getPassword().equals(password))
                .map(User::getSellerProfile);
    }

    public Seller updateSellerProfile(Long sellerId, User updatedUserData, String businessName,
                                      String businessDescription, String taxId) {
        User existingUser = userRepository.findById(sellerId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        existingUser.setName(updatedUserData.getName());
        existingUser.setPhone(updatedUserData.getPhone());

        Seller sellerProfile = existingUser.getSellerProfile();
        sellerProfile.setBusinessName(businessName);
        sellerProfile.setBusinessDescription(businessDescription);
        sellerProfile.setTaxId(taxId);

        if (updatedUserData.getAddresses() != null) {
            existingUser.getAddresses().clear();
            updatedUserData.getAddresses().forEach(address -> {
                address.setUser(existingUser);
                existingUser.getAddresses().add(address);
            });
        }

        User savedUser = userRepository.save(existingUser);
        return savedUser.getSellerProfile();
    }

    public Optional<Seller> getSellerById(Long sellerId) {
        return sellerRepository.findById(sellerId);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
