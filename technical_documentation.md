# LinkStream Technical Documentation

This document provides a comprehensive analysis of the LinkStream architecture, detailing its modules, operations, and the Java features utilized.

---

## 🏗️ 1. Architecture Deep-Dive

LinkStream follows a **Layered Architecture** with clear separation of concerns (Models, Repositories, Services, Controllers).

### 📦 A. Main Module (`src/Main.java`)
**The Orchestrator**
- **Operations**:
  - `main(String[] args)`: Initializes the application components and starts the server.
  - `startWebServer(...)`: Spins up a multi-threaded `HttpServer` to handle API and Redirect requests.
  - `parseQuery(String query)`: Parses URI query parameters into a `Map`.
- **Java Features Used**:
  - **HTTP Server API**: `com.sun.net.httpserver.HttpServer` for lightweight web serving.
  - **Lambda Expressions**: Used in `server.createContext` for concise request handling.
  - **Multithreading**: `Executors.newFixedThreadPool(10)` handles concurrent user requests.
  - **Streams API**: `java.util.stream.Collectors` for query parameter parsing.

### 📦 B. Service Layer (`src/service/URLShortenerService.java`)
**The Business Brain**
- **Operations**:
  - `shortenURL(...)`: Validates input, determines link type (`Permanent` vs `Temporary`), and generates codes.
  - `redirect(...)`: Validates link integrity (expiry, password) and records analytics.
  - `deleteURL(...)`: Removes links from the repository.
- **Java Features Used**:
  - **Polymorphism**: Interacts with `AbstractLink` while the actual object may be `TemporaryLink` or `PermanentLink`.
  - **Custom Exceptions**: Uses `InvalidURLException`, `LinkExpiredException`, etc., for granular error handling.
  - **Date/Time API**: `java.time.LocalDateTime` for high-precision expiry tracking.

### 📦 C. Data Layer (`src/repository/URLRepository.java`)
**Thread-Safe Persistence**
- **Operations**:
  - `save(AbstractLink link)`: Adds/Updates links in the map.
  - `getAll(User user)`: Retrieves links based on user roles (Admin vs Guest).
  - `saveData()` / `loadData()`: Handles binary persistence.
- **Java Features Used**:
  - **Concurrent Collections**: `ConcurrentHashMap` ensures thread safety without global locks.
  - **Java Serialization**: `ObjectOutputStream` / `ObjectInputStream` for object persistence to `links.dat`.
  - **Scheduled Executor**: `scheduler.scheduleAtFixedRate` for periodic auto-saving.
  - **Filter Logic**: Streams and `filter()` for role-based access control.

### 📦 D. Model Layer (`src/model/`)
**The Logic Entities**
- **Files**: `AbstractLink.java`, `TemporaryLink.java`, `PermanentLink.java`, `User.java`
- **Operations**:
  - `recordClick(deviceType)`: Advanced logic to track device variety and timestamped clicks.
  - `isExpired()`: Abstract logic overridden by child classes to determine link validity.
- **Java Features Used**:
  - **Abstraction**: `abstract class AbstractLink` defines the blueprint.
  - **Inheritance**: `TemporaryLink extends AbstractLink`.
  - **Encapsulation**: Private fields with strictly defined getters/setters and serializable IDs.

### 📦 E. Security Module (`src/security/`)
**Authentication Hub**
- **Files**: `Authenticator.java` (Interface), `LoginService.java`
- **Operations**:
  - `login(u, p)`: Simple yet effective verification against the `UserRepository`.
- **Java Features Used**:
  - **Interfaces**: Decouples the authentication logic from the rest of the application.

### 📦 F. Utility Layer (`src/utils/Base62Encoder.java`)
**The Mathematical Core**
- **Operations**:
  - `encode(long num)`: Converts numeric IDs into alphanumeric Base62 strings (0-9, a-z, A-Z).
- **Java Features Used**:
  - **Static Methods**: Pure functional logic that doesn't require object instantiation.
  - **StringBuilder**: Efficient string manipulation for code generation.

---

## 🔄 2. Operational Workflow

1. **Shorten**: User inputs a URL -> `ShortenerService` validates -> `Base62Encoder` generates code -> `URLRepository` persists.
2. **Access**: Visitor opens short link -> `HttpServer` captures request -> `ShortenerService` checks expiry/password -> Record analytic -> **302 Redirect**.
3. **Analytics**: Admin requests stats -> `URLRepository` filters based on `User` object -> JSON response sent to Frontend.
