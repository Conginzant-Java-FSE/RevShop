package com.revature.revshop.controller;

import com.revature.revshop.dto.*;
import com.revature.revshop.exception.InvalidInputException;
import com.revature.revshop.model.Address;
import com.revature.revshop.model.Buyer;
import com.revature.revshop.model.Seller;
import com.revature.revshop.model.Shipper;
import com.revature.revshop.model.User;
import com.revature.revshop.security.CustomUserDetailsService;
import com.revature.revshop.security.JwtUtil;
import com.revature.revshop.service.BuyerService;
import com.revature.revshop.service.SellerService;
import com.revature.revshop.service.ShipperService;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

        private static final Logger log = LoggerFactory.getLogger(AuthController.class);
        private static final String INVALID_CREDENTIALS = "Invalid email or password";
        private static final String LOGIN_SUCCESSFUL = "Login successful";

        private final BuyerService buyerService;
        private final SellerService sellerService;
        private final ShipperService shipperService;
        private final AuthenticationManager authenticationManager;
        private final JwtUtil jwtUtil;
        private final PasswordEncoder passwordEncoder;
        private final CustomUserDetailsService userDetailsService;

        public AuthController(BuyerService buyerService,
                        SellerService sellerService,
                        ShipperService shipperService,
                        AuthenticationManager authenticationManager,
                        JwtUtil jwtUtil,
                        PasswordEncoder passwordEncoder,
                        CustomUserDetailsService userDetailsService) {
                this.buyerService = buyerService;
                this.sellerService = sellerService;
                this.shipperService = shipperService;
                this.authenticationManager = authenticationManager;
                this.jwtUtil = jwtUtil;
                this.passwordEncoder = passwordEncoder;
                this.userDetailsService = userDetailsService;
        }

        @PostMapping("/register/buyer")
        public ResponseEntity<ApiResponse<BuyerDTO>> registerBuyer(
                        @Valid @RequestBody BuyerDTO buyerDTO) {

                log.info("POST /api/auth/register/buyer - email={}", buyerDTO.getEmail());

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
                        @Valid @RequestBody SellerDTO sellerDTO) {

                log.info("POST /api/auth/register/seller - email={}", sellerDTO.getEmail());

                if (sellerService.existsByEmail(sellerDTO.getEmail())) {
                        throw new InvalidInputException("Email already exists");
                }

                User user = buildUserFromSellerDTO(sellerDTO);

                Seller seller = sellerService.registerSeller(
                                user,
                                sellerDTO.getBusinessName(),
                                sellerDTO.getBusinessDescription(),
                                sellerDTO.getTaxId());

                SellerDTO responseDTO = convertSellerToDTO(seller);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new ApiResponse<>("Seller registered successfully", responseDTO));
        }

        @PostMapping("/login/buyer")
        public ResponseEntity<ApiResponse<LoginResponse>> loginBuyer(
                        @Valid @RequestBody LoginRequest loginRequest) {

                log.info("POST /api/auth/login/buyer - email={}", loginRequest.getEmail());

                try {
                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        loginRequest.getEmail(),
                                                        loginRequest.getPassword()));
                } catch (AuthenticationException e) {
                        throw new InvalidInputException(INVALID_CREDENTIALS);
                }

                UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());

                String jwt = jwtUtil.generateToken(userDetails);

                Buyer buyer = buyerService.loginBuyer(
                                loginRequest.getEmail(),
                                loginRequest.getPassword())
                                .orElseThrow(() -> new InvalidInputException(INVALID_CREDENTIALS));

                LoginResponse response = buildLoginResponse(
                                buyer.getUser(), jwt);

                return ResponseEntity.ok(
                                new ApiResponse<>(LOGIN_SUCCESSFUL, response));
        }

        @PostMapping("/login/seller")
        public ResponseEntity<ApiResponse<LoginResponse>> loginSeller(
                        @Valid @RequestBody LoginRequest loginRequest) {

                log.info("POST /api/auth/login/seller - email={}", loginRequest.getEmail());

                try {
                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        loginRequest.getEmail(),
                                                        loginRequest.getPassword()));
                } catch (AuthenticationException e) {
                        throw new InvalidInputException(INVALID_CREDENTIALS);
                }

                UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());

                String jwt = jwtUtil.generateToken(userDetails);

                Seller seller = sellerService.loginSeller(
                                loginRequest.getEmail(),
                                loginRequest.getPassword())
                                .orElseThrow(() -> new InvalidInputException(INVALID_CREDENTIALS));

                LoginResponse response = buildLoginResponse(
                                seller.getUser(), jwt);

                return ResponseEntity.ok(
                                new ApiResponse<>(LOGIN_SUCCESSFUL, response));
        }

        @PostMapping("/login/shipper")
        public ResponseEntity<ApiResponse<ShipperLoginResponse>> loginShipper(
                        @RequestBody ShipperLoginRequest loginRequest) {

                log.info("POST /api/auth/login/shipper - email={}", loginRequest.getEmail());

                Shipper shipper = shipperService.loginShipper(
                                loginRequest.getEmail(),
                                loginRequest.getPassword())
                                .orElseThrow(() -> new InvalidInputException(INVALID_CREDENTIALS));

                UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
                String jwt = jwtUtil.generateToken(userDetails);

                ShipperLoginResponse response = new ShipperLoginResponse();
                response.setShipperId(shipper.getShipperId());
                response.setName(shipper.getName());
                response.setEmail(shipper.getEmail());
                response.setPhone(shipper.getPhone());
                response.setVehicleNumber(shipper.getVehicleNumber());
                response.setIsAvailable(shipper.getIsAvailable());
                response.setRole("SHIPPER");
                response.setToken(jwt);

                return ResponseEntity.ok(
                                new ApiResponse<>(LOGIN_SUCCESSFUL, response));
        }

        @PostMapping("/register/shipper")
        public ResponseEntity<ApiResponse<ShipperLoginResponse>> registerShipper(
                        @RequestBody ShipperRegisterRequest req) {

                log.info("POST /api/auth/register/shipper - email={}", req.getEmail());

                Shipper shipper = shipperService.registerShipper(
                                req.getName(),
                                req.getEmail(),
                                req.getPhone(),
                                req.getVehicleNumber(),
                                req.getPassword());

                UserDetails userDetails = userDetailsService.loadUserByUsername(req.getEmail());
                String jwt = jwtUtil.generateToken(userDetails);

                ShipperLoginResponse response = new ShipperLoginResponse();
                response.setShipperId(shipper.getShipperId());
                response.setName(shipper.getName());
                response.setEmail(shipper.getEmail());
                response.setPhone(shipper.getPhone());
                response.setVehicleNumber(shipper.getVehicleNumber());
                response.setIsAvailable(shipper.getIsAvailable());
                response.setRole("SHIPPER");
                response.setToken(jwt);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new ApiResponse<>("Shipper registered successfully", response));
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
                response.setMessage(LOGIN_SUCCESSFUL);

                return response;
        }

        private List<Address> mapAddresses(List<AddressDTO> addressDTOs, User user) {

                if (addressDTOs == null)
                        return new ArrayList<>();

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
                }).toList();
        }
}