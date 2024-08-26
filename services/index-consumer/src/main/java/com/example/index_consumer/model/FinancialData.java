package com.example.index_consumer.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FinancialData {
    private String indexName;
    private double value;
    private long timestamp;
}