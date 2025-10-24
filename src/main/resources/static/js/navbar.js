import { httpRequest } from './api.js';

document.addEventListener('DOMContentLoaded', async () => {
    const authArea = document.getElementById('navAuthArea');
    const greetingEl = document.getElementById('navGreeting');
    if (!authArea) return;

    let isAuthenticated = false;
    let nickname = null;

    try {
        // /api/me 호출로 인증 상태 확인
        const res = await httpRequest('GET', '/api/me');
        if (!res.ok) throw new Error('not authenticated');
        const data = await res.json();
        nickname = data?.nickname || data?.name || null;
        isAuthenticated = true;
    } catch (_) {
        isAuthenticated = false;
    }

    // 헤더 인사말(선택)
    if (greetingEl) {
        greetingEl.textContent = isAuthenticated && nickname ? `안녕하세요, ${nickname}님!` : '';
    }

    // 버튼 렌더링
    if (isAuthenticated) {
        authArea.innerHTML = `
      <a class="btn btn-outline" href="/my-history">히스토리</a>
      <form id="navbarLogoutForm" action="/logout" method="post" class="logout-form">
        <button type="submit" class="btn btn-primary">로그아웃</button>
      </form>
    `;

        // 로그아웃 시 토큰 제거
        const logoutForm = document.getElementById('navbarLogoutForm');
        if (logoutForm) {
            logoutForm.addEventListener('submit', () => {
                try { localStorage.removeItem('access_token'); } catch (e) {}
            });
        }
    } else {
        authArea.innerHTML = `
      <a class="btn btn-primary" href="/login">로그인/가입</a>
    `;
    }
});