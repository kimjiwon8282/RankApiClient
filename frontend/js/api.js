// /js/api.js

const ACCESS_TOKEN_KEY = 'access_token';

function getAccessToken() {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
}

function setAccessToken(token) {
    localStorage.setItem(ACCESS_TOKEN_KEY, token);
}

function clearAccessToken() {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
}

function redirectToLogin() {
    clearAccessToken();
    window.location.href = '/login.html';
}

function createRequestOptions(method, data, accessToken) {
    const headers = {
        Accept: 'application/json'
    };

    if (data !== undefined && data !== null) {
        headers['Content-Type'] = 'application/json';
    }

    if (accessToken) {
        headers['Authorization'] = `Bearer ${accessToken}`;
    }

    const options = {
        method,
        headers,
        credentials: 'include'
    };

    if (data !== undefined && data !== null) {
        options.body = JSON.stringify(data);
    }

    return options;
}

async function refreshAccessToken() {
    const response = await fetch('/api/token', {
        method: 'POST',
        headers: {
            Accept: 'application/json'
        },
        credentials: 'include'
    });

    if (!response.ok) {
        return null;
    }

    const data = await response.json();
    return data.accessToken;
}

export async function httpRequest(method, url, data = null, options = {}) {
    const { redirectOnUnauthorized = true } = options;

    let accessToken = getAccessToken();

    let response = await fetch(
        url,
        createRequestOptions(method, data, accessToken)
    );

    if (response.status !== 401) {
        return response;
    }

    console.log('Access Token 만료 또는 없음. 재발급을 시도합니다.');

    try {
        const newAccessToken = await refreshAccessToken();

        if (!newAccessToken) {
            console.log('Refresh Token 만료 또는 없음.');

            clearAccessToken();

            if (redirectOnUnauthorized) {
                redirectToLogin();
            }

            return response;
        }

        setAccessToken(newAccessToken);

        response = await fetch(
            url,
            createRequestOptions(method, data, newAccessToken)
        );

        return response;
    } catch (error) {
        console.error('토큰 재발급 중 오류 발생:', error);

        clearAccessToken();

        if (redirectOnUnauthorized) {
            redirectToLogin();
        }

        return response;
    }
}