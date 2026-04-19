// ===== Register =====
function register() {
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

    console.log("Gender value:", gender); // DEBUG

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

    // ===== API Call =====
    fetch("/api/auth/register", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            firstName: firstName,
            lastName: lastName,
            age: parseInt(age),
            gender: gender || "Male",
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
