package com.bemain.spb.domain.config;

import com.bemain.spb.domain.etc.jwt.JwtAuthenticationFilter;
import com.bemain.spb.domain.etc.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService; // 이제 CustomUserDetailsService가 주입됩니다.

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. 공용 API (인증 불필요)
                        // [변경] /api/auth -> /api/v1/auth
                        .requestMatchers("/api/v1/auth/**", "/error", "/test").permitAll()
                        .requestMatchers("/api/v1/tags/**", "/api/v1/labs").permitAll() // 조회는 누구나
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // 2. 개발자 전용 (랩 등록, 수정, 삭제)
                        // [변경] 권한 이름 앞에 ROLE_ 붙임
                        .requestMatchers(HttpMethod.POST, "/api/v1/labs").hasAuthority("ROLE_DEVELOPER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/labs/**").hasAuthority("ROLE_DEVELOPER")

                        // 3. 해커 전용 (실습 시작, 리포트 작성)
                        .requestMatchers("/api/v1/labs/*/start", "/api/v1/labs/*/stop").hasAuthority("ROLE_HACKER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/reports").hasAuthority("ROLE_HACKER")

                        // 4. 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 프론트엔드 개발 시 localhost:3000 허용
        configuration.setAllowedOrigins(List.of("http://192.168.0.9:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}