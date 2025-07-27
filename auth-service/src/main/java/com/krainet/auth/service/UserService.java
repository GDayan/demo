package com.krainet.auth.service;

import com.krainet.auth.dto.NotificationDTO;
import com.krainet.auth.dto.UserDTO;
import com.krainet.auth.entity.Role;
import com.krainet.auth.entity.User;
import com.krainet.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final String notificationServiceUrl;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RestTemplate restTemplate,
                       @Value("${notification.service.url}") String notificationServiceUrl) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
        this.notificationServiceUrl = notificationServiceUrl;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    public UserDTO createUser(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setRole(Role.USER); // Default role for registration

        User savedUser = userRepository.save(user);
        logger.info("User created: {}", savedUser.getUsername());

        notifyAdmins("Created", savedUser);
        return convertToDTO(savedUser);
    }

    public UserDTO getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        checkAccess(user);
        return convertToDTO(user);
    }

    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        checkAccess(user);
        return convertToDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        checkAdminAccess();
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        checkAccess(user);

        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        logger.info("User updated: {}", updatedUser.getUsername());

        notifyAdmins("Updated", updatedUser);
        return convertToDTO(updatedUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        checkAccess(user);

        userRepository.delete(user);
        logger.info("User deleted: {}", user.getUsername());

        notifyAdmins("Deleted", user);
    }

    private void checkAccess(User user) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !user.getUsername().equals(currentUsername)) {
            throw new SecurityException("Access denied");
        }
    }

    private void checkAdminAccess() {
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new SecurityException("Admin access required");
        }
    }

    private void notifyAdmins(String action, User user) {
        List<User> admins = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMIN)
                .collect(Collectors.toList());

        for (User admin : admins) {
            NotificationDTO notification = new NotificationDTO();
            notification.setTo(admin.getEmail());
            notification.setSubject(String.format("%s user %s", action, user.getUsername()));
            notification.setText(String.format("%s user with username - %s, password - %s, email - %s",
                    action, user.getUsername(), user.getPassword(), user.getEmail()));
            restTemplate.postForObject(notificationServiceUrl, notification, Void.class);
            logger.info("Notification sent to admin: {}", admin.getEmail());
        }
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole());
        return dto;
    }
}