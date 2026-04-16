# LinkStream | Java URL Shortener & Tracker

A professional-grade URL shortening and analytics tracking system built with Java. This project demonstrates clean architecture, persistence logic, and Base62 encoding.

## 🚀 Features

- **Base62 Encoding**: Generates short, readable URL slugs (e.g., `cB2`).
- **Persistence**: Automatically saves and loads data from `data.txt` to survive restarts.
- **Analytics Tracking**: Tracks total clicks and last-click timestamps for every link.
- **Robust Controller**: User-friendly console interface with input validation.
- **Premium Web Mockup**: includes a modern dashboard design (`/web`).

## 🏗️ Architecture

- `src/model`: Core entities (URL, Tracking data).
- `src/utils`: Helper classes (Encoding logic).
- `src/repository`: Data access and persistence layer.
- `src/service`: Business logic for shortening and redirection.
- `src/controller`: User interaction layer.

## 🛠️ How to Run

1. **Compile the project**:
   ```bash
   javac -d out src/**/*.java src/Main.java
   ```

2. **Run the application**:
   ```bash
   java -cp out Main
   ```

## 📊 Sample Data Format (`data.txt`)
Data is stored in a pipe-delimited format:
```text
cB2|https://google.com|12
aF9|https://github.com|45
```

## 🌟 Future Roadmap
- [ ] MySQL Database Integration (JDBC)
- [ ] REST API using Spring Boot
- [ ] Real-time Dashboard with React
