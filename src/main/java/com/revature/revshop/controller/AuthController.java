package com.revature.revshop.controller;

import com.revature.revshop.dto.*;
import com.revature.revshop.exception.InvalidInputException;
import com.revature.revshop.model.Address;
import com.revature.revshop.model.Buyer;
import com.revature.revshop.model.Seller;
import com.revature.revshop.model.User;
import com.revature.revshop.security.CustomUserDetailsService;
import com.revature.revshop.security.JwtUtil;
import com.revature.revshop.service.BuyerService;
import com.revature.revshop.service.SellerService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final BuyerService buyerService;
    private final SellerService sellerService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;

    public AuthController(BuyerService buyerService,
                          SellerService sellerService,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder,
                          CustomUserDetailsService userDetailsService) {
        this.buyerService = buyerService;
        this.sellerService = sellerService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }


    @PostMapping("/register/buyer")
    public ResponseEntity<ApiResponse<BuyerDTO>> registerBuyer(
            @RequestBody BuyerDTO buyerDTO) {

        if (buyerService.existsByEmail(buyerDTO.getEmail())) {
            throw new InvalidInputException("Email already exists");
        }

        User user = buildUserFromBuyerDTO(buyerDTO);

        Buyer buyer = buyerService.registerBuyer(user);

        BuyerDTO responseDTO = convertBuyerToDTO(buyer);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Buyer registered successfully", responseDTO));
    }


    @PostMapping("/register/seller")
    public ResponseEntity<ApiResponse<SellerDTO>> registerSeller(
            @RequestBody SellerDTO sellerDTO) {

        if (sellerService.existsByEmail(sellerDTO.getEmail())) {
            throw new InvalidInputException("Email already exists");
        }

        User user = buildUserFromSellerDTO(sellerDTO);

        Seller seller = sellerService.registerSeller(
                user,
                sellerDTO.getBusinessName(),
                sellerDTO.getBusinessDescription(),
                sellerDTO.getTaxId()
        );

        SellerDTO responseDTO = convertSellerToDTO(seller);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Seller registered successfully", responseDTO));
    }


    @PostMapping("/login/buyer")
    public ResponseEntity<ApiResponse<LoginResponse>> loginBuyer(
            @RequestBody LoginRequest loginRequest) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            throw new InvalidInputException("Invalid email or password");
        }

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(loginRequest.getEmail());

        String jwt = jwtUtil.generateToken(userDetails);

        Buyer buyer = buyerService.loginBuyer(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        ).orElseThrow(() ->
                new InvalidInputException("Invalid email or password"));

        LoginResponse response = buildLoginResponse(
                buyer.getUser(), jwt);

        return ResponseEntity.ok(
                new ApiResponse<>("Login successful", response)
        );
    }


    @PostMapping("/login/seller")
    public ResponseEntity<ApiResponse<LoginResponse>> loginSeller(
            @RequestBody LoginRequest loginRequest) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            throw new InvalidInputException("Invalid email or password");
        }

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(loginRequest.getEmail());

        String jwt = jwtUtil.generateToken(userDetails);

        Seller seller = sellerService.loginSeller(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        ).orElseThrow(() ->
                new InvalidInputException("Invalid email or password"));

        LoginResponse response = buildLoginResponse(
                seller.getUser(), jwt);

        return ResponseEntity.ok(
                new ApiResponse<>("Login successful", response)
        );
    }


    private User buildUserFromBuyerDTO(BuyerDTO dto) {

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setAge(dto.getAge());
        user.setSecurityQuestion(dto.getSecurityQuestion());
        user.setSecurityAnswer(dto.getSecurityAnswer());
        user.setAddresses(mapAddresses(dto.getAddresses(), user));

        return user;
    }

    private User buildUserFromSellerDTO(SellerDTO dto) {

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setAge(dto.getAge());
        user.setSecurityQuestion(dto.getSecurityQuestion());
        user.setSecurityAnswer(dto.getSecurityAnswer());
        user.setAddresses(mapAddresses(dto.getAddresses(), user));

        return user;
    }

    private BuyerDTO convertBuyerToDTO(Buyer buyer) {

        BuyerDTO dto = new BuyerDTO();
        dto.setUserId(buyer.getUser().getUserId());
        dto.setName(buyer.getUser().getName());
        dto.setEmail(buyer.getUser().getEmail());
        dto.setPhone(buyer.getUser().getPhone());
        dto.setAge(buyer.getUser().getAge());
        dto.setRole("BUYER");

        return dto;
    }

    private SellerDTO convertSellerToDTO(Seller seller) {

        SellerDTO dto = new SellerDTO();
        dto.setUserId(seller.getUser().getUserId());
        dto.setName(seller.getUser().getName());
        dto.setEmail(seller.getUser().getEmail());
        dto.setPhone(seller.getUser().getPhone());
        dto.setAge(seller.getUser().getAge());
        dto.setRole("SELLER");
        dto.setBusinessName(seller.getBusinessName());
        dto.setBusinessDescription(seller.getBusinessDescription());
        dto.setTaxId(seller.getTaxId());

        return dto;
    }

    private LoginResponse buildLoginResponse(User user, String token) {

        LoginResponse response = new LoginResponse();
        response.setUserId(user.getUserId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setToken(token);
        response.setMessage("Login successful");

        return response;
    }

    private List<Address> mapAddresses(List<AddressDTO> addressDTOs, User user) {

        if (addressDTOs == null) return new ArrayList<>();

        return addressDTOs.stream().map(dto -> {
            Address address = new Address();
            address.setAddressLine(dto.getAddressLine());
            address.setCity(dto.getCity());
            address.setState(dto.getState());
            address.setZipCode(dto.getZipCode());
            address.setCountry(dto.getCountry());
            address.setIsDefault(dto.getIsDefault() != null && dto.getIsDefault());
            address.setAddressType(dto.getAddressType());
            address.setUser(user);
            return address;
        }).collect(Collectors.toList());
    }
}