import { httpRequest } from './api.js';

document.addEventListener('DOMContentLoaded', () => {
    const $ = (id) => document.getElementById(id);

    // 예측 요소
    const btnPredict = $('btnPredict');
    const aiStatus = $('aiStatus');
    const predRank = $('predRank');
    const predRankClipped = $('predRankClipped');
    const respQuery = $('respQuery');
    const respTitle = $('respTitle');
    const expId = $('expId');

    // 저장 요소
    const btnSave = $('btnSave');
    const saveStatus = $('saveStatus');

    // 마지막 요청/결과 상태
    const state = { lastReq: null, lastPred: null };

    // ------- 예측 -------
    btnPredict?.addEventListener('click', async () => {
        aiStatus.textContent = '요청 중…';

        const lastReq = {
            query: $('query').value,
            title: $('title').value,
            lprice: parseInt($('lprice').value) || 0,
            hprice: parseInt($('hprice').value) || 0,
            mallName: $('mallName').value,
            brand: $('brand').value,
            maker: $('maker').value,
            productId: $('productId').value,
            productType: $('productType').value,
            category1: $('category1').value,
            category2: $('category2').value,
            category3: $('category3').value,
            category4: $('category4').value
        };

        try {
            const res = await httpRequest('POST', '/ai/predict', { items: [lastReq], clip_to_range: true });
            if (!res.ok) {
                aiStatus.textContent = `실패 (${res.status})`;
                state.lastReq = null;
                state.lastPred = null;
                return;
            }

            const data = await res.json();
            aiStatus.textContent = '성공';

            const r = (data.results && data.results[0]) || null;
            state.lastReq = lastReq;
            state.lastPred = r;

            // 화면 반영
            predRank.textContent = r?.pred_rank ?? '';
            predRankClipped.textContent = r?.pred_rank_clipped ?? '';
            respQuery.textContent = r?.query ?? '';
            respTitle.textContent = r?.title ?? '';
            expId.textContent = r?.exp_id ?? '';
        } catch (e) {
            aiStatus.textContent = '요청 오류';
            state.lastReq = null;
            state.lastPred = null;
            predRank.textContent = '';
            predRankClipped.textContent = '';
            respQuery.textContent = '';
            respTitle.textContent = '';
            expId.textContent = '';
            console.error(e);
        }
    });

    // ------- 히스토리 저장 -------
    btnSave?.addEventListener('click', async () => {
        const accessToken = localStorage.getItem('access_token');
        if (!accessToken) {
            window.location.href = '/login';
            return;
        }

        if (!state.lastReq || !state.lastPred) {
            saveStatus.textContent = '저장할 예측 결과가 없습니다.';
            return;
        }

        saveStatus.textContent = '저장 중...';

        const savePayload = {
            ...state.lastReq,
            predRank: state.lastPred.pred_rank,
            predRankClipped: state.lastPred.pred_rank_clipped
        };

        try {
            const res = await httpRequest('POST', '/ai/save', savePayload);

            if (res.ok) {
                saveStatus.textContent = '저장 성공!';
            } else {
                saveStatus.textContent = `저장 실패: ${res.statusText}`;
            }
        } catch (e) {
            console.error(e);
            saveStatus.textContent = '저장 중 오류가 발생했습니다.';
        }
    });
});