package com.example.model.factoryconfig;

import com.example.model.factories.POFactory;
import com.example.model.factories.ClassBFactory;
import com.example.model.factories.ClassAFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductFactoryConfig {

    @Value("${product.factory.type}")
    private String factoryType;

    @Bean
    public POFactory productFactory() {
        return switch (factoryType.toUpperCase()) {
            case "A" -> new ClassAFactory();
            case "B" -> new ClassBFactory();
            default -> throw new IllegalArgumentException("Unknown factory type: " + factoryType);
        };
    }
}