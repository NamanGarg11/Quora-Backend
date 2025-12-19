# Quora-Backend
It is a reactive knowledge-sharing backend inspired by platforms like Quora.
It enables users to ask questions, search content, explore topics  — all built with Spring WebFlux, Reactive MongoDB, and Kafka event streaming for scalability.

Designed to be lightweight, real-time, and extensible, BranChain can power Q&A platforms, discussion forums, or knowledge bases that require high performance and reactive APIs.

## 🚀 API-Endpoints
<img width="1837" height="893" alt="Screenshot from 2025-12-19 14-28-19" src="https://github.com/user-attachments/assets/74b4bee6-9d2b-485d-8e28-c21cb0610e4f" />


- ⚡ Tech Highlights

    - Reactive APIs (Spring WebFlux).

    - Reactive MongoDB for non-blocking persistence.

    - Kafka integration for event-driven architecture.

    - Scalable design ready for microservices expansion.
  
## Table of Contents
- Project Overview
- Tech Stack
- Features
- Architecture Overview
- Data Model (High-level)
- API Reference
    - Questions
- Pagination
- Setup and Prerequisites
    - Java and Build Tools
    - MongoDB
    - Kafka
- Running the Application
- Development Tips
- Roadmap

---

## Project Overview
This project provides a scalable, reactive backend for a Q&A application with:
- Non-blocking I/O for high throughput.
- MongoDB for storage.
- Kafka for event streaming (e.g., view events).
- Optional Elasticsearch for search capabilities.

---

## Tech Stack
- Java 17
- Spring Boot (WebFlux)
- Spring Data MongoDB (Reactive)
- Reactor (Flux/Mono)
- Apache Kafka
- Redis

---

## Features
- Questions:
    - Create question with title, content, and tags (comma/space-separated tags string).
    - Retrieve a question by ID.
    - List all questions.
    - Delete a question by ID.
    - Search questions by title/content with offset pagination.
    - Cursor-based listing by creation time.
    - Paginated, count-inclusive listing response structure.
    - Filter questions by tag.
- Tags:
    - Store and query questions by tag name.
- Events:
    - Emits view count events to Kafka when a question is fetched by ID.
- Extensible for:
    - Answers
    - Likes
    - Trending tags
    - Elasticsearch indexing

---

## Architecture Overview
- Controller layer: Defines REST endpoints.
- Service layer: Business logic, mapping, and integration with messaging.
- Repository layer: Reactive MongoDB repositories.
- Event Publisher: Kafka producers for domain events (e.g., view count).
- DTO/Adapter: Converters between persistence models and API responses.

---

## Data Model (High-level)
- Question:
    - id: String
    - title: String
    - content: String
    - tags: List<String> (normalized tag names)
    - createdAt: LocalDateTime
    - updatedAt: LocalDateTime

- Tag:
    - tagName: String (unique)
    - usageCount: int

Note: A question can hold a list of tags as strings; tag statistics can be managed separately.

---

## API Reference

Base Path: /api

### Questions

- Create Question
    - Method: POST
    - Path: /api/questions
    - Body: QuestionRequestDTO
        - title: string
        - content: string
        - tag: string (comma/space-separated tags; stored as list internally)
    - Response: QuestionResponseDTO (Mono)
    - Notes: On success, returns the created question.

- Get Question by ID
    - Method: GET
    - Path: /api/questions/{id}
    - Response: QuestionResponseDTO (Mono)
    - Side-effect: Publishes a view-count event to Kafka.

- Get All Questions
    - Method: GET
    - Path: /api/questions
    - Response: Flux<QuestionResponseDTO>

- Delete Question by ID
    - Method: DELETE
    - Path: /api/questions/{id}
    - Response: Mono<Void>

- Search Questions (offset pagination)
    - Method: GET
    - Path: /api/questions/search
    - Query Params:
        - query: string (search term; title or content contains)
        - page: int (default 0)
        - size: int (default 10)
    - Response: Flux<QuestionResponseDTO>

- Cursor-based Query (createdAt-based)
    - Method: GET
    - Path: /api/questions/questions
    - Query Params:
        - Cursor: string (ISO-8601 timestamp; optional)
        - size: int (page size)
    - Response: Flux<QuestionResponseDTO>
    - Notes:
        - If cursor is invalid/missing, returns the top by createdAt ascending.
        - If valid, returns items created after the cursor.

- Paginated Query with Total Count
    - Method: GET
    - Path: /api/questions/pagination
    - Query Params:
        - searchTerm: string
        - page: int
        - size: int
    - Response: Mono<PaginationRespnseDTO<Question>>
        - data: List<Question>
        - total: long
        - page: int
        - size: int

- Get Questions by Tag
    - Method: GET
    - Path: /api/questions/tag/{tag}
    - Query Params:
        - page: int (default 0)
        - size: int (default 10)
    - Response: Flux<QuestionResponseDTO>
    - Notes: Filters questions by tag name.

---

## Pagination
- Offset-based: Uses page and size.
- Cursor-based: Uses an ISO-8601 timestamp Cursor parameter to fetch items created after the cursor time.

---

🛠️ Setup and Prerequisites

This project is fully containerized.
You do not need to install MongoDB, Redis, or Kafka manually.

1) System Requirements

Make sure the following are installed:

Docker (v20+)

Docker Compose (v2+)

Verify installation:

docker --version
docker compose version

2) Java & Build Tools (Optional)

Java is only required for local development.

Java 17

Gradle (project build tool)

Verify (optional):

java -version
./gradlew -v


⚠️ Not required if you run the application via Docker only.

3) Infrastructure Services (Dockerized)

All infrastructure dependencies are managed via Docker Compose:

Service	Purpose
MongoDB	Primary database
Redis	Caching & relationship storage
Kafka	Event streaming (view-count events)
Zookeeper	Kafka coordination
Quora Backend	Spring Boot WebFlux API

No manual setup is required.

4) Running the Full Stack

From the project root:

docker compose up -d


Docker Compose will:

Pull required images (MongoDB, Redis, Kafka)

Build the backend image if not present

Create containers, network, and volumes

Start services in the correct order

5) Kafka Topics

Kafka is used to emit question view-count events.

In local development, topics can be auto-created by Kafka.

Optional manual creation:

docker exec -it quora-kafka kafka-topics.sh \
  --create \
  --topic view-count-events \
  --bootstrap-server kafka:9092 \
  --partitions 1 \
  --replication-factor 1


How it’s used:

Every time a question is fetched by ID, a view-count event is published

Future Kafka consumers can aggregate analytics or persist metrics
