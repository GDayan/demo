package com.krainet.auth.controller;

import com.krainet.auth.dto.UserDTO;
import com.krainet.auth.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        logger.info("Fetching user with id: {}", id);
        return ResponseEntity.ok(userService.getUser(id));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Fetching current user: {}", username);
        UserDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        logger.info("Fetching all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        logger.info("Updating user with id: {}", id);
        return ResponseEntity.ok(userService.updateUser(id, userDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.info("Deleting user with id: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}