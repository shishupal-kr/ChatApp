// ===== State =====
window.editingMessageId = null;
let replyingToMessageId = null;
let selectedMessages = new Set();
var currentUser = getUser();
var token = getToken();
var readTriggered = false;
let currentPage = 0;
let pageSize = 20;
let loading = false;

function markConversationAsRead() {
    if (!stompClient || !stompClient.connected || !selectedUser) {
        return;
    }

    stompClient.send("/app/read", {}, JSON.stringify({
        receiver: selectedUser
    }));

    readTriggered = true;
}

// ===== Route Params =====
var params = new URLSearchParams(window.location.search);
var selectedUser = params.get("user");

if (selectedUser) {
    sessionStorage.setItem("lastChatUser", selectedUser);
}

var stompClient = null;

// ===== WebSocket =====
function connectWebSocket() {
    if (stompClient) {
        console.log("WebSocket already created");
        return;
    }

    var socket = new SockJS('/chat');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect(
        { Authorization: "Bearer " + token },
        function () {

            stompClient.subscribe("/user/queue/messages", function (message) {
                var msg = JSON.parse(message.body);
                renderMessage(msg);

                if (msg.sender === selectedUser && msg.status !== "READ") {
                    markConversationAsRead();
                }
            });

            stompClient.subscribe("/topic/online-users", function (message) {
                var onlineUsers = JSON.parse(message.body);
                updateOnlineIndicator(onlineUsers);
            });

            // Receive current online users list on fresh connect
            stompClient.subscribe("/user/queue/online-users", function (message) {
                var onlineUsers = JSON.parse(message.body);
                updateOnlineIndicator(onlineUsers);
            });

            stompClient.subscribe("/user/queue/read-receipt", function (message) {
                var messageId = message.body;
                var tickElement = document.querySelector(
                    "[data-id='" + messageId + "'] .message-ticks"
                );
                if (tickElement) {
                    tickElement.innerText = "✔✔";
                    tickElement.style.color = "#4fc3f7";
                }
            });

            stompClient.subscribe("/user/queue/delivered", function (message) {
                var messageId = message.body;
                var tickElement = document.querySelector(
                    "[data-id='" + messageId + "'] .message-ticks"
                );
                if (tickElement) {
                    tickElement.innerText = "✔✔";
                    tickElement.style.color = "#999";
                }
            });

            // Delete message subscription
            stompClient.subscribe("/user/queue/delete", function (message) {
                var messageId = JSON.parse(message.body);
                removeMessageFromUI(messageId);
            });

            // Edit message subscription
            stompClient.subscribe("/user/queue/edit", function (message) {
                var updatedMsg = JSON.parse(message.body);
                updateEditedMessageInUI(updatedMsg);
            });


            // Typing indicator subscription
            stompClient.subscribe("/user/queue/typing", function (message) {
                var data = JSON.parse(message.body);

                if (data.sender === selectedUser) {
                    showTypingIndicator();
                }
            });

            loadHistory();
        }
    );
}

if (!selectedUser) {
    window.location.href = "/html/friends.html";
}

function goBack() {
    window.location.href = "/html/friends.html";
}

// ===== Header =====
// Set header name and profile initial
document.getElementById("chatTitle").innerText = selectedUser;
document.getElementById("profileCircle").innerText = selectedUser.charAt(0).toUpperCase();

function openProfilePage() {
    window.location.href = "/html/profile.html?user=" + selectedUser;
}

