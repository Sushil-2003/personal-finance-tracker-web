// Global state
let transactions = []; // Store all transactions
let currentUser = null; // Current logged-in user
let categories = []; // Available categories
let expensePieChart = null; // Pie chart instance
let summaryBarChart = null; // Bar chart instance

const API_BASE_URL = '/api'; // Base URL for API requests

//const API_BASE_URL = 'https://personal-finance-tracker-web.onrender.com/api';

// DOM references for views
const views = {
    dashboard: document.getElementById("dashboard-view"),
    transactions: document.getElementById("transactions-view"),
    reports: document.getElementById("reports-view"),
    budget: document.getElementById("budget-view"),
    settings: document.getElementById("settings-view"),
    login: document.getElementById("login-view"),
    register: document.getElementById("register-view")
};

// Make API requests (GET, POST, PUT, DELETE)
async function apiRequest(endpoint, method = 'GET', data = null) {
    const token = localStorage.getItem('jwtToken');
    const headers = { 'Content-Type': 'application/json' };
    if (token) headers['Authorization'] = `Bearer ${token}`;
    const options = { method, headers };
    if (data) options.body = JSON.stringify(data);
    const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
    if (!response.ok) {
        const error = await response.json().catch(() => ({}));
        if (response.status === 401) {
            handleLogout();
            throw new Error('Unauthorized');
        }
        throw new Error(error.message || `HTTP error! status: ${response.status}`);
    }
    return response.status === 204 ? null : response.json();
}

// Show a specific view and hide others
function showView(viewId) {
    Object.values(views).forEach(view => view.classList.toggle("hidden", view.id !== viewId));
    if (expensePieChart) expensePieChart.destroy();
    if (summaryBarChart) summaryBarChart.destroy();
    if (viewId === "dashboard-view") setTimeout(() => renderExpensePieChart("expense-pie-chart"), 100);
    if (viewId === "reports-view") setTimeout(() => {
        renderSummaryBarChart();
        renderExpensePieChart("expense-pie-chart-reports");
    }, 100);
}

// Populate category dropdowns for transactions and filters
async function populateCategories() {
    categories = await apiRequest('/categories').catch(() => []);
    ['category', 'filter-category'].forEach(id => {
        const dropdown = document.getElementById(id);
        if (!dropdown) return;
        const currentValue = dropdown.value;
        dropdown.innerHTML = id === 'filter-category' ? '<option value="">All Categories</option>' : '';
        categories.forEach(cat => {
            const option = document.createElement("option");
            option.value = option.textContent = cat.name;
            dropdown.appendChild(option);
        });
        dropdown.value = categories.some(cat => cat.name === currentValue) ? currentValue : (id === 'filter-category' ? '' : categories[0]?.name || '');
    });
}

// Calculate financial summary (income, expense, balance)
function calculateSummary() {
    const totalIncome = transactions.filter(t => t.type === "income").reduce((sum, t) => sum + t.amount, 0);
    const totalExpense = transactions.filter(t => t.type === "expense").reduce((sum, t) => sum + t.amount, 0);
    return { totalIncome, totalExpense, currentMoney: totalIncome - totalExpense };
}

// Prepare and show a view with optional setup logic
async function prepareView(viewId, setup = () => {}) {
    if (!currentUser) return showView("login-view");
    showView(viewId);
    await setup();
}

// Fetch transactions from the API
async function fetchTransactions() {
    transactions = await apiRequest('/transactions').catch(() => []);
}

