package com.dental.management.repository;

import com.dental.management.entity.User;
import com.dental.management.entity.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Save and find user by username")
    void shouldSaveAndFindByUsername() {
        User user = new User("admin_jane", "hashed_pass_placeholder", "Jane Admin", UserRole.ADMIN);
        User saved = userRepository.save(user);

        assertNotNull(saved.getId(), "User id should be generated upon persist");
        Optional<User> found = userRepository.findByUsername("admin_jane");
        assertTrue(found.isPresent());
        assertEquals("Jane Admin", found.get().getFullName());
        assertEquals(UserRole.ADMIN, found.get().getRole());
        assertTrue(userRepository.existsByUsername("admin_jane"));
    }

    @Test
    @DisplayName("Unique constraint on username throws exception on duplicate")
    void duplicateUsernameShouldThrowException() {
        User user1 = new User("same_username", "pass123", "User One", UserRole.STAFF);
        userRepository.saveAndFlush(user1);

        User user2 = new User("same_username", "pass456", "User Two", UserRole.RECEPTIONIST);
        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(user2);
        });
    }
}
