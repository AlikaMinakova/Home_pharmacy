package ru.home_pharmacy.home_pharmacy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.home_pharmacy.home_pharmacy.dto.RegistrationDto;
import ru.home_pharmacy.home_pharmacy.entity.Role;
import ru.home_pharmacy.home_pharmacy.entity.User;
import ru.home_pharmacy.home_pharmacy.repository.UserRepository;
import ru.home_pharmacy.home_pharmacy.util.PhoneUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Регистрирует покупателя: имя, телефон (маска) и пароль.
     * Логин = нормализованный номер телефона.
     */
    public void register(RegistrationDto dto) {
        String phone = PhoneUtils.normalize(dto.getPhone());
        if (userRepository.existsByUsername(phone)) {
            throw new RuntimeException("Покупатель с таким номером уже зарегистрирован");
        }
        User user = User.builder()
                .username(phone)
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName().trim())
                .role(Role.CUSTOMER)
                .build();
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}