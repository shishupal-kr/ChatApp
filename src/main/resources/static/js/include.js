function loadHeader() {
    fetch('/header.html')
        .then(response => response.text())
        .then(data => {
            document.head.insertAdjacentHTML("beforeend", data);
        });
}