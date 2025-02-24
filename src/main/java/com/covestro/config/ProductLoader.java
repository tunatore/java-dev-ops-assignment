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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@code ProductLoader} is a Spring service that loads product data from a JSON file into the database.
 * It implements {@link CommandLineRunner} to execute the data loading process on application startup.
 */
@Service
@Configuration
@Slf4j
public class ProductLoader implements CommandLineRunner {
    private static final String PRODUCTS_JSON_PATH = "products.json";
    private final ProductRepository productRepository;
    private final CurrencyRepository currencyRepository;
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructs a {@code ProductLoader} with the necessary repositories and object mapper.
     *
     * @param productRepository    The repository for product entities.
     * @param currencyRepository   The repository for currency entities.
     * @param categoryRepository   The repository for category entities.
     * @param objectMapper         The object mapper for JSON processing.
     */
    @Autowired
    public ProductLoader(ProductRepository productRepository,
                         CurrencyRepository currencyRepository,
                         CategoryRepository categoryRepository,
                         ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.currencyRepository = currencyRepository;
        this.categoryRepository = categoryRepository;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Loads product data from the JSON file into the database.
     *
     * @throws IOException If an I/O error occurs while reading the JSON file.
     */
    public void loadProductsFromJson() throws IOException {
        ClassPathResource resource = new ClassPathResource(PRODUCTS_JSON_PATH);
        try (InputStream inputStream = resource.getInputStream()) {
            TypeReference<JsonProductList> typeReference = new TypeReference<>() {};
            JsonProductList jsonProductList = objectMapper.readValue(inputStream, typeReference);
            
            Map<String, Currency> currencyMap = new HashMap<>();
            Map<String, Category> categoryMap = new HashMap<>();
            
            extractAndSaveCurrenciesAndCategories(jsonProductList.getProducts(), currencyMap, categoryMap);
            List<Product> savedProducts = saveProducts(jsonProductList, currencyMap, categoryMap);
            logCategoryCounts(savedProducts);
        }
    }
    
    /**
     * Saves the products from the JSON list into the database.
     *
     * @param jsonProductList The list of products from the JSON file.
     * @param currencyMap     A map of currency codes to currency entities.
     * @param categoryMap     A map of category names to category entities.
     * @return A list of saved product entities.
     */
    private List<Product> saveProducts(JsonProductList jsonProductList,
                                       Map<String, Currency> currencyMap,
                                       Map<String, Category> categoryMap) {
        int count = 0;
        List<Product> savedProducts = new ArrayList<>();
        for (JsonProduct productJson : jsonProductList.getProducts()) {
            Currency currency = currencyMap.get(productJson.getCurrency().getCode());
            Category category = categoryMap.get(productJson.getCategory().getName());
            
            Product product = new Product();
            product.setMaterialId(productJson.getMaterialId());
            product.setName(productJson.getName());
            product.setPrice(productJson.getPrice());
            product.setCurrency(currency);
            product.setCategory(category);
            
            try {
                productRepository.save(product);
                savedProducts.add(product);
                count++;
            } catch (Exception e) {
                log.error("Error saving product: {}", productJson.getName(), e);
            }
        }
        
        log.info("Json products have been saved! count: {}", count);
        return savedProducts;
    }
    
    /**
     * Logs the counts of materials per category and lists all saved products.
     *
     * @param savedProducts The list of saved product entities.
     */
    private void logCategoryCounts(List<Product> savedProducts) {
        Map<String, Long> categoryCounts = savedProducts.stream()
                .collect(Collectors.groupingBy(product -> product.getCategory().getName(),
                        Collectors.counting()));
        
        log.info("Material counts per category:");
        categoryCounts.forEach((category, count) -> log.info("{}: {}", category, count));
        
        log.info("Successfully saved all products:");
        savedProducts.forEach(product -> log.info("{}", product));
    }
    
    /**
     * Extracts and saves currencies and categories from the product list.
     *
     * @param products      The list of products from the JSON file.
     * @param currencyMap   A map to store currency codes and entities.
     * @param categoryMap   A map to store category names and entities.
     */
    private void extractAndSaveCurrenciesAndCategories(List<JsonProduct> products,
                                                       Map<String, Currency> currencyMap,
                                                       Map<String, Category> categoryMap) {
        try {
            Set<Currency> currencies = new HashSet<>();
            Set<Category> categories = new HashSet<>();
            
            for (JsonProduct product : products) {
                Currency currency = new Currency();
                currency.setCode(product.getCurrency().getCode());
                currencies.add(currency);
                
                Category category = new Category();
                category.setName(product.getCategory().getName());
                categories.add(category);
            }
            
            currencyRepository.saveAll(currencies);
            categoryRepository.saveAll(categories);
            
            currencyRepository.findAll().forEach(currency -> currencyMap.put(currency.getCode(), currency));
            categoryRepository.findAll().forEach(category -> categoryMap.put(category.getName(), category));
            
        } catch (Exception e) {
            log.error("Error while saving currencies / categories!", e);
        }
    }
    
    /**
     * Checks if the database is empty.
     *
     * @return {@code true} if the database is empty, {@code false} otherwise.
     */
    private boolean isDatabaseEmpty() {
        return productRepository.count() == 0;
    }
    
    /**
     * Runs the product loading process on application startup if the database is empty.
     *
     * @param args The command line arguments.
     */
    @Override
    public void run(String... args) {
        if (isDatabaseEmpty()) {
            try {
                loadProductsFromJson();
            } catch (IOException e) {
                log.error("Error while saving products in database!", e);
            }
        } else {
            log.warn("Database is not empty, product were not saved!");
        }
    }
}