// ===== History =====
function loadHistory() {

    if (loading) return;
    loading = true;

    apiFetch(`/api/chat/history/${selectedUser}?page=${currentPage}&size=${pageSize}`)
    .then(res => res.json())
    .then(messages => {

        var container = document.getElementById("messages");

        // If first page, clear container
        if (currentPage === 0) {
            container.innerHTML = "";
        }

        const previousHeight = container.scrollHeight;

        messages.reverse().forEach(function(msg) {
            renderMessage(msg, currentPage !== 0);
        });

        const newHeight = container.scrollHeight;

        if (currentPage !== 0) {
            container.scrollTop = newHeight - previousHeight;
        }

        currentPage++;
        loading = false;

        // Scroll to bottom only first time
        if (currentPage === 1) {
            container.scrollTop = container.scrollHeight;
        }

        // Mark as READ only once when chat is opened
        if (!readTriggered && currentPage === 1) {

            var hasUnread = messages.some(function(m) {
                return m.sender === selectedUser &&
                       m.status !== "READ";
            });

            if (hasUnread) {
                markConversationAsRead();
            }
        }
    })
    .catch(() => {
        loading = false;
    });
}

// ===== Typing =====
// Send typing indicator when user types
var messageInput = document.getElementById("messageInput");

var typingTimeout;

messageInput.addEventListener("input", function () {

    // Send typing event
    stompClient.send("/app/typing", {}, JSON.stringify({
        receiver: selectedUser
    }));

    // Optional: prevent spamming (basic debounce)
    clearTimeout(typingTimeout);
    typingTimeout = setTimeout(function () {
        // You can later send "stopped typing" here if needed
    }, 1000);

});

// ===== Send =====
function sendMessage() {

    var input = document.getElementById("messageInput");
    var content = input.value.trim();
    if (!content) return;

    // If editing an existing message
    if (window.editingMessageId) {
        stompClient.send("/app/edit", {}, JSON.stringify({
            id: window.editingMessageId,
            content: content
        }));

        window.editingMessageId = null;
        input.value = "";
        return;
    }

    // Normal send
    stompClient.send("/app/private-message", {}, JSON.stringify({
        receiver: selectedUser,
        content: content,
        replyToId: replyingToMessageId
    }));

    input.value = "";
    cancelReply();
}

// ===== Render =====
function renderMessage(msg, prepend = false) {

    var wrapper = document.createElement("div");
    if (msg.id) {
        wrapper.setAttribute("data-id", msg.id);
    }
    wrapper.classList.add("glass-message");

    const isSent = (msg.sender && currentUser) &&
                   msg.sender.toLowerCase().trim() === currentUser.toLowerCase().trim();

    if (isSent)
        wrapper.classList.add("sent");
    else
        wrapper.classList.add("received");

    var bubble = document.createElement("div");
    bubble.classList.add("glass-bubble");
    // If message is a reply, show replied message preview above content
    if (msg.replyToContent) {
        const replyBox = document.createElement("div");
        replyBox.classList.add("reply-preview-bubble");

        replyBox.innerHTML = `
            <div class="reply-sender">${msg.replyToSender || ""}</div>
            <div class="reply-text">${msg.replyToContent}</div>
        `;

        bubble.appendChild(replyBox);
    }

    const messageText = document.createElement("div");
    messageText.classList.add("message-text");
    messageText.innerText = msg.content;
    bubble.appendChild(messageText);

    // Long press to select (mobile + desktop)
    bubble.style.cursor = "pointer";

    let pressTimer;

    function startPress(e) {
        e.stopPropagation();
        pressTimer = setTimeout(function () {
            toggleMessageSelection(wrapper, msg.id);
        }, 500); // 500ms long press
    }

    function cancelPress() {
        clearTimeout(pressTimer);
    }

    bubble.addEventListener("mousedown", startPress);
    bubble.addEventListener("touchstart", startPress);

    bubble.addEventListener("mouseup", cancelPress);
    bubble.addEventListener("mouseleave", cancelPress);
    bubble.addEventListener("touchend", cancelPress);

    // Meta (time + ticks)
    var meta = document.createElement("div");
    meta.classList.add("message-meta");

    var time = document.createElement("span");
    time.classList.add("message-time");

    var date = msg.timestamp ? new Date(msg.timestamp) : new Date();
    var formattedTime = date.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});

    if (msg.edited === true) {
        time.innerHTML = '<span class="edited-label">edited</span> ' + formattedTime;
    } else {
        time.innerText = formattedTime;
    }

    meta.appendChild(time);

    if (isSent) {

        var ticks = document.createElement("span");
        ticks.classList.add("message-ticks");

        // Default if backend does not send status yet
        var status = msg.status || "SENT";

        if (status === "SENT") {
            ticks.innerText = "✔"; // 1 tick
            ticks.style.color = "#999";
        }
        else if (status === "DELIVERED") {
            ticks.innerText = "✔✔"; // 2 grey ticks
            ticks.style.color = "#999";
        }
        else if (status === "READ") {
            ticks.innerText = "✔✔"; // 2 blue ticks
            ticks.style.color = "#4fc3f7";
        }

        meta.appendChild(ticks);
    }

    wrapper.appendChild(bubble);
    wrapper.appendChild(meta);

    var container = document.getElementById("messages");

    if (prepend) {
        container.prepend(wrapper);
    } else {
        container.appendChild(wrapper);
        container.scrollTop = container.scrollHeight;
    }

}

