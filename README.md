# DAT250: Poll Application - Group 11

A fully containerized Voting & Polling application built for the **DAT250: Software Technology** course. This system allows users to create polls, vote in real-time, and view aggregated results.

## 🚀 Project Overview

The **PollApp** is a distributed web application designed to demonstrate modern software architecture principles, including:
- **Microservices-ready architecture** (Separated Frontend & Backend).
- **Message Queuing** for decoupling vote processing.
- **In-Memory Caching** for high-speed data access.
- **Containerization** for consistent deployment across environments.

### 🌟 Key Functionalities
- **User Management:** Users can register and log in (IoT devices/Users).
- **Poll Creation:** Registered users can create public or private polls.
- **Voting Mechanism:** Real-time voting interface.
- **Live Results:** View poll statistics and results instantly.
- **Distributed Design:** Uses RabbitMQ to handle vote ingress and Redis for caching active polls.

---

## 🛠️ Technology Stack

| Component | Technology             | Description |
| :--- |:-----------------------| :--- |
| **Backend** | Java 21, Spring Boot   | REST API, Business Logic, and Data Handling. |
| **Frontend** | JavaScript + React     | Responsive Single Page Application (SPA). |
| **Database** | Hibernate H2           | Persistent storage for users and polls. |
| **Messaging** | RabbitMQ               | Asynchronous message broker for processing votes. |
| **Cache** | Redis                  | High-performance caching for vote counts. |
| **Build Tools** | Gradle, Npm            | Dependency management and build automation. |
| **DevOps** | Docker, Docker Compose | Containerization and orchestration. |
| **CI/CD** | GitHub Actions         | Automated testing and Docker Hub publishing. |

---

## 📚 Documentation
[📄 Read Project Description](ProjectDescription.md) | [✅ View Task Requirements](TASK.md)

---

## 🐳 How to Run the Application

We provide two ways to run the application. **Method 1 is highly recommended** as it handles all infrastructure (Redis/RabbitMQ) automatically.

### Prerequisites (General)
* **Ports:** Ensure `3000` (Frontend), `8080` (Backend), `5672` (RabbitMQ), and `6379` (Redis) are free.

---

### Method 1: The "Docker" Run (Recommended) 🏆
*Runs the pre-built images from Docker Hub. No Java or Node.js installation required.*

1.  **Get the Orchestration File:**
    * **Option A (Clone Repo):**
        ```bash
        git clone [https://github.com/WonderBattle/DAT250Project-PollApp.git](https://github.com/WonderBattle/DAT250Project-PollApp.git)
        cd DAT250Project-PollApp
        ```
    * **Option B (Single File):** Download just the [docker-compose-publish.yml](docker-compose-publish.yml) file to a folder.

2.  **Start the Application:**
    ```bash
    docker compose -f docker-compose-publish.yml up
    ```

3.  **Access the App:**
    * **Frontend:** [http://localhost:3000](http://localhost:3000)
    * **Backend:** [http://localhost:8080](http://localhost:8080)

4.  **Stop the App:**
    ```bash
    docker compose -f docker-compose-publish.yml down
    ```

---

### Method 2: Manual Run (Development Mode) 🛠️
*Runs the code directly on your machine without Docker containers for the app logic.*

**⚠️ Additional Prerequisites for Method 2:**
* **Java 21** and **Node.js (v20+)** installed.
* **Redis** and **RabbitMQ** must be installed and running locally on standard ports (6379 & 5672).

#### Step 1: Clone the Repository
```bash
git clone [https://github.com/WonderBattle/DAT250Project-PollApp.git](https://github.com/WonderBattle/DAT250Project-PollApp.git)
cd DAT250Project-PollApp
```
#### Step 2: Run the Backend (Spring Boot)

Open a terminal in the root folder:

```bash
# Linux/Mac
./gradlew bootRun

# Windows
.\gradlew.bat bootRun
```

*Wait for the log: "Started PollApplication in ... seconds"*

#### Step 3: Run the Frontend (React)

Open a **new** terminal window:

```bash
cd poll-app-frontend

# Install dependencies
npm install

# Start the dev server
npm start
```

*The browser should open automatically at http://localhost:3000*

---

## 🧪 Testing & CI/CD
This repository includes a **GitHub Actions** pipeline that:
1.  **Tests:** Runs `./gradlew build` and unit tests on every push.
2.  **Publishes:** Automatically builds and pushes Docker images to Docker Hub (`elenacg/pollapp-backend` & `pollapp-frontend`) when changes are pushed to `main`.
