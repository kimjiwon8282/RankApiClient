// 1. 특정 이름의 쿠키 값을 읽어오는 함수
function getCookie(name) {
    let matches = document.cookie.match(new RegExp(
        "(?:^|; )" + name.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, '\\$1') + "=([^;]*)"
    ));
    return matches ? decodeURIComponent(matches[1]) : undefined;
}

// 2. 쿠키 삭제 함수 (만료일을 과거로 돌려버림)
function deleteCookie(name) {
    document.cookie = name + "=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
}

// 3. 페이지가 로딩될 때 실행되는 메인 로직
document.addEventListener("DOMContentLoaded", function() {
    // 백엔드(OAuth2SuccessHandler)가 구워준 임시 쿠키를 찾는다!
    const tempToken = getCookie('temp_access_token');

    if (tempToken) {
        // 1) 쿠키에서 토큰을 빼서 로컬 스토리지에 안전하게 저장한다.
        localStorage.setItem("access_token", tempToken);

        // 2) 탈취 방지를 위해 브라우저 쿠키에서 즉시 삭제한다. (매우 중요)
        deleteCookie('temp_access_token');

        console.log("소셜 로그인 완료! 토큰 저장 성공");
    }
});