// ===== Selection =====
function toggleMessageSelection(wrapper, messageId) {
    if (selectedMessages.has(messageId)) {
        selectedMessages.delete(messageId);
        wrapper.classList.remove("selected");
    } else {
        selectedMessages.add(messageId);
        wrapper.classList.add("selected");
    }
    updateSelectionHeader();
}

function removeMessageFromUI(id) {
    var element = document.querySelector("[data-id='" + id + "']");
    if (element) {
        element.remove();
    }
}

function updateEditedMessageInUI(msg) {
    var wrapper = document.querySelector("[data-id='" + msg.id + "']");
    if (!wrapper) return;

    var bubble = wrapper.querySelector(".glass-bubble");
    if (!bubble) return;

    var textDiv = bubble.querySelector(".message-text");
    if (textDiv) {
        textDiv.innerText = msg.content;
    }

    var timeSpan = wrapper.querySelector(".message-time");
    if (timeSpan) {
        var date = msg.timestamp ? new Date(msg.timestamp) : new Date();
        var formattedTime = date.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
        timeSpan.innerHTML = '<span class="edited-label">edited</span> ' + formattedTime;
    }
}

// ===== Selection Header =====
function updateSelectionHeader() {
    const normalHeader = document.getElementById("normalHeader");
    const selectionHeader = document.getElementById("selectionHeader");
    const selectionCount = document.getElementById("selectionCount");

    if (!normalHeader || !selectionHeader) return;

    if (selectedMessages.size > 0) {
        normalHeader.style.display = "none";
        selectionHeader.style.display = "flex";
        selectionCount.innerText = selectedMessages.size;
    } else {
        normalHeader.style.display = "flex";
        selectionHeader.style.display = "none";
    }
}

function clearSelection() {
    selectedMessages.clear();
    document.querySelectorAll(".glass-message.selected").forEach(el => {
        el.classList.remove("selected");
    });
    updateSelectionHeader();
}

function replySelected() {
    if (selectedMessages.size !== 1) return;

    const id = [...selectedMessages][0];
    const bubble = document.querySelector("[data-id='" + id + "'] .glass-bubble");

    if (!bubble) return;

    startReply(id, bubble.innerText);
    clearSelection();
}

function copySelected() {
    const texts = [];

    selectedMessages.forEach(id => {
        const bubble = document.querySelector("[data-id='" + id + "'] .glass-bubble");
        if (bubble) texts.push(bubble.innerText);
    });

    if (texts.length > 0) {
        navigator.clipboard.writeText(texts.join("\n"));
    }

    clearSelection();
}

function deleteSelected() {
    selectedMessages.forEach(id => {
        stompClient.send("/app/delete", {}, JSON.stringify({ id: id }));
    });

    clearSelection();
}

