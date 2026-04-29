# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build fat JAR
mvn clean package

# Run the server (port 8080)
mvn spring-boot:run
# or
java -jar target/lrucache-0.0.1-SNAPSHOT.jar

# Compile only (no JAR)
mvn compile
```

There are no tests yet. When adding them, use `mvn test` to run all and `mvn test -Dtest=ClassName` to run a single class.

## Architecture

The project is a Spring Boot 3.x REST API wrapping a hand-rolled LRU cache. Java 21, Maven, no external data store.

### Layer responsibilities

- **`LRUCache<K, V>`** — pure data structure; HashMap + doubly-linked list for O(1) get/put/invalidate. Not thread-safe. No Spring dependency.
- **`LruCacheApplication`** — Spring Boot entry point. Also declares the `LRUCache<String, String>` singleton `@Bean`, reading `cache.capacity` from `application.properties`.
- **`CacheService`** — the thread-safety boundary. Every method is `synchronized` because `get` is not a pure read (it calls `moveToFront`, mutating the linked list). Injected with the `LRUCache` bean.
- **`CacheController`** — `@RestController` on `/api/cache`. Delegates entirely to `CacheService`. Uses `record PostRequest(String key, String value)` for POST and `record PutRequest(String value)` for PUT.

### REST endpoints

| Method | Path | Request body | Success | Error |
|--------|------|-------------|---------|-------|
| `POST` | `/api/cache` | `{"key","value"}` | `201` | `409` key exists |
| `GET` | `/api/cache/{key}` | — | `200 {"key","value"}` | `404` |
| `PUT` | `/api/cache/{key}` | `{"value"}` | `200 {"key","value"}` | `404` key missing |
| `DELETE` | `/api/cache/{key}` | — | `204` | `404` |
| `GET` | `/api/cache/stats` | — | `200 {"size","capacity"}` | — |

### Configuration (`application.properties`)

| Key | Default | Effect |
|-----|---------|--------|
| `server.port` | `8080` | Tomcat listen port |
| `cache.capacity` | `100` | Max entries before LRU eviction |
