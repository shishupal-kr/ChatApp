// ===== Init =====
document.addEventListener("DOMContentLoaded", loadProfile);

// ===== Data Load =====
async function loadProfile() {
    try {
        const params = new URLSearchParams(window.location.search);
        let profileUsername = params.get("user");

        let response;

        if (profileUsername) {
            response = await apiFetch("/api/user/" + encodeURIComponent(profileUsername));
        } else {
            response = await apiFetch("/api/auth/me");
        }

        if (!response || !response.ok) {
            console.error("Failed to load profile. Status:", response && response.status);
            return;
        }

        const user = await response.json();
        const isOwnProfile = !profileUsername;

        document.getElementById("displayName").innerText =
            user.fullName || user.username;

        document.getElementById("profileUsername").innerText =
            "@" + user.username;

        document.getElementById("profileAvatar").innerText =
            user.username.charAt(0).toUpperCase();

        const emailEl = document.getElementById("profileEmail");
        if (emailEl) {
            if (isOwnProfile && user.email) {
                emailEl.innerText = user.email;
                emailEl.hidden = false;
            } else {
                emailEl.hidden = true;
            }
        }

        const ring = document.getElementById("statusRing");
        if (ring) {
            ring.classList.remove("online", "offline");
        }

        if (user.status === "ONLINE") {
            ring.classList.add("online");
            ring.title = "Online";
        } else {
            ring.classList.add("offline");
            ring.title = "Offline";
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
