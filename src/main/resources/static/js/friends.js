let debounceTimer;
var currentUser = localStorage.getItem("username");
var token = localStorage.getItem("token");
var stompClient = null;

if (!currentUser || !token) {
  window.location.href = "/html/login.html";
}


// toggle menu dropdown
function toggleMenu() {
  const menu = document.getElementById("menuDropdown");
  menu.classList.toggle("show");
}

// Close menu when clicking outside
document.addEventListener("click", function (event) {
  const wrapper = document.querySelector(".menu-wrapper");
  if (!wrapper.contains(event.target)) {
    document.getElementById("menuDropdown").classList.remove("show");
  }
});

// Load conversations immediately (independent of WebSocket)
fetch("/api/chat/conversations", {
  headers: { Authorization: "Bearer " + token }
})
  .then(res => res.json())
  .then(data => {
    renderConversations(data);

    // Load unread counts initially
    fetch("/api/chat/unread-counts", {
      headers: { Authorization: "Bearer " + token }
    })
    .then(res => res.json())
    .then(unreadMap => {
      Object.keys(unreadMap).forEach(username => {
        var wrapper = document.querySelector("[data-user='" + username + "']");
        if (!wrapper) return;

        var parentItem = wrapper.closest(".friend-item");
        var badge = parentItem.querySelector(".unread-badge");

        if (badge) {
          badge.innerText = unreadMap[username];
        } else {
          var newBadge = document.createElement("span");
          newBadge.className = "unread-badge";
          newBadge.innerText = unreadMap[username];
          parentItem.querySelector(".friend-right").appendChild(newBadge);
        }
      });
    });

    // After conversations render, fetch current online users
    return fetch("/api/chat/online-users", {
      headers: { Authorization: "Bearer " + localStorage.getItem("token")}
    });
  })
  .then(res => res.json())
  .then(users => updateOnlineStatus(users));

function connectWebSocket() {
  if (stompClient) {
    return;
  }

  var socket = new SockJS('/chat');
  stompClient = Stomp.over(socket);
  stompClient.debug = null;

  stompClient.connect(
    { Authorization: "Bearer " + token },
    function () {
      stompClient.subscribe("/topic/online-users", function (message) {
        var onlineUsers = JSON.parse(message.body);
        updateOnlineStatus(onlineUsers);
      });

      stompClient.subscribe("/user/queue/online-users", function (message) {
        var onlineUsers = JSON.parse(message.body);
        updateOnlineStatus(onlineUsers);
      });

      // Real-time unread badge update
      stompClient.subscribe("/user/queue/unread-update", function (message) {
        var sender = message.body;

        var container = document.getElementById("friendList");
        var wrapper = container.querySelector("[data-user='" + sender + "']");

        if (!wrapper) return;

        var parentItem = wrapper.closest(".friend-item");
        var badge = parentItem.querySelector(".unread-badge");

        if (badge) {
          var count = parseInt(badge.innerText || "0");
          badge.innerText = count + 1;
        } else {
          var newBadge = document.createElement("span");
          newBadge.className = "unread-badge";
          newBadge.innerText = "1";
          parentItem.querySelector(".friend-right").appendChild(newBadge);
        }
      });
    }
  );
}

document.addEventListener("DOMContentLoaded", function () {
  connectWebSocket();
});

window.addEventListener("beforeunload", function () {
  if (stompClient && stompClient.connected) {
    stompClient.disconnect();
  }
  stompClient = null;
});



  // Render conversations
function renderConversations(conversations) {

  var container = document.getElementById("friendList");
  container.innerHTML = "";

  if (!conversations || conversations.length === 0) {
    var empty = document.createElement("div");
    empty.className = "empty-state";
    empty.innerText = "No conversations yet";
    container.appendChild(empty);
    return;
  }

  conversations.forEach(function(conv) {

    var div = document.createElement("div");
    div.className = "friend-item";

    // Format timestamp to HH:MM
    var time = "";
    if (conv.timestamp) {
      var date = new Date(conv.timestamp);
      time = date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }

    // Unread count (if backend not sending yet, default 0)
    var unread = conv.unreadCount || 0;
    var unreadHtml = unread > 0 ?
        "<span class='unread-badge'>" + unread + "</span>" : "";

    var initial = conv.username.charAt(0).toUpperCase();

    div.innerHTML =
      "<div class='friend-left'>" +
          "<div class='friend-avatar-wrapper offline' data-user='" + conv.username + "'>" +
              "<div class='friend-avatar'>" + initial + "</div>" +
          "</div>" +
          "<div class='friend-text'>" +
              "<div class='friend-name'>" + conv.username + "</div>" +
              "<div class='friend-message'>" + (conv.lastMessage || "") + "</div>" +
          "</div>" +
      "</div>" +
      "<div class='friend-right'>" +
          "<div class='friend-time'>" + time + "</div>" +
          unreadHtml +
      "</div>";

    div.onclick = function() {
      window.location.href = "/html/chat.html?user=" + conv.username;
    };

    container.appendChild(div);
  });
}

  // handle incoming message