// Render the transaction list with filters and sorting
function renderTransactionList() {
    const list = document.getElementById("transaction-list");
    const filterCategory = document.getElementById("filter-category").value;
    const filterType = document.getElementById("filter-type").value;
    const sortDate = document.getElementById("sort-date").value;

    const filtered = transactions.filter(t =>
        (!filterCategory || t.category === filterCategory) &&
        (!filterType || t.type === filterType)
    ).sort((a, b) => (sortDate === "asc" ? 1 : -1) * (new Date(a.date) - new Date(b.date)));

    list.innerHTML = filtered.map(t => `
        <li class="${t.type}">
            <span>${t.type.charAt(0).toUpperCase() + t.type.slice(1)}</span>
            <span>${t.category}</span>
            <em>${t.date}</em>
            <span>₹${t.amount.toFixed(2)}</span>
            <div class="action-buttons">
                <button class="edit-btn btn" data-id="${t.id}">Edit</button>
                <button class="delete-btn btn btn-danger" data-id="${t.id}">Delete</button>
            </div>
        </li>
    `).join("");
    list.querySelectorAll(".edit-btn").forEach(btn => btn.addEventListener("click", handleEditTransaction));
    list.querySelectorAll(".delete-btn").forEach(btn => btn.addEventListener("click", handleDeleteTransaction));
}

// Render expense pie chart for dashboard/reports
function renderExpensePieChart(canvasId) {
    const canvas = document.getElementById(canvasId);
    if (!canvas || typeof Chart === 'undefined' || !canvas.getContext("2d") || !canvas.offsetParent) return;

    const expenseByCategory = transactions.filter(t => t.type === "expense")
        .reduce((acc, t) => ({ ...acc, [t.category]: (acc[t.category] || 0) + t.amount }), {});
    const data = Object.keys(expenseByCategory).length ? {
        labels: Object.keys(expenseByCategory),
        datasets: [{
            label: "Expenses",
            data: Object.values(expenseByCategory),
            backgroundColor: ["#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF", "#FF9F40", "#C9CBCF"],
            borderColor: ["#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF", "#FF9F40", "#C9CBCF"],
            borderWidth: 1
        }]
    } : {
        labels: ['No Data'],
        datasets: [{ label: "Expenses", data: [1], backgroundColor: ["#E0E0E0"], borderColor: ["#E0E0E0"], borderWidth: 1 }]
    };

    const options = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            title: { display: true, text: "Expense Distribution", font: { size: 16 } },
            legend: { position: "bottom" },
            tooltip: { enabled: Object.keys(expenseByCategory).length, callbacks: { label: ctx => Object.keys(expenseByCategory).length ? `${ctx.label}: ₹${ctx.raw.toFixed(2)}` : 'No expense data' } }
        }
    };

    if (canvasId === "expense-pie-chart") expensePieChart = new Chart(canvas.getContext("2d"), { type: "pie", data, options });
    else new Chart(canvas.getContext("2d"), { type: "pie", data, options });
}

