package com.devfat.mini_ecommerce.config;


import com.devfat.mini_ecommerce.security.JwtAuthenticationFilter;
import lombok.AllArgsConstructor;
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
import com.devfat.mini_ecommerce.security.JwtAccessDeniedHandler;
import com.devfat.mini_ecommerce.security.JwtAuthenticationEntryPoint;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {

    private static final String[] PUBLIC_URLS = {
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh-token",
            "/api/v1/auth/logout",
    };
    private static final String[] PUBLIC_GET_URLS = {
            "/api/v1/products",
            "/api/v1/products/{id}",
            "/api/v1/categories",
            "/api/v1/categories/{id}"
    };
    private static final String[] SWAGGER_URLS = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v1/api-docs/**",
            "/v1/api-docs"
    };

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Khởi tạo BCryptPasswordEncoder mặc định (strength = 10)
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        /**
         * Lấy code từ "https://docs.spring.io/spring-security/reference/servlet/configuration/java.html#jc-httpsecurity"
         * https://www.baeldung.com/spring-security-deactivate#bd-removing-spring-security-dependency
         *  .formLogin(Customizer.withDefaults()) - Không cần vì nó sẽ sinh ra trang html
         *  .httpBasic(Customizer.withDefaults()) - Không cần vì nó sẽ sinh ra trang html
         *  .anyRequest().permitAll() -- tất cả api đều truy cập được http://localhost:8085/
         */
        http
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_URLS).permitAll()
                        .requestMatchers(SWAGGER_URLS).permitAll()
                        .anyRequest().authenticated())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement((session) ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> {
                    exception
                            .authenticationEntryPoint(jwtAuthenticationEntryPoint) // tầng filter ném ex -> chưa xác thực - Xử lý res JSON 401
                            .accessDeniedHandler(jwtAccessDeniedHandler); // tầng filter ném ex -> đã xác thực nhưng không có quyền truy cập  -> xử lý res json 403
                });
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // TODO: Nhớ đổi sau khi đã deploy chính thức không phải đang chạy local
        configuration.setAllowedOrigins(List.of("http://localhost:8085")); // set cho url nào
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
