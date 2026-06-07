// Shared API client + session helpers for the dashboard.
// The dashboard is served from /cps/dashboard, the API from /cps/api.
const API_BASE = '../api/';

function saveSession(token, user) {
    sessionStorage.setItem('cps_token', token);
    sessionStorage.setItem('cps_user', JSON.stringify(user));
}

function getToken() {
    return sessionStorage.getItem('cps_token');
}

function getUser() {
    return JSON.parse(sessionStorage.getItem('cps_user') || 'null');
}

function logout() {
    sessionStorage.clear();
    window.location.href = 'index.html';
}

// Redirect to the login page if there is no valid session.
function requireAuth() {
    if (!getToken()) {
        window.location.href = 'index.html';
    }
}

async function apiPost(path, body) {
    const res = await fetch(API_BASE + path, {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify(body),
    });
    return handle(res);
}

async function apiGet(path, params = {}) {
    const query = new URLSearchParams(
        Object.entries(params).filter(([, v]) => v !== '' && v != null)
    ).toString();
    const res = await fetch(API_BASE + path + (query ? '?' + query : ''), {
        headers: authHeaders(),
    });
    return handle(res);
}

function authHeaders() {
    const headers = { 'Content-Type': 'application/json' };
    const token = getToken();
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    return headers;
}

async function handle(res) {
    if (res.status === 401) {
        logout();
        throw new Error('Сесијата истече');
    }
    const data = await res.json().catch(() => ({}));
    if (!res.ok || data.success === false) {
        throw new Error(data.message || ('Грешка ' + res.status));
    }
    return data;
}
