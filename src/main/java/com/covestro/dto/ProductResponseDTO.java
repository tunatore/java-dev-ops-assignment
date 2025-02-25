package com.covestro.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO representing a product response.
 * This class is used to transfer product information from the server to the client.
 */
@Getter
@Setter
public class ProductResponseDTO {
    private Long id;
    private String materialId;
    private String name;
    private BigDecimal price;
    private String currencyCode;
    private String categoryName;
    private LocalDateTime lastUpdate;
}
