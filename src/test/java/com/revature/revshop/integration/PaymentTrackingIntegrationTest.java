package com.revature.revshop.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.revshop.model.*;
import com.revature.revshop.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
                "springdoc.api-docs.enabled=false",
                "springdoc.swagger-ui.enabled=false"
})
public class PaymentTrackingIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private AddressRepository addressRepository;

        @Autowired
        private OrdersRepository ordersRepository;

        @Autowired
        private PaymentsRepository paymentsRepository;

        @Autowired
        private TrackingDetailsRepository trackingDetailsRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        private ObjectMapper objectMapper;

        private User testUser;
        private Orders testOrder;

        @BeforeEach
        void setUp() {
                testUser = new User();
                testUser.setName("Test Buyer");
                testUser.setEmail("testbuyer@example.com");
                testUser.setPassword(passwordEncoder.encode("password"));
                testUser.setRole(Role.BUYER);
                testUser = userRepository.save(testUser);

                Address testAddress = new Address();
                testAddress.setAddressLine("123 Test Street");
                testAddress.setCity("TestCity");
                testAddress.setState("TestState");
                testAddress.setCountry("TestCountry");
                testAddress.setZipCode("12345");
                testAddress.setUser(testUser);
                testAddress = addressRepository.save(testAddress);

                testOrder = new Orders();
                testOrder.setUser(testUser);
                testOrder.setOrderNumber("ORD-INT-001");
                testOrder.setTotalAmount(new BigDecimal("500.00"));
                testOrder.setShippingAddress(testAddress);
                testOrder.setBillingAddress(testAddress);
                testOrder.setStatus(Orders.OrderStatus.PENDING);
                testOrder.setOrderDate(LocalDateTime.now());
                testOrder = ordersRepository.save(testOrder);
        }

        @Test
        @WithMockUser(username = "testbuyer@example.com", roles = "BUYER")
        void testCreatePayment_ReturnsPendingStatus() throws Exception {
                String paymentJson = "{\"amount\": 500.00, \"paymentMethod\": \"CREDIT_CARD\", \"transactionId\": \"TXN-INT-001\"}";

                mockMvc.perform(post("/api/payments")
                                .param("orderId", testOrder.getOrderId().toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(paymentJson))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.data.paymentStatus").value("PENDING"))
                                .andExpect(jsonPath("$.data.amount").value(500.00));
        }

        @Test
        @WithMockUser(username = "testbuyer@example.com", roles = "BUYER")
        void testUpdatePaymentStatus_ToSuccess() throws Exception {
                Payments payment = new Payments();
                payment.setOrder(testOrder);
                payment.setAmount(new BigDecimal("500.00"));
                payment.setPaymentMethod(Payments.PaymentMethod.CREDIT_CARD);
                payment.setPaymentStatus(Payments.PaymentStatus.PENDING);
                payment.setTransactionId("TXN-INT-002");
                payment.setPaymentDate(LocalDateTime.now());
                payment = paymentsRepository.save(payment);

                mockMvc.perform(put("/api/payments/" + payment.getPaymentId() + "/status")
                                .param("status", "SUCCESS"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"));
        }

        @Test
        @WithMockUser(username = "testbuyer@example.com", roles = "BUYER")
        void testCreateTrackingDetail_ForOrder() throws Exception {
                String trackingJson = "{\"status\": \"PROCESSING\", \"description\": \"Order is being processed\"}";

                mockMvc.perform(post("/api/tracking")
                                .param("orderId", testOrder.getOrderId().toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(trackingJson))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.data.status").value("PROCESSING"))
                                .andExpect(jsonPath("$.data.description").value("Order is being processed"));
        }

        @Test
        @WithMockUser(username = "testbuyer@example.com", roles = "BUYER")
        void testUpdateTrackingStatus_ToShipped() throws Exception {
                TrackingDetails tracking = new TrackingDetails();
                tracking.setOrder(testOrder);
                tracking.setStatus("PROCESSING");
                tracking.setDescription("Order is being processed");
                tracking = trackingDetailsRepository.save(tracking);

                mockMvc.perform(put("/api/tracking/" + tracking.getTrackingId())
                                .param("status", "SHIPPED")
                                .param("description", "Package shipped via courier"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("SHIPPED"))
                                .andExpect(jsonPath("$.data.description").value("Package shipped via courier"));
        }

        @Test
        @WithMockUser(username = "testbuyer@example.com", roles = "BUYER")
        void testFullFlow_OrderToPaymentToTracking() throws Exception {
                String paymentJson = "{\"amount\": 500.00, \"paymentMethod\": \"UPI\", \"transactionId\": \"TXN-FLOW-001\"}";

                MvcResult paymentResult = mockMvc.perform(post("/api/payments")
                                .param("orderId", testOrder.getOrderId().toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(paymentJson))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.data.paymentStatus").value("PENDING"))
                                .andReturn();

                Integer paymentId = objectMapper.readTree(paymentResult.getResponse().getContentAsString())
                                .path("data").path("paymentId").asInt();

                mockMvc.perform(put("/api/payments/" + paymentId + "/status")
                                .param("status", "SUCCESS"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"));

                String trackingJson = "{\"status\": \"SHIPPED\", \"description\": \"Package dispatched after payment confirmed\"}";

                mockMvc.perform(post("/api/tracking")
                                .param("orderId", testOrder.getOrderId().toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(trackingJson))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.data.status").value("SHIPPED"));
        }
}
