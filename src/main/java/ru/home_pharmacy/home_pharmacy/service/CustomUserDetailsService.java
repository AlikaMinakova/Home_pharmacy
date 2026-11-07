package ru.home_pharmacy.home_pharmacy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.home_pharmacy.home_pharmacy.entity.User;
import ru.home_pharmacy.home_pharmacy.repository.UserRepository;
import ru.home_pharmacy.home_pharmacy.util.PhoneUtils;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);

        // Покупатель может ввести телефон в свободном формате, а логин хранится
        // в каноническом виде (11 цифр с ведущей 7) — нормализуем и пробуем ещё раз.
        if (user.isEmpty() && username != null && isPhoneLike(username)) {
            user = userRepository.findByUsername(PhoneUtils.normalize(username));
        }

        User found = user.orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));

        return new org.springframework.security.core.userdetails.User(
                found.getUsername(),
                found.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + found.getRole().name()))
        );
    }

    private boolean isPhoneLike(String value) {
        String digits = value.replaceAll("\\D", "");
        return digits.length() == 10 || digits.length() == 11;
    }
}