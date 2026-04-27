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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
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

    // --- 의존성 주입 --------------------------------------------------------------
    /** Spring Boot가 자동 생성해 주는 OAuth2 클라이언트 등록 저장소 */
    private final ClientRegistrationRepository clientRegistrationRepository;

    /** 소셜 로그인 후 사용자 정보를 DB에 저장/업데이트하는 커스텀 서비스 */
    private final OAuth2UserCustomService oAuth2UserCustomService;

    /** JWT 생성·검증·인증정보 추출을 처리하는 유틸리티 */
    private final TokenProvider tokenProvider;

    /** 리프레시 토큰을 영속화(DB)에 저장/조회하는 JPA 리포지토리 */
    private final RefreshTokenRepository refreshTokenRepository;

    /** 유저 조회 및 기타 비즈니스 로직을 수행하는 서비스 */
    private final UserService userService;

    // CookieUtil 주입
    private final CookieUtil cookieUtil;

    // 로그아웃 핸들러
    private final TokenLogoutHandler tokenLogoutHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final RefreshTokenHashService refreshTokenHashService;

    // --- 정적 리소스 및 H2 콘솔 제외 ------------------------------------------------

    /** H2 콘솔(/h2-console)과 정적 리소스(/static/**, /css/**, /js/**)를 스프링 시큐리티 필터 체인 밖으로 제외합니다. */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) ->
                web.ignoring()
                        //                .requestMatchers(PathRequest.toH2Console())   // 개발 중 편의를
                        // 위한 H2 콘솔
                        .requestMatchers("/static/**", "/css/**", "/js/**"); // 정적 리소스
    }

    // --- 보안 필터 체인 설정 --------------------------------------------------------

    /**
     * HTTP 보안 설정의 전반적인 골격. - CSRF, 세션, 폼로그인 등 기본 보안 기능 비활성화 - JWT 인증 필터 등록 - 엔드포인트별 접근 권한 설정 -
     * OAuth2 로그인 설정 - 인증 실패 시 처리
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1) 토큰 기반 인증을 사용하므로 기본 세션·폼로그인·CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                // 2) 로그아웃 처리 설정 (POST /logout)
                .logout(
                        logout ->
                                logout.logoutUrl("/logout") // 로그아웃 처리 엔드포인트
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
                                        .deleteCookies("refresh_token") // HTTP 쿠키에 저장된 리프레시 토큰 삭제
                                        .permitAll() // 로그아웃 엔드포인트는 누구나 호출 가능
                        )

                // 3) Stateless: 세션을 만들지 않고 매 요청 JWT로 인증
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4) JWT 인증 필터를 UsernamePasswordAuthenticationFilter 이전에 등록
                .addFilterBefore(
                        tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)

                // 5) URL별 접근 권한 설정
                .authorizeHttpRequests(
                        auth ->
                                auth
                                        // 1. 정적 화면/페이지 라우트
                                        // 프론트 분리 전에는 Spring이 HTML을 내려주기 때문에 permitAll 유지
                                        .requestMatchers(
                                                "/",
                                                "/login",
                                                "/signup",
                                                "/home",
                                                "/keyword-analysis",
                                                "/optimize-product-name",
                                                "/my-history")
                                        .permitAll()

                                        // 2. 정적 리소스 및 브라우저 자동 요청
                                        .requestMatchers(
                                                "/favicon.ico",
                                                "/.well-known/**",
                                                "/static/**",
                                                "/css/**",
                                                "/js/**",
                                                "/image/**",
                                                "/data/**")
                                        .permitAll()

                                        // 3. Swagger / OpenAPI 문서
                                        .requestMatchers(
                                                "/swagger-ui/**",
                                                "/swagger-ui.html",
                                                "/v3/api-docs/**",
                                                "/v3/api-docs.yaml",
                                                "/swagger-resources/**",
                                                "/webjars/**")
                                        .permitAll()

                                        // 4. OAuth2 진입점/콜백
                                        .requestMatchers(
                                                "/oauth2/authorization/**", "/login/oauth2/**")
                                        .permitAll()

                                        // 5. 회원가입/로그인/이메일 인증/토큰 재발급
                                        .requestMatchers(
                                                "/api/login",
                                                "/signup",
                                                "/api/user/**",
                                                "/api/token")
                                        .permitAll()

                                        // 6. 관리자 API
                                        .requestMatchers("/api/admin/**")
                                        .hasRole("ADMIN")

                                        // 7. 로그인 사용자 API
                                        .requestMatchers(
                                                "/api/me",
                                                "/api/categories/suggest",
                                                "/api/**",
                                                "/ai/**")
                                        .hasAnyRole("USER", "ADMIN")

                                        // 8. 위에서 명시하지 않은 요청은 막기
                                        .anyRequest()
                                        .denyAll())

                // 6) OAuth2 로그인(소셜 로그인) 설정
                .oauth2Login(
                        oauth2 ->
                                oauth2.loginPage("/login") // 커스텀 로그인 화면

                                        // 인가 요청 저장소: 쿠키 기반 구현체 사용
                                        .authorizationEndpoint(
                                                ae ->
                                                        ae.authorizationRequestRepository(
                                                                        oAuth2AuthorizationRequestBasedOnCookieRepository())
                                                                .authorizationRequestResolver(
                                                                        authorizationRequestResolver()))

                                        // user-info 엔드포인트: 커스텀 서비스 사용
                                        .userInfoEndpoint(
                                                ui -> ui.userService(oAuth2UserCustomService))

                                        // 로그인 성공 후 처리 핸들러
                                        .successHandler(oAuth2SuccessHandler()))

                // 7) API 인증 실패 시 401 Unauthorized 반환
                .exceptionHandling(
                        ex ->
                                ex.authenticationEntryPoint(
                                                jwtAuthenticationEntryPoint) // 401 에러 처리
                                        .accessDeniedHandler(jwtAccessDeniedHandler) // 403 에러 처리
                        );

        return http.build();
    }

    // --- 빈 등록 목록 -------------------------------------------------------------

    /** JWT 인증 필터를 빈으로 등록 */
    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1) 허용할 출처 (프론트엔드 주소)
        configuration.setAllowedOrigins(allowedOrigins);
        // 2) 허용할 HTTP 메서드 (GET, POST 등 모든 요청 허용)
        configuration.addAllowedMethod("*");

        // 3) 허용할 헤더 (인증 토큰 등을 실어 보낼 수 있게 모든 헤더 허용)
        configuration.addAllowedHeader("*");

        // 4) 중요: 쿠키/인증 정보를 포함한 요청을 허용할 것인가?
        // 우리는 리프레시 토큰을 쿠키로 주고받으므로 반드시 true여야 합니다.
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 위 설정을 적용
        return source;
    }

    /**
     * 스프링 시큐리티가 자동 구성한 AuthenticationManager를 Bean으로 등록합니다. 이 Bean을 주입받아
     * authManager.authenticate(...)로 사용자 인증 처리를 할 수 있습니다.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg)
            throws Exception {
        return cfg.getAuthenticationManager();
    }

    /**
     * OAuth2 로그인 성공 후 처리 핸들러 빈 등록 - Refresh Token 생성·DB 저장·쿠키 저장 - Access Token 생성 - 인증 임시 속성 정리 -
     * 최종 리다이렉트
     */
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

    /** OAuth2 인가 요청 정보를 쿠키에 보관하는 저장소 빈 등록 */
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
