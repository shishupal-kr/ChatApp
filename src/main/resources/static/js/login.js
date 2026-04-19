async function readAuthMessage(response, fallbackMessage) {
    try {
        const data = await response.json();
        if (data && typeof data.message === "string" && data.message.trim()) {
            return data.message;
        }
    } catch (e) {
        try {
            const text = await response.text();
            if (text && text.trim()) {
                return text;
            }
        } catch (ignored) {
        }
    }

    return fallbackMessage;
}

function showLoginMessage(message) {
    var messageBox = document.getElementById("loginMessage");
    if (!messageBox) {
        return;
    }

    messageBox.innerText = message || "";
}

window.addEventListener("DOMContentLoaded", function () {
    var params = new URLSearchParams(window.location.search);
    var message = params.get("message");

    if (message) {
        showLoginMessage(message);
        window.history.replaceState({}, document.title, window.location.pathname);
    }
});

// ===== Auth =====
async function login() {
    var username = document.getElementById("username").value.trim();
    var password = document.getElementById("password").value;
    var error = document.getElementById("errorMessage");

    showLoginMessage("");
    if (error) {
        error.innerText = "";
    }

    if (!username || !password) {
        if (error) {
            error.innerText = "Username and password are required";
        }
        return;
    }

    try {
        var res = await fetch("/api/auth/login", {
            method: "POST",
            headers: {"Content-Type":"application/json"},
            body: JSON.stringify({username, password})
        });

        if (!res.ok) {
            if (error) {
                error.innerText = await readAuthMessage(res, "Login failed. Please try again.");
            }
            return;
        }

        var token = await res.text();

        if (!token || token.length < 20) {
            if (error) {
                error.innerText = "Login failed. Please try again.";
            }
            return;
        }

        localStorage.setItem("token", token);
        localStorage.setItem("username", username);

        window.location.href = "/html/friends.html";
    } catch (e) {
        if (error) {
            error.innerText = "Unable to login right now. Please check your connection and try again.";
        }
    }
}

// ===== Navigation =====
function goToRegister() {
    window.location.href = "/html/register.html";
}
