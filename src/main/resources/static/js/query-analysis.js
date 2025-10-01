// /js/query-analysis.js
import { httpRequest } from './api.js';

document.addEventListener('DOMContentLoaded', () => {
    const queryInput = document.getElementById('query');
    const analysisResultEl = document.getElementById('queryAnalysisResult');

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

    // 쇼핑 인사이트 데이터를 서버에서 가져오는 비동기 함수입니다.
    const fetchAnalysisData = async (query) => {
        if (!query || !query.trim()) {
            analysisResultEl.textContent = ''; // 검색어가 없으면 결과를 비웁니다.
            return;
        }

        analysisResultEl.textContent = '데이터 분석 중...';

        try {
            // 참고: 이 API는 새로 만들어야 하는 백엔드 엔드포인트입니다.
            const url = `/naver/api/category-trend?query=${encodeURIComponent(query)}`;
            const res = await httpRequest('GET', url);

            if (!res.ok) {
                // API에서 에러 응답 시 (예: 404, 500)
                const errorText = await res.text();
                throw new Error(`API 요청 실패: ${res.status} ${errorText}`);
            }

            const data = await res.json();

            // 받은 JSON 객체를 보기 좋게 문자열로 변환하여 <pre> 태그에 넣습니다.
            analysisResultEl.textContent = JSON.stringify(data, null, 2);

        } catch (error) {
            console.error('분석 데이터를 가져오는 데 실패했습니다:', error);
            analysisResultEl.textContent = '분석 데이터를 불러오는 데 실패했습니다.';
        }
    };

    // 검색어 입력창에 디바운스가 적용된 이벤트 리스너를 추가합니다.
    // 600ms 동안 추가 입력이 없으면 API를 호출합니다.
    queryInput.addEventListener('input', debounce(e => {
        fetchAnalysisData(e.target.value);
    }, 600));

    // 페이지가 처음 로드될 때 검색어 입력창에 이미 값이 있다면 바로 실행합니다.
    if (queryInput.value) {
        fetchAnalysisData(queryInput.value);
    }
});