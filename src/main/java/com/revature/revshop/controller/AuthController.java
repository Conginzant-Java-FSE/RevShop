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
import com.revature.revshop.service.EmailService;
import com.revature.revshop.service.OtpService;
import com.revature.revshop.service.SellerService;
import com.revature.revshop.service.ShipperService;
import com.revature.revshop.service.UserService;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        private final UserService userService;
        private final EmailService emailService;
        private final OtpService otpService;

        @org.springframework.beans.factory.annotation.Autowired
        private JwtUtil jwtUtil;
        @org.springframework.beans.factory.annotation.Autowired
        private PasswordEncoder passwordEncoder;
        @org.springframework.beans.factory.annotation.Autowired
        private CustomUserDetailsService userDetailsService;

        public AuthController(BuyerService buyerService,
                        SellerService sellerService,
                        ShipperService shipperService,
                        AuthenticationManager authenticationManager,
                        UserService userService,
                        EmailService emailService,
                        OtpService otpService) {
                this.buyerService = buyerService;
                this.sellerService = sellerService;
                this.shipperService = shipperService;
                this.authenticationManager = authenticationManager;
                this.userService = userService;
                this.emailService = emailService;
                this.otpService = otpService;
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

                // Send welcome email
                emailService.sendUserRegistrationEmail(buyer.getUser().getEmail(), buyer.getUser().getName());

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

                // Send welcome email
                emailService.sendUserRegistrationEmail(seller.getUser().getEmail(), seller.getUser().getName());

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
                } catch (BadCredentialsException e) {
                        log.warn("Login failed for buyer email={}: Invalid credentials", loginRequest.getEmail());
                        throw new InvalidInputException(INVALID_CREDENTIALS);
                } catch (DisabledException e) {
                        log.warn("Login failed for buyer email={}: Account is inactive", loginRequest.getEmail());
                        throw new InvalidInputException("Account is inactive. Please contact support.");
                } catch (AuthenticationException e) {
                        log.error("Authentication error for buyer email={}: {}", loginRequest.getEmail(),
                                        e.getMessage());
                        throw new InvalidInputException(INVALID_CREDENTIALS);
                }

                User user = userService.getUserByEmail(loginRequest.getEmail())
                                .orElseThrow(() -> new InvalidInputException(INVALID_CREDENTIALS));

                if (!com.revature.revshop.model.Role.BUYER.equals(user.getRole())) {
                        log.warn("Login failed for buyer email={}: Role mismatch. Expected BUYER, found {}",
                                        loginRequest.getEmail(), user.getRole());
                        throw new InvalidInputException("This account is not registered as a Buyer.");
                }

                UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
                String jwt = jwtUtil.generateToken(userDetails);

                LoginResponse response = buildLoginResponse(user, jwt);

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
                } catch (BadCredentialsException e) {
                        log.warn("Login failed for seller email={}: Invalid credentials", loginRequest.getEmail());
                        throw new InvalidInputException(INVALID_CREDENTIALS);
                } catch (DisabledException e) {
                        log.warn("Login failed for seller email={}: Account is inactive", loginRequest.getEmail());
                        throw new InvalidInputException("Account is inactive. Please contact support.");
                } catch (AuthenticationException e) {
                        log.error("Authentication error for seller email={}: {}", loginRequest.getEmail(),
                                        e.getMessage());
                        throw new InvalidInputException(INVALID_CREDENTIALS);
                }

                User user = userService.getUserByEmail(loginRequest.getEmail())
                                .orElseThrow(() -> new InvalidInputException(INVALID_CREDENTIALS));

                if (!com.revature.revshop.model.Role.SELLER.equals(user.getRole())) {
                        log.warn("Login failed for seller email={}: Role mismatch. Expected SELLER, found {}",
                                        loginRequest.getEmail(), user.getRole());
                        throw new InvalidInputException("This account is not registered as a Seller.");
                }

                UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
                String jwt = jwtUtil.generateToken(userDetails);

                LoginResponse response = buildLoginResponse(user, jwt);

                return ResponseEntity.ok(
                                new ApiResponse<>(LOGIN_SUCCESSFUL, response));
        }

        @PostMapping("/reactivate")
        public ResponseEntity<ApiResponse<LoginResponse>> reactivateAccount(
                        @Valid @RequestBody LoginRequest loginRequest) {

                log.info("POST /api/auth/reactivate - email={}", loginRequest.getEmail());

                try {
                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        loginRequest.getEmail(),
                                                        loginRequest.getPassword()));
                } catch (BadCredentialsException e) {
                        log.warn("Reactivation failed for email={}: Invalid credentials", loginRequest.getEmail());
                        throw new InvalidInputException(INVALID_CREDENTIALS);
                } catch (DisabledException e) {
                        log.info("Credentials verified for disabled account email={}. Reactivating...",
                                        loginRequest.getEmail());
                        User user = userService.getUserByEmail(loginRequest.getEmail())
                                        .orElseThrow(() -> new InvalidInputException(INVALID_CREDENTIALS));

                        // Reactivate user
                        user = userService.reactivateUser(user.getUserId());

                        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
                        String jwt = jwtUtil.generateToken(userDetails);

                        LoginResponse response = buildLoginResponse(user, jwt);

                        return ResponseEntity.ok(
                                        new ApiResponse<>("Account successfully reactivated", response));
                } catch (AuthenticationException e) {
                        log.error("Authentication error for email={}: {}", loginRequest.getEmail(),
                                        e.getMessage());
                        throw new InvalidInputException(INVALID_CREDENTIALS);
                }

                // If authenticate succeeded, user was already active!
                User user = userService.getUserByEmail(loginRequest.getEmail())
                                .orElseThrow(() -> new InvalidInputException(INVALID_CREDENTIALS));
                UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
                String jwt = jwtUtil.generateToken(userDetails);
                LoginResponse response = buildLoginResponse(user, jwt);
                return ResponseEntity.ok(
                                new ApiResponse<>("Account successfully reactivated", response));
        }

        @PostMapping("/login/shipper")
        public ResponseEntity<ApiResponse<ShipperLoginResponse>> loginShipper(
                        @RequestBody ShipperLoginRequest loginRequest) {

                log.info("POST /api/auth/login/shipper - email={}", loginRequest.getEmail());

                try {
                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        loginRequest.getEmail(),
                                                        loginRequest.getPassword()));
                } catch (BadCredentialsException e) {
                        log.warn("Login failed for shipper email={}: Invalid credentials", loginRequest.getEmail());
                        throw new InvalidInputException(INVALID_CREDENTIALS);
                } catch (AuthenticationException e) {
                        log.error("Authentication error for shipper email={}: {}", loginRequest.getEmail(),
                                        e.getMessage());
                        throw new InvalidInputException(INVALID_CREDENTIALS);
                }

                // In this architecture, shippers might not be in the User table but in Shipper
                // table.
                // However CustomUserDetailsService handles both.
                // Let's get shipper details.
                Shipper shipper = shipperService.getShipperByEmail(loginRequest.getEmail())
                                .orElseThrow(() -> {
                                        log.warn("Login failed for shipper email={}: Shipper profile not found",
                                                        loginRequest.getEmail());
                                        return new InvalidInputException(INVALID_CREDENTIALS);
                                });

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

                // Send welcome email
                emailService.sendUserRegistrationEmail(shipper.getEmail(), shipper.getName());

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

        // ===================== Forgot Password =====================

        @GetMapping("/security-question")
        public ResponseEntity<ApiResponse<String>> getSecurityQuestion(@RequestParam String email) {
                log.info("GET /api/auth/security-question - email={}", email);
                String question = userService.getSecurityQuestionByEmail(email);
                return ResponseEntity.ok(new ApiResponse<>("Security question fetched", question));
        }

        @PostMapping("/reset-password")
        public ResponseEntity<ApiResponse<String>> resetPassword(
                        @RequestBody ForgotPasswordRequest request) {
                log.info("POST /api/auth/reset-password - email={}", request.getEmail());
                userService.resetPasswordBySecurity(
                                request.getEmail(),
                                request.getSecurityAnswer(),
                                request.getNewPassword());
                return ResponseEntity.ok(new ApiResponse<>("Password reset successful", null));
        }

        // ===================== OTP Verification =====================

        @PostMapping("/otp/send")
        public ResponseEntity<ApiResponse<String>> sendOtp(@RequestParam String email) {
                log.info("POST /api/auth/otp/send - email={}", email);
                otpService.generateOtp(email);
                return ResponseEntity.ok(new ApiResponse<>("OTP sent successfully to " + email, null));
        }

        @PostMapping("/otp/verify")
        public ResponseEntity<ApiResponse<Boolean>> verifyOtp(@RequestBody Map<String, String> body) {
                String email = body.get("email");
                String otp = body.get("otp");
                log.info("POST /api/auth/otp/verify - email={}", email);

                boolean isValid = otpService.verifyOtp(email, otp);
                if (!isValid) {
                        return ResponseEntity.badRequest().body(new ApiResponse<>("Invalid or expired OTP", false));
                }
                return ResponseEntity.ok(new ApiResponse<>("OTP verified successfully", true));
        }

        // ===================== Forgot Password / Links =====================

        @PostMapping("/forgot-password/send-link")
        public ResponseEntity<ApiResponse<String>> sendResetLink(@RequestBody Map<String, String> body) {
                String email = body.get("email");
                log.info("POST /api/auth/forgot-password/send-link - email={}", email);
                userService.generatePasswordResetToken(email);
                return ResponseEntity.ok(new ApiResponse<>("Password reset link sent to " + email, null));
        }

        @PostMapping("/forgot-password/reset-via-link")
        public ResponseEntity<ApiResponse<String>> resetViaLink(@RequestBody Map<String, String> body) {
                String token = body.get("token");
                String newPassword = body.get("newPassword");
                log.info("POST /api/auth/forgot-password/reset-via-link");
                userService.resetPasswordWithToken(token, newPassword);
                return ResponseEntity.ok(new ApiResponse<>("Password reset successfully", null));
        }
}
