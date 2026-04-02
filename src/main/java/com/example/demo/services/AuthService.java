package com.example.demo.services;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.dto.response.JwtResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repositories.UserRepository;
import com.example.demo.security.JwtUtil;
import lombok.RequiredArgsConstructor;
<<<<<<< HEAD
import org.springframework.security.authentication.BadCredentialsException;
=======
>>>>>>> c9a64acd04492b6ec54c00c0eac45d06b6618803
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public JwtResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
<<<<<<< HEAD
            throw new BadCredentialsException("Неверный email или пароль");
=======
            throw new RuntimeException("Неверный пароль");
>>>>>>> c9a64acd04492b6ec54c00c0eac45d06b6618803
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new JwtResponse(token, user.getEmail(), user.getRole().name(), user.getFullName());
    }

    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
<<<<<<< HEAD
            throw new IllegalArgumentException("Email уже зарегистрирован");
=======
            throw new RuntimeException("Email уже зарегистрирован");
>>>>>>> c9a64acd04492b6ec54c00c0eac45d06b6618803
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isActive(true)
                .build();

        userRepository.save(user);
        return "Пользователь успешно зарегистрирован";
    }
}
