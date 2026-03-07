document.addEventListener("DOMContentLoaded", loadProfile);

async function loadProfile() {
    try {
        const token = localStorage.getItem("token");

        const params = new URLSearchParams(window.location.search);
        let profileUsername = params.get("user");

        let response;

        if (profileUsername) {
            response = await fetch("/api/user/" + profileUsername, {
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
            console.error("Failed to load profile");
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