package com.covestro.controller.IT;

import com.covestro.dto.ProductRequestDTO;
import com.covestro.dto.ProductResponseDTO;
import com.covestro.repository.CategoryRepository;
import com.covestro.repository.CurrencyRepository;
import com.covestro.repository.ProductRepository;
import com.covestro.repository.entity.Category;
import com.covestro.repository.entity.Currency;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public class ProductControllerIntegrationTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CurrencyRepository currencyRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Container
    private static final MySQLContainer<?> MY_SQL_CONTAINER = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("testtest")
            .withReuse(true);
    
    @DynamicPropertySource
    private static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MY_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MY_SQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MY_SQL_CONTAINER::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }
    
    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        currencyRepository.deleteAll();
        categoryRepository.deleteAll();
        
        Currency currency = new Currency();
        currency.setCode("USD");
        currencyRepository.save(currency);
        
        Currency currency2 = new Currency();
        currency2.setCode("EUR");
        currencyRepository.save(currency2);
        
        Category category = new Category();
        category.setName("TestCategory");
        categoryRepository.save(category);
        
        Category updatedCategory = new Category();
        updatedCategory.setName("UpdatedCategory");
        categoryRepository.save(updatedCategory);
    }
    
    @Test
    public void testGetAllProducts() throws Exception {
        webTestClient.get().uri("/api/v1/products")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }
    
    @Test
    public void testGetProductById() throws Exception {
        ProductRequestDTO productRequestDTO = new ProductRequestDTO();
        productRequestDTO.setMaterialId("12345");
        productRequestDTO.setName("Test Product");
        productRequestDTO.setPrice(new BigDecimal("100.00"));
        productRequestDTO.setCurrencyCode("USD");
        productRequestDTO.setCategoryName("TestCategory");
        
        ProductResponseDTO createdProduct = webTestClient.post().uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(productRequestDTO))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductResponseDTO.class)
                .returnResult()
                .getResponseBody();
        
        Long productId = createdProduct.getId();
        
        webTestClient.get().uri("/api/v1/products/" + productId) // Use the retrieved ID
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }
    
    @Test
    public void testCreateProduct() throws Exception {
        ProductRequestDTO productRequestDTO = new ProductRequestDTO();
        productRequestDTO.setMaterialId("12345");
        productRequestDTO.setName("Test Product");
        productRequestDTO.setPrice(new BigDecimal("100.00"));
        productRequestDTO.setCurrencyCode("USD");
        productRequestDTO.setCategoryName("TestCategory");
        
        webTestClient.post().uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(productRequestDTO))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }
    
    @Test
    public void testUpdateProduct() throws Exception {
        ProductRequestDTO productRequestDTO = new ProductRequestDTO();
        productRequestDTO.setMaterialId("12345");
        productRequestDTO.setName("Test Product");
        productRequestDTO.setPrice(new BigDecimal("100.00"));
        productRequestDTO.setCurrencyCode("USD");
        productRequestDTO.setCategoryName("TestCategory");
        
        ProductResponseDTO createdProduct = webTestClient.post().uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(productRequestDTO))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductResponseDTO.class)
                .returnResult()
                .getResponseBody();
        
        Long productId = createdProduct.getId();
        
        ProductRequestDTO updatedProductRequestDTO = new ProductRequestDTO();
        updatedProductRequestDTO.setMaterialId("54321");
        updatedProductRequestDTO.setName("Updated Product");
        updatedProductRequestDTO.setPrice(new BigDecimal("200.00"));
        updatedProductRequestDTO.setCurrencyCode("EUR");
        updatedProductRequestDTO.setCategoryName("UpdatedCategory");
        
        webTestClient.put().uri("/api/v1/products/" + productId) // Use the retrieved ID
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(updatedProductRequestDTO))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }
}
