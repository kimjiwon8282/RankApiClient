import { httpRequest } from './api.js';

document.addEventListener('DOMContentLoaded', async () => {
    const $ = (id) => document.getElementById(id);

    const statusEl    = $('status');
    const greetingEl  = $('greeting');
    // 기존 변수명 유지: historyListEl → 이제 테이블의 <tbody id="historyList">
    const historyListEl = $('historyList');

    statusEl.textContent = '히스토리를 불러오는 중...';

    try {
        const res = await httpRequest('GET', '/ai/histories');

        if (!res.ok) {
            // 토큰 재발급 후에도 실패했다면 로그인 페이지로 이동
            window.location.href = '/login';
            return;
        }

        const data = await res.json();
        const nickname  = data.nickname;
        const histories = data.histories || [];

        greetingEl.textContent = `${nickname}님의 히스토리 목록입니다.`;
        statusEl.textContent = '';

        // 표 본문 초기화
        historyListEl.innerHTML = '';

        if (histories.length === 0) {
            historyListEl.innerHTML = `
        <tr>
          <td colspan="4" style="text-align:center; color:#6b7280; padding:24px;">
            아직 저장된 히스토리가 없습니다.
          </td>
        </tr>`;
            return;
        }

        // 데이터 렌더링
        const rows = histories.map(toRowHtml).join('');
        historyListEl.innerHTML = rows;

    } catch (e) {
        console.error('Failed to load history:', e);
        statusEl.textContent = '히스토리를 불러오는데 실패했습니다. 로그인 상태를 확인해주세요.';
        window.location.href = '/login';
    }
});

/* ---------- helpers ---------- */

function toRowHtml(item){
    const query   = escapeHtml(item.query || '없음');
    const title   = escapeHtml(item.title || '제목 없음');
    const rankNum = item.predRankClipped != null ? Math.round(Number(item.predRankClipped)) : null;
    const rankCls = rankNum != null && rankNum <= 10 ? 'rank-badge top10' : 'rank-badge';
    const dateTxt = formatDate(item.createdAt);

    return `
    <tr>
      <td class="ellipsis" title="${query}">${query}</td>
      <td class="ellipsis" title="${title}">${title}</td>
      <td><span class="${rankCls}">${rankNum ?? '-'}</span></td>
      <td>${dateTxt}</td>
    </tr>
  `;
}

function escapeHtml(str){
    return String(str)
        .replaceAll('&','&amp;')
        .replaceAll('<','&lt;')
        .replaceAll('>','&gt;')
        .replaceAll('"','&quot;')
        .replaceAll("'","&#39;");
}
function formatDate(iso){
    if (!iso) return '-';
    try{
        const d = new Date(iso);
        return d.toLocaleString();
    }catch(_){ return '-';}
}