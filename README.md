# Backend Service Provider for Pluto Interview

## Frameworks and Libraries Used
- **Java 21**: The project is built using Java 21, ensuring modern language features and performance.
- **Spring Boot 3.5.8**: Provides a robust framework for building microservices and web applications.
- **LangChain4j 1.9.1**: Enables seamless integration of language models for advanced AI capabilities.

## Techniques Leveraged
- **Microservices Architecture**: The backend is designed to be modular and scalable.
- **Event-Driven Design**: Utilizes events for decoupled communication between components.
- **Asynchronous Processing**: Ensures high performance and responsiveness.
- **Security**: Implements modern security practices, including authentication and authorization.

## Prerequisites
To run this project, ensure you have the following installed:
- **Java Development Kit (JDK) 21**
- **Maven 3.8+**: For dependency management and build automation.
- **PostgreSQL**: The database used for persistent storage.

## Getting Started
1. Clone the repository:
   ```bash
   git clone <repository-url>
   ```
2. Navigate to the project directory:
   ```bash
   cd pluto-interview
   ```
3. Configure the database settings in `application.yaml`.
4. Build the project:
   ```bash
   ./mvnw clean install
   ```
5. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```
