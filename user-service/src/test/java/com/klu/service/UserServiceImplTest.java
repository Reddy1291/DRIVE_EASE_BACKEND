package com.klu.service;

import com.klu.config.JwtUtil;
import com.klu.entity.User;
import com.klu.repo.UserRepository;
import com.klu.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private User sampleUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "passwordEncoder", passwordEncoder);

        sampleUser = new User();
        sampleUser.setUserId(1L);
        sampleUser.setName("Alice");
        sampleUser.setEmail("alice@example.com");
        sampleUser.setPassword("plainPass");
        sampleUser.setRole("CUSTOMER");
        sampleUser.setPhone("1234567890");
    }

    @Test
    void testRegisterUser() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered = userService.register(sampleUser);

        assertNotNull(registered);
        assertTrue(passwordEncoder.matches("plainPass", registered.getPassword()));
        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    void testLoginSuccess() {
        String hashed = passwordEncoder.encode("plainPass");
        sampleUser.setPassword(hashed);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleUser));
        when(jwtUtil.generateToken(1L, "alice@example.com", "CUSTOMER")).thenReturn("mockToken123");

        Map<String, String> credentials = Map.of("email", "alice@example.com", "password", "plainPass");
        Map<String, String> result = userService.login(credentials);

        assertNotNull(result);
        assertEquals("mockToken123", result.get("token"));
    }

    @Test
    void testLoginInvalidPasswordThrowsException() {
        String hashed = passwordEncoder.encode("correctPass");
        sampleUser.setPassword(hashed);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleUser));

        Map<String, String> credentials = Map.of("email", "alice@example.com", "password", "wrongPass");
        assertThrows(RuntimeException.class, () -> userService.login(credentials));
    }

    @Test
    void testLoginNonExistentUserThrowsException() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        Map<String, String> credentials = Map.of("email", "notfound@example.com", "password", "pass");
        assertThrows(RuntimeException.class, () -> userService.login(credentials));
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(sampleUser));

        List<User> users = userService.getAllUsers();
        assertEquals(1, users.size());
        assertEquals("Alice", users.get(0).getName());
    }

    @Test
    void testGetUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        User found = userService.getUserById(1L);
        assertNotNull(found);
        assertEquals(1L, found.getUserId());
    }

    @Test
    void testUpdateUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        User updateData = new User();
        updateData.setName("Alice Updated");
        updateData.setEmail("alice.updated@example.com");
        updateData.setPhone("9876543210");
        updateData.setRole("CUSTOMER");

        User updated = userService.updateUser(1L, updateData);
        assertNotNull(updated);
        assertEquals("Alice Updated", sampleUser.getName());
    }

    @Test
    void testDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }
}
