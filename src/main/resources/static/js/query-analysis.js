// /js/query-analysis.js
import { httpRequest } from './api.js';

document.addEventListener('DOMContentLoaded', () => {
    const queryInput = document.getElementById('query');
    const analysisResultEl = document.getElementById('queryAnalysisResult');

    // 마지막으로 그렸던 전체 응답 (리사이즈 재렌더 용)
    let lastChartData = null;
    let debounceTimer;

    // ─────────────────────────────────────────────────────────────
    // 디바운스 유틸
    const debounce = (func, delay) => {
        return function (...args) {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => func.apply(this, args), delay);
        };
    };

    // ─────────────────────────────────────────────────────────────
    // Chart.js 차트 인스턴스 전역 보관
    let monthlyChartInstance = null;
    let weeklyChartInstance = null;

    // Chart.js config 빌더
    const buildLineChartConfig = (labels, dataValues, datasetLabelText) => {
        return {
            type: 'line',
            data: {
                labels: labels, // 예: ['10.01', '10.08', ...]
                datasets: [
                    {
                        label: datasetLabelText,
                        data: dataValues, // 예: [32, 45, 50, ...]
                        fill: false,
                        tension: 0.3,
                        pointRadius: 3,
                        borderWidth: 2,
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false, // canvas height 기준으로 맞추기
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: (value) => value.toString()
                        },
                        grid: {
                            color: '#e5e7eb'
                        }
                    },
                    x: {
                        ticks: {
                            maxRotation: 0,
                            autoSkip: true,
                            maxTicksLimit: 6,
                        },
                        grid: {
                            color: '#f3f4f6'
                        }
                    }
                },
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false,
                        callbacks: {
                            // 날짜(타이틀) → 작게, 회색으로 표시
                            title: function (tooltipItems) {
                                const date = tooltipItems[0].label;
                                return `날짜: ${date}`;
                            },
                            // 값(라벨) → 크게, 단위 % 붙이기
                            label: function (tooltipItem) {
                                const value = tooltipItem.formattedValue;
                                return `검색 비율: ${value}%`;
                            }
                        },
                        // 시각적 스타일 커스터마이징
                        titleColor: '#9ca3af',      // 회색(기본 Tailwind gray-400)
                        titleFont: { size: 10, weight: 'normal' },
                        bodyColor: '#111827',       // 검은색 텍스트
                        bodyFont: { size: 13, weight: 'bold' },
                        backgroundColor: 'rgba(255,255,255,0.9)',
                        borderColor: '#d1d5db',
                        borderWidth: 1,
                        boxPadding: 4,
                        padding: 8,
                        displayColors: false, // 왼쪽 색상 네모 제거
                    },
                }
            }
        };
    };

    // 특정 canvasId에 차트를 그리는 함수
    const drawLineChartWithChartJS = (canvasId, chartKey, labels, values, labelText) => {
        const canvasEl = document.getElementById(canvasId);
        if (!canvasEl) return;

        // 기존 차트가 있으면 파괴 (중복 생성 방지)
        if (chartKey === 'monthly' && monthlyChartInstance) {
            monthlyChartInstance.destroy();
            monthlyChartInstance = null;
        }
        if (chartKey === 'weekly' && weeklyChartInstance) {
            weeklyChartInstance.destroy();
            weeklyChartInstance = null;
        }

        const ctx = canvasEl.getContext('2d');
        const config = buildLineChartConfig(labels, values, labelText);

        if (chartKey === 'monthly') {
            monthlyChartInstance = new Chart(ctx, config);
        } else if (chartKey === 'weekly') {
            weeklyChartInstance = new Chart(ctx, config);
        }
    };

    // 차트 전부 초기화 (데이터 없을 때 등)
    const clearCharts = () => {
        if (monthlyChartInstance) {
            monthlyChartInstance.destroy();
            monthlyChartInstance = null;
        }
        if (weeklyChartInstance) {
            weeklyChartInstance.destroy();
            weeklyChartInstance = null;
        }

        // 캔버스 지우기 (선택)
        const m = document.getElementById('qaMonthly');
        const w = document.getElementById('qaWeekly');
        if (m) {
            const ctx = m.getContext('2d');
            ctx && ctx.clearRect(0, 0, m.width, m.height);
        }
        if (w) {
            const ctx = w.getContext('2d');
            ctx && ctx.clearRect(0, 0, w.width, w.height);
        }

        const graphSection = document.getElementById('graphSection');
        if (graphSection) {
            graphSection.classList.add('hidden');
        }
    };

    // 네이버(또는 DB) 응답 배열을 그래프 friendly하게 변환
    // items: [{ period: "...", ratio: ...}, ...]
    // labelFmt: period 문자열 -> 축에 찍을 라벨로 변환하는 함수
    const parseSeries = (items, labelFmt) => {
        if (!Array.isArray(items)) return [];
        const sorted = [...items].sort((a, b) => String(a.period).localeCompare(String(b.period)));
        return sorted.map(x => ({
            label: labelFmt(String(x.period)),
            value: Number(x.ratio) || 0
        }));
    };

    // 실제 차트를 갱신하는 핵심 함수
    const renderQueryCharts = (data) => {
        if (!data) {
            clearCharts();
            return;
        }

        // monthlyResponse / weeklyResponse 케이스 모두 대비
        const mRes = data.monthlyResponse ?? data.MonthlyResponse;
        const wRes = data.weeklyResponse ?? data.WeeklyResponse;

        // results[0].data 형태라고 가정
        const monthlyRaw = mRes?.results?.[0]?.data ?? [];
        const weeklyRaw  = wRes?.results?.[0]?.data ?? [];

        const graphSection = document.getElementById('graphSection');

        const hasMonthly = Array.isArray(monthlyRaw) && monthlyRaw.length > 0;
        const hasWeekly = Array.isArray(weeklyRaw) && weeklyRaw.length > 0;
        const hasAny = hasMonthly || hasWeekly;

        if (!hasAny) {
            clearCharts();
            if (graphSection) {
                graphSection.classList.add('hidden');
            }
            return;
        } else {
            if (graphSection) {
                graphSection.classList.remove('hidden');
            }
        }

        // 라벨 포맷터
        // 월간: 'YYYY-MM-DD', 'YYYYMMDD', 'YYYYMM' 등을 'YY.MM' 식으로
        const fmtMonth = (p) => {
            if (/^\d{4}-\d{2}-\d{2}$/.test(p)) return `${p.slice(2,4)}.${p.slice(5,7)}`; // 2025-10-27 -> 25.10
            if (/^\d{8}$/.test(p))            return `${p.slice(2,4)}.${p.slice(4,6)}`; // 20251027 -> 25.10
            if (/^\d{6}$/.test(p))            return `${p.slice(2,4)}.${p.slice(4,6)}`; // 202510 -> 25.10
            return p;
        };

        // 주간: 날짜 시작일 같은 값이면 'MM.DD' 형식 등으로 단순화
        const fmtWeek = (p) => {
            if (/^\d{4}-\d{2}-\d{2}$/.test(p)) return `${p.slice(5,7)}.${p.slice(8,10)}`; // 2025-10-27 -> 10.27
            if (/^\d{8}$/.test(p))            return `${p.slice(4,6)}.${p.slice(6,8)}`;  // 20251027 -> 10.27
            return p.replace('W', '-W');
        };

        // 가공된 데이터
        const monthly = parseSeries(monthlyRaw, fmtMonth); // [{label:'25.10', value:32}, ...]
        const weekly  = parseSeries(weeklyRaw,  fmtWeek);  // [{label:'10.20', value:44}, ...]

        // Chart.js에 넣을 labels / data
        const monthLabels = monthly.map(pt => pt.label);
        const monthValues = monthly.map(pt => pt.value);
        const weekLabels  = weekly.map(pt => pt.label);
        const weekValues  = weekly.map(pt => pt.value);

        // 월간 차트
        drawLineChartWithChartJS(
            'qaMonthly',
            'monthly',
            monthLabels,
            monthValues,
            '월간 검색 비율(%)'
        );

        // 주간 차트
        drawLineChartWithChartJS(
            'qaWeekly',
            'weekly',
            weekLabels,
            weekValues,
            '주간 검색 비율(%)'
        );
    };

    // 창 크기 변경 시 차트 재렌더 (반응형 유지)
    window.addEventListener('resize', () => {
        if (lastChartData) {
            renderQueryCharts(lastChartData);
        }
    });

    // ─────────────────────────────────────────────────────────────
    // 백엔드에서 키워드 분석 데이터 불러오기
    const fetchAnalysisData = async (query) => {
        if (!query || !query.trim()) {
            // 검색어가 비어있는 상태 (초기 상태)
            analysisResultEl.textContent = '';

            clearCharts(); // 그래프 섹션 숨기기 등

            // 전역 플래그 업데이트
            window.__dataFlags = window.__dataFlags || {
                hasKeywordData: false,
                hasChartData: false
            };
            window.__dataFlags.hasChartData = false;

            // 이 상태에서는 "데이터가 존재하지 않습니다" 안내문을 보여주면 안 된다.
            const noDataHintEl = document.getElementById('noDataHint');
            if (noDataHintEl) {
                noDataHintEl.classList.add('hidden');
            }

            return;
        }

        analysisResultEl.textContent = '데이터 분석 중...';

        try {
            // 네가 이미 쓰고 있는 API 그대로 재사용
            const url = `/naver/api/category-trend?query=${encodeURIComponent(query)}`;
            const res = await httpRequest('GET', url);

            if (!res.ok) {
                const errorText = await res.text();
                throw new Error(`API 요청 실패: ${res.status} ${errorText}`);
            }

            const data = await res.json();

            // 디버그 JSON 그대로 보여주기
            analysisResultEl.textContent = JSON.stringify(data, null, 2);

            // 차트 렌더
            renderQueryCharts(data);

            // 리사이즈 대비 저장
            lastChartData = data;

        } catch (error) {
            console.error('분석 데이터를 가져오는 데 실패했습니다:', error);
            analysisResultEl.textContent = '분석 데이터를 불러오는 데 실패했습니다.';
            clearCharts();
        }
    };

    // 입력 값이 바뀔 때마다 디바운스 후 분석 데이터 로드
    queryInput.addEventListener('input', debounce(e => {
        fetchAnalysisData(e.target.value);
    }, 600));

    // 페이지 첫 로드시 input에 값 있으면 자동으로 호출
    if (queryInput.value) {
        fetchAnalysisData(queryInput.value);
    }
});