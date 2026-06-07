requireAuth();

const user = getUser();
document.getElementById('userLabel').textContent =
    user ? (user.full_name + ' (' + user.role + ')') : '';

loadDashboard();

async function loadDashboard() {
    try {
        const stats = await apiGet('statistics.php');
        renderTotals(stats.totals);
        renderCourseChart(stats.per_course);
        renderTrendChart(stats.trend);

        const recent = await apiGet('attendance.php', { limit: 8 });
        renderRecent(recent.records);
    } catch (err) {
        console.error(err);
        alert('Грешка при вчитување: ' + err.message);
    }
}

function renderTotals(t) {
    document.getElementById('statToday').textContent = t.today;
    document.getElementById('statWeek').textContent = t.week;
    document.getElementById('statAll').textContent = t.all;
    document.getElementById('statStudents').textContent = t.unique_students;
}

function renderCourseChart(perCourse) {
    new Chart(document.getElementById('courseChart'), {
        type: 'bar',
        data: {
            labels: perCourse.map(r => r.course),
            datasets: [{
                label: 'Присуства',
                data: perCourse.map(r => Number(r.total)),
                backgroundColor: '#1565c0',
                borderRadius: 6,
            }],
        },
        options: {
            plugins: { legend: { display: false } },
            scales: { y: { beginAtZero: true, ticks: { precision: 0 } } },
        },
    });
}

function renderTrendChart(trend) {
    new Chart(document.getElementById('trendChart'), {
        type: 'line',
        data: {
            labels: trend.map(r => r.day),
            datasets: [{
                label: 'Присуства',
                data: trend.map(r => Number(r.total)),
                borderColor: '#00acc1',
                backgroundColor: 'rgba(0,172,193,0.15)',
                fill: true,
                tension: 0.3,
            }],
        },
        options: {
            plugins: { legend: { display: false } },
            scales: { y: { beginAtZero: true, ticks: { precision: 0 } } },
        },
    });
}

function renderRecent(records) {
    const body = document.getElementById('recentBody');
    const empty = document.getElementById('recentEmpty');
    body.innerHTML = '';
    if (!records || records.length === 0) {
        empty.style.display = 'block';
        return;
    }
    empty.style.display = 'none';
    for (const r of records) {
        const tr = document.createElement('tr');
        tr.innerHTML =
            `<td>${escapeHtml(r.student_name)}</td>` +
            `<td>${escapeHtml(r.student_id)}</td>` +
            `<td>${escapeHtml(r.course || '')}</td>` +
            `<td>${escapeHtml(r.timestamp)}</td>`;
        body.appendChild(tr);
    }
}

function escapeHtml(s) {
    return String(s ?? '').replace(/[&<>"']/g, c =>
        ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}
