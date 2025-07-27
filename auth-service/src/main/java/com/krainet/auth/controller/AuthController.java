package com.krainet.auth.controller;

import com.krainet.auth.dto.LoginRequest;
import com.krainet.auth.dto.UserDTO;
import com.krainet.auth.service.UserService;
import com.krainet.auth.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtil.generateToken(userDetails);
        logger.info("User logged in: {}", loginRequest.getUsername());
        return ResponseEntity.ok(jwt);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody UserDTO userDTO) {
        UserDTO createdUser = userService.createUser(userDTO);
        return ResponseEntity.ok(createdUser);
    }
}
