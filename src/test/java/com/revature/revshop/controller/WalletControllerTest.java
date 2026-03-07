package com.revature.revshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.revshop.model.User;
import com.revature.revshop.model.Wallet;
import com.revature.revshop.service.UserService;
import com.revature.revshop.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WalletControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WalletService walletService;

    @Mock
    private UserService userService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private User sampleUser;
    private Wallet sampleWallet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        WalletController walletController = new WalletController(walletService, userService);
        mockMvc = MockMvcBuilders.standaloneSetup(walletController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("user@example.com", null,
                Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        sampleUser = new User();
        sampleUser.setUserId(1L);
        sampleUser.setEmail("user@example.com");

        sampleWallet = new Wallet();
        sampleWallet.setWalletId(1L);
        sampleWallet.setUser(sampleUser);
        sampleWallet.setBalance(new BigDecimal("100.00"));
        sampleWallet.setKycVerified(true);
        sampleWallet.setActive(true);
    }

    @Test
    void testGetBalance_Success() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(java.util.Optional.of(sampleUser));
        when(walletService.getWalletByUser(1L)).thenReturn(sampleWallet);

        mockMvc.perform(get("/api/wallets/balance"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(100.00));
    }

    @Test
    void testSendSmsOtp_Success() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(java.util.Optional.of(sampleUser));

        mockMvc.perform(post("/api/wallets/kyc/send-sms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("mobileNumber", "1234567890"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("SMS OTP sent successfully to 1234567890"));
    }

    @Test
    void testVerifyKyc_Success() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(java.util.Optional.of(sampleUser));
        when(walletService.verifyMobileKyc(anyLong(), anyString(), anyString())).thenReturn(sampleWallet);

        mockMvc.perform(post("/api/wallets/kyc/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("mobileNumber", "1234567890", "otp", "123456"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Wallet created and KYC verified successfully"));
    }

    @Test
    void testGetTransactions_Success() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(java.util.Optional.of(sampleUser));
        when(walletService.getWalletTransactions(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/wallets/transactions"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
