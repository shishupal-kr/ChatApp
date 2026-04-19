async function readErrorMessage(response, fallbackMessage) {
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

// ===== Register =====
async function register() {
    var username = document.getElementById("username").value.trim();
    var email = document.getElementById("email").value.trim();
    var password = document.getElementById("password").value;
    var confirmPassword = document.getElementById("confirmPassword").value;
    var error = document.getElementById("errorMessage");

    error.innerText = "";

    // ===== Validation =====
    var firstName = document.getElementById("firstName").value.trim();
    var lastName = document.getElementById("lastName").value.trim();
    var age = document.getElementById("age").value.trim();
    var genderElement = document.getElementById("gender");
    var gender = genderElement ? genderElement.value.trim() : "";

    if (!firstName || !lastName || !age || !username || !email || !password || !confirmPassword) {
        error.innerText = "All fields are required";
        return;
    }

    if (!gender) {
        error.innerText = "Please select gender";
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

    if (!Number.isInteger(parseInt(age, 10)) || parseInt(age, 10) <= 0) {
        error.innerText = "Please enter a valid age";
        return;
    }

    // ===== API Call =====
    try {
        const response = await fetch("/api/auth/register", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                firstName: firstName,
                lastName: lastName,
                age: parseInt(age, 10),
                gender: gender,
                username: username,
                email: email,
                password: password
            })
        });

        if (!response.ok) {
            error.innerText = await readErrorMessage(response, "Registration failed. Please try again.");
            return;
        }

        window.location.href = "/html/login.html?message=" + encodeURIComponent("Registration successful. Please log in.");
    } catch (e) {
        error.innerText = "Unable to register right now. Please check your connection and try again.";
    }
}

// ===== Navigation =====
function goBack() {
    if (document.referrer) {
        window.history.back();
        return;
    }

    window.location.href = "/html/login.html";
}

function goToLogin() {
    window.location.href = "/html/login.html";
}
