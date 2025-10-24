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
                const keywordContainer = document.getElementById('keywordRecommendList');
                keywordContainer.innerHTML = '';

                // 클릭 시 상품명에 추가하는 기능 ON/OFF
                const CLICK_TO_APPEND = true; // true 로 바꾸면 칩 클릭 시 #title 에 키워드가 추가됩니다.

                keywords.forEach((keyword) => {
                    // 기본: 클릭되지 않는 칩(span)
                    const chip = document.createElement('span');
                    chip.className = 'keyword-chip';
                    chip.textContent = keyword;

                    if (CLICK_TO_APPEND) {
                        chip.tabIndex = 0;           // 접근성
                        chip.role = 'button';
                        chip.classList.add('is-clickable');
                        chip.addEventListener('click', () => {
                            const titleInput = document.getElementById('title');
                            if (!titleInput) return;

                            const current = (titleInput.value || '').trim();
                            // 이미 포함되어 있으면 중복으로 붙이지 않음
                            const exists = new RegExp(`(^|\\s)${keyword}(\\s|$)`).test(current);
                            const next = exists ? current : (current ? `${current} ${keyword}` : keyword);
                            titleInput.value = next;
                            titleInput.dispatchEvent(new Event('input'));
                        });
                    }

                    keywordContainer.appendChild(chip);
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