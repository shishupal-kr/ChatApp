// ===== Auth Helpers =====
function getToken() {
    return localStorage.getItem("token");
}

function getUser() {
    return localStorage.getItem("username");
}

// ===== Auth Guard =====
// Runs immediately on load — redirects if no token/username in storage
(function guardAuth() {
    if (!getToken() || !getUser()) {
        window.location.href = "/html/login.html";
    }
})();

// ===== Session Validation =====
// Calls /api/auth/me to confirm the token is still valid on the server.
// Redirects to login if the server returns 401 (expired/invalid token).
async function validateSession() {
    try {
        const res = await fetch("/api/auth/me", {
            headers: { Authorization: "Bearer " + getToken() }
        });
        if (res.status === 401) {
            localStorage.clear();
            window.location.href = "/html/login.html";
        }
    } catch (e) {
        // Network error — stay on page, don't force logout
    }
}

// ===== Authenticated Fetch =====
// Drop-in replacement for fetch() that:
//   - Automatically adds the Authorization header
//   - Redirects to login on 401 (expired token)
async function apiFetch(url, options) {
    options = options || {};
    options.headers = Object.assign(
        { Authorization: "Bearer " + getToken() },
        options.headers || {}
    );

    const res = await fetch(url, options);

    if (res.status === 401) {
        localStorage.clear();
        window.location.href = "/html/login.html";
        throw new Error("Session expired");
    }

    return res;
}

// ===== Logout =====
function logout() {
    localStorage.clear();
    window.location.href = "/html/login.html";
}
