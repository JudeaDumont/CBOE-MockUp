package com.example.user_details_service.service;

import com.example.user_details_service.model.User;
import com.example.user_details_service.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final KafkaProducerService kafkaProducerService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .roles(user.getRole()) // Simplified role handling
            .build();
    }

    public User saveUserDetails(User userDetails) {
        User save = userRepository.save(userDetails);
        kafkaProducerService.sendMessage("user-registration-topic", userDetails);
        return save;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void deleteUserDetails(Long id) {
        userRepository.deleteById(id);
    }
}
