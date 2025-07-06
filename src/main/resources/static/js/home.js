document.addEventListener('DOMContentLoaded', async () => {
    // 쿠키에서 이름(name)에 해당하는 값을 꺼내는 헬퍼
    function getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
        return null;
    }

    // HTTP 요청에 Authorization 헤더 자동 추가,
    // 401 발생 시 리프레시 토큰으로 엑세스 토큰 재발급 후 재요청
    async function httpRequest(method, url, data) {
        let accessToken = localStorage.getItem('access_token');
        const headers = { 'Content-Type': 'application/json' };
        if (accessToken) {
            headers['Authorization'] = `Bearer ${accessToken}`;
        }

        const options = { method, headers };
        if (data) {
            options.body = JSON.stringify(data);
        }

        let response = await fetch(url, options);

        if (response.status === 401) {
            // access_token이 만료된 경우 refresh_token으로 재발급 시도
            const refreshToken = getCookie('refresh_token');
            if (refreshToken) {
                const refreshRes = await fetch('/api/token', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ refreshToken })
                });
                if (refreshRes.ok) {
                    const { accessToken: newAccessToken } = await refreshRes.json();
                    localStorage.setItem('access_token', newAccessToken);
                    headers['Authorization'] = `Bearer ${newAccessToken}`;
                    // 재발급받은 토큰으로 원래 요청 다시 시도
                    response = await fetch(url, options);
                }
            }
            // 여기서 더 이상 window.location.href = '/login'; 하지 않음!
        }
        return response;
    }

    const greetingEl = document.getElementById('greeting');
    const authBtnArea = document.getElementById('authBtnArea');
    let isAuthenticated = false;

    try {
        // 1) /api/me 호출해서 사용자 정보 가져오기
        const res = await httpRequest('GET', '/api/me');
        if (!res.ok) throw new Error('not authenticated');

        const { nickname } = await res.json();
        // 2) 인증된 사용자라면 닉네임으로 인사
        greetingEl.innerText = `안녕하세요, ${nickname}님!`;
        isAuthenticated = true;
    } catch (err) {
        // 인증되지 않은 경우 방문자 인사
        greetingEl.innerText = '안녕하세요, 방문자님!';
        isAuthenticated = false;
    }

    // 버튼 동적 렌더링
    if (authBtnArea) {
        if (isAuthenticated) {
            // 로그아웃 버튼 생성
            authBtnArea.innerHTML = `
              <form id="logoutForm" action="/logout" method="post">
                <button type="submit">로그아웃</button>
              </form>
            `;
            // 로그아웃 시 access_token 삭제
            const logoutForm = document.getElementById('logoutForm');
            if (logoutForm) {
                logoutForm.addEventListener('submit', () => {
                    localStorage.removeItem('access_token');
                });
            }
        } else {
            // 로그인하러 가기 버튼 생성
            authBtnArea.innerHTML = `
              <a href="/login">
                <button type="button">로그인하러 가기</button>
              </a>
            `;
        }
    }
});
