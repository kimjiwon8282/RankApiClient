// /js/navbar.js
import { httpRequest } from './api.js';

document.addEventListener('DOMContentLoaded', async () => {
    const authArea = document.getElementById('navAuthArea');
    const greetingEl = document.getElementById('navGreeting');

    if (!authArea) {
        return;
    }

    let isAuthenticated = false;
    let nickname = null;

    try {
        const res = await httpRequest('GET', '/api/me', null, {
            redirectOnUnauthorized: false
        });

        if (res.ok) {
            const data = await res.json();
            nickname = data?.nickname || data?.name || null;
            isAuthenticated = true;
        }
    } catch (error) {
        console.warn('로그인 상태 확인 실패:', error);
        isAuthenticated = false;
    }

    if (greetingEl) {
        greetingEl.textContent =
            isAuthenticated && nickname ? `안녕하세요, ${nickname}님!` : '';
    }

    if (isAuthenticated) {
        authArea.innerHTML = `
            <a class="btn btn-outline" href="/my-history.html">히스토리</a>
            <button id="navbarLogoutButton" type="button" class="btn btn-primary">
                로그아웃
            </button>
        `;

        const logoutButton = document.getElementById('navbarLogoutButton');

        if (logoutButton) {
            logoutButton.addEventListener('click', async () => {
                try {
                    await fetch('/logout', {
                        method: 'POST',
                        credentials: 'include'
                    });
                } catch (error) {
                    console.error('로그아웃 요청 중 오류 발생:', error);
                } finally {
                    localStorage.removeItem('access_token');
                    window.location.href = '/login.html';
                }
            });
        }
    } else {
        authArea.innerHTML = `
            <a class="btn btn-primary" href="/login.html">로그인/가입</a>
        `;
    }
});