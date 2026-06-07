requireAuth();

const user = getUser();
document.getElementById('userLabel').textContent =
    user ? (user.full_name + ' (' + user.role + ')') : '';

document.getElementById('applyBtn').addEventListener('click', loadRecords);
document.getElementById('resetBtn').addEventListener('click', () => {
    document.getElementById('fDate').value = '';
    document.getElementById('fCourse').value = '';
    document.getElementById('fStudent').value = '';
    loadRecords();
});

loadRecords();

async function loadRecords() {
    try {
        const data = await apiGet('attendance.php', {
            date: document.getElementById('fDate').value,
            course: document.getElementById('fCourse').value.trim(),
            student: document.getElementById('fStudent').value.trim(),
            limit: 500,
        });
        renderTable(data.records);
    } catch (err) {
        console.error(err);
        alert('Грешка при вчитување: ' + err.message);
    }
}

function renderTable(records) {
    const body = document.getElementById('tableBody');
    const empty = document.getElementById('emptyMsg');
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
            `<td>${escapeHtml(r.class_name || '')}</td>` +
            `<td>${escapeHtml(r.timestamp)}</td>`;
        body.appendChild(tr);
    }
}

function escapeHtml(s) {
    return String(s ?? '').replace(/[&<>"']/g, c =>
        ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}
