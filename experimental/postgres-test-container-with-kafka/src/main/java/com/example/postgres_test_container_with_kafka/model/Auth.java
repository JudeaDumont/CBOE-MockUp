package com.example.postgres_test_container_with_kafka.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "auth_user") // Renamed to avoid reserved keyword conflicts
@Getter
@Setter
public class Auth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String username;
    private String password;
    private String role; // Simplified role management
}
