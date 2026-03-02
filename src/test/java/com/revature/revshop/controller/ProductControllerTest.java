package com.revature.revshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.revshop.dto.ProductDTO;
import com.revature.revshop.exception.InvalidInputException;
import com.revature.revshop.exception.ResourceNotFoundException;
import com.revature.revshop.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.revature.revshop.exception.GlobalExceptionHandler;
import com.revature.revshop.security.JwtAuthenticationFilter;

@WebMvcTest(
        controllers = {ProductController.class, GlobalExceptionHandler.class},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private ProductService productService;



    private ProductDTO sampleDTO;

    @BeforeEach
    void setUp() {
        sampleDTO = new ProductDTO();
        sampleDTO.setProductId(1L);
        sampleDTO.setName("Wireless Headphones");
        sampleDTO.setDescription("Noise-cancelling headphones");
        sampleDTO.setMrp(new BigDecimal("5000"));
        sampleDTO.setSellingPrice(new BigDecimal("4000"));
        sampleDTO.setStockQuantity(200);
        sampleDTO.setThresholdQuantity(20);
        sampleDTO.setIsActive(true);
        sampleDTO.setCategoryId(1L);
        sampleDTO.setSellerId(10L);
    }



    @Test
    void createProduct_shouldReturn201_withApiResponseBody() throws Exception {
        when(productService.createProduct(any(ProductDTO.class))).thenReturn(sampleDTO);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Product created successfully"))
                .andExpect(jsonPath("$.data.name").value("Wireless Headphones"))
                .andExpect(jsonPath("$.data.sellingPrice").value(4000))
                .andExpect(jsonPath("$.data.categoryId").value(1));
    }

    @Test
    void createProduct_shouldReturn400_whenRequiredFieldsMissing() throws Exception {

        ProductDTO invalid = new ProductDTO();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getAllProducts_shouldReturn200_withPageOfProducts() throws Exception {
        ProductDTO product2 = new ProductDTO();
        product2.setProductId(2L);
        product2.setName("Smart Watch");
        product2.setMrp(new BigDecimal("15000"));
        product2.setSellingPrice(new BigDecimal("12000"));
        product2.setStockQuantity(50);
        product2.setThresholdQuantity(5);
        product2.setIsActive(true);
        product2.setCategoryId(1L);
        product2.setSellerId(10L);

        Page<ProductDTO> productPage = new PageImpl<>(Arrays.asList(sampleDTO, product2));
        when(productService.getAllProducts(any(Pageable.class))).thenReturn(productPage);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Products fetched successfully"))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].name").value("Wireless Headphones"))
                .andExpect(jsonPath("$.data.content[1].name").value("Smart Watch"));
    }

    @Test
    void getAllProducts_shouldReturn200_withEmptyPage_whenNoProducts() throws Exception {
        Page<ProductDTO> emptyPage = new PageImpl<>(List.of());
        when(productService.getAllProducts(any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }


    @Test
    void getProductById_shouldReturn200_withProduct_whenFound() throws Exception {
        when(productService.getProductById(1L)).thenReturn(sampleDTO);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Product fetched successfully"))
                .andExpect(jsonPath("$.data.productId").value(1))
                .andExpect(jsonPath("$.data.name").value("Wireless Headphones"));
    }

    @Test
    void getProductById_shouldReturn404_whenProductNotFound() throws Exception {
        when(productService.getProductById(999L))
                .thenThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());
    }



    @Test
    void updateProduct_shouldReturn200_withUpdatedProduct() throws Exception {
        ProductDTO updated = new ProductDTO();
        updated.setProductId(1L);
        updated.setName("Premium Headphones");
        updated.setDescription("Updated description");
        updated.setMrp(new BigDecimal("6000"));
        updated.setSellingPrice(new BigDecimal("5000"));
        updated.setStockQuantity(150);
        updated.setThresholdQuantity(15);
        updated.setIsActive(true);
        updated.setCategoryId(1L);
        updated.setSellerId(10L);

        when(productService.updateProduct(eq(1L), any(ProductDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Product updated successfully"))
                .andExpect(jsonPath("$.data.name").value("Premium Headphones"))
                .andExpect(jsonPath("$.data.sellingPrice").value(5000));
    }

    @Test
    void updateProduct_shouldReturn404_whenProductNotFound() throws Exception {
        when(productService.updateProduct(eq(999L), any(ProductDTO.class)))
                .thenThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(put("/api/products/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDTO)))
                .andExpect(status().isNotFound());
    }



    @Test
    void deleteProduct_shouldReturn200_withNullData_whenDeleted() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Product deleted successfully"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void deleteProduct_shouldReturn404_whenProductNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Product not found"))
                .when(productService).deleteProduct(999L);

        mockMvc.perform(delete("/api/products/999"))
                .andExpect(status().isNotFound());
    }



    @Test
    void searchProducts_shouldReturn200_withMatchingProductPage() throws Exception {
        Page<ProductDTO> productPage = new PageImpl<>(List.of(sampleDTO));
        when(productService.searchProducts(eq("head"), any(Pageable.class))).thenReturn(productPage);

        mockMvc.perform(get("/api/products/search").param("keyword", "head"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Products fetched successfully"))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name").value("Wireless Headphones"));
    }

    @Test
    void searchProducts_shouldReturn200_withEmptyPage_whenNoMatch() throws Exception {
        Page<ProductDTO> emptyPage = new PageImpl<>(List.of());
        when(productService.searchProducts(eq("xyz"), any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/products/search").param("keyword", "xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    void searchProducts_shouldReturn400_whenKeywordIsBlank() throws Exception {
        when(productService.searchProducts(eq("   "), any(Pageable.class)))
                .thenThrow(new InvalidInputException("Search keyword is required"));

        mockMvc.perform(get("/api/products/search").param("keyword", "   "))
                .andExpect(status().isBadRequest());
    }



    @Test
    void filterProducts_shouldReturn200_withFilteredProductPage() throws Exception {
        Page<ProductDTO> productPage = new PageImpl<>(List.of(sampleDTO));
        when(productService.filterProducts(eq(3000.0), eq(5000.0), eq(1L), any(Pageable.class)))
                .thenReturn(productPage);

        mockMvc.perform(get("/api/products/filter")
                        .param("minPrice", "3000")
                        .param("maxPrice", "5000")
                        .param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Filtered products fetched successfully"))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name").value("Wireless Headphones"));
    }


    @Test
    void apiResponse_shouldContainStatusSuccessField() throws Exception {
        when(productService.getProductById(1L)).thenReturn(sampleDTO);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }
}
