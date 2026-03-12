package com.creditcard.config;

import com.creditcard.model.User;
import com.creditcard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // Create ADMIN user
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .email("admin@creditcard.com")
                .fullName("System Administrator")
                .roles(Set.of("ADMIN", "USER"))
                .enabled(true)
                .build();
            userRepository.save(admin);
            log.info("==> Default admin user created  [username: admin | password: admin123]");
        }

        // Create test USER
        if (!userRepository.existsByUsername("john")) {
            User user = User.builder()
                .username("john")
                .password(passwordEncoder.encode("john123"))
                .email("john@example.com")
                .fullName("John Doe")
                .roles(Set.of("USER"))
                .enabled(true)
                .build();
            userRepository.save(user);
            log.info("==> Test user created           [username: john  | password: john123]");
        }

        // Create second test USER
        if (!userRepository.existsByUsername("jane")) {
            User user2 = User.builder()
                .username("jane")
                .password(passwordEncoder.encode("jane123"))
                .email("jane@example.com")
                .fullName("Jane Smith")
                .roles(Set.of("USER"))
                .enabled(true)
                .build();
            userRepository.save(user2);
            log.info("==> Test user created           [username: jane  | password: jane123]");
        }

        log.info("================================================");
        log.info("   Credit Card Management System is running!   ");
        log.info("   H2 Console: http://localhost:8080/h2-console ");
        log.info("   API Base  : http://localhost:8080/api        ");
        log.info("================================================");
    }
}
