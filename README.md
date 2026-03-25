# 🏢 Syndico App

> A modern web application for residential syndicate (syndic) management — simplifying communication, payment tracking, and administrative operations between residents, clients, and administrators.

![Java](https://img.shields.io/badge/Java-17+-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen?style=flat-square&logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8+-blue?style=flat-square&logo=mysql)
![Bootstrap](https://img.shields.io/badge/Bootstrap-5-purple?style=flat-square&logo=bootstrap)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)

---

## 📖 Overview

**Syndico** is a full-stack web application built to digitize and streamline the day-to-day operations of residential building syndicates. It provides distinct dashboards and workflows for **residents**, **clients**, and **administrators**, and includes an integrated AI chatbot assistant and multilingual support.

Built by a team of 3 full-stack developers using **Spring Boot + Thymeleaf + MySQL + Bootstrap 5**.

---

## ✨ Features

- 👤 **Role-based access** — separate interfaces for residents, clients, and administrators
- 💳 **Payment tracking** — manage and monitor syndicate fees and payments
- 📢 **Communication tools** — announcements and messaging between parties
- 🤖 **Chatbot assistant** — AI-powered assistant to guide users and answer FAQs
- 🌍 **Multilingual support** — dynamic language switching (French, English, and more)
- 🔐 **Secure authentication** — Spring Security with role-based authorization
- 📱 **Responsive design** — mobile-friendly UI with Bootstrap 5

---

## ⚙️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Frontend** | HTML5, CSS3, JavaScript (ES6), Thymeleaf, Bootstrap 5 |
| **Backend** | Spring Boot 3.5.7, Spring MVC, Spring Security, Spring Data JPA |
| **Database** | MySQL 8+ |
| **ORM** | Hibernate / JPA |
| **Build Tool** | Maven |
| **IDE** | IntelliJ IDEA |

---

## 🧱 Project Architecture

```
syndico-app/
│
├── src/main/java/
│   ├── controllers/        # REST Controllers (API + Thymeleaf routes)
│   ├── services/           # Business logic layer
│   ├── repositories/       # Spring Data JPA repositories
│   ├── models/             # JPA entities (database models)
│   ├── config/             # Spring Security & global configuration
│   └── dto/                # Data Transfer Objects
│
├── src/main/resources/
│   ├── templates/          # Thymeleaf HTML views
│   ├── static/
│   │   ├── css/            # Bootstrap and custom stylesheets
│   │   ├── js/             # Frontend JavaScript (ES6)
│   │   └── images/         # Static image assets
│   ├── i18n/               # Internationalization message bundles
│   └── application.properties  # Spring Boot configuration
│
└── pom.xml                 # Maven dependencies and build config
```

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8+
- IntelliJ IDEA (recommended) or any Java IDE

### 1. Clone the Repository

```bash
git clone https://github.com/msabr/Syndico-app.git
cd Syndico-app
```

### 2. Configure the Database

Create a MySQL database (or let Spring Boot create it automatically):

```sql
CREATE DATABASE syndico_db;
```

Then update `src/main/resources/application.properties` with your credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/syndico_db
spring.datasource.username=root
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.thymeleaf.cache=false
```

### 3. Build & Run

```bash
mvn clean install
mvn spring-boot:run
```

Then open your browser at: **[http://localhost:8080](http://localhost:8080)**

---

## 👥 Contributing — Adding a New Feature Page

Each developer follows this standard MVC pattern when adding a new feature:

### Step 1 — Create the Entity (Model)

```java
// src/main/java/models/Payment.java
@Entity
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double amount;
    private LocalDate date;
    // getters & setters
}
```

### Step 2 — Create the Repository

```java
// src/main/java/repositories/PaymentRepository.java
public interface PaymentRepository extends JpaRepository<Payment, Long> {}
```

### Step 3 — Create the Service

```java
// src/main/java/services/PaymentService.java
@Service
public class PaymentService {
    @Autowired private PaymentRepository repo;
    public List<Payment> getAll() { return repo.findAll(); }
}
```

### Step 4 — Create the Controller

```java
// src/main/java/controllers/PaymentController.java
@Controller
@RequestMapping("/payments")
public class PaymentController {
    @Autowired private PaymentService service;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("payments", service.getAll());
        return "payments"; // maps to templates/payments.html
    }
}
```

### Step 5 — Create the Thymeleaf View

```html
<!-- src/main/resources/templates/payments.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Payments</title>
    <link rel="stylesheet" th:href="@{/css/bootstrap.min.css}">
</head>
<body>
    <div class="container mt-5">
        <h1>Payments</h1>
        <table class="table">
            <thead>
                <tr><th>ID</th><th>Amount</th><th>Date</th></tr>
            </thead>
            <tbody>
                <tr th:each="p : ${payments}">
                    <td th:text="${p.id}"></td>
                    <td th:text="${p.amount}"></td>
                    <td th:text="${p.date}"></td>
                </tr>
            </tbody>
        </table>
    </div>
</body>
</html>
```

✅ You've added a complete, database-connected page following the MVC pattern.

---

## 🤖 Chatbot Assistant

Syndico includes a built-in AI assistant that can:

- Guide users through platform features
- Answer frequently asked questions
- Help administrators manage residents and payments

> 💡 **Roadmap:** The chatbot will be extended with a fine-tuned OpenAI API / LangChain microservice for advanced conversational capabilities.

---

## 🌍 Multilingual Support

The app uses **Spring i18n** (internationalization) with message property files:

```
src/main/resources/i18n/
├── messages_fr.properties   # French
├── messages_en.properties   # English
└── messages_ar.properties   # Arabic (planned)
```

A language selector is available in the navigation bar for dynamic switching.

---

## 🚧 Roadmap

- [x] Core syndicate management (residents, payments, announcements)
- [x] Role-based authentication (Admin / Client / Resident)
- [x] Integrated AI chatbot assistant
- [x] Multilingual support (FR / EN)
- [ ] AI-powered data insights and analytics dashboard
- [ ] Progressive Web App (PWA) support
- [ ] Performance optimization (caching, lazy loading)
- [ ] Mobile-native version

---

## 🧑‍💻 Team

| Name | Role | Branch |
|------|------|--------|
| **Soufiane ZEKAOUI** | Full Stack Developer & Architect | `soufiane` |
| **AbdelKrim ZIDOUH** | Full Stack Developer | `branch-1` |
| **Mohamed SABIR** | Full Stack Developer | `sabir` |

---

## 📜 License

This project is licensed under the **MIT License** — free for educational and professional use.

---

<p align="center">Made with ❤️ by the Syndico Team</p>
