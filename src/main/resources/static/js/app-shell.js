function setAppTheme(theme) {
    const isLight = theme === "light";
    document.body.classList.toggle("light-mode", isLight);
    document.documentElement.classList.toggle("light-mode", isLight);
}

function applyStoredTheme() {
    setAppTheme(localStorage.getItem("theme"));
}

const APP_SHELL_NAV_ITEMS = [
    {
        key: "chat",
        href: "/html/friends.html",
        icon: "💬",
        label: "Chat",
        onclick: "return openChatTab(event)"
    },
    {
        key: "friends",
        href: "/html/friends.html",
        icon: "👥",
        label: "Friends"
    },
    {
        key: "profile",
        href: "/html/profile.html",
        icon: "👤",
        label: "Profile"
    },
    {
        key: "settings",
        href: "/html/settings.html",
        icon: "⚙️",
        label: "Settings"
    }
];

function renderAppBottomNav() {
    const mount = document.querySelector("[data-app-nav]");
    if (!mount) {
        return;
    }

    const activeKey = document.body.dataset.nav || "";
    const nav = document.createElement("nav");
    nav.className = "nav-bar";
    nav.setAttribute("aria-label", "Primary");

    nav.innerHTML = APP_SHELL_NAV_ITEMS.map((item) => {
        const activeClass = item.key === activeKey ? " active" : "";
        const onclickAttr = item.onclick ? ` onclick="${item.onclick}"` : "";

        return `
            <a class="nav-item${activeClass}" href="${item.href}"${onclickAttr}>
                <span class="nav-icon">${item.icon}</span>
                <span class="nav-text">${item.label}</span>
            </a>
        `;
    }).join("");

    mount.replaceWith(nav);
}

applyStoredTheme();
window.addEventListener("storage", function (event) {
    if (event.key === "theme") {
        setAppTheme(event.newValue);
    }
});
document.addEventListener("DOMContentLoaded", renderAppBottomNav);