function handleIncomingMessage(msg) {

  var container = document.getElementById("friendList");

  // Find existing conversation item
  var existing = container.querySelector("[data-user='" + msg.sender + "']");

  // If conversation not present, reload full list
  if (!existing) {
    fetch("/api/chat/conversations", {
      headers: { Authorization: "Bearer " + token }
    })
    .then(res => res.json())
    .then(data => renderConversations(data));
    return;
  }

  // Update last message preview
  var parentItem = existing.closest(".friend-item");
  var messagePreview = parentItem.querySelector(".friend-message");
  messagePreview.innerText = msg.content;

  // Update time
  var timeDiv = parentItem.querySelector(".friend-time");
  var date = new Date();
  timeDiv.innerText = date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

  // Increase unread badge
  var badge = parentItem.querySelector(".unread-badge");

  if (badge) {
    var count = parseInt(badge.innerText);
    badge.innerText = count + 1;
  } else {
    var newBadge = document.createElement("span");
    newBadge.className = "unread-badge";
    newBadge.innerText = "1";
    parentItem.querySelector(".friend-right").appendChild(newBadge);
  }

  // Move conversation visually to the top
  container.prepend(parentItem);
}

function updateOnlineStatus(onlineUsers) {

  var wrappers = document.querySelectorAll('.friend-avatar-wrapper');

  wrappers.forEach(function(wrapper) {
    var username = wrapper.getAttribute('data-user');

    if (onlineUsers.includes(username)) {
      wrapper.classList.remove('offline');
      wrapper.classList.add('online');
    } else {
      wrapper.classList.remove('online');
      wrapper.classList.add('offline');
    }
  });
}

function showTypingPreview(username) {

  var container = document.getElementById("friendList");
  var wrapper = container.querySelector("[data-user='" + username + "']");

  if (!wrapper) return;

  var parentItem = wrapper.closest(".friend-item");
  var preview = parentItem.querySelector(".friend-message");

  preview.innerText = "Typing...";
  preview.style.fontStyle = "italic";
  preview.style.color = "#25d366";

  // Reset after 2 seconds
  setTimeout(function () {
    fetch("/api/chat/conversations", {
      headers: { Authorization: "Bearer " + token }
    })
    .then(res => res.json())
    .then(data => renderConversations(data));
  }, 2000);
}

// ================= SEARCH WITH DEBOUNCE =================
function handleSearchKey(event) {
  const keyword = event.target.value.trim();

  // Press Enter → instant search
  if (event.key === "Enter") {
    searchUser(keyword);
    return;
  }

  // Debounce (300ms delay)
  clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => {
    if (keyword === "") {
      // Restore conversations list
      fetch("/api/chat/conversations", {
        headers: { Authorization: "Bearer " + token }
      })
      .then(res => res.json())
      .then(data => renderConversations(data));
    } else {
      searchUser(keyword);
    }
  }, 300);
}

function searchUser(keyword) {
  if (!keyword || keyword.length < 3) {
    return;
  }

  const encodedKeyword = encodeURIComponent(keyword);

  fetch(`/api/chat/users/search?keyword=${encodedKeyword}`, {
    headers: { Authorization: "Bearer " + token }
  })
    .then(res => res.json())
    .then(data => {
      const container = document.getElementById("friendList");
      container.innerHTML = "";

      if (!data || data.length === 0) {
        const empty = document.createElement("div");
        empty.className = "empty-state";
        empty.innerText = "No user found";
        container.appendChild(empty);
        return;
      }

      data.forEach(username => {
        const div = document.createElement("div");
        div.className = "friend-item";
        div.innerText = username;
        div.onclick = () => {
          window.location.href = "/html/chat.html?user=" + username;
        };
        container.appendChild(div);
      });
    });
}

function logout() {
  localStorage.clear();
  window.location.href = "/html/login.html";
}
