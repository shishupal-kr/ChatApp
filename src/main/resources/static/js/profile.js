// ===== Init =====
document.addEventListener("DOMContentLoaded", loadProfile);

// ===== Data Load =====
async function loadProfile() {
    try {
        const token = localStorage.getItem("token");

        const params = new URLSearchParams(window.location.search);
        let profileUsername = params.get("user");

        let response;

        if (profileUsername) {
            response = await fetch("/api/user/" + encodeURIComponent(profileUsername), {
                headers: {
                    "Authorization": "Bearer " + token
                }
            });
        } else {
            response = await fetch("/api/auth/me", {
                headers: {
                    "Authorization": "Bearer " + token
                }
            });
        }

        if (!response.ok) {
            console.error("Failed to load profile. Status:", response.status);
            return;
        }

        const user = await response.json();

        document.getElementById("displayName").innerText =
            user.fullName || user.username;

        document.getElementById("profileUsername").innerText =
            "@" + user.username;

        document.getElementById("profileAvatar").innerText =
            user.username.charAt(0).toUpperCase();

        const ring = document.getElementById("statusRing");

        if (user.status === "ONLINE") {
            ring.classList.add("online");
            ring.classList.remove("offline");
        } else {
            ring.classList.add("offline");
            ring.classList.remove("online");
        }

    } catch (error) {
        console.error("Profile load error:", error);
    }
}

// ===== Navigation =====
function goBack() {
    const params = new URLSearchParams(window.location.search);
    const profileUsername = params.get("user");

    if (profileUsername) {
        window.location.href = "/html/chat.html?user=" + encodeURIComponent(profileUsername);
        return;
    }

    if (document.referrer) {
        window.history.back();
        return;
    }

    window.location.href = "/html/friends.html";
}
