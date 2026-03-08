// ===== Auth =====
function login() {
    var username = document.getElementById("username").value.trim();
    var password = document.getElementById("password").value;

    fetch("/api/auth/login", {
        method: "POST",
        headers: {"Content-Type":"application/json"},
        body: JSON.stringify({username, password})
    })
    .then(res => res.text())
    .then(token => {

        if (!token || token.length < 20) {
            alert("Login failed");
            return;
        }

        localStorage.setItem("token", token);
        localStorage.setItem("username", username);

        window.location.href = "/html/friends.html";
    });
}

// ===== Navigation =====
function goToRegister() {
    window.location.href = "/html/register.html";
}
