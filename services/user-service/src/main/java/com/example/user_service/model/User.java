package com.example.user_service.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users") // Use a different table name
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
}