// ===== Theme =====
function toggleTheme() {
    document.body.classList.toggle("light-mode");
    localStorage.setItem("theme",
        document.body.classList.contains("light-mode") ? "light" : "dark");
}

// ===== Navigation =====
function goBack() {
    if (document.referrer) {
        window.history.back();
        return;
    }

    window.location.href = "/html/friends.html";
}

// ===== About =====
function showAbout() {
    alert("ChatApp v1.0\nBuilt with Spring Boot & WebSocket.");
}

// ===== Account =====
function deleteAccount() {
    if (!confirm("Are you sure?")) return;

    apiFetch("/api/auth/delete-account", { method: "DELETE" })
    .then(() => {
        alert("Account deleted");
        logout();
    });
}
