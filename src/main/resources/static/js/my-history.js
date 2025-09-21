import { httpRequest } from './api.js';

document.addEventListener('DOMContentLoaded', async () => {
    const $ = (id) => document.getElementById(id);

    const statusEl = $('status');
    const greetingEl = $('greeting');
    const historyListEl = $('historyList');

    statusEl.textContent = '히스토리를 불러오는 중...';

    try {
        const res = await httpRequest('GET', '/ai/histories');

        if (!res.ok) {
            // 토큰 재발급 후에도 실패했다면 로그인 페이지로 리다이렉트
            window.location.href = '/login';
            return;
        }

        const data = await res.json();
        const nickname = data.nickname;
        const histories = data.histories || [];

        greetingEl.textContent = `${nickname}님의 히스토리 목록입니다.`;
        statusEl.textContent = '';

        // 히스토리 목록을 화면에 렌더링
        historyListEl.innerHTML = ''; // 기존 목록 초기화
        if (histories.length > 0) {
            histories.forEach(item => {
                const listItem = document.createElement('li');
                listItem.style.border = '1px solid #ccc';
                listItem.style.padding = '10px';
                listItem.style.marginBottom = '10px';
                listItem.innerHTML = `
                    <strong>쿼리:</strong> ${item.query || '없음'}<br>
                    <strong>제목:</strong> ${item.title || '없음'}<br>
                    <strong>예측 랭킹:</strong> ${item.predRankClipped || '없음'}<br>
                    <strong>저장일:</strong> ${item.createdAt ? new Date(item.createdAt).toLocaleString() : '없음'}<br>
                `;
                historyListEl.appendChild(listItem);
            });
        } else {
            historyListEl.innerHTML = '<li>아직 저장된 히스토리가 없습니다.</li>';
        }

    } catch (e) {
        console.error('Failed to load history:', e);
        statusEl.textContent = '히스토리를 불러오는데 실패했습니다. 로그인 상태를 확인해주세요.';
        window.location.href = '/login';
    }
});