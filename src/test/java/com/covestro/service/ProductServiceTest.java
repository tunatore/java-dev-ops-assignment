package com.covestro.service;

import com.covestro.dto.ProductRequestDTO;
import com.covestro.repository.CategoryRepository;
import com.covestro.repository.CurrencyRepository;
import com.covestro.repository.ProductRepository;
import com.covestro.repository.entity.Category;
import com.covestro.repository.entity.Currency;
import com.covestro.repository.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private CurrencyRepository currencyRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @InjectMocks
    private ProductService productService;
    
    private ProductRequestDTO productRequestDTO;
    private Product product;
    private Currency currency;
    private Category category;
    
    @BeforeEach
    void setUp() {
        productRequestDTO = new ProductRequestDTO();
        productRequestDTO.setMaterialId("12345");
        productRequestDTO.setName("Test Product");
        productRequestDTO.setPrice(new BigDecimal("100.00"));
        productRequestDTO.setCurrencyCode("USD");
        productRequestDTO.setCategoryName("TestCategory");
        
        currency = new Currency();
        currency.setCode("USD");
        
        category = new Category();
        category.setName("TestCategory");
        
        product = new Product();
        product.setId(1L);
        product.setMaterialId("12345");
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        product.setCurrency(currency);
        product.setCategory(category);
        product.setLastUpdate(LocalDateTime.now());
    }
    
    @Test
    void testGetAllProductsShouldReturnProducts() {
        when(productRepository.findAll()).thenReturn(List.of(product));
        
        Flux<Product> result = productService.getAllProducts();
        
        StepVerifier.create(result)
                .expectNext(product)
                .verifyComplete();
        
        verify(productRepository, times(1)).findAll();
    }
    
    @Test
    void testGetProductByIdShouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        
        Mono<Product> result = productService.getProductById(1L);
        
        StepVerifier.create(result)
                .expectNext(product)
                .verifyComplete();
        
        verify(productRepository, times(1)).findById(1L);
    }
    
    @Test
    void testGetProductByIdShouldReturnEmptyWhenProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        
        Mono<Product> result = productService.getProductById(1L);
        
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(productRepository, times(1)).findById(1L);
    }
    
    @Test
    void testUpdateProductShouldReturnUpdatedProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(currency));
        when(categoryRepository.findByName("TestCategory")).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        
        Mono<Product> result = productService.updateProduct(1L, productRequestDTO);
        
        StepVerifier.create(result)
                .expectNext(product)
                .verifyComplete();
        
        verify(productRepository, times(1)).findById(1L);
        verify(currencyRepository, times(1)).findByCode("USD");
        verify(categoryRepository, times(1)).findByName("TestCategory");
        verify(productRepository, times(1)).save(any(Product.class));
    }
    
    @Test
    void testUpdateProductShouldReturnEmptyWhenProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        
        Mono<Product> result = productService.updateProduct(1L, productRequestDTO);
        
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(productRepository, times(1)).findById(1L);
    }
    
    @Test
    void testCreateProductShouldReturnCreatedProduct() {
        when(categoryRepository.findByName("TestCategory")).thenReturn(Optional.of(category));
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(currency));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        
        Mono<Product> result = productService.createProduct(productRequestDTO);
        
        StepVerifier.create(result)
                .expectNext(product)
                .verifyComplete();
        
        verify(categoryRepository, times(1)).findByName("TestCategory");
        verify(currencyRepository, times(1)).findByCode("USD");
        verify(productRepository, times(1)).save(any(Product.class));
    }
    
    @Test
    void testCreateProductShouldThrowExceptionWhenCategoryNotFound() {
        when(categoryRepository.findByName("TestCategory")).thenReturn(Optional.empty());
        
        Mono<Product> result = productService.createProduct(productRequestDTO);
        
        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();
        
        verify(categoryRepository, times(1)).findByName("TestCategory");
    }
}