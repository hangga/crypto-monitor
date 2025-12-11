# Crypto Monitor — Example Resilient Ktor API (Circuit Breaker + Kotlin Flow)

**Crypto Monitor** is an *example implementation* demonstrating how to build **resilient, fault-tolerant APIs** using:

* **Ktor (Kotlin server framework)**
* **Resilience4j Circuit Breaker**
* **Kotlin Flow** for streaming
* **Fallback caching**
* **Polling external APIs (Coingecko)**

This project is intentionally simple and designed for educational purposes, tutorials, and articles.
It is **not intended for production use**.

---

## **⚠️ Important Note**

> **This repository is for demonstration and educational purposes only.
> It is *not* a production-ready system.
> The crypto price API, error handling, cache, and streaming logic are simplified for clarity.**

---

## **Features**

### ✅ Resilient External Calls

External price fetching goes through a **Resilience4j Circuit Breaker**, slowing or stopping calls when the upstream service is unstable.

### ✅ Kotlin Flow Streaming

Continuously emits BTC/USD price updates using Kotlin Flow.

### ✅ Simple Fallback Cache

If the external provider fails, the API returns the last known price instead of failing.

### ✅ Clean Modular Architecture

Configurable, testable, and easy to extend.

### ✅ Example of HOCON-based Configuration

Uses `ConfigFactory.load()` to read `application.conf`.

---

## **Architecture Overview**

```
 ┌────────────────┐      ┌──────────────────────┐
 │   Web Client   │ ---> │     Ktor Server      │
 └────────────────┘      └──────────┬───────────┘
                                    │
                          ┌─────────┴─────────┐
                          │   PriceRoutes     │
                          └─────────┬─────────┘
                                    │
                      ┌─────────────┴─────────────────┐
                      │    PriceFlow (Kotlin Flow)    │
                      └─────────────┬─────────────────┘
                                    │
                      ┌─────────────┴─────────────────┐
                      │   ExternalCryptoService (CB)  │
                      └─────────────┬─────────────────┘
                                    │
                          ┌─────────┴──────────┐
                          │   Fallback Cache   │
                          └────────────────────┘
```

---

## **Run the Project**

### **1. Clone**

```bash
git clone https://github.com/hangga/crypto-monitor.git
cd crypto-monitor
```

### **2. Run**

```bash
./gradlew run
```

or using the JAR:

```bash
java -jar build/libs/crypto-monitor-all.jar
```

### 3. Local Test
```bash
$ curl http://localhost:8080/stream/price
```

#### Example Output:

```bash
data: {"id":"bitcoin","currency":"usd","price":90209.0}

data: {"id":"bitcoin","currency":"usd","price":90209.0}

data: {"id":"bitcoin","currency":"usd","price":90209.0}

data: {"id":"bitcoin","currency":"usd","price":90209.0}

data: {"id":"bitcoin","currency":"usd","price":90209.0}

data: {"id":"bitcoin","currency":"usd","price":90209.0,"source":"fallback"}

data: {"id":"bitcoin","currency":"usd","price":90209.0,"source":"fallback"}

data: {"id":"bitcoin","currency":"usd","price":90209.0,"source":"fallback"}

data: {"id":"bitcoin","currency":"usd","price":90209.0,"source":"fallback"}

data: {"id":"bitcoin","currency":"usd","price":90209.0,"source":"fallback"}

data: {"id":"bitcoin","currency":"usd","price":90209.0,"source":"fallback"}

data: {"id":"bitcoin","currency":"usd","price":90209.0,"source":"fallback"}

data: {"id":"bitcoin","currency":"usd","price":90209.0,"source":"fallback"}
```

#### Demo

![run](https://github.com/hangga/crypto-monitor/blob/main/crypto-monitor.gif?raw=true)

---

## **Endpoints**

### **`GET /`**

Simple health check.

### **`GET /price`**

Returns the latest BTC/USD price using:

* circuit breaker
* external fetch
* fallback cache

### **`GET /stream/price`**

Streams price updates (using Kotlin Flow).

---

## **Configuration (`application.conf`)**

```hocon
ktor {
  crypto {
    externalApiUrl = "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=usd"
    pollIntervalMs = 2000
  }
}
```

---

## **Project Structure**

```
src/
 └─ main/
     ├─ kotlin/id/web/hangga/
     │   ├─ Application.kt
     │   ├─ config/CryptoConfig.kt
     │   ├─ resilience/CircuitBreakerConfigWrapper.kt
     │   ├─ service/ExternalCryptoService.kt
     │   ├─ service/FallbackCache.kt
     │   ├─ stream/PriceFlow.kt
     │   └─ routes/PriceRoutes.kt
     └─ resources/
         └─ application.conf
```

---

## **Purpose**

This project was created as the code companion for articles or study topics such as:

> **“Designing Resilient APIs with Ktor, Circuit Breakers, and Kotlin Flow.”**

It focuses on clarity and learning — not production stability, security, or performance tuning.

---

## **License**

MIT License — free for personal, educational, or commercial use.

---

If you want, I can also:

* Improve the README with diagrams and badges
* Create a cleaner folder structure
* Add usage examples or diagrams
* Generate the article draft

Just tell me: **“improve README”** or **“continue article draft”**.
