package com.example.RankCat.config.auth;

import com.example.RankCat.config.jwt.TokenAuthenticationFilter;
import com.example.RankCat.config.jwt.TokenProvider;
import com.example.RankCat.repository.RefreshTokenRepository;
import com.example.RankCat.service.user.impl.UserService;
import com.example.RankCat.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class WebOAuthSecurityConfig {

    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2UserCustomService oAuth2UserCustomService;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;
    private final CookieUtil cookieUtil;
    private final TokenLogoutHandler tokenLogoutHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final RefreshTokenHashService refreshTokenHashService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // REST API 서버이므로 기본 세션/폼로그인/CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                // POST /logout 처리
                .logout(
                        logout ->
                                logout.logoutUrl("/logout")
                                        .addLogoutHandler(tokenLogoutHandler)
                                        .logoutSuccessHandler(
                                                (request, response, authentication) -> {
                                                    response.setStatus(HttpServletResponse.SC_OK);
                                                    response.setCharacterEncoding("UTF-8");
                                                    response.setContentType("application/json");
                                                    response.getWriter()
                                                            .write(
                                                                    "{\"message\": \"Logout successful\"}");
                                                })
                                        .deleteCookies("refresh_token")
                                        .permitAll())

                // JWT 기반 Stateless 인증
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // JWT 인증 필터 등록
                .addFilterBefore(
                        tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(
                        auth ->
                                auth
                                        // CORS Preflight 요청 허용
                                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                                        .permitAll()

                                        // 브라우저 자동 요청
                                        .requestMatchers("/favicon.ico", "/.well-known/**")
                                        .permitAll()

                                        // Health check
                                        .requestMatchers(
                                                "/actuator/health",
                                                "/actuator/health/**",
                                                "/api/health")
                                        .permitAll()

                                        // Swagger / OpenAPI 문서
                                        .requestMatchers(
                                                "/swagger-ui/**",
                                                "/swagger-ui.html",
                                                "/v3/api-docs/**",
                                                "/v3/api-docs.yaml",
                                                "/swagger-resources/**",
                                                "/webjars/**")
                                        .permitAll()

                                        // OAuth2 로그인 진입점/콜백
                                        .requestMatchers(
                                                "/oauth2/authorization/**", "/login/oauth2/**")
                                        .permitAll()

                                        // 인증 없이 접근 가능한 API
                                        .requestMatchers(HttpMethod.POST, "/api/login")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.POST, "/signup")
                                        .permitAll()
                                        .requestMatchers("/api/user/**")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.POST, "/api/token")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.POST, "/logout")
                                        .permitAll()

                                        // 관리자 API
                                        .requestMatchers("/api/admin/**")
                                        .hasRole("ADMIN")

                                        // 로그인 사용자 API
                                        .requestMatchers("/api/**", "/ai/**")
                                        .hasAnyRole("USER", "ADMIN")

                                        // 위에서 명시하지 않은 요청은 차단
                                        .anyRequest()
                                        .denyAll())

                // OAuth2 로그인 설정
                .oauth2Login(
                        oauth2 ->
                                oauth2.authorizationEndpoint(
                                                ae ->
                                                        ae.authorizationRequestRepository(
                                                                        oAuth2AuthorizationRequestBasedOnCookieRepository())
                                                                .authorizationRequestResolver(
                                                                        authorizationRequestResolver()))
                                        .userInfoEndpoint(
                                                ui -> ui.userService(oAuth2UserCustomService))
                                        .successHandler(oAuth2SuccessHandler()))

                // 인증/인가 실패 시 JSON 401/403 처리
                .exceptionHandling(
                        ex ->
                                ex.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                                        .accessDeniedHandler(jwtAccessDeniedHandler));

        return http.build();
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(allowedOrigins);
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");

        // refresh_token을 HttpOnly Cookie로 주고받으므로 credentials 허용
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg)
            throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler() {
        return new OAuth2SuccessHandler(
                tokenProvider,
                refreshTokenRepository,
                oAuth2AuthorizationRequestBasedOnCookieRepository(),
                userService,
                cookieUtil,
                refreshTokenHashService);
    }

    @Bean
    public OAuth2AuthorizationRequestBasedOnCookieRepository
            oAuth2AuthorizationRequestBasedOnCookieRepository() {
        return new OAuth2AuthorizationRequestBasedOnCookieRepository(cookieUtil);
    }

    @Bean
    public OAuth2AuthorizationRequestResolver authorizationRequestResolver() {
        DefaultOAuth2AuthorizationRequestResolver defaultResolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository, "/oauth2/authorization");

        defaultResolver.setAuthorizationRequestCustomizer(
                customizer ->
                        customizer.additionalParameters(
                                params -> params.put("prompt", "select_account")));

        return defaultResolver;
    }
}
