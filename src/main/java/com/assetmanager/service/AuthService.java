package com.assetmanager.service;

import com.assetmanager.domain.User;
import com.assetmanager.security.jwt.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       UserService userService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    public String login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
        return tokenProvider.generateToken(authentication.getName(),
                authentication.getAuthorities().iterator().next().getAuthority());
    }

    public String register(String email, String password, String name) {
        User user = userService.register(email, password, name);
        return tokenProvider.generateToken(user.getEmail(), user.getRole().name());
    }
}
