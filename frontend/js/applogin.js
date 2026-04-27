// /js/applogin.js

document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');

    if (!loginForm) {
        return;
    }

    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;

        if (!email || !password) {
            alert('이메일과 비밀번호를 입력해주세요.');
            return;
        }

        try {
            const response = await fetch('/api/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({ email, password })
            });

            if (!response.ok) {
                alert('이메일 또는 비밀번호가 일치하지 않습니다.');
                return;
            }

            const data = await response.json();

            if (!data.accessToken) {
                alert('로그인 응답에 accessToken이 없습니다.');
                return;
            }

            localStorage.setItem('access_token', data.accessToken);
            window.location.href = '/home.html';
        } catch (error) {
            console.error('로그인 요청 중 오류 발생:', error);
            alert('로그인 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
        }
    });
});