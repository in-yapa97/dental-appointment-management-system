package com.dental.management.service;

import com.dental.management.dto.AuthResponse;
import com.dental.management.dto.LoginRequest;
import com.dental.management.dto.RegisterRequest;
import com.dental.management.dto.UserResponse;
import com.dental.management.entity.User;
import com.dental.management.entity.enums.UserRole;
import com.dental.management.exception.DuplicateUsernameException;
import com.dental.management.exception.InactiveUserException;
import com.dental.management.exception.InvalidCredentialsException;
import com.dental.management.repository.UserRepository;
import com.dental.management.security.JwtUtils;
import com.dental.management.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("johndoe", "hashed_password", "John Doe", UserRole.STAFF);
        sampleUser.setId(1L);
        sampleUser.setActive(true);
    }

    @Nested
    @DisplayName("User Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Should successfully register user with hashed password and default STAFF role")
        void shouldRegisterUserSuccessfully() {
            RegisterRequest request = new RegisterRequest("newuser", "plainPassword123", "New User");

            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(passwordEncoder.encode("plainPassword123")).thenReturn("bcrypt_hashed_value");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User saved = invocation.getArgument(0);
                saved.setId(10L);
                return saved;
            });

            UserResponse response = authService.register(request);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(10L);
            assertThat(response.getUsername()).isEqualTo("newuser");
            assertThat(response.getFullName()).isEqualTo("New User");
            assertThat(response.getRole()).isEqualTo(UserRole.STAFF);
            assertThat(response.isActive()).isTrue();

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User captured = userCaptor.getValue();
            assertThat(captured.getPassword()).isEqualTo("bcrypt_hashed_value");
            assertThat(captured.getRole()).isEqualTo(UserRole.STAFF);
        }

        @Test
        @DisplayName("Should reject registration when username already exists")
        void shouldRejectDuplicateUsername() {
            RegisterRequest request = new RegisterRequest("johndoe", "password123", "John Doe");

            when(userRepository.existsByUsername("johndoe")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(DuplicateUsernameException.class)
                    .hasMessageContaining("johndoe");

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("User Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should successfully authenticate valid credentials and return JWT AuthResponse")
        void shouldAuthenticateSuccessfully() {
            LoginRequest request = new LoginRequest("johndoe", "correctPassword");

            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(sampleUser));
            when(passwordEncoder.matches("correctPassword", "hashed_password")).thenReturn(true);
            when(jwtUtils.generateToken("johndoe", "STAFF")).thenReturn("mock.jwt.token");

            AuthResponse response = authService.login(request);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("mock.jwt.token");
            assertThat(response.getType()).isEqualTo("Bearer");
            assertThat(response.getUser()).isNotNull();
            assertThat(response.getUser().getUsername()).isEqualTo("johndoe");
            assertThat(response.getUser().getRole()).isEqualTo(UserRole.STAFF);
        }

        @Test
        @DisplayName("Should reject login when username does not exist")
        void shouldRejectNonExistentUser() {
            LoginRequest request = new LoginRequest("unknown", "password123");

            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Invalid username or password");

            verify(jwtUtils, never()).generateToken(anyString(), anyString());
        }

        @Test
        @DisplayName("Should reject login when password is incorrect")
        void shouldRejectIncorrectPassword() {
            LoginRequest request = new LoginRequest("johndoe", "wrongPassword");

            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(sampleUser));
            when(passwordEncoder.matches("wrongPassword", "hashed_password")).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Invalid username or password");

            verify(jwtUtils, never()).generateToken(anyString(), anyString());
        }

        @Test
        @DisplayName("Should reject login when user account is inactive")
        void shouldRejectInactiveUser() {
            sampleUser.setActive(false);
            LoginRequest request = new LoginRequest("johndoe", "correctPassword");

            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(sampleUser));
            when(passwordEncoder.matches("correctPassword", "hashed_password")).thenReturn(true);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(InactiveUserException.class)
                    .hasMessageContaining("inactive");

            verify(jwtUtils, never()).generateToken(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Current User Query Tests")
    class CurrentUserTests {

        @Test
        @DisplayName("Should return safe user details for existing user")
        void shouldReturnCurrentUser() {
            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(sampleUser));

            UserResponse response = authService.getCurrentUser("johndoe");

            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo("johndoe");
            assertThat(response.getFullName()).isEqualTo("John Doe");
            assertThat(response.getRole()).isEqualTo(UserRole.STAFF);
        }

        @Test
        @DisplayName("Should throw exception if current user is not found")
        void shouldThrowWhenCurrentUserNotFound() {
            when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.getCurrentUser("nonexistent"))
                    .isInstanceOf(InvalidCredentialsException.class);
        }
    }
}
