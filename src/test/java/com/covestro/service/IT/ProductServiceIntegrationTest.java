package com.covestro.service.IT;

import com.covestro.dto.ProductRequestDTO;
import com.covestro.repository.CategoryRepository;
import com.covestro.repository.CurrencyRepository;
import com.covestro.repository.ProductRepository;
import com.covestro.repository.entity.Category;
import com.covestro.repository.entity.Currency;
import com.covestro.repository.entity.Product;
import com.covestro.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public class ProductServiceIntegrationTest {
    
    @Autowired
    private ProductService productService;
    
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
    static void setProperties(DynamicPropertyRegistry registry) {
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
        
        Category category = new Category();
        category.setName("TestCategory");
        categoryRepository.save(category);
        
        Category updatedCategory = new Category();
        updatedCategory.setName("UpdatedCategory");
        categoryRepository.save(updatedCategory);
    }
    
    @Test
    void testGetAllProductsShouldReturnFluxOfProducts() {
        ProductRequestDTO requestDTO = new ProductRequestDTO();
        requestDTO.setMaterialId("12345");
        requestDTO.setName("Test Product");
        requestDTO.setPrice(new BigDecimal("100.00"));
        requestDTO.setCurrencyCode("USD");
        requestDTO.setCategoryName("TestCategory");
        
        productService.createProduct(requestDTO).block();
        
        Flux<Product> products = productService.getAllProducts();
        
        StepVerifier.create(products)
                .expectNextMatches(product -> {
                    assertEquals("12345", product.getMaterialId());
                    assertEquals("Test Product", product.getName());
                    assertEquals(new BigDecimal("100.00"), product.getPrice());
                    assertEquals("USD", product.getCurrency().getCode());
                    assertEquals("TestCategory", product.getCategory().getName());
                    assertNotNull(product.getLastUpdate());
                    return true;
                })
                .verifyComplete();
    }
    
    @Test
    void testGetProductByIdShouldReturnMonoOfProduct() {
        ProductRequestDTO requestDTO = new ProductRequestDTO();
        requestDTO.setMaterialId("12345");
        requestDTO.setName("Test Product");
        requestDTO.setPrice(new BigDecimal("100.00"));
        requestDTO.setCurrencyCode("USD");
        requestDTO.setCategoryName("TestCategory");
        
        Product createdProduct = productService.createProduct(requestDTO).block();
        
        Mono<Product> product = productService.getProductById(createdProduct.getId());
        
        StepVerifier.create(product)
                .expectNextMatches(p -> {
                    assertEquals("12345", p.getMaterialId());
                    assertEquals("Test Product", p.getName());
                    assertEquals(new BigDecimal("100.00"), p.getPrice());
                    assertEquals("USD", p.getCurrency().getCode());
                    assertEquals("TestCategory", p.getCategory().getName());
                    assertNotNull(p.getLastUpdate());
                    return true;
                })
                .verifyComplete();
    }
    
    @Test
    void testUpdateProductShouldReturnUpdatedProduct() {
        ProductRequestDTO requestDTO = new ProductRequestDTO();
        requestDTO.setMaterialId("12345");
        requestDTO.setName("Test Product");
        requestDTO.setPrice(new BigDecimal("100.00"));
        requestDTO.setCurrencyCode("USD");
        requestDTO.setCategoryName("TestCategory");
        
        Product createdProduct = productService.createProduct(requestDTO).block();
        
        ProductRequestDTO updateDTO = new ProductRequestDTO();
        updateDTO.setMaterialId("54321");
        updateDTO.setName("Updated Product");
        updateDTO.setPrice(new BigDecimal("200.00"));
        updateDTO.setCurrencyCode("EUR");
        updateDTO.setCategoryName("UpdatedCategory");
        
        Mono<Product> updatedProduct = productService.updateProduct(createdProduct.getId(), updateDTO);
        
        StepVerifier.create(updatedProduct)
                .expectNextMatches(p -> {
                    assertEquals("54321", p.getMaterialId());
                    assertEquals("Updated Product", p.getName());
                    assertEquals(new BigDecimal("200.00"), p.getPrice());
                    assertEquals("EUR", p.getCurrency().getCode());
                    assertEquals("UpdatedCategory", p.getCategory().getName());
                    assertNotNull(p.getLastUpdate());
                    return true;
                })
                .verifyComplete();
    }
    
    @Test
    void testCreateProductShouldReturnCreatedProduct() {
        ProductRequestDTO requestDTO = new ProductRequestDTO();
        requestDTO.setMaterialId("12345");
        requestDTO.setName("Test Product");
        requestDTO.setPrice(new BigDecimal("100.00"));
        requestDTO.setCurrencyCode("USD");
        requestDTO.setCategoryName("TestCategory");
        
        Mono<Product> createdProduct = productService.createProduct(requestDTO);
        
        StepVerifier.create(createdProduct)
                .expectNextMatches(p -> {
                    assertEquals("12345", p.getMaterialId());
                    assertEquals("Test Product", p.getName());
                    assertEquals(new BigDecimal("100.00"), p.getPrice());
                    assertEquals("USD", p.getCurrency().getCode());
                    assertEquals("TestCategory", p.getCategory().getName());
                    assertNotNull(p.getLastUpdate());
                    return true;
                })
                .verifyComplete();
    }
}