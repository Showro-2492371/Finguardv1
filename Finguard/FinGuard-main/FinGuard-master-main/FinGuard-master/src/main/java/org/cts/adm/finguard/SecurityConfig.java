package org.cts.adm.finguard;
import org.cts.adm.finguard.Jwt.JwtFilter;
import org.cts.adm.finguard.Jwt.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
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

                        // PUBLIC endpoints (NO change)
                        .requestMatchers(
                                "/api/customer/login",
                                "/api/customer/signup",
                                "/v3/api-docs/**",
                                "/swagger-ui/**"
                        ).permitAll()

                        // CUSTOMER endpoints (USER or ADMIN)
                        .requestMatchers("/api/customer/**")
                        .hasAnyRole("USER", "ADMIN")

                        // ADMIN-only endpoints (future safe)
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // Everything else secured
                        .anyRequest().authenticated()
                )

                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }
}



//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    private final JwtUtil jwtUtil;
//
//    public SecurityConfig(JwtUtil jwtUtil) {
//        this.jwtUtil = jwtUtil;
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//
//        http
//                // JWT is stateless
//                .csrf(csrf -> csrf.disable())
//                .sessionManagement(session ->
//                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//
//                // Add JWT filter
//                .addFilterBefore(new JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
//
//                // Endpoint security
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/customer/**",
//                                         "/api/customer/kyc/**",
//                                "/api/customer/kyc/download/**",
//                                "/api/customer/transaction/add",
//                                "/api/risk/alerts",
//                                "/api/risk/alerts/**",
//                                "/v3/api-docs/**",
//                                "/swagger-ui/**"
//                        ).permitAll() // login only
//                        .anyRequest().authenticated()
//                )
//
//                // Disable default auth mechanisms
//                .httpBasic(httpBasic -> httpBasic.disable())
//                .formLogin(form -> form.disable());
//
//        return http.build();
//    }
//}
