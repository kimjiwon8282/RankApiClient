// /js/token.js

const TEMP_ACCESS_TOKEN_COOKIE_NAME = 'temp_access_token';
const ACCESS_TOKEN_STORAGE_KEY = 'access_token';

/**
 * 특정 이름의 쿠키 값을 읽어오는 함수
 */
function getCookie(name) {
    const escapedName = name.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, '\\$1');
    const matches = document.cookie.match(new RegExp(
        `(?:^|; )${escapedName}=([^;]*)`
    ));

    return matches ? decodeURIComponent(matches[1]) : undefined;
}

/**
 * 쿠키 삭제 함수
 */
function deleteCookie(name) {
    document.cookie = `${name}=; Max-Age=0; path=/;`;
}

/**
 * OAuth2SuccessHandler가 내려준 임시 Access Token 쿠키를
 * localStorage로 옮기고, 임시 쿠키는 즉시 삭제한다.
 */
document.addEventListener('DOMContentLoaded', () => {
    const tempAccessToken = getCookie(TEMP_ACCESS_TOKEN_COOKIE_NAME);

    if (!tempAccessToken) {
        return;
    }

    localStorage.setItem(ACCESS_TOKEN_STORAGE_KEY, tempAccessToken);
    deleteCookie(TEMP_ACCESS_TOKEN_COOKIE_NAME);

    console.log('소셜 로그인 완료. Access Token 저장 완료.');
});