// /js/keyword-analysis.js
import { httpRequest } from './api.js';

const $ = (id) => document.getElementById(id);
const els = {};

function initElements() {
    [
        'adminGuard',
        'refreshOverviewButton',
        'overviewCheckedAt',
        'collectorStatusGrid',
        'jobsLimit',
        'loadJobsButton',
        'jobsTbody',
        'recommendHint',
        'recommendLimit',
        'recommendButton',
        'recommendResult',
        'analysisQuery',
        'analysisButton',
        'analysisTbody',
        'categoryTrendQuery',
        'categoryTrendButton',
        'categoryTrendSummary',
        'shopTrendQuery',
        'shopTrendButton',
        'shopTrendTbody',
        'keywordTrendCategoryCode',
        'keywordTrendKeywords',
        'keywordTrendButton',
        'keywordTrendSummary',
        'rawResult',
        'clearRawButton',
    ].forEach((id) => {
        els[id] = $(id);
    });
}

function escapeHtml(value) {
    if (value === null || value === undefined) return '';

    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function formatNumber(value) {
    if (value === null || value === undefined || value === '') return '-';

    const normalized = String(value).replace(/,/g, '');
    const numberValue = Number(normalized);

    if (Number.isNaN(numberValue)) return escapeHtml(value);

    return numberValue.toLocaleString('ko-KR');
}

function formatDate(value) {
    if (!value) return '-';

    const numberValue = Number(value);

    if (Number.isNaN(numberValue)) return escapeHtml(value);

    return new Date(numberValue).toLocaleString('ko-KR');
}

function formatDuration(ms) {
    if (ms === null || ms === undefined || ms === '') return '-';

    const value = Number(ms);

    if (Number.isNaN(value)) return escapeHtml(ms);
    if (value < 1000) return `${value}ms`;
    if (value < 60000) return `${(value / 1000).toFixed(1)}초`;

    return `${Math.floor(value / 60000)}분 ${Math.round((value % 60000) / 1000)}초`;
}

function statusClass(status) {
    const value = String(status || '').toUpperCase();

    if (value.includes('SUCCESS') && !value.includes('PARTIAL')) return 'success';
    if (value.includes('FAIL') || value.includes('ERROR')) return 'fail';
    if (value.includes('PARTIAL') || value.includes('RUNNING')) return 'warn';

    return 'neutral';
}

function badge(status) {
    return `<span class="badge ${statusClass(status)}">${escapeHtml(status || 'UNKNOWN')}</span>`;
}

function setRaw(title, data) {
    if (!els.rawResult) return;
    els.rawResult.textContent = `${title}\n${JSON.stringify(data, null, 2)}`;
}

function showGuard(message) {
    if (!els.adminGuard) return;
    els.adminGuard.textContent = message;
    els.adminGuard.classList.remove('hidden');
}

function hideGuard() {
    if (!els.adminGuard) return;
    els.adminGuard.classList.add('hidden');
    els.adminGuard.textContent = '';
}

async function requestJson(method, url, data = null) {
    const response = await httpRequest(method, url, data, {
        redirectOnUnauthorized: false,
    });

    const contentType = response.headers.get('content-type') || '';
    const body = contentType.includes('application/json')
        ? await response.json().catch(() => null)
        : await response.text().catch(() => '');

    if (!response.ok) {
        const error = new Error(`요청 실패 (${response.status})`);
        error.status = response.status;
        error.body = body;
        throw error;
    }

    return body;
}

function renderError(target, error, colspan = null) {
    const message =
        error.status === 403
            ? '관리자 권한이 없습니다.'
            : error.status === 401
                ? '로그인이 필요합니다.'
                : `요청 실패 (${error.status || 'ERR'})`;

    if (!target) {
        setRaw('ERROR', {
            message,
            status: error.status,
            body: error.body || String(error),
        });
        return;
    }

    if (target.tagName === 'TBODY') {
        target.innerHTML = `<tr><td colspan="${colspan || 1}" class="empty-cell">${escapeHtml(message)}</td></tr>`;
    } else {
        target.classList.remove('empty-state');
        target.innerHTML = `<span class="empty-state">${escapeHtml(message)}</span>`;
    }

    setRaw('ERROR', {
        message,
        status: error.status,
        body: error.body || String(error),
    });
}

function renderStatusOverview(data) {
    els.overviewCheckedAt.textContent = data?.checkedAt
        ? `확인 시각: ${formatDate(data.checkedAt)}`
        : '확인 시각: -';

    const jobs = [
        ['연관 키워드 수집', data?.relatedKeywordJob],
        ['카테고리 트렌드 수집', data?.categoryTrendJob],
        ['쇼핑 검색 트렌드 수집', data?.shopSearchTrendJob],
    ];

    els.collectorStatusGrid.innerHTML = jobs
        .map(([label, job]) => {
            if (!job) {
                return `
          <div class="status-card">
            <div class="status-title">
              <span>${escapeHtml(label)}</span>
              ${badge('UNKNOWN')}
            </div>
            <div class="status-meta">데이터 없음</div>
          </div>
        `;
            }

            return `
        <div class="status-card">
          <div class="status-title">
            <span>${escapeHtml(label)}</span>
            ${badge(job.latestStatus)}
          </div>
          <div class="status-meta">
            <div><strong>잡 이름</strong> ${escapeHtml(job.jobName || '-')}</div>
            <div><strong>최근 시작</strong> ${formatDate(job.latestStartedAt)}</div>
            <div><strong>최근 종료</strong> ${formatDate(job.latestFinishedAt)}</div>
            <div><strong>마지막 성공</strong> ${formatDate(job.lastSuccessAt)}</div>
            <div><strong>성공/실패</strong> ${formatNumber(job.latestSuccessCount)} / ${formatNumber(job.latestFailCount)}</div>
            ${
                job.latestErrorMessage
                    ? `<div><strong>오류</strong> ${escapeHtml(job.latestErrorMessage)}</div>`
                    : ''
            }
          </div>
        </div>
      `;
        })
        .join('');
}

function renderJobs(rows) {
    if (!Array.isArray(rows) || rows.length === 0) {
        els.jobsTbody.innerHTML =
            '<tr><td colspan="6" class="empty-cell">실행 이력이 없습니다.</td></tr>';
        return;
    }

    els.jobsTbody.innerHTML = rows
        .map(
            (job) => `
        <tr>
          <td>${escapeHtml(job.jobName || '-')}</td>
          <td>${badge(job.status)}</td>
          <td>${escapeHtml(job.triggerType || '-')}</td>
          <td>${formatDate(job.startedAt)}</td>
          <td>${formatDuration(job.durationMs)}</td>
          <td>${formatNumber(job.successCount)} / ${formatNumber(job.failCount)}</td>
        </tr>
      `
        )
        .join('');
}

function renderRecommend(data) {
    const items = data?.recommended || data?.keywords || data?.items || [];

    if (!items.length) {
        els.recommendResult.classList.add('empty-state');
        els.recommendResult.textContent = '추천 키워드가 없습니다.';
        return;
    }

    els.recommendResult.classList.remove('empty-state');
    els.recommendResult.innerHTML = items
        .map((item) => {
            const keyword =
                typeof item === 'string'
                    ? item
                    : item.keyword || item.relKeyword || item.name || JSON.stringify(item);

            return `<span class="keyword-chip">${escapeHtml(keyword)}</span>`;
        })
        .join('');
}

function renderKeywordAnalysis(data) {
    const rows = data?.relatedKeywords || data?.keywords || data?.items || [];

    if (!rows.length) {
        els.analysisTbody.innerHTML =
            '<tr><td colspan="5" class="empty-cell">분석 데이터가 없습니다.</td></tr>';
        return;
    }

    els.analysisTbody.innerHTML = rows
        .slice(0, 30)
        .map((row) => {
            const pc = Number(String(row.monthlyPcQcCnt || 0).replace(/,/g, '')) || 0;
            const mobile = Number(String(row.monthlyMobileQcCnt || 0).replace(/,/g, '')) || 0;

            return `
        <tr>
          <td>${escapeHtml(row.relKeyword || row.keyword || '-')}</td>
          <td class="number">${formatNumber(pc + mobile)}</td>
          <td class="number">${formatNumber(row.monthlyPcQcCnt)}</td>
          <td class="number">${formatNumber(row.monthlyMobileQcCnt)}</td>
          <td>${escapeHtml(row.compIdx || row.competition || '-')}</td>
        </tr>
      `;
        })
        .join('');
}

function summaryItem(label, value) {
    return `
    <div class="summary-item">
      <strong>${escapeHtml(label)}</strong>
      <span>${escapeHtml(value ?? '-')}</span>
    </div>
  `;
}

function renderCategoryTrend(data) {
    els.categoryTrendSummary.classList.remove('empty-state');
    els.categoryTrendSummary.innerHTML = `
    <div class="summary-list">
      ${summaryItem('카테고리 코드', data?.categoryCode)}
      ${summaryItem('카테고리 이름', data?.categoryName)}
      ${summaryItem('월간 신선도', data?.monthlyFresh)}
      ${summaryItem('주간 신선도', data?.weeklyFresh)}
      ${summaryItem('월간 수집 시각', formatDate(data?.monthlyCallAt))}
      ${summaryItem('주간 수집 시각', formatDate(data?.weeklyCallAt))}
    </div>
  `;
}

function renderKeywordTrend(data) {
    els.keywordTrendSummary.classList.remove('empty-state');
    els.keywordTrendSummary.innerHTML = `
    <div class="summary-list">
      ${summaryItem('카테고리 코드', data?.categoryCode)}
      ${summaryItem(
        '키워드',
        Array.isArray(data?.keywords) ? data.keywords.join(', ') : '-'
    )}
      ${summaryItem('기간', `${data?.startDate || '-'} ~ ${data?.endDate || '-'}`)}
      ${summaryItem('집계 단위', data?.timeUnit)}
      ${summaryItem('신선도', data?.fresh)}
      ${summaryItem('수집 시각', formatDate(data?.callAt))}
    </div>
  `;
}

function renderShopTrend(data) {
    const items = data?.items || data?.products || [];

    if (!items.length) {
        els.shopTrendTbody.innerHTML =
            '<tr><td colspan="4" class="empty-cell">쇼핑 검색 결과가 없습니다.</td></tr>';
        return;
    }

    els.shopTrendTbody.innerHTML = items
        .slice(0, 20)
        .map(
            (item) => `
        <tr>
          <td class="number">${formatNumber(item.rank)}</td>
          <td>
            ${
                item.link
                    ? `<a href="${escapeHtml(item.link)}" target="_blank" rel="noopener">${escapeHtml(item.title || '-')}</a>`
                    : escapeHtml(item.title || '-')
            }
          </td>
          <td class="number">${formatNumber(item.lprice || item.price)}</td>
          <td>${escapeHtml(item.mallName || item.mall || '-')}</td>
        </tr>
      `
        )
        .join('');
}

async function loadOverview() {
    try {
        hideGuard();

        const data = await requestJson('GET', '/api/admin/collectors/status');

        renderStatusOverview(data);
        setRaw('GET /api/admin/collectors/status', data);
    } catch (error) {
        if (error.status === 401) {
            showGuard('로그인이 필요합니다. 로그인 후 다시 접근해 주세요.');
        }

        if (error.status === 403) {
            showGuard('관리자 권한이 없습니다. ADMIN 계정으로 로그인해 주세요.');
        }

        renderError(els.collectorStatusGrid, error);
    }
}

async function loadJobs() {
    const limit = Math.min(Math.max(Number(els.jobsLimit.value) || 20, 1), 100);

    try {
        const data = await requestJson('GET', `/api/admin/collectors/jobs?limit=${limit}`);

        renderJobs(data);
        setRaw(`GET /api/admin/collectors/jobs?limit=${limit}`, data);
    } catch (error) {
        renderError(els.jobsTbody, error, 6);
    }
}

async function loadRecommend() {
    const hint = els.recommendHint.value.trim();
    const limit = Math.min(Math.max(Number(els.recommendLimit.value) || 15, 1), 50);

    if (!hint) return;

    try {
        const data = await requestJson(
            'GET',
            `/api/admin/naver/keyword/recommend?hint=${encodeURIComponent(hint)}&limit=${limit}`
        );

        renderRecommend(data);
        setRaw('GET /api/admin/naver/keyword/recommend', data);
    } catch (error) {
        renderError(els.recommendResult, error);
    }
}

async function loadKeywordAnalysis() {
    const query = els.analysisQuery.value.trim();

    if (!query) return;

    try {
        const data = await requestJson(
            'GET',
            `/api/admin/naver/keyword/analysis?query=${encodeURIComponent(query)}`
        );

        renderKeywordAnalysis(data);
        setRaw('GET /api/admin/naver/keyword/analysis', data);
    } catch (error) {
        renderError(els.analysisTbody, error, 5);
    }
}

async function loadCategoryTrend() {
    const query = els.categoryTrendQuery.value.trim();

    if (!query) return;

    try {
        const data = await requestJson(
            'GET',
            `/api/admin/naver/category-trend?query=${encodeURIComponent(query)}`
        );

        renderCategoryTrend(data);
        setRaw('GET /api/admin/naver/category-trend', data);
    } catch (error) {
        renderError(els.categoryTrendSummary, error);
    }
}

async function loadShopTrend() {
    const query = els.shopTrendQuery.value.trim();

    if (!query) return;

    try {
        const data = await requestJson(
            'GET',
            `/api/admin/naver/shop-trend?query=${encodeURIComponent(query)}`
        );

        renderShopTrend(data);
        setRaw('GET /api/admin/naver/shop-trend', data);
    } catch (error) {
        renderError(els.shopTrendTbody, error, 4);
    }
}

async function loadKeywordTrend() {
    const categoryCode = els.keywordTrendCategoryCode.value.trim();
    const keywords = els.keywordTrendKeywords.value
        .split(',')
        .map((keyword) => keyword.trim())
        .filter(Boolean);

    if (!categoryCode || keywords.length === 0) return;

    const params = new URLSearchParams();
    params.set('categoryCode', categoryCode);
    keywords.forEach((keyword) => params.append('keywords', keyword));

    try {
        const data = await requestJson(
            'GET',
            `/api/admin/naver/keyword-trend?${params.toString()}`
        );

        renderKeywordTrend(data);
        setRaw('GET /api/admin/naver/keyword-trend', data);
    } catch (error) {
        renderError(els.keywordTrendSummary, error);
    }
}

function bindEvents() {
    els.refreshOverviewButton?.addEventListener('click', loadOverview);
    els.loadJobsButton?.addEventListener('click', loadJobs);
    els.recommendButton?.addEventListener('click', loadRecommend);
    els.analysisButton?.addEventListener('click', loadKeywordAnalysis);
    els.categoryTrendButton?.addEventListener('click', loadCategoryTrend);
    els.shopTrendButton?.addEventListener('click', loadShopTrend);
    els.keywordTrendButton?.addEventListener('click', loadKeywordTrend);

    els.clearRawButton?.addEventListener('click', () => {
        els.rawResult.textContent = '아직 요청이 없습니다.';
    });

    [
        ['recommendHint', loadRecommend],
        ['analysisQuery', loadKeywordAnalysis],
        ['categoryTrendQuery', loadCategoryTrend],
        ['shopTrendQuery', loadShopTrend],
    ].forEach(([id, handler]) => {
        els[id]?.addEventListener('keydown', (event) => {
            if (event.key === 'Enter') handler();
        });
    });
}

document.addEventListener('DOMContentLoaded', async () => {
    initElements();
    bindEvents();

    await loadOverview();
    await loadJobs();
});