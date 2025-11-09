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

    /* ---------- 유틸: 숫자 체크/포맷 ---------- */
    const isNumeric = (v) => v !== null && v !== undefined && v !== '' && !isNaN(Number(String(v).replace(/,/g, '')));
    const toNum = (v) => (isNumeric(v) ? Number(String(v).replace(/,/g, '')) : null);
    const fmt = (v) => (isNumeric(v) ? toNum(v).toLocaleString('ko-KR') : (v ?? ''));

    /* ---------- 경쟁도(원시값) -> {cls,label,icon} 변환 로직 ---------- */
    function normalizeCompetition(raw) {
        const s = (raw ?? '').toString().trim();

        // 텍스트 기반 판정 (한국어/영어)
        if (/^(높음|강|상|high|strong)/i.test(s)) return { cls: 'comp-high', label: s || '높음', icon: '🔥' };
        if (/^(낮음|약|하|low|weak)/i.test(s)) return { cls: 'comp-low', label: s || '낮음', icon: '✅' };
        if (/^(중간|보통|medium|mid)/i.test(s)) return { cls: 'comp-medium', label: s || '중간', icon: '⚖️' };

        // 숫자 기반 판정
        if (isNumeric(s)) {
            let v = Number(s);
            // v가 0~1 범위(비율)로 오면 0~100으로 변환
            if (v > 0 && v <= 1) v = v * 100;
            // 임계값 (임의 설정: 70이상=높음, 40이상=중간, 나머지=낮음)
            if (v >= 70) return { cls: 'comp-high', label: `${Math.round(v)}%`, icon: '🔥' };
            if (v >= 40) return { cls: 'comp-medium', label: `${Math.round(v)}%`, icon: '⚠️' };
            return { cls: 'comp-low', label: `${Math.round(v)}%`, icon: '✅' };
        }

        // 기본값: 중간
        return { cls: 'comp-medium', label: s || '중간', icon: '⚖️' };
    }

    /* ---------- 테이블 렌더링 ---------- */
    const renderEmpty = (message = '데이터가 없습니다.') => {
        tbody.innerHTML = `<tr class="analysis-placeholder-row"><td colspan="8" class="analysis-placeholder-cell" style="padding:18px 8px; color:var(--ka-muted);">${message}</td></tr>`;
    };

    const renderTable = (data) => {
        const rows = (data && Array.isArray(data.relatedKeywords)) ? data.relatedKeywords : [];

        if (!rows.length) {
            renderEmpty('데이터가 없습니다.');
            return;
        }

        // build rows using class-based markup (CSS에서 스타일링)
        const html = rows.map((r) => {
            const pc = toNum(r.monthlyPcQcCnt);
            const mo = toNum(r.monthlyMobileQcCnt);
            const total = (pc !== null && mo !== null) ? (pc + mo) : (r.monthlyTotalQcCnt ?? '');

            const avePcClk = isNumeric(r.monthlyAvePcClkCnt) ? Number(r.monthlyAvePcClkCnt).toFixed(1) : (r.monthlyAvePcClkCnt ?? '');
            const aveMoClk = isNumeric(r.monthlyAveMobileClkCnt) ? Number(r.monthlyAveMobileClkCnt).toFixed(1) : (r.monthlyAveMobileClkCnt ?? '');

            // CTR: 서버에서 0~1 소수로 줄 수도 있으니 처리
            let avePcCtr = r.monthlyAvePcCtr ?? '';
            if (isNumeric(avePcCtr)) {
                const v = Number(avePcCtr);
                avePcCtr = (v > 0 && v <= 1) ? (v * 100).toFixed(2) + '%' : (v.toFixed ? v.toFixed(2) + '%' : String(v));
            }
            let aveMoCtr = r.monthlyAveMobileCtr ?? '';
            if (isNumeric(aveMoCtr)) {
                const v = Number(aveMoCtr);
                aveMoCtr = (v > 0 && v <= 1) ? (v * 100).toFixed(2) + '%' : (v.toFixed ? v.toFixed(2) + '%' : String(v));
            }

            const depth = r.plAvgDepth ?? (r.pageDepth ?? '') ?? '';
            const compRaw = r.compIdx ?? r.competition ?? r.comp ?? '';

            const comp = normalizeCompetition(compRaw);

            return `
      <tr>
        <td class="col-keyword">${escapeHtml(r.relKeyword ?? r.keyword ?? '')}</td>
        <td class="col-month num">${fmt(total)}</td>
        <td class="col-pc num">${fmt(pc)}</td>
        <td class="col-mobile num">${fmt(mo)}</td>
        <td class="col-click num">${fmt(avePcClk)}${avePcClk || aveMoClk ? ' / ' + fmt(aveMoClk) : ''}</td>
        <td class="col-ctr num">${escapeHtml(avePcCtr)}${(avePcCtr || aveMoCtr) ? ' / ' + escapeHtml(aveMoCtr) : ''}</td>
        <td class="col-depth">${escapeHtml(depth)}</td>
        <td class="col-compet competition" data-competition="${escapeHtml(String(comp.label))}">${makeBadgeHtml(comp)}</td>
      </tr>
      `;
        }).join('');

        tbody.innerHTML = html;
    };

    /* ---------- 안전한 HTML 이스케이프(간단) ---------- */
    function escapeHtml(str) {
        if (str === null || str === undefined) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    /* ---------- 배지 HTML 생성 ---------- */
    function makeBadgeHtml({ cls = 'comp-medium', label = '중간', icon = '⚖️' } = {}) {
        // label, icon은 이미 이스케이프 처리되어야 함 (호출 시 escapeHtml 적용)
        // 클래스는 CSS에서 스타일링
        return `<span class="badge ${cls}"><span class="icon">${escapeHtml(icon)}</span><span class="label">${escapeHtml(label)}</span></span>`;
    }

    /* ---------- API 호출 ---------- */
    const fetchKeywordAnalysis = async (query) => {
        if (!query || !query.trim()) {
            analysisStatusEl.textContent = '';
            tbody.innerHTML = `<tr><td colspan="8" style="padding:18px 8px; color:var(--ka-muted);">분석할 키워드를 입력해주세요.</td></tr>`;
            return;
        }

        analysisStatusEl.textContent = '데이터 조회 중...';

        try {
            const url = `/naver/api/keyword/analysis?query=${encodeURIComponent(query)}`;
            const res = await httpRequest('GET', url);

            if (!res || !res.ok) {
                const status = res?.status ?? 'ERR';
                analysisStatusEl.textContent = `실패 (${status})`;
                tbody.innerHTML = `<tr><td colspan="8" style="padding:18px 8px; color:#ef4444;">'${escapeHtml(query)}' 데이터 조회 실패</td></tr>`;
                return;
            }

            // httpRequest의 반환이 fetch Response와 유사하다고 가정 (기존 코드와 호환)
            const data = await res.json();
            renderTable(data);
            if (analysisResultEl) {
                analysisResultEl.textContent = ''; // 디버그 숨김
                analysisResultEl.style.display = 'none';
            }
            analysisStatusEl.textContent = '조회 완료';
        } catch (err) {
            console.error(err);
            analysisStatusEl.textContent = '오류 발생';
            tbody.innerHTML = `<tr><td colspan="8" style="padding:18px 8px; color:#ef4444;">요청 중 오류가 발생했습니다.</td></tr>`;
            if (analysisResultEl) {
                analysisResultEl.style.display = 'block';
                analysisResultEl.textContent = String(err);
            }
        }
    };

    /* ---------- 입력 이벤트 바인딩 (디바운스 500ms) ---------- */
    queryInput.addEventListener('input', debounce((e) => {
        fetchKeywordAnalysis(e.target.value);
    }, 500));

    /* ---------- 초기 URL 파라미터 q 처리 ---------- */
    const urlParams = new URLSearchParams(window.location.search);
    const initialQuery = urlParams.get('q');
    if (initialQuery) {
        queryInput.value = initialQuery;
        fetchKeywordAnalysis(initialQuery);
    } else {
        // 초기 안내 문구
        tbody.innerHTML = `<tr><td colspan="8" style="padding:18px 8px; color:var(--ka-muted);">분석할 키워드를 입력해주세요.</td></tr>`;
    }
});
