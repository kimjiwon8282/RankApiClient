// api.js

function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
    return null;
}

export async function httpRequest(method, url, data) {
    let accessToken = localStorage.getItem('access_token');
    const headers = { 'Content-Type': 'application/json' };
    if (accessToken) {
        headers['Authorization'] = `Bearer ${accessToken}`;
    }

    const options = { method, headers };
    if (data) {
        options.body = JSON.stringify(data);
    }

    let response = await fetch(url, options);

    if (response.status === 401) {
        const refreshToken = getCookie('refresh_token');
        if (refreshToken) {
            const refreshRes = await fetch('/api/token', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ refreshToken })
            });
            if (refreshRes.ok) {
                const { accessToken: newAccessToken } = await refreshRes.json();
                localStorage.setItem('access_token', newAccessToken);
                headers['Authorization'] = `Bearer ${newAccessToken}`;
                response = await fetch(url, options);
            }
        }
    }
    return response;
}