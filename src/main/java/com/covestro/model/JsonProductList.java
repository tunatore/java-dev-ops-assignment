package com.covestro.model;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * Represents a list of products from a JSON source.
 * This class is used for deserializing JSON data containing an array of {@link JsonProduct} objects.
 */
@Getter
@Setter
public class JsonProductList {
    private List<JsonProduct> products;
}
