# Weather App Suite

## Project Overview
The Weather App Suite is a microservices-based application designed to simulate and process weather data using Spring Boot, Apache Kafka, and Apache Zookeeper. It demonstrates a fully containerized deployment using Docker Compose, showcasing efficient multi-stage Docker builds.

## Features
* **Weather Data Generation:** A `weather-producer` microservice simulates fetching and sending real-time weather data.
* **Asynchronous Messaging:** Utilizes Apache Kafka as a high-throughput, fault-tolerant messaging queue for data ingestion.
* **Weather Data Consumption:** A `weather-consumer` microservice processes the weather data from Kafka.
* **Containerized Deployment:** The entire suite (producer, consumer, Kafka, Zookeeper) can be easily brought up and managed using Docker Compose.
* **Optimized Docker Images:** Implements multi-stage Dockerfiles to create lean, production-ready application images, significantly reducing final image size.

## Technologies Used
* **Backend:** Java 21, Spring Boot
* **Messaging:** Apache Kafka
* **Coordination:** Apache Zookeeper
* **Containerization:** Docker, Docker Compose
* **Build Tool:** Maven

## Architecture
https://excalidraw.com/#json=UP2DyJ_ov1rnkGbyT2PaX,V_CldiwIHqONCRc73qJK3w

## How to Run the Application

### Prerequisites
* [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running.

### Steps
1.  **Clone the Repository:**
    ```bash
    git clone [https://github.com/JennyTran17/weather-app-suite.git](https://github.com/JennyTran17/weather-app-suite.git)
    ```
2.  **Navigate to the Project Root:**
    ```bash
    cd weather-app-suite
    ```
3.  **Build and Deploy the Services:**
    This command will build your Spring Boot applications into Docker images and then start all services (producer, consumer, Kafka, Zookeeper) in detached mode.
    ```bash
    docker compose up -d --build --force-recreate
    ```
4.  **Verify Services Are Running (Optional):**
    ```bash
    docker ps
    ```
    You should see all four services listed with `Up` status.
5.  **Check Application Logs (Optional):**
    To see the real-time output from your applications:
    ```bash
    docker compose logs -f
    ```
    (Press `Ctrl+C` to exit log viewing)

## How to Test/Verify Functionality
* **Producer Activity:** Check the logs of the `weather-producer` service for messages indicating data is being sent to Kafka:
    ```bash
    docker compose logs -f weather-producer
    ```
  Look for output related to sending weather data.
* **Consumer Activity:** Check the logs of the `weather-consumer` service for messages indicating data is being received from Kafka:
    ```bash
    docker compose logs -f weather-consumer
    ```
  Look for output related to processing weather data.
* **Access Consumer API (if applicable):** If your `weather-consumer` exposes a REST endpoint (e.g., on `http://localhost:8081`), you can use `curl` or a web browser:
    ```bash
    curl http://localhost:8081/weather # Adjust endpoint if different
    ```

## Project Structure
weather-app-suite/

├── weather-consumer/         
│   └── Dockerfile            
├── weather-producer/         
│   └── Dockerfile            
└── docker-compose.yml        
├── .gitignore                
└── README.md

## Lessons Learned & Challenges Overcome
* **Mastering Multi-Stage Docker Builds:** Successfully implemented multi-stage Dockerfiles to create minimal, secure, and production-ready images by separating the build environment from the runtime environment.
* **Resolving Maven Build Issues in Docker:** Debugged and fixed persistent "mvn: not found" errors by utilizing `eclipse-temurin` as a base image for the build stage and manually installing Maven, demonstrating robust troubleshooting skills.
* **Overcoming Docker Image Resolution Issues:** Addressed network-related "image not found" errors for common base images, showcasing persistence and adaptability in diagnosing complex environment problems.
* **Orchestrating Microservices with Docker Compose:** Gained practical experience in defining, linking, and managing multiple interdependent services within a Docker ecosystem.

## Future Enhancements
* Implement a simple web-based UI for real-time weather display.
* Integrate with a real external weather API instead of simulating data.
* Add a database to persist weather data for historical analysis.
* Implement comprehensive unit and integration tests.
* Set up a basic CI/CD pipeline for automated builds and deployments.