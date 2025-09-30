// /js/keyword-recommend.js
import { httpRequest } from './api.js';

document.addEventListener('DOMContentLoaded', () => {
    // 필요한 HTML 요소들을 가져옵니다.
    const queryInput = document.getElementById('query');
    const recommendListEl = document.getElementById('keywordRecommendList');

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

    // 추천 키워드를 서버에서 가져오는 비동기 함수입니다.
    const fetchRecommendedKeywords = async (query) => {
        // 검색어가 비어있으면 목록을 지웁니다.
        if (!query || !query.trim()) {
            recommendListEl.innerHTML = '';
            return;
        }

        try {
            const url = `/naver/api/keyword/recommend?hint=${encodeURIComponent(query)}`;
            const res = await httpRequest('GET', url);

            if (!res.ok) throw new Error(`API 요청 실패`);

            const data = await res.json();
            const keywords = data.recommended || [];

            recommendListEl.innerHTML = ''; // 이전 결과를 지웁니다.

            if (keywords.length > 0) {
                keywords.forEach(keyword => {
                    // 클릭 기능이 없으므로 단순한 div 태그로 텍스트만 표시합니다.
                    const item = document.createElement('div');
                    item.className = 'keyword-item'; // CSS 스타일링을 위한 클래스
                    item.textContent = keyword;
                    recommendListEl.appendChild(item);
                });
            }

        } catch (error) {
            console.error('추천 키워드를 가져오는 데 실패했습니다:', error);
            recommendListEl.innerHTML = ''; // 오류 발생 시 목록을 비웁니다.
        }
    };

    // 검색어 입력창에 디바운스가 적용된 이벤트 리스너를 추가합니다.
    queryInput.addEventListener('input', debounce(e => fetchRecommendedKeywords(e.target.value), 500));

    // 페이지가 처음 로드될 때 검색어 입력창에 이미 값이 있다면 바로 실행합니다.
    if (queryInput.value) {
        fetchRecommendedKeywords(queryInput.value);
    }
});