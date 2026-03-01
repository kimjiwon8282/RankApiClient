package com.example.RankCat.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "2. 인증 및 로그인(2)", description = "사용자 로그인 및 토큰 발급 관련 API")
@RestController
public class OAuthSwaggerController {

    @Operation(
            summary = "소셜 로그인 (구글/네이버/카카오) - 브라우저 리다이렉트 전용",
            description =
                    "### ⚠️ 주의: 이 API는 Swagger UI에서 'Execute'로 테스트할 수 없습니다.\n\n"
                            + "이 엔드포인트는 Spring Security가 자동으로 가로채어 처리합니다. 프론트엔드 연동 시 아래 흐름을 따라주세요.\n\n"
                            + "**[소셜 로그인 연동 흐름]**\n"
                            + "1. **요청**: 프론트엔드에서 `location.href = 'http://서버주소/oauth2/authorization/{provider}'` 로 페이지를 이동시킵니다. (AJAX/Fetch 요청 불가)\n"
                            + "2. **인증**: 사용자가 소셜 로그인 페이지에서 로그인을 완료합니다.\n"
                            + "3. **리다이렉트 & 토큰 전달**: 서버 처리가 완료되면 프론트엔드의 특정 페이지(예: `/home`)로 강제 리다이렉트 됩니다. **(주의: 보안상 URL 파라미터에는 아무것도 붙지 않습니다!)**\n"
                            + "   - **AccessToken**: 브라우저 쿠키(`temp_access_token`)에 딱 **60초 동안만** 임시 저장되어 전달됩니다. (자바스크립트로 접근 가능)\n"
                            + "   - **RefreshToken**: 보안을 위해 `HttpOnly` 쿠키로 브라우저에 자동 저장됩니다. (자바스크립트 접근 불가)\n"
                            + "4. **저장 및 정리**: 프론트엔드는 화면이 로딩되자마자 `document.cookie`에서 `temp_access_token` 값을 추출하여 로컬 스토리지(또는 메모리)에 저장하고, **보안을 위해 즉시 브라우저에서 해당 쿠키를 삭제**해야 합니다.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "302",
                description = "소셜 로그인 성공 후 프론트엔드 URL로 리다이렉트",
                headers = {
                    @Header(
                            name = "Set-Cookie",
                            description =
                                    "1. refresh_token=...; Path=/; HttpOnly; Max-Age=259200\n"
                                            + "2. temp_access_token=...; Path=/; Max-Age=60 (HttpOnly 아님 - 프론트엔드 수령용)")
                })
    })
    @GetMapping("/oauth2/authorization/{provider}")
    public void oauth2Login(
            @Parameter(description = "소셜 로그인 제공자 (google, naver, kakao)", example = "google")
                    @PathVariable
                    String provider) {
        // 실제 로직은 Spring Security의 OAuth2 필터가 가로채서 처리하므로,
        // 이 메서드 내부는 비워둡니다. 오직 Swagger 명세서 노출을 위한 껍데기입니다.
    }

    @Operation(
            summary = "로그아웃 - 서버 측 세션 및 토큰 무효화",
            description =
                    "### ⚠️ 주의: 반드시 POST 방식으로 요청해야 합니다.\n\n"
                            + "**[로그아웃 처리 흐름]**\n"
                            + "1. **서버**: DB에서 해당 유저의 `RefreshToken`을 즉시 삭제합니다.\n"
                            + "2. **서버**: 브라우저의 `refresh_token` 쿠키를 만료(삭제) 처리합니다.\n"
                            + "3. **응답**: 성공 시 200 OK와 메시지를 반환합니다.\n"
                            + "4. **프론트엔드**: 응답 수령 후 **로컬 스토리지의 `access_token`을 반드시 직접 삭제**해야 합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "로그아웃 성공")})
    @PostMapping("/logout") // 스프링 시큐리티 기본값은 POST입니다.
    public void logout() {
        // 실제 로직은 TokenLogoutHandler가 처리합니다.
    }
}
