# RAF Cloud

RAF Cloud is a full-stack web application for managing users and virtual machines through a browser-based interface.

The project consists of an Angular frontend and a Spring Boot backend, with JWT-based authentication, permission-based access control, machine management, scheduled operations, WebSocket communication, and persistence using JPA/Hibernate.

## Features

- User authentication with JWT
- User registration and management
- Permission-based access control
- Virtual machine creation and management
- Start, stop and restart machine operations
- Scheduled machine operations
- Operation error tracking
- Real-time communication using WebSockets
- REST API communication between frontend and backend
- In-memory H2 database for local development

## Tech Stack

### Frontend

- Angular
- TypeScript
- Angular Material
- RxJS
- SCSS

### Backend

- Java 21
- Spring Boot
- Spring Data JPA
- Hibernate
- Spring WebSocket
- JWT authentication
- H2 Database
- Maven

## Project Structure

```text
raf-cloud/
├── backend/       # Spring Boot REST API
├── frontend/      # Angular web application
├── .gitignore
└── README.md
```

## Running the Project

### Backend

Navigate to the backend directory:

```bash
cd backend
```

Run the application:

```bash
mvn spring-boot:run
```

The backend starts locally and initializes the development database.

### Frontend

Navigate to the frontend directory:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start the Angular development server:

```bash
npm start
```

Open:

```text
http://localhost:4200
```

## Demo Account

For local development, the application initializes a demo administrator account:

```text
Email: admin@raf.rs
Password: admin123
```

These credentials are intended only for the local development environment.

## Build and Test

Backend:

```bash
cd backend
mvn clean test
```

Frontend:

```bash
cd frontend
npm install
npm run build
```

## Architecture

The Angular frontend communicates with the Spring Boot backend through REST endpoints. Authentication is handled using JWT tokens, while authorization is based on user permissions.

The backend uses Spring Data JPA and Hibernate for persistence and WebSockets for real-time application communication.

## Author

Mihailo Dragicevic