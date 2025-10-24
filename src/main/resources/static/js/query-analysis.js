// /js/query-analysis.js
import { httpRequest } from './api.js';

document.addEventListener('DOMContentLoaded', () => {
    const queryInput = document.getElementById('query');
    const analysisResultEl = document.getElementById('queryAnalysisResult');

    // 마지막으로 그렸던 데이터(리사이즈 대응)
    let lastChartData = null;
    let debounceTimer;

    // ─────────────────────────────────────────────────────────────────────────────
    // 유틸: 디바운스
    const debounce = (func, delay) => {
        return function (...args) {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => func.apply(this, args), delay);
        };
    };

    // ─────────────────────────────────────────────────────────────────────────────
    // 차트 그리기 유틸
    const clearCanvas = (id) => {
        const canvas = document.getElementById(id);
        if (!canvas) return;
        const dpr = window.devicePixelRatio || 1;
        const parentWidth = canvas.parentElement ? canvas.parentElement.clientWidth : canvas.clientWidth || 600;
        const baseHeight = Number(canvas.getAttribute('height') || 160);

        canvas.width = Math.max(320, parentWidth * dpr);
        canvas.height = baseHeight * dpr;

        const ctx = canvas.getContext('2d');
        ctx.setTransform(1, 0, 0, 1, 0, 0);
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        ctx.scale(dpr, dpr);
    };

    const clearCharts = () => {
        clearCanvas('qaMonthly');
        clearCanvas('qaWeekly');
    };

    const parseSeries = (items, labelFmt) => {
        if (!Array.isArray(items)) return [];
        const sorted = [...items].sort((a, b) => String(a.period).localeCompare(String(b.period)));
        return sorted.map(x => ({
            label: labelFmt(String(x.period)),
            value: Number(x.ratio) || 0
        }));
    };

    const drawLineChart = (canvasId, series, { color = '#2563eb' } = {}) => {
        const canvas = document.getElementById(canvasId);
        if (!canvas) return;

        // 리사이즈 및 초기화
        const dpr = window.devicePixelRatio || 1;
        const parentWidth = canvas.parentElement ? canvas.parentElement.clientWidth : canvas.clientWidth || 600;
        const baseHeight = Number(canvas.getAttribute('height') || 160);

        canvas.width = Math.max(320, parentWidth * dpr);
        canvas.height = baseHeight * dpr;

        const ctx = canvas.getContext('2d');
        ctx.setTransform(1, 0, 0, 1, 0, 0);
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        ctx.scale(dpr, dpr);

        const width = canvas.width / dpr;
        const height = canvas.height / dpr;

        const pad = { t: 14, r: 12, b: 28, l: 36 };
        const plotW = width - pad.l - pad.r;
        const plotH = height - pad.t - pad.b;

        const xs = series.map(s => s.label);
        const ys = series.map(s => s.value);

        if (xs.length === 0) {
            ctx.fillStyle = '#6b7280';
            ctx.font = '12px system-ui,-apple-system,Segoe UI,Roboto,sans-serif';
            ctx.fillText('데이터가 없습니다', pad.l, pad.t + 16);
            return;
        }

        const minY = Math.min(0, ...ys);
        const maxY = Math.max(100, ...ys); // ratio가 보통 0~100
        const yRange = maxY - minY || 1;

        // 그리드
        ctx.strokeStyle = '#e5e7eb';
        ctx.lineWidth = 1;
        ctx.beginPath();
        for (let g = 0; g <= 4; g++) {
            const y = pad.t + (plotH * g / 4);
            ctx.moveTo(pad.l, y);
            ctx.lineTo(width - pad.r, y);
        }
        ctx.stroke();

        // y라벨
        ctx.fillStyle = '#6b7280';
        ctx.font = '12px system-ui,-apple-system,Segoe UI,Roboto,sans-serif';
        for (let g = 0; g <= 4; g++) {
            const val = (maxY - yRange * g / 4);
            const y = pad.t + (plotH * g / 4) + 4;
            ctx.fillText(val.toFixed(0), 6, y);
        }

        // x라벨 (너무 많으면 간격 띄움)
        const step = Math.ceil(xs.length / 6) || 1;
        xs.forEach((lab, i) => {
            if (i % step !== 0) return;
            const x = pad.l + (plotW * (xs.length <= 1 ? 0 : i / (xs.length - 1)));
            ctx.fillText(lab, x - 8, height - 8);
        });

        // 라인
        ctx.strokeStyle = color;
        ctx.lineWidth = 2;
        ctx.beginPath();
        series.forEach((pt, i) => {
            const x = pad.l + (plotW * (series.length <= 1 ? 0 : i / (series.length - 1)));
            const y = pad.t + plotH * (1 - ((pt.value - minY) / yRange));
            if (i === 0) ctx.moveTo(x, y); else ctx.lineTo(x, y);
        });
        ctx.stroke();

        // 포인트
        ctx.fillStyle = color;
        series.forEach((pt, i) => {
            const x = pad.l + (plotW * (series.length <= 1 ? 0 : i / (series.length - 1)));
            const y = pad.t + plotH * (1 - ((pt.value - minY) / yRange));
            ctx.beginPath();
            ctx.arc(x, y, 3, 0, Math.PI * 2);
            ctx.fill();
        });
    };

    // ⬇ 기존 renderQueryCharts 를 이걸로 교체
    const renderQueryCharts = (data) => {
        if (!data) { clearCharts(); return; }

        // 응답 키가 대/소문자 섞여 올 수도 있으니 모두 대비
        const mRes = data.monthlyResponse ?? data.MonthlyResponse;
        const wRes = data.weeklyResponse ?? data.WeeklyResponse;

        // 실제 배열 위치: results[0].data
        const monthlyRaw = mRes?.results?.[0]?.data ?? [];
        const weeklyRaw  = wRes?.results?.[0]?.data ?? [];

        // 라벨 포맷터
        const fmtMonth = (p) => {
            // 'YYYY-MM-DD' 또는 'YYYYMMDD' -> 'YY.MM'
            if (/^\d{4}-\d{2}-\d{2}$/.test(p)) return `${p.slice(2,4)}.${p.slice(5,7)}`;
            if (/^\d{8}$/.test(p))            return `${p.slice(2,4)}.${p.slice(4,6)}`;
            if (/^\d{6}$/.test(p))            return `${p.slice(2,4)}.${p.slice(4,6)}`;
            return p;
        };
        const fmtWeek = (p) => {
            // 주간도 날짜 형태로 내려오므로 'MM.DD' 정도로 축약
            if (/^\d{4}-\d{2}-\d{2}$/.test(p)) return `${p.slice(5,7)}.${p.slice(8,10)}`;
            if (/^\d{8}$/.test(p))            return `${p.slice(4,6)}.${p.slice(6,8)}`;
            return p.replace('W', '-W');
        };

        const monthly = parseSeries(monthlyRaw, fmtMonth);
        const weekly  = parseSeries(weeklyRaw,  fmtWeek);

        drawLineChart('qaMonthly', monthly, { color: '#2563eb' });
        drawLineChart('qaWeekly',  weekly,  { color: '#10b981' });
    };

    // 리사이즈 시 다시 그리기
    window.addEventListener('resize', () => {
        if (lastChartData) renderQueryCharts(lastChartData);
    });

    // ─────────────────────────────────────────────────────────────────────────────
    // 데이터 로드
    const fetchAnalysisData = async (query) => {
        // ⬇⬇ 타이포 수정됨: !query || !query.trim()
        if (!query || !query.trim()) {
            analysisResultEl.textContent = ''; // 검색어 없으면 비움
            clearCharts();
            return;
        }

        analysisResultEl.textContent = '데이터 분석 중...';

        try {
            const url = `/naver/api/category-trend?query=${encodeURIComponent(query)}`;
            const res = await httpRequest('GET', url);

            if (!res.ok) {
                const errorText = await res.text();
                throw new Error(`API 요청 실패: ${res.status} ${errorText}`);
            }

            const data = await res.json();

            // JSON은 계속 pre에 보여주되, 그래프도 렌더링
            analysisResultEl.textContent = JSON.stringify(data, null, 2);
            renderQueryCharts(data);
            lastChartData = data;

        } catch (error) {
            console.error('분석 데이터를 가져오는 데 실패했습니다:', error);
            analysisResultEl.textContent = '분석 데이터를 불러오는 데 실패했습니다.';
            clearCharts();
        }
    };

    // 입력 디바운스 후 호출 (600ms)
    queryInput.addEventListener('input', debounce(e => {
        fetchAnalysisData(e.target.value);
    }, 600));

    // 초기 값이 있으면 즉시 실행
    if (queryInput.value) {
        fetchAnalysisData(queryInput.value);
    }
});