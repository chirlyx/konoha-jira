package com.konoha.jira.auth;

import com.konoha.jira.auth.dto.AuthResponse;
import com.konoha.jira.auth.dto.LoginRequest;
import com.konoha.jira.auth.dto.SignupRequest;
import com.konoha.jira.entity.Ninja;
import com.konoha.jira.enums.Rank;
import com.konoha.jira.enums.Role;
import com.konoha.jira.repository.NinjaRepository;
import com.konoha.jira.security.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final NinjaRepository ninjaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(NinjaRepository ninjaRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.ninjaRepository = ninjaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (ninjaRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        Ninja ninja = Ninja.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .village(request.getVillage())
                .role(Role.ROLE_GENIN)
                .rank(Rank.GENIN)
                .experience(0)
                .active(true)
                .build();
        ninjaRepository.save(ninja);

        var springUser = org.springframework.security.core.userdetails.User
                .withUsername(ninja.getUsername())
                .password(ninja.getPassword())
                .authorities(ninja.getRole().name())
                .build();
        String token = jwtService.generateToken(springUser);
        return AuthResponse.builder()
                .token(token)
                .username(ninja.getUsername())
                .role(ninja.getRole())
                .rank(ninja.getRank())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        Ninja ninja = ninjaRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        String token = jwtService.generateToken((User) authentication.getPrincipal());
        return AuthResponse.builder()
                .token(token)
                .username(ninja.getUsername())
                .role(ninja.getRole())
                .rank(ninja.getRank())
                .build();
    }
}

