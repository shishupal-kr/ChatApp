// ===== Register =====
function register() {
    var username = document.getElementById("username").value.trim();
    var email = document.getElementById("email").value.trim();
    var password = document.getElementById("password").value;
    var confirmPassword = document.getElementById("confirmPassword").value;
    var error = document.getElementById("errorMessage");

    error.innerText = "";

    // ===== Validation =====
    if (!username || !email || !password || !confirmPassword) {
        error.innerText = "All fields are required";
        return;
    }

    if (username.length < 4) {
        error.innerText = "Username must be at least 4 characters";
        return;
    }

    if (!email.includes("@")) {
        error.innerText = "Invalid! email address should have @ symbol";
        return;
    }

    if (password.length < 6) {
        error.innerText = "Password must be at least 6 characters";
        return;
    }

    if (password !== confirmPassword) {
        error.innerText = "Passwords do not match";
        return;
    }

    // ===== API Call =====
    fetch("/api/auth/register", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            username: username,
            email: email,
            password: password
        })
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("Registration failed");
            }
            return response.text();
        })
        .then(() => {
            alert("Registration successful! Please login.");
            window.location.href = "/html/login.html";
        })
        .catch(() => {
            error.innerText = "Username or email may already exist";
        });
}

// ===== Navigation =====
function goToLogin() {
    window.location.href = "/html/login.html";
}
