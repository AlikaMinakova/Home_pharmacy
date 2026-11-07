package ru.home_pharmacy.home_pharmacy.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.home_pharmacy.home_pharmacy.entity.Role;
import ru.home_pharmacy.home_pharmacy.entity.User;
import ru.home_pharmacy.home_pharmacy.repository.UserRepository;

/**
 * При старте приложения создаёт учётную запись сотрудника по умолчанию,
 * если её ещё нет в базе.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.EMPLOYEE)
                    .build();
            userRepository.save(admin);
        }
    }
}