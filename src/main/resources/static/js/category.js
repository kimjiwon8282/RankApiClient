// /js/category.js
import { httpRequest } from './api.js';

const USE_VEGETABLE_ONLY = false;
// ↑ true로 바꾸면 식품>농산물>채소 계열만 노출(선택)

const CategoryCatalog = (() => {
    let tree = null; // { L1: { L2: { L3: [L4...] } } }

    function passFilter(r) {
        if (!USE_VEGETABLE_ONLY) return true;
        return r['대분류'] === '식품' && r['중분류'] === '농산물';
    }

    async function load() {
        if (tree) return tree;

        const rows = await fetch('/data/categoryData.json')
            .then(r => r.ok ? r.json() : [])
            .catch(() => []);

        const t = {};
        for (const r of rows) {
            if (!passFilter(r)) continue;

            const l1 = r['대분류'] || '';
            const l2 = r['중분류'] || '';
            const l3 = r['소분류'] || '';
            const l4 = r['세분류'] || '';
            if (!l1 || !l2 || !l3 || !l4) continue;

            t[l1] ??= {};
            t[l1][l2] ??= {};
            t[l1][l2][l3] ??= new Set();
            t[l1][l2][l3].add(l4);
        }
        // Set → Array
        for (const l1 of Object.keys(t)) {
            for (const l2 of Object.keys(t[l1])) {
                for (const l3 of Object.keys(t[l1][l2])) {
                    t[l1][l2][l3] = Array.from(t[l1][l2][l3]).sort();
                }
            }
        }
        tree = t;
        return tree;
    }

    function fillSelect(selectEl, options, placeholder='선택') {
        if (!selectEl) return;
        const prev = selectEl.value;
        selectEl.innerHTML = '';
        const opt0 = document.createElement('option');
        opt0.value = '';
        opt0.textContent = placeholder;
        selectEl.appendChild(opt0);
        for (const v of options) {
            const o = document.createElement('option');
            o.value = o.textContent = v;
            selectEl.appendChild(o);
        }
        if (prev && options.includes(prev)) selectEl.value = prev;
    }

    function applyPath(path = {}) {
        const sel1 = document.getElementById('category1');
        const sel2 = document.getElementById('category2');
        const sel3 = document.getElementById('category3');
        const sel4 = document.getElementById('category4');
        if (!tree || !sel1 || !sel2 || !sel3 || !sel4) return;

        const l1s = Object.keys(tree).sort();
        fillSelect(sel1, l1s);
        if (path.category1 && l1s.includes(path.category1)) sel1.value = path.category1;

        const l2s = sel1.value ? Object.keys(tree[sel1.value] || {}).sort() : [];
        fillSelect(sel2, l2s);
        if (path.category2 && l2s.includes(path.category2)) sel2.value = path.category2;

        const l3s = (sel1.value && sel2.value)
            ? Object.keys((tree[sel1.value] || {})[sel2.value] || {}).sort()
            : [];
        fillSelect(sel3, l3s);
        if (path.category3 && l3s.includes(path.category3)) sel3.value = path.category3;

        const l4s = (sel1.value && sel2.value && sel3.value)
            ? ((tree[sel1.value] || {})[sel2.value] || {})[sel3.value] || []
            : [];
        fillSelect(sel4, l4s);
        if (path.category4 && l4s.includes(path.category4)) sel4.value = path.category4;
    }

    function wireCascading() {
        const sel1 = document.getElementById('category1');
        const sel2 = document.getElementById('category2');
        const sel3 = document.getElementById('category3');
        const sel4 = document.getElementById('category4');

        sel1?.addEventListener('change', () => {
            const l2s = sel1.value ? Object.keys(tree[sel1.value] || {}).sort() : [];
            fillSelect(sel2, l2s);
            fillSelect(sel3, []);
            fillSelect(sel4, []);
        });
        sel2?.addEventListener('change', () => {
            const l3s = (sel1.value && sel2.value)
                ? Object.keys((tree[sel1.value] || {})[sel2.value] || {}).sort()
                : [];
            fillSelect(sel3, l3s);
            fillSelect(sel4, []);
        });
        sel3?.addEventListener('change', () => {
            const l4s = (sel1.value && sel2.value && sel3.value)
                ? ((tree[sel1.value] || {})[sel2.value] || {})[sel3.value] || []
                : [];
            fillSelect(sel4, l4s);
        });
    }

    return { load, applyPath, wireCascading };
})();

// === 초기화 & 자동 채움 ===
document.addEventListener('DOMContentLoaded', async () => {
    const $ = (id) => document.getElementById(id);
    const aiStatus = $('aiStatus');
    const queryInput = $('query');

    await CategoryCatalog.load();
    CategoryCatalog.wireCascading();
    CategoryCatalog.applyPath({});

    let tId = null;
    const debounce = (fn, ms=400) => (...args) => {
        if (tId) clearTimeout(tId);
        tId = setTimeout(() => fn(...args), ms);
    };

    async function autoFillByQuery(q) {
        if (!q || !q.trim()) return;
        try {
            if (aiStatus) aiStatus.textContent = '카테고리 탐색 중…';
            const url = `/api/categories/suggest?query=${encodeURIComponent(q)}&topN=10`;
            const res = await httpRequest('GET', url);
            if (!res.ok) {
                if (aiStatus) aiStatus.textContent = `카테고리 탐색 실패(${res.status})`;
                return;
            }
            const data = await res.json(); // { source, callAt, recommended }
            if (data?.recommended) {
                CategoryCatalog.applyPath(data.recommended);
                if (aiStatus) {
                    const label = data.source === 'rank1' ? 'rank#1'
                        : data.source === 'majority' ? '최빈'
                            : '추천없음';
                    aiStatus.textContent = `카테고리 자동 채움 (${label})`;
                }
            } else {
                if (aiStatus) aiStatus.textContent = '카테고리 추천 없음';
            }
        } catch (e) {
            console.error(e);
            if (aiStatus) aiStatus.textContent = '카테고리 탐색 오류';
        }
    }

    queryInput?.addEventListener('input', debounce(e => {
        autoFillByQuery(e.target.value);
    }, 400));

    if (queryInput?.value) autoFillByQuery(queryInput.value);
});
