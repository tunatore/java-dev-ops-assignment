package com.covestro.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO representing a product request.
 * This class is used to transfer product information from the client to the server for creation or update operations.
 * It includes validation constraints to ensure data integrity.
 */
@Getter
@Setter
public class ProductRequestDTO {
    
    @NotBlank(message = "Material ID is required")
    private String materialId;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    
    @NotBlank(message = "Currency code is required")
    private String currencyCode;
    
    @NotBlank(message = "Category name is required")
    private String categoryName;
}
