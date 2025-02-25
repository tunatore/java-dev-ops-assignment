package com.covestro.controller;

import com.covestro.dto.ProductRequestDTO;
import com.covestro.dto.ProductResponseDTO;
import com.covestro.repository.entity.Category;
import com.covestro.repository.entity.Currency;
import com.covestro.repository.entity.Product;
import com.covestro.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {
    
    @Mock
    private ProductService productService;
    
    @InjectMocks
    private ProductController productController;
    
    private Product product;
    private ProductRequestDTO productRequestDTO;
    private ProductResponseDTO productResponseDTO;
    
    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setMaterialId("123");
        product.setName("Test Product");
        product.setPrice(BigDecimal.TEN);
        product.setLastUpdate(LocalDateTime.now());
        
        Currency currency = new Currency();
        currency.setCode("USD");
        product.setCurrency(currency);
        
        Category category = new Category();
        category.setName("TestCategory");
        product.setCategory(category);
        
        productRequestDTO = new ProductRequestDTO();
        productRequestDTO.setMaterialId("123");
        productRequestDTO.setName("Test Product");
        productRequestDTO.setPrice(BigDecimal.TEN);
        
        productResponseDTO = new ProductResponseDTO();
        productResponseDTO.setId(1L);
        productResponseDTO.setMaterialId("123");
        productResponseDTO.setName("Test Product");
        productResponseDTO.setPrice(BigDecimal.TEN);
        productResponseDTO.setLastUpdate(product.getLastUpdate());
        productResponseDTO.setCurrencyCode(currency.getCode());
        productResponseDTO.setCategoryName(category.getName());
    }
    
    @Test
    void testGetAllProductsShouldReturnFluxOfProductResponseDTO() {
        when(productService.getAllProducts()).thenReturn(Flux.just(product));
        
        Flux<ProductResponseDTO> result = productController.getAllProducts();
        
        StepVerifier.create(result)
                .expectNextMatches(actualDto -> {
                    assertEquals(productResponseDTO.getId(), actualDto.getId());
                    assertEquals(productResponseDTO.getMaterialId(), actualDto.getMaterialId());
                    assertEquals(productResponseDTO.getName(), actualDto.getName());
                    assertEquals(productResponseDTO.getPrice(), actualDto.getPrice());
                    assertEquals(productResponseDTO.getCurrencyCode(), actualDto.getCurrencyCode());
                    assertEquals(productResponseDTO.getCategoryName(), actualDto.getCategoryName());
                    assertEquals(productResponseDTO.getLastUpdate(), actualDto.getLastUpdate());
                    return true;
                })
                .verifyComplete();
        
        verify(productService, times(1)).getAllProducts();
    }
    
    @Test
    void testGetProductByIdShouldReturnProductResponseDTO() {
        when(productService.getProductById(1L)).thenReturn(Mono.just(product));
        
        Mono<ResponseEntity<ProductResponseDTO>> result = productController.getProductById(1L);
        
        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
                    assertEquals(productResponseDTO.getId(), responseEntity.getBody().getId());
                    assertEquals(productResponseDTO.getMaterialId(), responseEntity.getBody().getMaterialId());
                    assertEquals(productResponseDTO.getName(), responseEntity.getBody().getName());
                    assertEquals(productResponseDTO.getPrice(), responseEntity.getBody().getPrice());
                    assertEquals(productResponseDTO.getCurrencyCode(), responseEntity.getBody().getCurrencyCode());
                    assertEquals(productResponseDTO.getCategoryName(), responseEntity.getBody().getCategoryName());
                    assertEquals(productResponseDTO.getLastUpdate(), responseEntity.getBody().getLastUpdate());
                    return true;
                })
                .verifyComplete();
        
        verify(productService, times(1)).getProductById(1L);
    }
    
    @Test
    void testGetProductByIdShouldReturnNotFound() {
        when(productService.getProductById(1L)).thenReturn(Mono.empty());
        
        Mono<ResponseEntity<ProductResponseDTO>> result = productController.getProductById(1L);
        
        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
                    return true;
                })
                .verifyComplete();
        
        verify(productService, times(1)).getProductById(1L);
    }
    
    @Test
    void testUpdateProductShouldReturnUpdatedProductResponseDTO() {
        when(productService.updateProduct(1L, productRequestDTO)).thenReturn(Mono.just(product));
        
        Mono<ResponseEntity<ProductResponseDTO>> result = productController.updateProduct(1L, productRequestDTO);
        
        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
                    assertEquals(productResponseDTO.getId(), responseEntity.getBody().getId());
                    assertEquals(productResponseDTO.getMaterialId(), responseEntity.getBody().getMaterialId());
                    assertEquals(productResponseDTO.getName(), responseEntity.getBody().getName());
                    assertEquals(productResponseDTO.getPrice(), responseEntity.getBody().getPrice());
                    assertEquals(productResponseDTO.getCurrencyCode(), responseEntity.getBody().getCurrencyCode());
                    assertEquals(productResponseDTO.getCategoryName(), responseEntity.getBody().getCategoryName());
                    assertEquals(productResponseDTO.getLastUpdate(), responseEntity.getBody().getLastUpdate());
                    return true;
                })
                .verifyComplete();
        
        verify(productService, times(1)).updateProduct(1L, productRequestDTO);
    }
    
    @Test
    void testUpdateProductShouldReturnNotFound() {
        when(productService.updateProduct(1L, productRequestDTO)).thenReturn(Mono.empty());
        
        Mono<ResponseEntity<ProductResponseDTO>> result = productController.updateProduct(1L, productRequestDTO);
        
        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
                    return true;
                })
                .verifyComplete();
        
        verify(productService, times(1)).updateProduct(1L, productRequestDTO);
    }
    
    @Test
    void testCreateProductShouldReturnCreatedProductResponseDTO() {
        when(productService.createProduct(productRequestDTO)).thenReturn(Mono.just(product));
        
        Mono<ResponseEntity<ProductResponseDTO>> result = productController.createProduct(productRequestDTO);
        
        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
                    assertEquals(productResponseDTO.getId(), responseEntity.getBody().getId());
                    assertEquals(productResponseDTO.getMaterialId(), responseEntity.getBody().getMaterialId());
                    assertEquals(productResponseDTO.getName(), responseEntity.getBody().getName());
                    assertEquals(productResponseDTO.getPrice(), responseEntity.getBody().getPrice());
                    assertEquals(productResponseDTO.getCurrencyCode(), responseEntity.getBody().getCurrencyCode());
                    assertEquals(productResponseDTO.getCategoryName(), responseEntity.getBody().getCategoryName());
                    assertEquals(productResponseDTO.getLastUpdate(), responseEntity.getBody().getLastUpdate());
                    return true;
                })
                .verifyComplete();
        
        verify(productService, times(1)).createProduct(productRequestDTO);
    }
    
    @Test
    void testCreateProductShouldReturnInternalServerError() {
        when(productService.createProduct(productRequestDTO)).thenReturn(Mono.empty());
        
        Mono<ResponseEntity<ProductResponseDTO>> result = productController.createProduct(productRequestDTO);
        
        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
                    return true;
                })
                .verifyComplete();
        
        verify(productService, times(1)).createProduct(productRequestDTO);
    }
}
