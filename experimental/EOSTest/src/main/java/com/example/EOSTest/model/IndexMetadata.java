package com.example.EOSTest.model;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class IndexMetadata implements Serializable {
    private String indexSymbol;
    private String indexName;
    private String sector;

    // Getters, setters, constructors
}
