package com.revature.revshop.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.revshop.dto.ProductDTO;
import com.revature.revshop.model.*;
import com.revature.revshop.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class ProductCatalogIntegrationTest {

        @Autowired
        private MockMvc mockMvc;
        private ObjectMapper objectMapper = new ObjectMapper();

        @Autowired
        private CategoryRepository categoryRepository;
        @Autowired
        private UserRepository userRepository;
        @Autowired
        private EntityManager entityManager;

        private Long savedCategoryId;
        private Long savedSellerId;
        private Long savedBuyerId;

        @BeforeEach
        void setUp() {
                // Category
                Category cat = new Category();
                cat.setName("Tech");
                cat.setDescription("Technology products");
                savedCategoryId = categoryRepository.save(cat).getCategoryId();

                // Seller user
                User sellerUser = new User();
                sellerUser.setName("Seller One");
                sellerUser.setEmail("seller@inttest.com");
                sellerUser.setPassword("$2a$10$dummy_seller");
                sellerUser.setRole(Role.SELLER);
                sellerUser = userRepository.save(sellerUser);
                Seller seller = new Seller(sellerUser);
                entityManager.persist(seller);
                savedSellerId = seller.getUserId();

                // Buyer user
                User buyerUser = new User();
                buyerUser.setName("Buyer One");
                buyerUser.setEmail("buyer@inttest.com");
                buyerUser.setPassword("$2a$10$dummy_buyer");
                buyerUser.setRole(Role.BUYER);
                buyerUser = userRepository.save(buyerUser);
                Buyer buyer = new Buyer(buyerUser);
                entityManager.persist(buyer);
                savedBuyerId = buyer.getUserId();
        }

        private Long createProductViaApi(String name) throws Exception {
                ProductDTO dto = new ProductDTO();
                dto.setName(name);
                dto.setDescription("Integration test product: " + name);
                dto.setMrp(new BigDecimal("10000"));
                dto.setSellingPrice(new BigDecimal("8000"));
                dto.setStockQuantity(100);
                dto.setThresholdQuantity(10);
                dto.setIsActive(true);
                dto.setCategoryId(savedCategoryId);
                dto.setSellerId(savedSellerId);

                MvcResult result = mockMvc.perform(post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andDo(print())
                                .andExpect(status().isCreated())
                                .andReturn();

                return objectMapper.readTree(result.getResponse().getContentAsString())
                                .path("data").path("productId").asLong();
        }

        @Test
        void createProduct_thenGetById_shouldReturnProductWithCorrectCategory() throws Exception {
                Long productId = createProductViaApi("4K Monitor");

                mockMvc.perform(get("/api/products/{id}", productId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.name").value("4K Monitor"))
                                .andExpect(jsonPath("$.data.categoryId").value(savedCategoryId))
                                .andExpect(jsonPath("$.data.sellerId").value(savedSellerId))
                                .andExpect(jsonPath("$.data.sellingPrice").value(8000));
        }

        @Test
        void getCategoryById_shouldMatchCategoryUsedInProduct() throws Exception {
                Long productId = createProductViaApi("USB Hub");

                MvcResult productResult = mockMvc.perform(get("/api/products/{id}", productId))
                                .andExpect(status().isOk())
                                .andReturn();

                long productCategoryId = objectMapper.readTree(
                                productResult.getResponse().getContentAsString())
                                .path("data").path("categoryId").asLong();

                mockMvc.perform(get("/api/categories/{id}", productCategoryId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.name").value("Tech"))
                                .andExpect(jsonPath("$.data.description").value("Technology products"));

                assertThat(productCategoryId).isEqualTo(savedCategoryId);
        }

        @Test
        void getAllProducts_shouldIncludeCreatedProduct() throws Exception {
                createProductViaApi("Gaming Mouse");

                mockMvc.perform(get("/api/products"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))))
                                .andExpect(jsonPath("$.data.content[*].name", hasItem("Gaming Mouse")));
        }

        @Test
        void searchProduct_shouldFindProductByKeyword() throws Exception {
                createProductViaApi("Mechanical Keyboard");

                mockMvc.perform(get("/api/products/search").param("keyword", "Keyboard"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.content[*].name", hasItem("Mechanical Keyboard")));
        }

        @Test
        void addToFavorites_thenGetFavorites_shouldShowProductInList() throws Exception {
                Long productId = createProductViaApi("Webcam");

                mockMvc.perform(post("/api/favorites/{buyerId}/{productId}",
                                savedBuyerId, productId))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.message")
                                                .value("Product added to favorites successfully"));

                mockMvc.perform(get("/api/favorites/{buyerId}", savedBuyerId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data", hasSize(1)))
                                .andExpect(jsonPath("$.data[0].productId").value(productId))
                                .andExpect(jsonPath("$.data[0].productName").value("Webcam"));
        }

        @Test
        void getFavorites_shouldReturnEmptyList_whenBuyerHasNoFavorites() throws Exception {
                mockMvc.perform(get("/api/favorites/{buyerId}", savedBuyerId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        void removeFromFavorites_afterAdding_shouldResultInEmptyList() throws Exception {
                Long productId = createProductViaApi("Desk Lamp");

                mockMvc.perform(post("/api/favorites/{buyerId}/{productId}",
                                savedBuyerId, productId))
                                .andExpect(status().isCreated());

                mockMvc.perform(delete("/api/favorites/{buyerId}/{productId}",
                                savedBuyerId, productId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message")
                                                .value("Product removed from favorites successfully"));

                mockMvc.perform(get("/api/favorites/{buyerId}", savedBuyerId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        void addToFavorites_twice_shouldReturn400OnSecondAttempt() throws Exception {
                Long productId = createProductViaApi("Ergonomic Chair");

                mockMvc.perform(post("/api/favorites/{buyerId}/{productId}",
                                savedBuyerId, productId))
                                .andExpect(status().isCreated());

                mockMvc.perform(post("/api/favorites/{buyerId}/{productId}",
                                savedBuyerId, productId))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void favorite_shouldLinkBuyerAndProductAndCategory_endToEnd() throws Exception {
                Long productId = createProductViaApi("Portable SSD");

                mockMvc.perform(post("/api/favorites/{buyerId}/{productId}",
                                savedBuyerId, productId))
                                .andExpect(status().isCreated());

                MvcResult favResult = mockMvc.perform(get("/api/favorites/{buyerId}", savedBuyerId))
                                .andExpect(status().isOk())
                                .andReturn();

                long favProductId = objectMapper.readTree(
                                favResult.getResponse().getContentAsString())
                                .path("data").get(0).path("productId").asLong();

                mockMvc.perform(get("/api/products/{id}", favProductId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.categoryId").value(savedCategoryId));
        }
}
