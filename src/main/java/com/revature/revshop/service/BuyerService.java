package com.revature.revshop.service;

import com.revature.revshop.exception.UserNotFoundException;
import com.revature.revshop.model.Buyer;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.BuyerRepository;
import com.revature.revshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BuyerService {

    private final BuyerRepository buyerRepository;
    private final UserRepository userRepository;

    @Autowired
    public BuyerService(BuyerRepository buyerRepository, UserRepository userRepository) {
        this.buyerRepository = buyerRepository;
        this.userRepository = userRepository;
    }

    public Buyer registerBuyer(User user) {
        user.setRole(com.revature.revshop.model.Role.BUYER);

        Buyer buyerProfile = new Buyer();
        buyerProfile.setUser(user);
        user.setBuyerProfile(buyerProfile);

        if (user.getAddresses() != null) {
            user.getAddresses().forEach(address -> address.setUser(user));
        }

        User savedUser = userRepository.save(user);
        return savedUser.getBuyerProfile();
    }

    public Optional<Buyer> loginBuyer(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> com.revature.revshop.model.Role.BUYER.equals(user.getRole()))
                .filter(user -> user.getPassword().equals(password))
                .map(User::getBuyerProfile);
    }

    public Buyer updateBuyerProfile(Long buyerId, User updatedUserData) {
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
