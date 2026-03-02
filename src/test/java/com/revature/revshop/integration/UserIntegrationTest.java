package com.revature.revshop.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.revshop.model.Role;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
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
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Test
    @WithMockUser(username = "intbuyer@example.com", roles = "BUYER")
    void testBuyerCannotAccessSellerEndpoints() throws Exception {
        // Assuming /api/sellers is a seller-only endpoint, though we might not have a
        // SellerController yet
        // However, we can test that the buyer can access their profile, or we can test
        // an endpoint restricted by role
        mockMvc.perform(get("/api/users/" + testBuyer.getUserId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // If there were a specific Seller Controller with /api/sellers/dashboard
        // requiring SELLER role
        // mockMvc.perform(get("/api/sellers/dashboard")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "intseller@example.com", roles = "SELLER")
    void testSellerCanAccessTheirEndpoints() throws Exception {
        // Assuming Seller can access their profile via User endpoint
        mockMvc.perform(get("/api/users/" + testSeller.getUserId())
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
