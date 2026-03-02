function goBack() {
    window.history.back();
}

function toggleTheme() {
    document.body.classList.toggle("light-mode");
    localStorage.setItem("theme",
        document.body.classList.contains("light-mode") ? "light" : "dark");
}

function showAbout() {
    alert("ChatApp v1.0\nBuilt with Spring Boot & WebSocket.");
}

function deleteAccount() {
    if (!confirm("Are you sure?")) return;

    fetch("/api/auth/delete-account", {
        method: "DELETE",
        headers: {
            "Authorization": "Bearer " + localStorage.getItem("token")
        }
    }).then(() => {
        alert("Account deleted");
        logout();
    });
}