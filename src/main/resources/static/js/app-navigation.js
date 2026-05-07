function isNativeInteractive(element) {
    return !!(element && element.closest("button, a, input, textarea, select, summary"));
}

function isTypingTarget(target) {
    if (!target) {
        return false;
    }

    const tagName = target.tagName;
    return target.isContentEditable ||
        tagName === "INPUT" ||
        tagName === "TEXTAREA" ||
        tagName === "SELECT";
}

function enhanceKeyboardControls() {
    document.querySelectorAll("[onclick]").forEach(function (element) {
        if (isNativeInteractive(element)) {
            return;
        }

        if (!element.hasAttribute("tabindex")) {
            element.setAttribute("tabindex", "0");
        }

        if (!element.hasAttribute("role")) {
            element.setAttribute("role", "button");
        }
    });
}

function activateOnKeyboard(event) {
    if (event.defaultPrevented || (event.key !== "Enter" && event.key !== " ")) {
        return;
    }

    const target = event.target.closest("[onclick]");
    if (!target || isNativeInteractive(target)) {
        return;
    }

    event.preventDefault();
    target.click();
}

const appShellBackState = {
    activeKeys: new Set(),
    suppressNextPop: false
};

function closeActiveUiLayer() {
    const forwardModal = document.getElementById("forwardModal");
    if (forwardModal && forwardModal.style.display !== "none" && typeof closeForwardModal === "function") {
        closeForwardModal();
        return true;
    }

    const selectionHeader = document.getElementById("selectionHeader");
    if (selectionHeader && selectionHeader.style.display !== "none" && typeof clearSelection === "function") {
        clearSelection();
        return true;
    }

    const replyBar = document.getElementById("replyBar");
    if (replyBar && replyBar.style.display !== "none" && typeof cancelReply === "function") {
        cancelReply();
        return true;
    }

    if (typeof searchOpen !== "undefined" && searchOpen && typeof closeSearch === "function") {
        closeSearch();
        return true;
    }

    return false;
}

function triggerBackControl() {
    const backControl = document.querySelector(".chat-back-btn, .back-btn, .back");
    if (backControl) {
        backControl.click();
        return true;
    }

    return false;
}

function handleGlobalKeyboardShortcuts(event) {
    if (event.defaultPrevented || isTypingTarget(event.target) || event.key !== "Escape") {
        return;
    }

    if (closeActiveUiLayer() || triggerBackControl()) {
        event.preventDefault();
    }
}

function pushUiBackState(key) {
    if (!key || appShellBackState.activeKeys.has(key)) {
        return;
    }

    appShellBackState.activeKeys.add(key);
    window.history.pushState({
        ...(window.history.state || {}),
        __appShellUi: key
    }, document.title, window.location.href);
}

function releaseUiBackState(key) {
    if (!key || !appShellBackState.activeKeys.has(key)) {
        return;
    }

    appShellBackState.activeKeys.delete(key);

    if (window.history.state && window.history.state.__appShellUi === key) {
        appShellBackState.suppressNextPop = true;
        window.history.back();
    }
}

function handleAppShellPopState() {
    if (appShellBackState.suppressNextPop) {
        appShellBackState.suppressNextPop = false;
        return;
    }

    closeActiveUiLayer();
}

window.appShellBack = {
    push: pushUiBackState,
    release: releaseUiBackState
};

document.addEventListener("DOMContentLoaded", enhanceKeyboardControls);
document.addEventListener("keydown", activateOnKeyboard);
document.addEventListener("keydown", handleGlobalKeyboardShortcuts);
window.addEventListener("popstate", handleAppShellPopState);
