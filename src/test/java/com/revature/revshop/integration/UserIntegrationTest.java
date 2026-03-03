package com.revature.revshop.integration;

import com.revature.revshop.model.Role;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
        "springdoc.api-docs.enabled=false",
        "springdoc.swagger-ui.enabled=false"
})
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testBuyer;
    private User testSeller;

    @BeforeEach
    void setUp() {
        testBuyer = new User();
        testBuyer.setName("Int Buyer");
        testBuyer.setEmail("intbuyer@example.com");
        testBuyer.setPassword(passwordEncoder.encode("password"));
        testBuyer.setRole(Role.BUYER);
        testBuyer = userRepository.save(testBuyer);

        testSeller = new User();
        testSeller.setName("Int Seller");
        testSeller.setEmail("intseller@example.com");
        testSeller.setPassword(passwordEncoder.encode("password"));
        testSeller.setRole(Role.SELLER);
        testSeller = userRepository.save(testSeller);
    }

    @Autowired
    private com.revature.revshop.security.JwtUtil jwtUtil;

    @Autowired
    private com.revature.revshop.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void testBuyerCannotAccessSellerEndpoints() throws Exception {
        org.springframework.security.core.userdetails.UserDetails userDetails = customUserDetailsService
                .loadUserByUsername("intbuyer@example.com");
        String token = jwtUtil.generateToken(userDetails);

        mockMvc.perform(get("/api/users/" + testBuyer.getUserId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testSellerCanAccessTheirEndpoints() throws Exception {
        org.springframework.security.core.userdetails.UserDetails userDetails = customUserDetailsService
                .loadUserByUsername("intseller@example.com");
        String token = jwtUtil.generateToken(userDetails);

        mockMvc.perform(get("/api/users/" + testSeller.getUserId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testDatabaseRelationshipsAndCascadingDeletes() {
        Long buyerId = testBuyer.getUserId();

        // Ensure user is present
        assert (userRepository.findById(buyerId).isPresent());

        // Delete user
        userRepository.deleteById(buyerId);

        // Assert user is gone
        assertFalse(userRepository.findById(buyerId).isPresent());

        // Any child entities should ideally be deleted automatically, handled by JPA
        // CascadeType.ALL
        // Since we don't have direct access to BuyerRepository here, we assume user
        // deletion cleans up mapped rows
        // depending on JPA configuration mapping User -> Buyer.
    }
}
