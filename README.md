# 🛠️ Development of Bank Card Management System

This project is a backend application for managing bank cards, built using **Spring Boot** and **Java 17**. It provides **RESTful APIs** for user registration, authentication and CRUD operations on users and bank cards. 
The application uses **PostgreSQL** as the database and implements **JWT-based authentication** for secure access.

---

## 📌 Features

*  User Registration & Login
*  JWT-based Authentication
*  Secure Password Storage using BCrypt
*  Role-based Access Control (User/Admin)
*  CRUD Operations for Users
*  CRUD Operations for Bank Cards
*  Create, view, block, activate, and delete cards 
*  Encrypted card number with masked 
*  Card status management (ACTIVE, BLOCKED, EXPIRED)
*  Check Balance
*  Credit/Debit funds card
*  Transfer funds between user's own cards
*  Filtering and Pagination
* Create Card block request


---

## ⚙️ Tech Stack

| Layer                     | Technology                   |
|---------------------------|------------------------------|
| Backend                   | Spring Boot 3.5.5            |
| Language                  | Java 17                      |
| Security                  | Spring Security + JWT        |
| Database                  | PostgreSQL                   |
| ORM                       | Spring Data JPA              |
| Database Migration Tool   | Liquibase                    |
| Build Tool                | Maven                        |
| API Documentation         | Swagger/OpenAPI              |
| Testing                   | Junit                        |
| Reducing Boilerplate code | Lombok                       |
| Containerization          | Dockerfile && Docker Compose |

---

## 🌐 API Endpoints

### 🔓 Auth

| Endpoint         | Method | Description       |
| ---------------- | ------ | ----------------- |
| `/auth/register` | POST   | Register new user |
| `/auth/login`    | POST   | Authenticate user |

### 👤 User Management

| Endpoint          | Method | Description                                          |
|-------------------| ------ |------------------------------------------------------|
| `/api/users`      | POST   | Create new user (Admin only)                         |
| `/api/users`      | GET    | Get users with filtering and pagination (Admin only) |
| `/api/users/{id}` | GET    | Get specific user details (if owner)                 |
| `/api/users/{id}` | PUT    | Update user (if owner)                               |
| `/api/users/{id}` | DELETE | Delete user (Admin only)                             |


### 🗂 Card Management

| Endpoint                    | Method | Description                                                        |
|-----------------------------|--------|--------------------------------------------------------------------|
| `/api/cards`                | POST   | Create a new card (Admin only)                                     |
| `/api/cards/transfer`       | POST   | Transfer funds between cards (User's own card)                     |
| `/api/cards/debit`          | POST   | Debit funds from a card                                            |
| `/api/cards/credit`         | POST   | Credit funds to a card (Admin only)                                |
| `/api/cards/block/request`  | POST   | Create card block request by user                                  |
| `/api/cards/{id}/block`     | PUT    | Block a card (Admin only)                                          |
| `/api/cards/{id}/activate`  | PUT    | Activate a card (Admin only)                                       |
| `/api/cards`                | GET    | Get cards with pagination and filtering (User's own cards)         |
| `/api/cards/block/request`  | GET    | Get card block requests with pagination and filtering (Admin only) |
| `/api/cards/{id}`           | GET    | Get card by ID                                                     |
| `/api/cards/{id}`           | DELETE | Delete a card (Admin only)                                         |


---

## 🧪 How to Run the Project

### 🔧 Prerequisites

* Java 17+
* PostgreSQL (running on default port)
* Maven
* Git
* Docker & Docker Compose (for containerized setup)
* Ports 8080 and 5432 should be free on your machine

---

### Option 1: Using Docker Compose

1. **Clone the repository**
   ```bash
   git clone https://github.com/garagoz/Bank_REST.git
   cd Bank_REST
   ```

2. **Build with maven**
   ```bash
   mvn clean package -DskipTests
   ```
   
3. **Start the application with Docker Compose**
   ```bash
   docker-compose up -d
   ```

4. **Wait for services to start**
   - Database: `http://localhost:5432`
   - Application: `http://localhost:8080`

5. **Access the API Documentation**
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - API Docs: `http://localhost:8080/api-docs`

---
### Option 2: Local Development
1. **Clone the repository**

   ```bash
   git clone https://github.com/garagoz/Bank_REST.git
   cd Bank_REST
   ```

2. **Create PostgreSQL database**

   ```sql
   CREATE DATABASE bankdb;
   ```

3. **Configure application.properties**

   ```
   spring.datasource.url=jdbc:postgresql://localhost:5432/bankdb
   spring.datasource.username=admin        #your db username
   spring.datasource.password=admin        #your db password
   spring.jpa.hibernate.ddl-auto=none
   ```

4. **Run the app**

   ```bash
   ./mvnw spring-boot:run
   ```


