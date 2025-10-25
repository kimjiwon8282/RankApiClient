// /js/keyword-analysis.js
import { httpRequest } from './api.js';

document.addEventListener('DOMContentLoaded', () => {
    const queryInput = document.getElementById('query');
    const analysisStatusEl = document.getElementById('analysisStatus');
    const analysisResultEl = document.getElementById('analysisResult'); // 숨김(pre) – 에러/디버그만 사용
    const tbody = document.getElementById('analysisTbody');

    let debounceTimer;

    const debounce = (func, delay) => {
        return function (...args) {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => func.apply(this, args), delay);
        };
    };

    // 숫자 포맷터
    const isNumeric = (v) => v !== null && v !== undefined && v !== '' && !isNaN(Number(String(v).replace(/,/g, '')));
    const toNum = (v) => (isNumeric(v) ? Number(String(v).replace(/,/g, '')) : null);
    const fmt = (v) => (isNumeric(v) ? toNum(v).toLocaleString() : (v ?? ''));

    // 테이블 렌더
    const renderTable = (data) => {
        const rows = (data && Array.isArray(data.relatedKeywords)) ? data.relatedKeywords : [];

        if (!rows.length) {
            tbody.innerHTML = `
        <tr><td colspan="8" style="padding:18px 8px; color:#6b7280;">데이터가 없습니다.</td></tr>
      `;
            return;
        }

        const trHtml = rows.map((r) => {
            const pc = toNum(r.monthlyPcQcCnt);
            const mo = toNum(r.monthlyMobileQcCnt);
            const total = (pc !== null && mo !== null) ? (pc + mo) : '';

            const avePcClk = isNumeric(r.monthlyAvePcClkCnt) ? Number(r.monthlyAvePcClkCnt).toFixed(1) : (r.monthlyAvePcClkCnt ?? '');
            const aveMoClk = isNumeric(r.monthlyAveMobileClkCnt) ? Number(r.monthlyAveMobileClkCnt).toFixed(1) : (r.monthlyAveMobileClkCnt ?? '');

            const avePcCtr = isNumeric(r.monthlyAvePcCtr) ? (Number(r.monthlyAvePcCtr) * 100).toFixed(2) + '%' : (r.monthlyAvePcCtr ?? '');
            const aveMoCtr = isNumeric(r.monthlyAveMobileCtr) ? (Number(r.monthlyAveMobileCtr) * 100).toFixed(2) + '%' : (r.monthlyAveMobileCtr ?? '');

            const depth = r.plAvgDepth ?? '';
            const comp = r.compIdx ?? '';

            return `
        <tr style="border-bottom:1px solid #f1f5f9;">
          <td style="padding:10px 20px;">${r.relKeyword ?? ''}</td>
          <td style="padding:10px 20px; font-variant-numeric: tabular-nums;">${fmt(total)}</td>
          <td style="padding:10px 20px; color:#475569; font-variant-numeric: tabular-nums;">${fmt(pc)}</td>
          <td style="padding:10px 20px; color:#475569; font-variant-numeric: tabular-nums;">${fmt(mo)}</td>
          <td style="padding:10px 20px; font-variant-numeric: tabular-nums;">${avePcClk} / ${aveMoClk}</td>
          <td style="padding:10px 20px; font-variant-numeric: tabular-nums;">${avePcCtr} / ${aveMoCtr}</td>
          <td style="padding:10px 20px;">${depth}</td>
          <td style="padding:10px 20px;">${comp}</td>
        </tr>
      `;
        }).join('');

        tbody.innerHTML = trHtml;
    };

    // API
    const fetchKeywordAnalysis = async (query) => {
        if (!query || !query.trim()) {
            analysisStatusEl.textContent = '';
            tbody.innerHTML = `<tr><td colspan="8" style="padding:18px 8px; color:#6b7280;">분석할 키워드를 입력해주세요.</td></tr>`;
            return;
        }

        analysisStatusEl.textContent = '데이터 조회 중...';

        try {
            const url = `/naver/api/keyword/analysis?query=${encodeURIComponent(query)}`;
            const res = await httpRequest('GET', url);

            if (!res.ok) {
                analysisStatusEl.textContent = `실패 (${res.status})`;
                tbody.innerHTML = `<tr><td colspan="8" style="padding:18px 8px; color:#ef4444;">'${query}' 데이터 조회 실패</td></tr>`;
                return;
            }

            const data = await res.json();
            renderTable(data);
            analysisResultEl.textContent = ''; // 디버그 숨김
            analysisStatusEl.textContent = '조회 완료';
        } catch (err) {
            console.error(err);
            analysisStatusEl.textContent = '오류 발생';
            tbody.innerHTML = `<tr><td colspan="8" style="padding:18px 8px; color:#ef4444;">요청 중 오류가 발생했습니다.</td></tr>`;
            analysisResultEl.textContent = String(err);
        }
    };

    // 입력 이벤트(디바운스)
    queryInput.addEventListener('input', debounce((e) => {
        fetchKeywordAnalysis(e.target.value);
    }, 500));

    // URL 파라미터로 초기 검색
    const urlParams = new URLSearchParams(window.location.search);
    const initialQuery = urlParams.get('q');
    if (initialQuery) {
        queryInput.value = initialQuery;
        fetchKeywordAnalysis(initialQuery);
    }
});