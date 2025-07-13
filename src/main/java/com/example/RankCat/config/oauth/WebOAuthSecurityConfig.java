package com.example.RankCat.config.oauth;


import com.example.RankCat.config.jwt.TokenAuthenticationFilter;
import com.example.RankCat.config.jwt.TokenProvider;
import com.example.RankCat.repository.RefreshTokenRepository;
import com.example.RankCat.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class WebOAuthSecurityConfig {

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


    // --- 정적 리소스 및 H2 콘솔 제외 ------------------------------------------------

    /**
     * H2 콘솔(/h2-console)과
     * 정적 리소스(/static/**, /css/**, /js/**)를
     * 스프링 시큐리티 필터 체인 밖으로 제외합니다.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
//                .requestMatchers(PathRequest.toH2Console())   // 개발 중 편의를 위한 H2 콘솔
                .requestMatchers("/static/**", "/css/**", "/js/**"); // 정적 리소스
    }


    // --- 보안 필터 체인 설정 --------------------------------------------------------

    /**
     * HTTP 보안 설정의 전반적인 골격.
     * - CSRF, 세션, 폼로그인 등 기본 보안 기능 비활성화
     * - JWT 인증 필터 등록
     * - 엔드포인트별 접근 권한 설정
     * - OAuth2 로그인 설정
     * - 인증 실패 시 처리
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1) 토큰 기반 인증을 사용하므로 기본 세션·폼로그인·CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                // 2) 로그아웃 처리 설정 (POST /logout)
                .logout(logout -> logout
                        .logoutUrl("/logout")               // 로그아웃 처리 엔드포인트
                        .logoutSuccessUrl("/login")         // 로그아웃 후 리다이렉트할 URL
                        .deleteCookies("refresh_token")     // HTTP 쿠키에 저장된 리프레시 토큰 삭제
                        .invalidateHttpSession(true)        // (만약 세션이 남아 있다면) 세션 무효화
                        .permitAll()                        // 로그아웃 엔드포인트는 누구나 호출 가능
                )

                // 3) Stateless: 세션을 만들지 않고 매 요청 JWT로 인증
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4) JWT 인증 필터를 UsernamePasswordAuthenticationFilter 이전에 등록
                .addFilterBefore(
                        tokenAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class
                )

                // 5) URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // – 로그인·회원가입·OAuth 진입점 (공개)
                        .requestMatchers(
                                "/login",
                                "/oauth2/authorization/**",
                                "/login/oauth2/**",
                                "/signup",
                                "/user",
                                "/home",
                                "/api/user/**",
                                "/api/login"
                        ).permitAll()


                        // – 토큰 재발급 엔드포인트 (공개)
                        .requestMatchers("/api/token").permitAll()

                        // – API 호출은 JWT 인증 필수
                        .requestMatchers("/api/**").authenticated()

                        // – 뷰 템플릿(게시글 목록·상세·작성)은 공개
                        .anyRequest().permitAll()
                )

                // 6) OAuth2 로그인(소셜 로그인) 설정
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")  // 커스텀 로그인 화면

                        // 인가 요청 저장소: 쿠키 기반 구현체 사용
                        .authorizationEndpoint(ae -> ae
                                .authorizationRequestRepository(
                                        oAuth2AuthorizationRequestBasedOnCookieRepository()
                                )
                                .authorizationRequestResolver(
                                        authorizationRequestResolver()
                                )
                        )

                        // user-info 엔드포인트: 커스텀 서비스 사용
                        .userInfoEndpoint(ui ->
                                ui.userService(oAuth2UserCustomService)
                        )

                        // 로그인 성공 후 처리 핸들러
                        .successHandler(oAuth2SuccessHandler())
                )

                // 7) API 인증 실패 시 401 Unauthorized 반환
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                request -> request.getRequestURI().startsWith("/api/")
                        )
                )
        ;

        return http.build();
    }


    // --- 빈 등록 목록 -------------------------------------------------------------

    /** JWT 인증 필터를 빈으로 등록 */
    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider);
    }

    /**
     * 스프링 시큐리티가 자동 구성한 AuthenticationManager를 Bean으로 등록합니다.
     * 이 Bean을 주입받아 authManager.authenticate(...)로 사용자 인증 처리를 할 수 있습니다.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    /**
     * OAuth2 로그인 성공 후 처리 핸들러 빈 등록
     * - Refresh Token 생성·DB 저장·쿠키 저장
     * - Access Token 생성
     * - 인증 임시 속성 정리
     * - 최종 리다이렉트
     */
    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler() {
        return new OAuth2SuccessHandler(
                tokenProvider,
                refreshTokenRepository,
                oAuth2AuthorizationRequestBasedOnCookieRepository(),
                userService
        );
    }

    /** OAuth2 인가 요청 정보를 쿠키에 보관하는 저장소 빈 등록 */
    @Bean
    public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() {
        return new OAuth2AuthorizationRequestBasedOnCookieRepository();
    }

    @Bean
    public OAuth2AuthorizationRequestResolver authorizationRequestResolver() {
        DefaultOAuth2AuthorizationRequestResolver defaultResolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository,
                        "/oauth2/authorization"
                );
        defaultResolver.setAuthorizationRequestCustomizer(customizer ->
                customizer.additionalParameters(params ->
                        params.put("prompt", "select_account")
                )
        );
        return defaultResolver;
    }

    /** 비밀번호 암호화를 위한 BCryptPasswordEncoder 빈 등록 */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
