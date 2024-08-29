package com.example.EOSTest.model;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class IndexTrade implements Serializable {
    private String indexSymbol;
    private double tradePrice;
    private int volume;

    // Getters, setters, constructors
}