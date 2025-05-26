# Backend DSI-Certificates Users Management

## Overview

`Backend_DSI-CertificatesUsers Management` is a Spring Boot-based backend application designed for managing digital certificates and user accounts within an organization. This system incorporates secure RESTful APIs, robust authentication and authorization mechanisms, and detailed auditing features to effectively manage the lifecycle of certificates and user data.

### Features

#### **Certificate Management**
- Full support for certificate lifecycle management (CRUD operations).
- Soft delete mechanism for certificates.
- Pagination and filtering for efficient querying.
- Scheduled notification service to alert administrators about certificate expiration.
- Auditing of changes to certificates, capturing creation, modification, and deletion actions.

#### **User Management**
- CRUD operations for managing user accounts.
- Role-based access control with roles such as `USER`, `ADMIN`, and `SUPER_ADMIN`.
- Token revocation feature for enhanced security.
- Support for soft deletions of users.
- Pagination and filtering for user data queries.
- Auditing of actions like user creation, update, and deletion.

#### **Authentication and Security**
- Stateless JWT-based authentication.
- Role-based endpoint authorization for secure access.
- Password encryption using BCrypt.
- RESTful API protection using Spring Security.
- Customizable CORS policy enabling secure interaction with front-end systems.

#### **Auditing**
- Tracks all relevant operations on certificates and users.
- Entity listener (`AuditListener`) integrates seamlessly with the Spring ecosystem for audit trail creation.
- Avoids redundant audit operations through `AuditContext`.

---

## Technologies Utilized

### **Backend:**
- **Java**: SDK Version 21
- **Spring Boot** (3.4.4):
    - Spring MVC (RESTful APIs)
    - Spring Data JPA (SQL Server integration)
    - Spring Security (Authentication/Authorization with JWT)
    - Spring Mail (Notification service)
    - Spring Actuator
- **Jakarta EE**: For persistence, validation, and more.
- **ModelMapper**: For DTO to Entity conversions.
- **Hibernate**: Object Relational Mapping and Envers for auditing.
- **Lombok**: Reduces boilerplate for getters, setters, and constructors.

### **Database:**
- **Microsoft SQL Server**:
    - Handles data storage with support for soft deletion (`isDeleted` flag).
    - Tracks updates using audit tables.

### **Build and Dependencies:**
- **Maven**: Dependency management and project build.
- **JJWT Library**: Used for secure JWT token generation and processing.

---

## API Endpoints Overview

### Authentication: `/api/auth`
- `POST /login`: Authenticate and generate a JWT token.
- `POST /logout`: Revoke the current user's token.

### User Management: `/api/users`
- `GET /`: Retrieve all users (Admin only).
- `POST /`: Create a new user (Admin only).
- `GET /{id}`: Retrieve user details by ID (Admin only).
- `PUT /{id}`: Update user details by ID (Admin only).
- `DELETE /{id}`: Soft delete a user by ID (Admin only).
- `POST /{id}/revoke-tokens`: Revoke all tokens for a user (Admin only).
- `GET /me`: Retrieve details of the authenticated user.

### Certificates: `/api/certificates`
- `GET /`: Retrieve all certificates with optional filtering and pagination.
- `POST /`: Create a new certificate record.
- `GET /{id}`: Fetch certificate details by ID.
- `PUT /{id}`: Update certificate details by ID.
- `DELETE /{id}`: Soft delete a certificate by ID.

---

## Configuration and Setup

### Prerequisites:
- **JDK 21**
- SQL Server Database.

### Setup Steps:
1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd Backend_DSI-Certificates_UsersManagment
   ```

2. Update the `application.properties`:
   ```properties
   # Spring Datasource Configuration
    spring.datasource.url=jdbc:sqlserver://${DB_HOST}:${DB_PORT};databaseName=${DB_NAME}
    spring.datasource.username=${DB_USERNAME}
    spring.datasource.password=${DB_PASSWORD}
    spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
    
    # Spring JPA Configuration
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=true
    spring.jpa.properties.hibernate.format_sql=true
    spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.SQLServerDialect
    
    # Spring Mail Configuration
    spring.mail.host=${MAIL_HOST}
    spring.mail.port=${MAIL_PORT}
    spring.mail.username=${MAIL_USERNAME}
    spring.mail.password=${MAIL_PASSWORD}
    spring.mail.properties.mail.smtp.auth=true
    spring.mail.properties.mail.smtp.starttls.enable=true
    
    # JWT Configuration
    jwt.secret.v2=${JWT_SECRET}
    jwt.expirationMs=${JWT_EXPIRATION_MS}
    jwt.refreshExpirationMs=${JWT_REFRESH_EXPIRATION_MS}

   ```

3. Build the application:
   ```bash
   mvn clean install
   ```

4. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### Access:
- By default, services run at `http://localhost:8080/api/...`.

---

## Security Highlights

- **Stateless Authentication**: Uses JWT for secure, stateless authentication.
- **Role-Based Access Control**: Protects administrative and user endpoints.
- **API Protection**: Each endpoint has specific role restrictions (`USER`, `ADMIN`, `SUPER_ADMIN`).

## Notable Features
- **Audit Logging**: Tracks actions with details about who performed what operation.
- **Soft Delete**: Ensures data integrity by marking records as deleted instead of permanent removal.
- **Job Scheduling**: Automated certificate expiration notifications.

---