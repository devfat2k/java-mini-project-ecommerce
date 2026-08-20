package com.devfat.mini_ecommerce.shared.config;

import com.devfat.mini_ecommerce.shared.security.JwtAccessDeniedHandler;
import com.devfat.mini_ecommerce.shared.security.JwtAuthenticationEntryPoint;
import com.devfat.mini_ecommerce.shared.security.JwtAuthenticationFilter;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
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
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] PUBLIC_AUTH_URLS = {
            "/api/v1/auth/**",
            "/api/v1/health",
            "/api/v1/payments/vnpay-return",
            "/api/v1/payments/vnpay-ipn"
    };
    private static final String[] PUBLIC_GET_URLS = {
            "/api/v1/products",
            "/api/v1/categories",
            "/api/v1/home"
    };
    private static final String[] PUBLIC_GET_WITH_ID_URLS = {
            "/api/v1/products/{id}",
            "/api/v1/categories/{id}",
            "/api/v1/products/{id}/reviews"
    };

    private static final String[] SWAGGER_URLS = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v1/api-docs/**",
            "/v1/api-docs"
    };

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;


    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Khởi tạo BCryptPasswordEncoder mặc định (strength = 10)
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. Public
                        .requestMatchers(PUBLIC_AUTH_URLS).permitAll()
                        .requestMatchers(SWAGGER_URLS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_URLS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_WITH_ID_URLS).permitAll()
                        // 2. Admin
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        // 3. Other
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(List.of("http://localhost:8085")); // set cho url nào
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE")); // các phương thức nào
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type")); // header

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Cách 1: Sẽ Manual lấy từ userDetailsService & passwordEncoder để biết xác thực như thé nào
//    @Bean
//    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) throws Exception {
//        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
//        authenticationProvider.setPasswordEncoder(passwordEncoder);
//
//        return new ProviderManager(authenticationProvider);
//    }

    // Cách 2: Tự động đi tìm các Bean đã được config để biết cách sẽ phải xác thực như thế nào?
    /**
     * FLOW: Client gửi email+password tới AuthController (sẽ code ở C4)
     *   -> Controller gọi authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password))
     *   -> AuthenticationManager (được Spring tự lắp ở đây) nội bộ gọi:
     *        1. CustomUserDetailsService.loadUserByUsername(email)  (đã code ở B1)
     *           -> lấy UserEntity từ DB, bọc thành UserPrincipal
     *        2. PasswordEncoder.matches(rawPassword, userPrincipal.getPassword())  (đã code ở B2)
     *           -> so sánh password nhập vào với hash trong DB
     *   -> Khớp -> trả về Authentication đã xác thực (chứa UserPrincipal bên trong)
     *   -> Không khớp -> tự động ném BadCredentialsException, KHÔNG cần tự viết logic so sánh thủ công
     *
     * Bean này CHƯA được gọi ở đâu trong code hiện tại — sẽ được inject vào AuthController/AuthService
     * ở C4 (Login API) để thực thi bước authenticate() ở trên.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
