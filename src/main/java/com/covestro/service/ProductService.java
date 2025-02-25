package com.covestro.service;

import com.covestro.dto.ProductRequestDTO;
import com.covestro.repository.CategoryRepository;
import com.covestro.repository.CurrencyRepository;
import com.covestro.repository.ProductRepository;
import com.covestro.repository.entity.Category;
import com.covestro.repository.entity.Currency;
import com.covestro.repository.entity.Product;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Service class handling product-related business logic including CRUD operations,
 * currency/category management, and circuit breaker-protected data access.
 * <p>
 * This service integrates with Resilience4j Circuit Breaker to handle failures gracefully
 * and uses reactive programming patterns for non-blocking I/O operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    private final CurrencyRepository currencyRepository;
    private final CategoryRepository categoryRepository;
    
    /**
     * Retrieves all products from the database with circuit breaker protection.
     *
     * @return Flux of all products
     * @see #genericFluxFallback(Throwable)
     */
    @CircuitBreaker(name = "productService", fallbackMethod = "genericFluxFallback")
    public Flux<Product> getAllProducts() {
        log.info("Fetching all products");
        return asyncBlockingTask(productRepository::findAll)
                .flatMapMany(Flux::fromIterable);
    }
    
    /**
     * Finds a product by its ID with circuit breaker protection.
     *
     * @param id Product ID to search for
     * @return Mono containing the found product or empty if not found
     * @see #genericFallback(Throwable)
     */
    @CircuitBreaker(name = "productService", fallbackMethod = "genericFallback")
    public Mono<Product> getProductById(Long id) {
        log.info("Fetching product by ID: {}", id);
        return asyncBlockingTask(() ->
                productRepository.findById(id).orElse(null)
        );
    }
    
    /**
     * Updates an existing product with circuit breaker protection.
     *
     * @param id ID of product to update
     * @param request DTO containing update information
     * @return Mono containing updated product or empty if not found
     * @throws IllegalArgumentException if category doesn't exist
     * @see #genericFallback(Throwable)
     */
    @Transactional
    @CircuitBreaker(name = "productService", fallbackMethod = "genericFallback")
    public Mono<Product> updateProduct(Long id, ProductRequestDTO request) {
        log.info("Updating product with ID: {}", id);
        return asyncBlockingTask(() -> productRepository.findById(id).orElse(null))
                .flatMap(existingProduct -> {
                    if (existingProduct != null) {
                        return updateExistingProduct(existingProduct, request);
                    } else {
                        log.warn("Product with ID {} not found", id);
                        return Mono.empty();
                    }
                });
    }
    
    /**
     * Creates a new product with circuit breaker protection.
     *
     * @param request DTO containing product information
     * @return Mono containing created product
     * @throws IllegalArgumentException if category doesn't exist
     * @see #genericFallback(Throwable)
     */
    @Transactional
    @CircuitBreaker(name = "productService", fallbackMethod = "genericFallback")
    public Mono<Product> createProduct(ProductRequestDTO request) {
        log.info("Creating new product");
        return asyncBlockingTask(() -> {
            Optional<Category> optionalCategory = categoryRepository.findByName(request.getCategoryName());
            return optionalCategory.orElseThrow(() ->
                    new IllegalArgumentException("Category not found: " + request.getCategoryName()));
        }).flatMap(category ->
                asyncBlockingTask(() -> buildAndSaveProduct(request, category))
        );
    }
    
    /**
     * Updates existing product entity with new values from DTO.
     *
     * @param existingProduct Product entity to update
     * @param request DTO containing new values
     * @return Mono containing updated product
     */
    private Mono<Product> updateExistingProduct(Product existingProduct, ProductRequestDTO request) {
        return Mono.zip(
                asyncBlockingTask(() -> findOrCreateCurrency(request.getCurrencyCode())),
                asyncBlockingTask(() -> categoryRepository.findByName(request.getCategoryName())
                        .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.getCategoryName())))
        ).flatMap(tuple -> {
            Currency currency = tuple.getT1();
            Category category = tuple.getT2();
            updateProductFields(existingProduct, request, currency, category);
            existingProduct.setLastUpdate(LocalDateTime.now());
            return saveProduct(existingProduct);
        });
    }
    
    /**
     * Finds or creates a currency entity by code.
     *
     * @param currencyCode Currency code to find/create
     * @return Existing or newly created currency entity
     */
    private Currency findOrCreateCurrency(String currencyCode) {
        return currencyRepository.findByCode(currencyCode)
                .orElseGet(() -> {
                    Currency newCurrency = new Currency();
                    newCurrency.setCode(currencyCode);
                    return currencyRepository.save(newCurrency);
                });
    }
    
    /**
     * Saves a product entity using async blocking task.
     *
     * @param product Product entity to save
     * @return Mono containing saved product
     */
    private Mono<Product> saveProduct(Product product) {
        return asyncBlockingTask(() -> productRepository.save(product));
    }
    
    /**
     * Builds and persists a new product entity from DTO.
     *
     * @param request Product creation DTO
     * @param category Associated category entity
     * @return Created product entity
     */
    private Product buildAndSaveProduct(ProductRequestDTO request, Category category) {
        Product product = new Product();
        updateProductFields(product, request, findOrCreateCurrency(request.getCurrencyCode()), category);
        product.setLastUpdate(LocalDateTime.now());
        return productRepository.save(product);
    }
    
    private void updateProductFields(Product product, ProductRequestDTO request,
                                     Currency currency, Category category) {
        product.setMaterialId(request.getMaterialId());
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setCurrency(currency);
        product.setCategory(category);
    }
    
    /**
     * Wraps blocking operations in reactive context.
     *
     * @param <T> Return type
     * @param task Supplier of blocking operation
     * @return Mono executing on bounded elastic scheduler
     */
    private <T> Mono<T> asyncBlockingTask(Supplier<T> task) {
        return Mono.fromSupplier(task)
                .subscribeOn(Schedulers.boundedElastic());
    }
    
    /**
     * Generic circuit breaker fallback for Mono return types.
     *
     * @param <T> Fallback type
     * @param throwable Exception that triggered fallback
     * @return Empty Mono
     */
    @SuppressWarnings("unused")
    private <T> Mono<T> genericFallback(Throwable throwable) {
        log.error("Generic fallback method called. Returning empty Mono.", throwable);
        return Mono.empty();
    }
    
    /**
     * Generic circuit breaker fallback for Flux return types.
     *
     * @param <T> Fallback type
     * @param throwable Exception that triggered fallback
     * @return Empty Flux
     */
    @SuppressWarnings("unused")
    private <T> Flux<T> genericFluxFallback(Throwable throwable){
        log.error("Generic fallback method called. Returning empty Flux.", throwable);
        return Flux.empty();
    }
}