# Finance Manager API

A REST API for personal expense management built with Java 17 and Spring Boot.

This project is used to apply and strengthen modern backend development practices, including REST API design, layered architecture, validation, persistence, testing and API documentation.

## Features

### Expense Management

* Create an expense
* Retrieve all expenses
* Retrieve an expense by ID
* Update an expense
* Delete an expense

### Search & Filtering

* Search expenses by name
* Filter expenses by amount range
* Filter expenses by date range
* Combined filtering
* Pagination and sorting
* Retrieve top expenses

### Statistics

* Monthly expense statistics
* Total monthly amount
* Average monthly amount
* Highest expense of the month
* Expense count

### Validation & Error Handling

* Request validation with Bean Validation
* Business parameter validation
* Centralized exception handling with `GlobalExceptionHandler`
* Consistent HTTP error responses

### API Documentation

* Swagger / OpenAPI documentation

### Testing

* Unit tests with JUnit 5 and Mockito
* Controller tests with MockMvc
* Integration testing of REST endpoints
* Validation and error-case testing

## Technology Stack

* Java 17
* Spring Boot 3
* Spring Web
* Spring Data JPA / Hibernate
* PostgreSQL
* Maven
* Bean Validation
* ModelMapper
* Lombok
* Swagger / OpenAPI
* JUnit 5
* Mockito
* MockMvc
* Git

## Architecture

The application follows a layered architecture:

```text
Client
  |
  v
REST Controller
  |
  v
Service
  |
  v
Repository
  |
  v
PostgreSQL
```

Additional layers are used for:

* DTOs
* Entity/DTO mapping
* Validation
* Exception handling
* Configuration

## Project Structure

```text
src/main/java/com/myfinance/finance_manager
├── config
├── controller
├── dto
├── exception
├── mapper
├── model
├── repository
└── service
    └── impl
```

## Getting Started

### Prerequisites

Make sure the following tools are installed:

* Java 17
* Maven
* PostgreSQL

### Database

Create a PostgreSQL database for the application and configure the database connection in:

```text
src/main/resources/application.properties
```

Example:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/finance_manager
spring.datasource.username=your_username
spring.datasource.password=your_password
```

Do not commit real credentials to the repository.

### Build the Project

Using Maven Wrapper:

```bash
./mvnw clean install
```

On Windows:

```bash
mvnw.cmd clean install
```

### Run the Application

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The application will start by default on:

```text
http://localhost:8080
```

## API Documentation

Once the application is running, Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

## Running Tests

```bash
./mvnw test
```

On Windows:

```bash
mvnw.cmd test
```

## Planned Improvements

The project is actively evolving. Planned improvements include:

* Income management
* Category management
* Budget management
* Bank account management
* Authentication and authorization with Spring Security / JWT
* Docker
* CI/CD pipeline
* Kafka
* Microservices architecture

These features are part of the roadmap and are not all implemented yet.

## Project Status

Active development.

The application is being improved incrementally while applying modern Java and Spring Boot backend development practices.

## Author

Georgio Tanios