// Render summary bar chart for reports
function renderSummaryBarChart() {
    const canvas = document.getElementById("summary-bar-chart");
    if (!canvas || typeof Chart === 'undefined' || !canvas.offsetParent) return;

    const summary = calculateSummary();
    const ctx = canvas.getContext("2d");
    if (summary.totalIncome || summary.totalExpense || summary.currentMoney) {
        summaryBarChart = new Chart(ctx, {
            type: "bar",
            data: {
                labels: ["Total Income", "Total Expense", "Current Balance"],
                datasets: [{
                    label: "Amount (₹)",
                    data: [summary.totalIncome, summary.totalExpense, summary.currentMoney],
                    backgroundColor: ["rgba(75, 192, 192, 0.6)", "rgba(255, 99, 132, 0.6)", "rgba(54, 162, 235, 0.6)"],
                    borderColor: ["rgba(75, 192, 192, 1)", "rgba(255, 99, 132, 1)", "rgba(54, 162, 235, 1)"],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { title: { display: true, text: "Financial Summary", font: { size: 16 } }, legend: { position: "bottom" } },
                scales: { y: { beginAtZero: true } }
            }
        });
    } else {
        ctx.textAlign = 'center';
        ctx.fillText('No summary data to display.', canvas.width / 2, canvas.height / 2);
    }
}

// Handle transaction form submission (add/edit)
async function handleTransactionFormSubmit(e) {
    e.preventDefault();
    if (!currentUser) return showView("login-view");

    const type = document.getElementById("type").value;
    const category = document.getElementById("category").value;
    const date = document.getElementById("date").value;
    const amount = parseFloat(document.getElementById("amount").value);
    const id = document.getElementById("transaction-id").value;

    if (!date || !category || isNaN(amount) || amount <= 0) return alert("Please fill all fields with valid data.");

    try {
        await apiRequest(`/transactions${id ? `/${id}` : ''}`, id ? 'PUT' : 'POST', { type, category, date, amount });
        await fetchTransactions();
        renderTransactionList();
        if (!views.dashboard.classList.contains("hidden")) await prepareView("dashboard-view");
        document.getElementById('transaction-form').reset();
        document.getElementById('transaction-id').value = '';
        document.querySelector('#transaction-form button[type="submit"]').textContent = "Add Transaction";
        document.getElementById('date').valueAsDate = new Date();
    } catch (error) {
        alert('Failed to save transaction: ' + error.message);
    }
}

// Handle transaction edit button click
function handleEditTransaction(e) {
    const transaction = transactions.find(t => t.id === parseInt(e.target.dataset.id));
    if (!transaction) return;
    document.getElementById("transaction-id").value = transaction.id;
    document.getElementById("type").value = transaction.type;
    document.getElementById("category").value = transaction.category;
    document.getElementById("date").value = transaction.date;
    document.getElementById("amount").value = transaction.amount;
    document.querySelector('#transaction-form button[type="submit"]').textContent = "Update Transaction";
    document.getElementById('transaction-form').scrollIntoView({ behavior: 'smooth' });
}

// Handle transaction delete button click
async function handleDeleteTransaction(e) {
    if (!confirm("Are you sure you want to delete this transaction?")) return;
    try {
        await apiRequest(`/transactions/${e.target.dataset.id}`, 'DELETE');
        await fetchTransactions();
        renderTransactionList();
        if (!views.dashboard.classList.contains("hidden")) await prepareView("dashboard-view");
    } catch (error) {
        alert('Failed to delete transaction: ' + error.message);
    }
}

// Render category list in settings view
async function renderCategoryList() {
    const ul = document.getElementById("category-list-settings");
    if (!ul) return;
    const cats = await apiRequest('/categories').catch(() => []);
    ul.innerHTML = cats.filter(cat => cat.id).map(cat => `
        <li style="display:flex; justify-content:space-between; align-items:center; padding:5px; border-bottom:1px solid #eee;">
            ${cat.name}
            <button class="delete-category-btn btn btn-danger btn-sm" data-id="${cat.id}" style="padding:0.25rem 0.5rem; font-size:0.8rem;">Delete</button>
        </li>
    `).join('');
    ul.querySelectorAll('.delete-category-btn').forEach(btn => btn.addEventListener('click', handleDeleteCategory));
}

// Add a new category
async function handleAddCategory() {
    const input = document.getElementById('new-category-name');
    const name = input.value.trim();
    if (!name) return alert("Please enter a category name.");
    try {
        await apiRequest('/categories', 'POST', { name });
        input.value = '';
        await renderCategoryList();
        await populateCategories();
        alert("Category added successfully!");
    } catch (error) {
        alert('Failed to add category: ' + error.message);
    }
}

// Delete a category
async function handleDeleteCategory(e) {
    if (!confirm("Are you sure you want to delete this category?")) return;
    try {
        await apiRequest(`/categories/${e.target.dataset.id}`, 'DELETE');
        await renderCategoryList();
        await populateCategories();
    } catch (error) {
        alert('Failed to delete category: ' + error.message);
    }
}

// Handle user login
async function handleLogin(e) {
    e.preventDefault();
    const email = document.getElementById("login-email").value;
    const password = document.getElementById("login-password").value;
    const msg = document.getElementById("login-message");
    try {
        const response = await apiRequest('/auth/login', 'POST', { email, password });
        localStorage.setItem('jwtToken', response.token);
        currentUser = response.user;
        await initApp();
        msg.style.display = "none";
    } catch (error) {
        msg.textContent = error.message || "Invalid email or password.";
        msg.className = "message error";
        msg.style.display = "block";
    }
}

// Handle user registration
async function handleRegister(e) {
    e.preventDefault();
    const name = document.getElementById("register-name").value;
    const email = document.getElementById("register-email").value;
    const password = document.getElementById("register-password").value;
    const msg = document.getElementById("register-message");

    if (password.length < 6) {
        msg.textContent = "Password must be at least 6 characters.";
        msg.className = "message error";
        msg.style.display = "block";
        return;
    }

    try {
        const response = await apiRequest('/auth/register', 'POST', { name, email, password });
        localStorage.setItem('jwtToken', response.token);
        currentUser = response.user;
        msg.textContent = "Registration successful! Redirecting...";
        msg.className = "message success";
        msg.style.display = "block";
        setTimeout(() => initApp(), 2000);
    } catch (error) {
        msg.textContent = error.message === "Email already in use" ? "Email already in use." : error.message || "Failed to register.";
        msg.className = "message error";
        msg.style.display = "block";
    }
}

// Handle user logout
function handleLogout() {
    currentUser = null;
    localStorage.removeItem('jwtToken');
    transactions = [];
    initApp();
}

// Toggle dark mode
async function toggleDarkMode() {
    try {
        const response = await apiRequest('/users/dark-mode', 'PUT');
        currentUser = response;
        document.body.classList.toggle("dark-mode", currentUser.darkModeEnabled);
    } catch (error) {
        alert('Failed to toggle dark mode: ' + error.message);
    }
}

// Export transactions to CSV
function exportToCsv() {
    if (!transactions.length) return alert("No transactions to export.");
    let csv = "Type,Category,Date,Amount\n";
    transactions.forEach(t => csv += `${t.type},"${t.category.replace(/"/g, '""')}",${t.date},${t.amount.toFixed(2)}\n`);
    const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `transactions_${currentUser.email.split('@')[0]}.csv`;
    link.click();
    URL.revokeObjectURL(url);
}

// Export transactions to PDF
function exportToPdf() {
    if (!transactions.length) return alert("No transactions to export.");
    if (typeof window.jspdf === 'undefined' || !window.jspdf.jsPDF) return alert('jsPDF library is not loaded.');

    const { jsPDF } = window.jspdf;
    const doc = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4' });
    const margin = 15, rowHeight = 8, pageHeight = doc.internal.pageSize.getHeight();
    let y = margin + 10;

    doc.setFontSize(16);
    doc.setFont("helvetica", "bold");
    doc.text(`Transaction History: ${currentUser.name || currentUser.email}`, margin, margin);
    doc.setFontSize(10);
    doc.setFont("helvetica", "normal");

    const col1X = margin, col2X = margin + 25, col3X = margin + 75, col4X = margin + 115;
    function addHeader() {
        doc.setFont("helvetica", "bold");
        doc.text("Type", col1X, y);
        doc.text("Category", col2X, y);
        doc.text("Date", col3X, y);
        doc.text("Amount (₹)", col4X, y);
        doc.line(margin, y + 2, doc.internal.pageSize.getWidth() - margin, y + 2);
        doc.setFont("helvetica", "normal");
        y += rowHeight;
    }
    addHeader();

    transactions.forEach(t => {
        if (y > pageHeight - margin - rowHeight) {
            doc.addPage();
            y = margin;
            addHeader();
        }
        doc.text(t.type.charAt(0).toUpperCase() + t.type.slice(1), col1X, y, { maxWidth: 25 });
        doc.text(t.category, col2X, y, { maxWidth: 45 });
        doc.text(t.date, col3X, y, { maxWidth: 35 });
        doc.text(t.amount.toFixed(2), col4X + 40, y, { align: 'right' });
        y += rowHeight;
    });

    doc.save(`transactions_${currentUser.email.split('@')[0]}_${new Date().toISOString().split('T')[0]}.pdf`);
}

// Initialize the app
async function initApp() {
    const token = localStorage.getItem('jwtToken');
    if (token) {
        try {
            currentUser = await apiRequest('/users/me');
            await fetchTransactions();
            document.body.classList.toggle("dark-mode", currentUser.darkModeEnabled);
        } catch (error) {
            currentUser = null;
            localStorage.removeItem('jwtToken');
        }
    }
    ['logout-link', 'login-nav-link'].forEach(id => {
        const el = document.getElementById(id);
        el.classList.toggle("hidden", id === 'logout-link' ? !currentUser : !!currentUser);
    });
    ['dashboard-link', 'transactions-link', 'reports-link', 'budget-link', 'settings-link'].forEach(id => {
        document.getElementById(id).style.display = currentUser ? '' : 'none';
    });
    await populateCategories();
    if (currentUser) {
        await prepareView("dashboard-view", async () => {
            await fetchTransactions();
            const summary = calculateSummary();
            document.getElementById("db-total-income").textContent = `₹${summary.totalIncome.toFixed(2)}`;
            document.getElementById("db-total-expense").textContent = `₹${summary.totalExpense.toFixed(2)}`;
            document.getElementById("db-current-balance").textContent = `₹${summary.currentMoney.toFixed(2)}`;
            renderExpensePieChart("expense-pie-chart");
        });
    } else {
        showView("login-view");
    }
}

// Event listeners
document.addEventListener('DOMContentLoaded', () => {
    // Navigation links
    document.getElementById("dashboard-link").addEventListener("click", e => {
        e.preventDefault();
        prepareView("dashboard-view", async () => {
            await fetchTransactions();
            const summary = calculateSummary();
            document.getElementById("db-total-income").textContent = `₹${summary.totalIncome.toFixed(2)}`;
            document.getElementById("db-total-expense").textContent = `₹${summary.totalExpense.toFixed(2)}`;
            document.getElementById("db-current-balance").textContent = `₹${summary.currentMoney.toFixed(2)}`;
            renderExpensePieChart("expense-pie-chart");
        });
    });
    document.getElementById("transactions-link").addEventListener("click", e => {
        e.preventDefault();
        prepareView("transactions-view", async () => {
            await populateCategories();
            document.getElementById('transaction-form').reset();
            document.getElementById('transaction-id').value = '';
            document.querySelector('#transaction-form button[type="submit"]').textContent = "Add Transaction";
            document.getElementById('date').valueAsDate = new Date();
            await fetchTransactions();
            renderTransactionList();
        });
    });
    document.getElementById("reports-link").addEventListener("click", e => {
        e.preventDefault();
        prepareView("reports-view");
    });
    document.getElementById("budget-link").addEventListener("click", e => {
        e.preventDefault();
        prepareView("budget-view");
    });
    document.getElementById("settings-link").addEventListener("click", e => {
        e.preventDefault();
        prepareView("settings-view", renderCategoryList);
    });
    document.getElementById("logout-link").addEventListener("click", e => {
        e.preventDefault();
        handleLogout();
    });
    document.getElementById("login-nav-link").addEventListener("click", e => {
        e.preventDefault();
        showView("login-view");
        document.getElementById("login-form").reset();
    });

    // Form submissions and actions
    document.getElementById("login-form").addEventListener("submit", handleLogin);
    document.getElementById("register-form").addEventListener("submit", handleRegister);
    document.getElementById("transaction-form").addEventListener("submit", handleTransactionFormSubmit);
    document.getElementById("show-register-link").addEventListener("click", e => {
        e.preventDefault();
        showView("register-view");
        document.getElementById("register-form").reset();
    });
    document.getElementById("show-login-link").addEventListener("click", e => {
        e.preventDefault();
        showView("login-view");
    });
    document.getElementById("filter-category").addEventListener("change", renderTransactionList);
    document.getElementById("filter-type").addEventListener("change", renderTransactionList);
    document.getElementById("sort-date").addEventListener("change", renderTransactionList);
    document.getElementById("export-csv").addEventListener("click", exportToCsv);
    document.getElementById("export-pdf").addEventListener("click", exportToPdf);
    document.getElementById("toggle-dark-mode").addEventListener("click", toggleDarkMode);
    document.getElementById("add-category-btn").addEventListener("click", handleAddCategory);

    initApp();
});