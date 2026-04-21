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
    nav.className = "app-bottom-nav";
    nav.setAttribute("aria-label", "Primary");

    nav.innerHTML = APP_SHELL_NAV_ITEMS.map((item) => {
        const activeClass = item.key === activeKey ? " active" : "";
        const onclickAttr = item.onclick ? ` onclick="${item.onclick}"` : "";

        return `
            <a class="app-nav-item${activeClass}" href="${item.href}"${onclickAttr}>
                <span class="app-nav-icon">${item.icon}</span>
                <span class="app-nav-label">${item.label}</span>
            </a>
        `;
    }).join("");

    mount.replaceWith(nav);
}

document.addEventListener("DOMContentLoaded", renderAppBottomNav);
