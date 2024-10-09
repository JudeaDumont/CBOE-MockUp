package com.example.EOSTest.model;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EnrichedIndexTrade implements Serializable {
    private String indexSymbol;
    private double tradePrice;
    private int volume;
    private String indexName;
    private String sector;

    // Getters, setters, constructors
}