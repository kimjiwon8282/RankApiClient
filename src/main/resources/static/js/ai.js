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
            query:       $('query').value,
            title:       $('title').value,
            lprice:      parseInt($('lprice').value) || 0,
            hprice:      parseInt($('hprice').value) || 0,
            mallName:    $('mallName').value,
            brand:       $('brand').value,
            maker:       $('maker').value,
            productId:   $('productId').value,
            productType: $('productType').value, // 문자열 유지
            category1:   $('category1').value,
            category2:   $('category2').value,
            category3:   $('category3').value,
            category4:   $('category4').value
        };

        try {
            const res = await fetch('/ai/predict', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ items: [lastReq], clip_to_range: true })
            });
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
});
