# 📱 OTP Verification System

🚀 A **real-time OTP (One-Time Password) verification system** built using **Spring Boot, Redis, and Twilio API**.

---

## ✨ Features

✔ Generate secure 6-digit OTP
✔ Send OTP via SMS using Twilio
✔ Store OTP in Redis with expiry (5 minutes)
✔ Verify OTP with validation
✔ Fast & scalable backend system

---

## 🛠 Tech Stack

* ☕ Java 11
* 🌱 Spring Boot
* 🔴 Redis (In-memory database)
* 📩 Twilio API (SMS service)
* 📦 Maven

---

## ⚙️ How It Works

```id="flow1"
User → Request OTP → Server generates OTP → 
Store in Redis → Send SMS → User enters OTP → Verify OTP
```

---

## 📡 API Endpoints

### 🔹 Send OTP

```id="api1"
POST /otp/send?mobile=+91XXXXXXXXXX
```

### 🔹 Verify OTP

```id="api2"
POST /otp/verify?mobile=+91XXXXXXXXXX&otp=123456
```

---

## 🚀 Getting Started

### 1️⃣ Clone Repository

```id="clone1"
git clone https://github.com/your-username/otp-verification-system.git
cd otp-verification-system
```

---

### 2️⃣ Start Redis

```id="redis1"
redis-server
```

---

### 3️⃣ Configure Application

```id="config1"
spring.redis.host=localhost
spring.redis.port=6379

twilio.account.sid=YOUR_SID
twilio.auth.token=YOUR_TOKEN
twilio.phone.number=+1XXXXXXXXXX
```

---

### 4️⃣ Run Project

```id="run1"
mvn spring-boot:run
```

---

## ⚠️ Important Notes

* 🔐 Do NOT expose your Twilio credentials publicly
* ⚡ Redis must be running before starting the app
* 📱 Twilio trial works only with verified numbers

---

## 📸 Future Improvements

* 🔑 JWT Authentication
* 📧 Email OTP support
* ⏱ Rate limiting
* 🌐 Frontend UI (React)

---

## 👨‍💻 Author

**Mrutyunjay Behera**

---

## ⭐ Support

If you like this project, don’t forget to ⭐ the repository!
