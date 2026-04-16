# LinkStream Technical Documentation

This document provides a deep dive into the architecture and modules of the LinkStream URL Shortener & Tracker.

---

## 🏗️ 1. Module Overview: The Layered Architecture

LinkStream follows a **Layered Architecture** pattern, which separates concerns and makes the system easier to test and scale.

### 📦 A. Model Layer (`src/model/URL.java`)
**The Data Blueprint**
- **Role**: Defines what a "URL" object looks like in our system.
- **Logic**: It stores the original URL, the generated short code, and advanced metadata like `expiryTime`, `maxClicks`, and `deviceStats`.
- **Key Method**: `recordClick(deviceType)` increments the count and updates the device-specific tracking map.

### 📦 B. Utils Layer (`src/utils/Base62Encoder.java`)
**The Engine**
- **Role**: Handles the mathematical conversion of IDs into short strings.
- **Logic**: Uses a character set of 62 (a-z, A-Z, 0-9). This is the industry standard (used by Bitly) because it creates very short, readable URLs.
- **Example**: ID `10000` might become `cLs`.

### 📦 C. Repository Layer (`src/repository/URLRepository.java`)
**The Database Manager**
- **Role**: Manages data storage and retrieval.
- **Logic**: Uses a `HashMap` for lightning-fast lookups (O(1) time complexity).
- **Persistence**: It reads/writes to `data.txt`. Every time a URL is saved or deleted, the repo synchronizes the text file to ensure no data is lost if the app restarts.

### 📦 D. Service Layer (`src/service/URLShortenerService.java`)
**The Brain**
- **Role**: Contains the "Business Logic".
- **Logic**: 
  - Validates URLs (adds `https://` if missing).
  - Checks for code collisions.
  - Detects device types from `User-Agent`.
  - Determines if a link is **Expired** or has reached its **Click Limit**.

### 📦 E. Controller Layer (`src/controller/URLController.java`)
**The CLI Interface**
- **Role**: Provides a text-based menu for terminal users.
- **Logic**: Translates user inputs into Service calls.

### 🚀 F. Entry Point & API (`src/Main.java`)
**The Orchestrator**
- **Role**: Launches the entire system.
- **Logic**: Actually starts **two systems at once**:
  1. **The CLI**: For local management.
  2. **The HTTP Server**: A background web server that exposes REST API endpoints (`/api/shorten`, `/api/admin/stats`, `/api/admin/delete`) and handles live browser **302 Redirects**.

---

## 🌐 2. The Web Frontend (`web/`)
- **Dashboard (`index.html`)**: A modern, single-page application (SPA) built with Vanilla JS.
- **Design System (`style.css`)**: Implements **Glassmorphism**, responsive layouts, and a dual-perspective (User/Admin) UI.
- **System Bridge**: It uses the `fetch()` API to communicate asynchronously with the Java backend.
