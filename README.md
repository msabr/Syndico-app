# 🏢 Syndico App

<p align="center">
  <b>A modern platform for managing residential syndicates</b><br/>
  Built with Spring Boot • Thymeleaf • MySQL
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Backend-SpringBoot-brightgreen?style=for-the-badge&logo=springboot"/>
  <img src="https://img.shields.io/badge/Frontend-Thymeleaf-blue?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Database-MySQL-orange?style=for-the-badge&logo=mysql"/>
  <img src="https://img.shields.io/badge/Architecture-Clean%20MVC-purple?style=for-the-badge"/>
</p>

---

## 🌟 Overview

**Syndico App** is a full-stack web application designed to simplify the management of residential buildings.

It connects:

* 🏠 Residents
* 👨‍💼 Administrators

Into one unified platform for:

* Payments 💳
* Complaints 🛠️
* Communication 📢
* Governance 🗳️

---

## 🚀 Features

✨ **Core Features**

* 🔐 Secure Authentication (Spring Security)
* 💳 Payment & Charges Management
* 📢 Notifications & Messaging
* 🛠️ Complaint & Maintenance Tracking
* 📅 Reservations System

🤖 **Smart Assistant**

* Chatbot based on database (Q&A system)
* Helps users navigate the platform

🌍 **Internationalization**

* Multi-language support (FR / EN / AR ready)

---

## 📸 Screenshots (Main Features)

### 🏠 Home

![Home](screenSyn/home.png)

### 📊 Admin Dashboard

![Admin Dashboard](screenSyn/admin-dashboard.png)

### 👤 Resident Dashboard

![Resident Dashboard](screenSyn/resident-dashboard.png)

### 🤖 Chatbot

![Chatbot](screenSyn/chatbot.png)

---

## 🖼️ More Screens (Full Platform)

👉 All platform screenshots are available here:

```
/screenSyn/
```

Examples you can include:

* complaints.png
* payments.png
* reservations.png
* messages.png
* documents.png

---

## 🧠 Architecture (Clean & Scalable)

The application follows a **modular Clean MVC architecture**:

```
                ┌────────────────────┐
                │   Presentation     │
                │ (Thymeleaf Views)  │
                └─────────┬──────────┘
                          │
                ┌─────────▼──────────┐
                │    Controllers     │
                │ (Admin / Resident) │
                └─────────┬──────────┘
                          │
                ┌─────────▼──────────┐
                │     Services       │
                │  Business Logic    │
                └─────────┬──────────┘
                          │
                ┌─────────▼──────────┐
                │   Repositories     │
                │   (Spring Data)    │
                └─────────┬──────────┘
                          │
                ┌─────────▼──────────┐
                │     Database       │
                │      MySQL         │
                └────────────────────┘
```

### 🔹 Key Points

* ✔ Feature-based controllers (`admin/`, `resident/`)
* ✔ Separation of concerns (Controller → Service → Repository)
* ✔ Thymeleaf for server-side rendering
* ✔ Refactored from monolithic to modular architecture

---

## ⚙️ Tech Stack

| Layer         | Technology                               |
|---------------| ---------------------------------------- |
| 🧠 Backend    | Spring Boot, Spring MVC, Spring Security |
| 🎨 Frontend   | Thymeleaf, HTML5, CSS3, Bootstrap, JS    |
| 🗄️ Database  | MySQL                                    |
| 🔄 ORM        | Hibernate / JPA                          |
| 🛠️ Build     | Maven                                    |

---

## 🚀 Getting Started

```bash
git clone https://github.com/msabr/Syndico-app.git
cd syndico-app
mvn spring-boot:run
```

👉 Open: http://localhost:8080

---

## 👨‍💻 Team

<p align="center">

| 👤 Name              | 🌐 GitHub                          | 💼 LinkedIn                                         | 📧 Email                                                         |
|----------------------|------------------------------------|-----------------------------------------------------|------------------------------------------------------------------|
| **Soufiane Zekaoui** | https://github.com/soufianezekaoui | https://linkedin.com/in/soufiane-zekaoui-445b1b352/ | [soufiane.zekaoui@gmail.com](mailto:soufiane.zekaoui@gmail.com)  |
| **SABR Mohamed**     | https://github.com/msabr           | link                                                | email                                                            |
| **AbdelkrimZidouh**  | https://github.com/AbdelkrimZidouh | link                                                | email                                                            |

</p>

---

## 🚧 Future Work

* 🐳 Docker deployment
* ☁️ Cloud hosting
* 📊 Advanced analytics
* 📱 Mobile optimization

---

<p align="center">
  ⭐ If you like this project, don't forget to star it!
</p>

<p align="center">
  Built with ❤️ by Syndico Team
</p>

