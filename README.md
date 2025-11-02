# 🏢 Syndico App

Syndico is a modern Web Application for Syndic Management, designed to simplify communication, payment tracking, and administrative management between residents, clients, and administrators.

🚀 Built by a team of 3 full-stack developers using Spring Boot + Thymeleaf + MySQL + Bootstrap 5, this app combines high performance, modular architecture, and smart innovation (including an integrated chatbot assistant and multilingual support).

⚙️ Tech Stack

Frontend:

HTML5, CSS3, JavaScript (ES6)

Thymeleaf (for dynamic server-side rendering)

Bootstrap 5 (responsive design)

Backend:

Spring Boot 3.5.7 (stable LTS version)

Spring MVC (REST Controllers)

Spring Security (Authentication & Roles)

Spring Data JPA (ORM with MySQL)

Lombok (optional – simplifies boilerplate code)

Database:

MySQL 8+

Build Tool:

Maven

IDE:

IntelliJ IDEA

🧱 Project Architecture
syndico-app/
│
├── src/main/java/
│   ├── controllers/            # REST Controllers (API + Thymeleaf routes)
│   ├── services/               # Business logic
│   ├── repositories/           # JPA repositories
│   ├── models/                 # JPA entities
│   ├── config/                 # Spring Security & global config
│   └── dto/                    # Data Transfer Objects
│
├── src/main/resources/
│   ├── templates/              # Thymeleaf views (HTML pages)
│   ├── static/
│   │   ├── css/                # Bootstrap and custom styles
│   │   ├── js/                 # Frontend logic (ES6)
│   │   └── images/             # Static assets
│   └── application.properties  # Spring Boot configuration
│
└── pom.xml                     # Dependencies and build setup

🚀 How to Run the App
1️⃣ Clone the Repository
git clone https://github.com/msabr/Syndico-app.git
cd Syndico-app
git checkout soufiane

2️⃣ Configure the Database

Create a database named syndico_db in MySQL.

Then open src/main/resources/application.properties and set your credentials:

spring.datasource.url=jdbc:mysql://localhost:3306/syndico_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.thymeleaf.cache=false

3️⃣ Build & Run

In IntelliJ Terminal (or CMD):

mvn clean install
mvn spring-boot:run


Then open:
👉 http://localhost:8080

👥 Team Collaboration — How to Add a New Page

Each developer can add a feature (page) that interacts with the database.
Follow these steps carefully ⬇️

🔹 Step 1 — Create the Model

Add your entity in models/.

@Entity
public class Payment {
@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
private double amount;
private LocalDate date;
}

🔹 Step 2 — Create the Repository

Add a JPA repository in repositories/.

public interface PaymentRepository extends JpaRepository<Payment, Long> {}

🔹 Step 3 — Create the Service

Business logic in services/.

@Service
public class PaymentService {
@Autowired private PaymentRepository repo;
public List<Payment> getAll() { return repo.findAll(); }
}

🔹 Step 4 — Create the Controller

Handle web routes in controllers/.

@Controller
@RequestMapping("/payments")
public class PaymentController {
@Autowired private PaymentService service;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("payments", service.getAll());
        return "payments"; // Refers to templates/payments.html
    }
}

🔹 Step 5 — Create the Frontend Page

Add a new payments.html in templates/.

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
      <thead><tr><th>ID</th><th>Amount</th><th>Date</th></tr></thead>
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


✅ You’ve just added a complete page (backend + frontend + DB).

💬 Chatbot Assistant (IA Integration)

Syndico includes a smart assistant that can:

Guide clients on using the platform

Answer frequently asked questions

Help administrators manage residents and payments

💡 The chatbot will later use a fine-tuned OpenAI API / LangChain microservice (modular extension).

🌍 Multi-Language Support

We use Spring’s i18n (internationalization) feature with message bundles (messages_fr.properties, messages_en.properties, etc.) to dynamically switch the language.
A language selector will be included in the navbar.

🚧 Future Improvements

🧠 Integration of AI modules for data insights

📱 Progressive Web App (PWA) version

📈 Admin dashboard with analytics (Chart.js)

🧩 Extension-based modularity (close for modification, open for extension)

⚡ Performance optimization with caching and lazy loading

🧑‍💻 Team Members
Name	Role	Git Branch
Soufiane	Full Stack Dev / Architect	soufiane
Teammate 1	Full Stack Dev	branch1
Teammate 2	Full Stack Dev	branch2
📜 License

This project is under the MIT License — open for educational and professional innovation.