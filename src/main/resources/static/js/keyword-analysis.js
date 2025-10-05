// /js/keyword-analysis.js
import { httpRequest } from './api.js';

document.addEventListener('DOMContentLoaded', () => {
    const queryInput = document.getElementById('query');
    const analysisResultEl = document.getElementById('analysisResult');
    const analysisStatusEl = document.getElementById('analysisStatus');

    let debounceTimer;

    // API 호출을 지연시키는 디바운스 함수입니다.
    const debounce = (func, delay) => {
        return function(...args) {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                func.apply(this, args);
            }, delay);
        };
    };

    // 키워드 분석 데이터를 서버에서 가져오는 비동기 함수입니다.
    const fetchKeywordAnalysis = async (query) => {
        if (!query || !query.trim()) {
            analysisResultEl.textContent = '분석할 키워드를 입력해주세요.';
            analysisStatusEl.textContent = '';
            return;
        }

        analysisStatusEl.textContent = '데이터 조회 중...';

        try {
            // 참고: 이 API는 새로 만들어야 하는 백엔드 엔드포인트입니다.
            const url = `/naver/api/keyword/analysis?query=${encodeURIComponent(query)}`;
            const res = await httpRequest('GET', url);

            if (!res.ok) {
                analysisResultEl.textContent = `'${query}'에 대한 분석 데이터를 찾을 수 없습니다. (오류 코드: ${res.status})`;
                throw new Error(`API 요청 실패`);
            }

            const data = await res.json();

            // 받은 JSON 객체를 보기 좋게 문자열로 변환하여 <pre> 태그에 넣습니다.
            analysisResultEl.textContent = JSON.stringify(data, null, 2);
            analysisStatusEl.textContent = '조회 완료';

        } catch (error) {
            console.error('키워드 분석 데이터를 가져오는 데 실패했습니다:', error);
            analysisStatusEl.textContent = '오류 발생';
        }
    };

    // 검색어 입력창에 디바운스가 적용된 이벤트 리스너를 추가합니다.
    queryInput.addEventListener('input', debounce(e => {
        fetchKeywordAnalysis(e.target.value);
    }, 500));

    // 페이지 로딩 시 URL에 쿼리 파라미터가 있으면 바로 검색 실행 (예: /keyword-analysis?q=가지)
    const urlParams = new URLSearchParams(window.location.search);
    const initialQuery = urlParams.get('q');
    if (initialQuery) {
        queryInput.value = initialQuery;
        fetchKeywordAnalysis(initialQuery);
    }
});