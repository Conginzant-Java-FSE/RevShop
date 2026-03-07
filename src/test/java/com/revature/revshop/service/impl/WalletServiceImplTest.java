package com.revature.revshop.service.impl;

import com.revature.revshop.model.User;
import com.revature.revshop.model.Wallet;
import com.revature.revshop.model.WalletTransaction;
import com.revature.revshop.repository.UserRepository;
import com.revature.revshop.repository.WalletRepository;
import com.revature.revshop.repository.WalletTransactionRepository;
import com.revature.revshop.service.TwilioSmsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TwilioSmsService twilioSmsService;

    @InjectMocks
    private WalletServiceImpl walletService;

    private User sampleUser;
    private Wallet sampleWallet;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setUserId(1L);

        sampleWallet = new Wallet();
        sampleWallet.setWalletId(1L);
        sampleWallet.setUser(sampleUser);
        sampleWallet.setBalance(new BigDecimal("100.00"));
        sampleWallet.setKycVerified(true);
        sampleWallet.setActive(true);

        ReflectionTestUtils.setField(walletService, "razorpayKeyId", "test_key");
        ReflectionTestUtils.setField(walletService, "razorpayKeySecret", "test_secret");
    }

    @Test
    void testSendSmsOtp_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        assertDoesNotThrow(() -> walletService.sendSmsOtp(1L, "1234567890"));
        verify(twilioSmsService).sendSmsOtp("1234567890");
    }

    @Test
    void testVerifyMobileKyc_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(twilioSmsService.verifyMobileKyc("1234567890", "123456")).thenReturn(true);
        when(walletRepository.findByUser_UserId(1L)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArguments()[0]);

        Wallet result = walletService.verifyMobileKyc(1L, "1234567890", "123456");

        assertNotNull(result);
        assertTrue(result.isKycVerified());
        assertTrue(result.isActive());
        assertEquals("1234567890", result.getMobileNumber());
    }

    @Test
    void testVerifyMobileKyc_InvalidOtp() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(twilioSmsService.verifyMobileKyc("1234567890", "123456")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> walletService.verifyMobileKyc(1L, "1234567890", "123456"));
    }

    @Test
    void testGetWalletByUser_Success() {
        when(walletRepository.findByUser_UserId(1L)).thenReturn(Optional.of(sampleWallet));

        Wallet result = walletService.getWalletByUser(1L);

        assertEquals(sampleWallet, result);
    }

    @Test
    void testDeductMoneyFromWallet_Success() {
        when(walletRepository.findByUser_UserId(1L)).thenReturn(Optional.of(sampleWallet));

        boolean result = walletService.deductMoneyFromWallet(1L, new BigDecimal("50.00"), "Test Deduction", "REF-1");

        assertTrue(result);
        assertEquals(new BigDecimal("50.00"), sampleWallet.getBalance());
        verify(walletRepository).save(sampleWallet);
        verify(walletTransactionRepository).save(any(WalletTransaction.class));
    }

    @Test
    void testDeductMoneyFromWallet_InsufficientBalance() {
        when(walletRepository.findByUser_UserId(1L)).thenReturn(Optional.of(sampleWallet));

        assertThrows(RuntimeException.class,
                () -> walletService.deductMoneyFromWallet(1L, new BigDecimal("150.00"), "Test", "REF-1"));
    }

    @Test
    void testGetWalletTransactions() {
        when(walletRepository.findByUser_UserId(1L)).thenReturn(Optional.of(sampleWallet));
        when(walletTransactionRepository.findByWallet_WalletIdOrderByCreatedAtDesc(1L))
                .thenReturn(Collections.singletonList(new WalletTransaction()));

        List<WalletTransaction> result = walletService.getWalletTransactions(1L);

        assertEquals(1, result.size());
    }
}
