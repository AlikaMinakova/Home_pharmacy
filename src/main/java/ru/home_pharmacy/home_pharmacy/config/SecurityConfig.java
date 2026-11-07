package ru.home_pharmacy.home_pharmacy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String EMPLOYEE = "EMPLOYEE";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            boolean customer = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));
            response.sendRedirect(customer ? "/catalog" : "/pharmacies");
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // оформление заказа и заявки доступно только покупателю
                        .requestMatchers("/catalog/order", "/catalog/request").hasRole("CUSTOMER")
                        // публичный доступ: вход, регистрация и просмотр каталога
                        .requestMatchers("/", "/login", "/register", "/catalog/**", "/uploads/**", "/no-photo.jpg").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico", "/error").permitAll()
                        // история покупателя доступна любому авторизованному пользователю
                        .requestMatchers("/my-orders", "/my-requests/**").authenticated()
                        // чат по заявке доступен покупателю и аптекарю
                        .requestMatchers("/chat/**").authenticated()
                        // всё, что относится к управлению, доступно только сотруднику
                        .requestMatchers("/medication/**", "/diseases/**", "/ingredients/**",
                                "/symptoms/**", "/pharmacies/**", "/orders/**", "/requests/**").hasRole(EMPLOYEE)
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(successHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/catalog")
                        .permitAll()
                );

        return http.build();
    }
}