package com.covestro.controller;

import com.covestro.dto.ProductRequestDTO;
import com.covestro.dto.ProductResponseDTO;
import com.covestro.repository.entity.Product;
import com.covestro.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST controller for products.
 * Provides endpoints for retrieving, creating, and updating products.
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    
    private final ProductService productService;
    
    @Operation(summary = "Get a list of products")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of products retrieved successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponseDTO.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ProductResponseDTO> getAllProducts() {
        log.info("Received request to get all products");
        return productService.getAllProducts()
                .map(this::convertToDto);
    }
    
    @Operation(summary = "Get a product by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product retrieved successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponseDTO.class))}),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ProductResponseDTO>> getProductById(@PathVariable Long id) {
        log.info("Received request to get product by ID: {}", id);
        return productService.getProductById(id)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Update a product by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponseDTO.class))}),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PutMapping("/{id}")
    public Mono<ResponseEntity<ProductResponseDTO>> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequestDTO productRequestDTO) {
        log.info("Received request to update product with ID: {}", id);
        return productService.updateProduct(id, productRequestDTO)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Create a new product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponseDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping
    public Mono<ResponseEntity<ProductResponseDTO>> createProduct(@Valid @RequestBody ProductRequestDTO productRequestDTO) {
        log.info("Received request to create a new product");
        return productService.createProduct(productRequestDTO)
                .map(this::convertToDto)
                .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }
    
    private ProductResponseDTO convertToDto(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setMaterialId(product.getMaterialId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setCurrencyCode(product.getCurrency().getCode());
        dto.setCategoryName(product.getCategory().getName());
        dto.setLastUpdate(product.getLastUpdate());
        
        return dto;
    }
}
