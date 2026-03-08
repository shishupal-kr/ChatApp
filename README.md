# ChatApp

ChatApp is a simple real-time 1:1 chat application built with Spring Boot, WebSocket (STOMP/SockJS), and JWT authentication. The UI is served from static HTML/CSS/JS and the backend stores messages in MySQL using JPA.

## Features
- User registration and login with username + password (JWT auth)
- Private 1:1 messaging over WebSocket
- Online user presence
- Unread message counts
- Typing indicator
- Message edit and delete
- Read and delivered receipts
- Basic profile and settings pages

## Tech Stack
- Spring Boot 4
- Spring Security + JWT
- WebSocket (STOMP/SockJS)
- Spring Data JPA + Hibernate
- MySQL
- Lombok
- Vanilla HTML/CSS/JS

## Requirements
- Java 25
- MySQL
- Maven (or use the included `./mvnw`)

## Setup
1. Create a database named `chatapp` in MySQL.
2. Update DB credentials in `src/main/resources/application.properties`.
3. Run the app:

```bash
./mvnw spring-boot:run
```

The app will be available at `http://localhost:8080`. & "https://cooper-nondivergent-ike.ngrok-free.dev/html/login.html"


## Authentication
- Register: `POST /api/auth/register`
  - Body: `{ "username": "...", "email": "...", "password": "..." }`
- Login: `POST /api/auth/login`
  - Body: `{ "username": "...", "password": "..." }`
  - Response: JWT token (string)

Use the token for protected endpoints:

```
Authorization: Bearer <token>
```

Token expiry is set to 1 hour in `JwtService`.

## REST API
- `GET /api/auth/me`
- `DELETE /api/auth/delete-account`
- `GET /api/chat/conversations`
- `GET /api/chat/history/{username}?page=0&size=20`
- `GET /api/chat/users/search?keyword=...`
- `GET /api/chat/online-users`
- `GET /api/chat/unread-counts`

## WebSocket
- Endpoint: `/chat` (SockJS)
- Send destinations:
  - `/app/private-message`
  - `/app/typing`
  - `/app/read`
  - `/app/edit`
  - `/app/delete`
- Subscribe destinations:
  - `/user/queue/messages`
  - `/user/queue/typing`
  - `/user/queue/read-receipt`
  - `/user/queue/delivered`
  - `/user/queue/edit`
  - `/user/queue/delete`
  - `/topic/online-users`
  - `/user/queue/online-users`
  - `/user/queue/unread-update`

## Project Structure
- `src/main/java` backend source
- `src/main/resources/static` frontend assets
- `src/main/resources/application.properties` configuration

## License
MIT
