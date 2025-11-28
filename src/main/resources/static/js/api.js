// api.js

// function getCookie(name) {
//     const value = `; ${document.cookie}`;
//     const parts = value.split(`; ${name}=`);
//     if (parts.length === 2) return parts.pop().split(';').shift();
//     return null;
// }

export async function httpRequest(method, url, data) {
    let accessToken = localStorage.getItem('access_token');
    const headers = { 'Content-Type': 'application/json' };

    // Access Token은 헤더에 직접 넣어야 함 (LocalStorage라 자동 전송 안 됨)
    if (accessToken) {
        headers['Authorization'] = `Bearer ${accessToken}`;
    }

    const options = { method, headers };
    if (data) {
        options.body = JSON.stringify(data);
    }

    let response = await fetch(url, options);

    // --- 401 에러(Access Token 만료) 발생 시 처리 ---
    if (response.status === 401) {
        console.log("Access Token 만료. HttpOnly 쿠키로 재발급 시도...");

        try {
            // [수정] body 내용을 싹 비웁니다.
            // 브라우저가 'refresh_token' 쿠키를 자동으로 서버에 보냅니다.
            const refreshRes = await fetch('/api/token', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                // body: JSON.stringify({ refreshToken })  <-- [삭제] 이거 없애야 함!
            });

            if (refreshRes.ok) {
                // 재발급 성공
                const { accessToken: newAccessToken } = await refreshRes.json();
                localStorage.setItem('access_token', newAccessToken); // 새 토큰 저장

                // [중요] 실패했던 원래 요청의 헤더를 새 토큰으로 교체
                headers['Authorization'] = `Bearer ${newAccessToken}`;

                // 기존 options에 새 header 적용하여 재요청
                // (fetch 옵션 객체는 얕은 복사가 될 수 있으므로 headers를 명시적으로 덮어쓰기)
                const retryOptions = {
                    ...options,
                    headers: { ...headers }
                };

                response = await fetch(url, retryOptions);
            } else {
                // 리프레시 토큰도 만료되었거나 없을 경우
                console.log("Refresh Token 만료/없음. 재로그인 필요.");
                // 필요하다면 여기서 window.location.href = '/login'; 처리
            }
        } catch (e) {
            console.error("토큰 갱신 중 에러 발생:", e);
        }
    }
    return response;
}