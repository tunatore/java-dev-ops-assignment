package com.covestro.config.IT;

import com.covestro.config.ProductLoader;
import com.covestro.repository.ProductRepository;
import com.covestro.repository.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class ProductLoaderIntegrationTest {
    
    @Autowired
    private ProductLoader productLoader;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Container
    private static final MySQLContainer<?> MY_SQL_CONTAINER = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @DynamicPropertySource
    private static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () ->
                "jdbc:mysql://" + MY_SQL_CONTAINER.getHost() + ":" +
                        MY_SQL_CONTAINER.getMappedPort(3306) + "/testdb"
        );
        registry.add("spring.datasource.username", MY_SQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MY_SQL_CONTAINER::getPassword);
    }
    
    @Test
    void testProductLoaderIntegration() {
        productRepository.deleteAll();
        assertEquals(0, productRepository.count(), "Database should be empty before test");
        
        productLoader.run();
        
        List<Product> loadedProducts = productRepository.findAll();
        assertFalse(loadedProducts.isEmpty(), "No products were loaded");
        
        Product firstProduct = loadedProducts.get(0);
        assertEquals("379457HY", firstProduct.getMaterialId(),
                "First product material ID mismatch");
    }
    
    @Test
    void testProductLoaderIntegrationCantRunTwice() {
        productRepository.deleteAll();
        productLoader.run();
        
        List<Product> loadedProducts = productRepository.findAll();
        assertFalse(loadedProducts.isEmpty(), "No products were loaded");
        
        long initialCount = productRepository.count();
        productLoader.run();
        assertEquals(initialCount, productRepository.count(),
                "Loader should not duplicate entries on re-run");
    }
}