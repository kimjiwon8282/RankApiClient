import { httpRequest } from './api.js';

document.addEventListener('DOMContentLoaded', async () => {
    const greetingEl = document.getElementById('greeting');
    const authBtnArea = document.getElementById('authBtnArea');
    const historyBtnArea = document.getElementById('historyBtnArea');
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

            // '내 히스토리 보기' 버튼 동적 생성
            historyBtnArea.innerHTML = `
                <a href="/my-history">
                    <button type="button">내 히스토리 보기</button>
                </a>
            `;
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