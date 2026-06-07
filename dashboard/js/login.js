// Redirect straight to the dashboard if already logged in.
if (getToken()) {
    window.location.href = 'dashboard.html';
}

document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const errorEl = document.getElementById('error');
    errorEl.textContent = '';

    try {
        const data = await apiPost('login.php', {
            username: document.getElementById('username').value.trim(),
            password: document.getElementById('password').value,
            role: document.getElementById('role').value,
        });
        saveSession(data.token, data.user);
        window.location.href = 'dashboard.html';
    } catch (err) {
        errorEl.textContent = err.message;
    }
});
