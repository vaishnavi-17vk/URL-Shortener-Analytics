# LinkStream | Java URL Shortener & Analytics Platform

A high-performance, full-stack URL shortening and analytics system built with Core Java. This project demonstrates modern software design patterns, concurrent persistence, and a real-time HTTP server.

## 🌟 Project Overview
LinkStream is more than just a URL shortener; it's a link management platform. It allows users to create short links with custom aliases, expiration dates, click limits, and password protection. It features a dual-interface: a terminal-based CLI for management and a high-speed HTTP server for redirects and web-based dashboard analytics.

## 🔄 Workflow
1. **Request Intake**: A request arrives via the **REST API** (`HttpServer` in `Main.java`) or **CLI** (`URLController`).
2. **Business Logic**: The request is processed by the `URLShortenerService`, which validates the URL and determines if the link should be `Permanent` or `Temporary`.
3. **Encoding**: If no custom alias is provided, the `Base62Encoder` generates a unique short code.
4. **Storage**: The `URLRepository` stores the link in a `ConcurrentHashMap` and persists it to `links.dat` using Java Serialization.
5. **Redirection**: When a short link is visited, the server records analytics (device type, timestamp) and performs a **302 Redirect** to the destination.

## 🚀 Key Features
- **Concurrent Persistence**: Uses `ConcurrentHashMap` and `ScheduledExecutorService` for thread-safe memory management and background auto-saving.
- **Advanced Link Logic**: Supports expiry dates, click quotas, and password-protected links.
- **Deep Analytics**: Tracks device distribution (Mobile vs Desktop) and real-time click counts.
- **RBAC (Role Based Access Control)**: Integrated User/Admin management system for secure link tracking.

## 🛠️ How to Run
1. **Compile**: `javac -d bin -sourcepath src src/Main.java`
2. **Run**: `java -cp bin Main`
3. **Access**: Open [http://localhost:8081](http://localhost:8081)

## 🏗️ Project Structure
- `src/model`: Core entities using Polymorphism and Encapsulation.
- `src/repository`: Thread-safe data access and serialization layer.
- `src/service`: Business logic for URL management.
- `src/security`: Authentication and Access Control implementations.
- `src/utils`: Mathematical utilities (Base62).
- `web/`: Modern Glassmorphic dashboard frontend.