function forwardSelected() {
    if (selectedMessages.size === 0) return;

    const texts = [];

    selectedMessages.forEach(id => {
        const bubble = document.querySelector("[data-id='" + id + "'] .message-text");
        if (bubble) texts.push(bubble.innerText);
    });

    if (texts.length === 0) return;

    window.forwardMessages = texts;
    openForwardModal();
    clearSelection();
}

// ===== Forward Modal =====
function openForwardModal() {
    apiFetch("/api/users")
    .then(res => res.json())
    .then(users => {
        const list = document.getElementById("forwardUserList");
        if (!list) return;

        list.innerHTML = "";

        users.forEach(user => {
            if (user === currentUser) return;

            const item = document.createElement("div");
            item.className = "forward-user-item";
            item.innerText = user;

            item.onclick = function () {
                sendForward(user);
            };

            list.appendChild(item);
        });

        document.getElementById("forwardModal").style.display = "flex";
    });
}

function closeForwardModal() {
    const modal = document.getElementById("forwardModal");
    if (modal) modal.style.display = "none";
}

function sendForward(receiver) {
    if (!window.forwardMessages) return;

    window.forwardMessages.forEach(text => {
        stompClient.send("/app/private-message", {}, JSON.stringify({
            receiver: receiver,
            content: text,
            replyToId: null
        }));
    });

    closeForwardModal();
}

function editSelected() {

    if (selectedMessages.size !== 1) return;

    const id = [...selectedMessages][0];
    const bubble = document.querySelector("[data-id='" + id + "'] .glass-bubble");

    if (!bubble) return;

    const oldText = bubble.innerText;

    document.getElementById("messageInput").value = oldText;
    document.getElementById("messageInput").focus();

    // Store editing id
    window.editingMessageId = id;

    clearSelection();
}

// ===== Online Indicator =====
function updateOnlineIndicator(onlineUsers) {

    var profileWrapper = document.getElementById("profileWrapper");

    if (!profileWrapper) return;

    if (onlineUsers.includes(selectedUser)) {
        profileWrapper.classList.remove("offline");
        profileWrapper.classList.add("online");
    } else {
        profileWrapper.classList.remove("online");
        profileWrapper.classList.add("offline");
    }
}

// ===== Init =====
document.addEventListener("DOMContentLoaded", function () {
    validateSession();
    connectWebSocket();

    const input = document.getElementById("messageInput");

    if (!input) return;

    input.addEventListener("keydown", function (event) {

        if (event.key === "Enter") {

            if (event.shiftKey) {
                return; // Shift + Enter → new line
            }

            event.preventDefault();
            sendMessage();
        }

    });

    const messagesDiv = document.getElementById("messages");

    if (messagesDiv) {
        messagesDiv.addEventListener("scroll", function () {
            if (messagesDiv.scrollTop <= 50 && !loading) {
                loadHistory();
            }
        });
    }

});

// ===== Reply =====
function startReply(messageId, content) {
    replyingToMessageId = messageId;

    var preview = document.getElementById("replyPreview");
    var bar = document.getElementById("replyBar");

    if (preview && bar) {
        preview.innerText = content;
        bar.style.display = "flex";
    }
}

function cancelReply() {
    replyingToMessageId = null;

    var bar = document.getElementById("replyBar");
    if (bar) {
        bar.style.display = "none";
    }
}

function showTypingIndicator() {
    var header = document.getElementById("chatTitle");
    if (!header) return;

    header.innerText = selectedUser + " is typing...";

    clearTimeout(window.typingIndicatorTimeout);
    window.typingIndicatorTimeout = setTimeout(function () {
        header.innerText = selectedUser;
    }, 1500);
}

// ===== Cleanup =====
window.addEventListener("beforeunload", function () {
    if (stompClient && stompClient.connected) {
        stompClient.disconnect();
    }
    stompClient = null;
});
