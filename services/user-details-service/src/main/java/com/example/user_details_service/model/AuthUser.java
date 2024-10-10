package com.example.user_details_service.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "auth_user") // Renamed to avoid reserved keyword conflicts
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String username;
    private String password;
    private String role; // Simplified role management
}
