package org.cts.adm.finguard;
import org.cts.adm.finguard.Jwt.JwtFilter;
import org.cts.adm.finguard.Jwt.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * BCrypt password encoder – used by signup (encoding) and login (matching).
     * BCrypt is the industry standard for password storage: it is adaptive,
     * salted automatically, and resistant to brute-force attacks.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .addFilterBefore(
                        new JwtFilter(jwtUtil),
                        UsernamePasswordAuthenticationFilter.class
                )

                .authorizeHttpRequests(auth -> auth

                        // PUBLIC endpoints
                        .requestMatchers(
                                "/api/customer/login",
                                "/api/customer/signup",
                                "/v3/api-docs/**",
                                "/swagger-ui/**"
                        ).permitAll()

                        // CUSTOMER endpoints (USER or ADMIN)
                        .requestMatchers("/api/customer/**")
                        .hasAnyRole("USER", "ADMIN")

                        // ADMIN-only endpoints
                        .requestMatchers("/api/admin/**", "/api/compliance/**", "/api/analytics/**")
                        .hasRole("ADMIN")

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }
}



