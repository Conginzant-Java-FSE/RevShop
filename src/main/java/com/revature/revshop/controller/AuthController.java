package com.revature.revshop.controller;


import com.revature.revshop.dto.*;
import com.revature.revshop.model.Buyer;
import com.revature.revshop.model.Seller;
import com.revature.revshop.model.User;
import com.revature.revshop.service.BuyerService;
import com.revature.revshop.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private BuyerService buyerService;

    @Autowired
    private SellerService sellerService;

    @PostMapping("/register/buyer")
    // <?> is a wildcard generic, this response can contain any type of body.
    public ResponseEntity<?> registerBuyer(@RequestBody BuyerDTO buyerDTO){
        if (buyerService.existsByEmail(buyerDTO.getEmail())){
            return ResponseEntity.badRequest().body("Email already Exists");
        }
        User user = new User();
        user.setName(buyerDTO.getName());
        user.setEmail(buyerDTO.getEmail());
        user.setPassword(buyerDTO.getPassword());
        user.setPhone(buyerDTO.getPhone());
        user.setAge(buyerDTO.getAge());

        Buyer buyer = buyerService.registerBuyer(user);

        BuyerDTO responseDTO = new BuyerDTO();
        if(buyer.getUser() != null){
            responseDTO.setUserId(buyer.getUser().getUserId());
            responseDTO.setName(buyer.getUser().getName());
            responseDTO.setEmail(buyer.getUser().getEmail());
            responseDTO.setPhone(buyer.getUser().getPhone());
            responseDTO.setAge(buyer.getUser().getAge());
            responseDTO.setRole("BUYER");
        }

        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @PostMapping("/register/seller")
    public ResponseEntity<?> registerSeller(@RequestBody SellerDTO sellerDTO) {
        if (sellerService.existsByEmail(sellerDTO.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        User user = new User();
        user.setName(sellerDTO.getName());
        user.setEmail(sellerDTO.getEmail());
        user.setPhone(sellerDTO.getPhone());
        user.setAge(sellerDTO.getAge());
        user.setPassword(sellerDTO.getPassword());

        Seller seller = sellerService.registerSeller(user, sellerDTO.getBusinessName(),
                sellerDTO.getBusinessDescription(), sellerDTO.getTaxId());
        SellerDTO responseDTO = new SellerDTO();
        if(seller.getUser() != null){
            responseDTO.setUserId(seller.getUser().getUserId());
            responseDTO.setName(seller.getUser().getName());
            responseDTO.setEmail(seller.getUser().getEmail());
            responseDTO.setPhone(seller.getUser().getPhone());
            responseDTO.setAge(seller.getUser().getAge());
            responseDTO.setRole("SELLER");
        }
        responseDTO.setBusinessName(seller.getBusinessName());
        responseDTO.setBusinessDescription(seller.getBusinessDescription());
        responseDTO.setTaxId(seller.getTaxId());

        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @PostMapping("/login/buyer")
    public ResponseEntity<?> loginBuyer(@RequestBody LoginRequest loginRequest){
        Optional<Buyer> buyerOptional = buyerService.loginBuyer(loginRequest.getEmail(), loginRequest.getPassword());
        if ( buyerOptional.isPresent()) {
            Buyer buyer = buyerOptional.get();
            LoginResponse loginResponse = new LoginResponse();
            if (buyer.getUser() != null){
                loginResponse.setUserId(buyer.getUser().getUserId());
                loginResponse.setName(buyer.getUser().getName());
                loginResponse.setEmail(buyer.getUser().getEmail());
                loginResponse.setRole(buyer.getUser().getRole().toString());
            }
//            return ResponseEntity.ok("login successful");
            loginResponse.setMessage("login successful");
            return ResponseEntity.ok(loginResponse);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid email or password");

    }

    @PostMapping("/login/seller")
    public ResponseEntity<?> loginSeller(@RequestBody LoginRequest loginRequest){
        Optional<Seller> sellerOptional = sellerService.loginSeller(loginRequest.getEmail(), loginRequest.getPassword());

        if(sellerOptional.isPresent()){
            Seller seller = sellerOptional.get();
            LoginResponse loginResponse = new LoginResponse();
            if(seller.getUser() != null){
                loginResponse.setUserId(seller.getUser().getUserId());
                loginResponse.setName(seller.getUser().getName());
                loginResponse.setEmail(seller.getUser().getEmail());
                loginResponse.setRole(seller.getUser().getRole().toString());
            }
//            return ResponseEntity.ok("Login successful");
            loginResponse.setMessage("Login successful");
            return ResponseEntity.ok(loginResponse);
        }
        return new ResponseEntity<>("Invalid email or password", HttpStatus.UNAUTHORIZED);
    }

}
