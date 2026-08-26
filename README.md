# E2EE Chat Server

API for an end-to-end encrypted chat application.

Client application is available in:
➡️ **[e2ee-chat-client](https://github.com/Zigix/e2ee-chat-client)**

Full application setup containing both server and client projects is available here:
➡️ **[e2ee-chat-application](https://github.com/Zigix/e2ee-chat-application)**

## Table of Contents

- [Project Overview](#project-overview)
- [Main Features](#main-features)
- [Technologies](#technologies)
- [Project Structure](#project-structure)
- [Running server locally](#running-server-locally)
- [Running complete application locally](#running-complete-application-locally)
- [API Overview](#api-overview)

## Project Overview

**E2EE Chat Server** is the backend of a web-based messaging application that allows users to communicate through private and group conversations.

It is designed to support an **End-to-End Encrypted (E2EE)** communication model, where message encryption and decryption take place on the client side. 

The application provides a REST API and the core functionality required to create and manage user accounts, handle authorization, manage users and communication channels, and handle the cryptographic keys required for E2EE communication. It also supports real-time messaging and secure data persistence.

## Main Features

- User registration and login.
- Authenticated user sessions.
- Access to the current user's account information.
- User search for finding other users to start conversations with.
- Public ECDH key retrieval required to establish encrypted communication.
- Creating private conversations between users.
- Creating group conversations.
- Viewing recent conversations.
- Viewing conversation details and its members.
- Loading message history for a conversation.
- Sending messages in real time.
- Receiving new messages and conversation updates in real time.
- Adding new members to group conversations.
- Removing members from group conversations.
- Leaving group conversations.
- Changing the name of group conversations.
- End-to-End Encryption support through client-managed encryption keys.
- Secure storage and retrieval of encrypted conversation keys.
- Updating conversation keys when conversation membership changes.
- Receiving notifications when new encryption key material becomes available.
- Receiving notifications about newly created conversations.
- Receiving notifications when members are added to or removed from a group.
- Receiving notifications when a member leaves a group.
- Receiving notifications when a group name is changed.
- Persistent storage of encrypted messages and conversation data.

## Technologies

The server-side application is built using the following technologies:

### Core

- **Java 21** - primary programming language
- **Spring Boot** - application framework
- **Maven** - dependency management and build automation

### Web & Security

- **Spring Web MVC** - REST API implementation
- **Spring Security** - authentication and authorization
- **JWT** (`java-jwt`) - token-based authentication
- **Spring WebSocket with STOMP** - real-time communication

### Data Persistence

- **Spring Data JPA / Hibernate** - object-relational mapping and database access
- **MySQL** - production database
- **H2** - database used for local and test environments
- **Liquibase** - database schema versioning and migrations

### Development & Deployment

- **Lombok** - reducing boilerplate code
- **Docker** - application containerization

## Project Structure

The backend is divided into several modules, each responsible for a specific part of the application's functionality:

- `auth` - user registration, login, JWT-based authentication, and user vault management.
- `user` - user data, user search, and public ECDH key management.
- `conversation` - private chats, group conversations, members, encrypted messages, and conversation keys envelopes.
- `ws` - WebSocket event payloads and real-time chat notifications.
- `config` - Spring Security, CORS, WebSocket, and application configuration.
- `db/changelog` - Liquibase database schema migrations.
- `src/test` - unit and controller tests for the main application modules.

## Running server locally

This option is useful when developing or testing the backend separately from the client.

The server can be started either directly with Maven or as a Docker container. In both cases, a running MySQL database instance is required.

### Configuration

Configure the required environment variables in a `.env` file or in your system environment:

```dotenv
DB_URL=jdbc:mysql://localhost:3306/e2ee_chat_app_db
DB_USERNAME=your_username
DB_PASSWORD=your_password
JWT_SECRET=your_secret
ALLOWED_ORIGINS=http://localhost:5173
```

### Run with Maven

Make sure that the required MySQL database is running and then start the application:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
.\mvnw.cmd spring-boot:run
```

The server will run on port **8080**.

### Run with Docker

The server can also be built and run as a standalone Docker container:

```bash
docker build -t e2ee-chat-server .
docker run -p 8080:8080 --env-file .env e2ee-chat-server
```

When running the server independently, the **MySQL database must be available** separately and its connection details must be provided through the environment variables.

## Running complete application locally

The complete application can be run using Docker Compose from the ➡️ **[e2ee-chat-application](https://github.com/Zigix/e2ee-chat-application)** repository.

The repository contains the server and client as Git submodules, along with the Docker Compose configuration and the complete instructions for setting up and running the application.

## API Overview

The server exposes a REST API for authentication, user management, conversations, messaging, and cryptographic key management.

#### Authentication

- `POST /api/auth/sign-up` - register a new user.
- `POST /api/auth/login` - authenticate a user and obtain a JWT token.

#### Users

- `GET /api/users/me` - get the currently authenticated user.
- `GET /api/users/search?q=...` - search for users by query.
- `GET /api/users/{userId}/public-key` - retrieve a user's public ECDH key.

#### Conversations

- `POST /api/rooms/private-chat` - create or retrieve a private conversation.
- `POST /api/rooms/group` - create a group conversation.
- `GET /api/rooms/recent` - retrieve the user's recent conversations.
- `GET /api/rooms/{roomId}` - retrieve conversation details and members.
- `GET /api/rooms/{roomId}/messages` - retrieve the message history of a conversation.
- `POST /api/rooms/{roomId}/name` - change the name of a group conversation.
- `POST /api/rooms/{roomId}/members` - add a member to a group conversation.
- `DELETE /api/rooms/{roomId}/members` - remove a member from a group conversation.
- `DELETE /api/rooms/{roomId}/leave` - leave a group conversation.

#### Room Keys

- `POST /api/rooms/{roomId}/keys/upload` - upload encrypted conversation keys envelopes.
- `POST /api/rooms/{roomId}/keys/rekey` - upload encrypted conversation keys envelopes for a new key version.
- `GET /api/rooms/{roomId}/my-key?version=...` - retrieve the authenticated user's conversation keys for a specific version.
- `GET /api/rooms/{roomId}/my-keys` - retrieve all conversation keys available to the authenticated user for a conversation.

#### WebSocket

The server exposes the following WebSocket endpoint:

- `/ws` - WebSocket connection endpoint.

The application uses `/app` as the STOMP application destination prefix and `/topic`, `/queue`, and `/user` as broker destination prefixes.

**User Notifications**

Each authenticated user can subscribe to their personal notification channel:

- `/user/queue/events` - receives general events and notifications related to the user's conversations, such as new conversations, group membership changes, conversation updates, and available encryption key material.

**Conversation Messages**

Clients can subscribe to a dedicated channel for each conversation:

- `/topic/rooms/{roomId}` - receives real-time messages and events associated with a specific conversation.

To send an encrypted message to a conversation, the client uses:

- `/app/rooms/{roomId}/send` - sends an encrypted message to the specified conversation.
