package com.covestro.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.math.BigDecimal;

@Getter
@Setter
public class JsonProduct {
    private String materialId;
    private String name;
    private BigDecimal price;
    @JsonDeserialize(using = CurrencyDeserializer.class)
    private Currency currency;
    
    @JsonDeserialize(using = CategoryDeserializer.class)
    private Category category;
    
    @Getter
    @Setter
    public static class Currency {
        private String code;
    }
    
    @Getter
    @Setter
    public static class Category {
        private String name;
    }
    
    private static class CurrencyDeserializer extends JsonDeserializer<Currency> {
        @Override
        public Currency deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            Currency currency = new Currency();
            currency.setCode(p.getText());
            return currency;
        }
    }
    
    private static class CategoryDeserializer extends JsonDeserializer<Category> {
        @Override
        public Category deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            Category category = new Category();
            category.setName(p.getText());
            return category;
        }
    }
}
