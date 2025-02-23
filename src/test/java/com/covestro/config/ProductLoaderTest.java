package com.covestro.config;

import com.covestro.model.JsonProduct;
import com.covestro.model.JsonProductList;
import com.covestro.repository.CategoryRepository;
import com.covestro.repository.CurrencyRepository;
import com.covestro.repository.ProductRepository;
import com.covestro.repository.entity.Category;
import com.covestro.repository.entity.Currency;
import com.covestro.repository.entity.Product;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductLoaderTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private CurrencyRepository currencyRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @InjectMocks
    private ProductLoader productLoader;
    
    private JsonProductList jsonProductList;
    private Currency mockCurrency;
    private Category mockCategory;
    
    @BeforeEach
    void setUp() {
        jsonProductList = new JsonProductList();
        List<JsonProduct> products = new ArrayList<>();
        
        JsonProduct product = new JsonProduct();
        product.setMaterialId("123");
        product.setName("Test Product");
        product.setPrice(BigDecimal.TEN);
        
        JsonProduct.Currency currency = new JsonProduct.Currency();
        currency.setCode("USD");
        product.setCurrency(currency);
        
        JsonProduct.Category category = new JsonProduct.Category();
        category.setName("Test Category");
        product.setCategory(category);
        
        products.add(product);
        jsonProductList.setProducts(products);
        
        mockCurrency = new Currency();
        mockCurrency.setCode("USD");
        
        mockCategory = new Category();
        mockCategory.setName("Test Category");
    }
    
    @Test
    void testLoadProductsFromJson() throws IOException {
        when(currencyRepository.findAll()).thenReturn(List.of(mockCurrency));
        when(categoryRepository.findAll()).thenReturn(List.of(mockCategory));
        when(productRepository.save(any(Product.class))).thenReturn(new Product());
        when(currencyRepository.saveAll(any())).thenReturn(new ArrayList<>());
        when(categoryRepository.saveAll(any())).thenReturn(new ArrayList<>());
        
        doReturn(jsonProductList)
                .when(objectMapper)
                .readValue(any(InputStream.class), ArgumentMatchers.<TypeReference<JsonProductList>>any());
        
        productLoader.loadProductsFromJson();
        
        verify(productRepository, times(1)).save(any(Product.class));
        verify(objectMapper, times(1)).readValue(any(InputStream.class), ArgumentMatchers.<TypeReference<JsonProductList>>any());
    }
    
    @Test
    void testLoadProductsFromJsonThrowsException() throws IOException {
        doThrow(new IOException()).when(objectMapper)
                .readValue(any(InputStream.class), ArgumentMatchers.<TypeReference<JsonProductList>>any());
        
        assertThrows(IOException.class, () -> productLoader.loadProductsFromJson());
        
        verify(productRepository, never()).save(any(Product.class));
        verify(objectMapper, times(1)).readValue(any(InputStream.class), ArgumentMatchers.<TypeReference<JsonProductList>>any());
    }
    
    @Test
    void testLoadsProductsWhenDatabaseIsEmpty() throws IOException {
        when(productRepository.count()).thenReturn(0L);
        when(currencyRepository.findAll()).thenReturn(List.of(mockCurrency));
        when(categoryRepository.findAll()).thenReturn(List.of(mockCategory));
        when(productRepository.save(any(Product.class))).thenReturn(new Product());
        when(currencyRepository.saveAll(any())).thenReturn(new ArrayList<>());
        when(categoryRepository.saveAll(any())).thenReturn(new ArrayList<>());
        
        doReturn(jsonProductList)
                .when(objectMapper)
                .readValue(any(InputStream.class), ArgumentMatchers.<TypeReference<JsonProductList>>any());
        
        productLoader.run();
        
        verify(productRepository, times(1)).save(any(Product.class));
        verify(objectMapper, times(1)).readValue(any(InputStream.class), ArgumentMatchers.<TypeReference<JsonProductList>>any());
    }
    
    @Test
    void testDoesNotLoadProductsWhenDatabaseIsNotEmpty() throws IOException {
        when(productRepository.count()).thenReturn(1L);
        
        productLoader.run();
        
        verify(productRepository, never()).save(any(Product.class));
        verify(objectMapper, never()).readValue(any(InputStream.class), ArgumentMatchers.<TypeReference<JsonProductList>>any());
    }
    
    @Test
    void testExtractAndSaveCurrenciesAndCategoriesOk() throws IOException {
        when(currencyRepository.findAll()).thenReturn(List.of(mockCurrency));
        when(categoryRepository.findAll()).thenReturn(List.of(mockCategory));
        when(productRepository.save(any(Product.class))).thenReturn(new Product());
        when(currencyRepository.saveAll(any())).thenReturn(new ArrayList<>());
        when(categoryRepository.saveAll(any())).thenReturn(new ArrayList<>());
        
        doReturn(jsonProductList)
                .when(objectMapper)
                .readValue(any(InputStream.class), ArgumentMatchers.<TypeReference<JsonProductList>>any());
        
        productLoader.loadProductsFromJson();
        
        verify(currencyRepository, times(1)).saveAll(any());
        verify(categoryRepository, times(1)).saveAll(any());
    }
    
    @Test
    void testExtractAndSaveCurrenciesAndCategoriesThrowsException() throws IOException {
        when(objectMapper.readValue(any(InputStream.class), ArgumentMatchers.<TypeReference<JsonProductList>>any()))
                .thenThrow(new IOException("JSON parsing failed"));
        
        IOException exception = assertThrows(IOException.class, () -> productLoader.loadProductsFromJson());
        assertEquals("JSON parsing failed", exception.getMessage());
        
        verify(currencyRepository, never()).saveAll(any());
        verify(categoryRepository, never()).saveAll(any());
        verify(productRepository, never()).save(any());
    }
}