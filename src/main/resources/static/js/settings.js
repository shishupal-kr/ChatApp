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
    window.location.href = "/html/about.html";
}

async function updatePassword() {
    var currentPassword = document.getElementById("currentPassword");
    var newPassword = document.getElementById("newPassword");

    if (!currentPassword.value.trim() || !newPassword.value.trim()) {
        alert("Current password and new password are required");
        return;
    }

    try {
        var res = await apiFetch("/api/auth/change-password", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                currentPassword: currentPassword.value.trim(),
                newPassword: newPassword.value.trim()
            })
        });

        if (!res.ok) {
            alert((await res.text()) || "Password update failed");
            return;
        }

        alert("Password updated");
        currentPassword.value = "";
        newPassword.value = "";
    } catch (e) {
        alert("Password update failed");
    }
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
