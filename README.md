# 🧧 CharityCollection Application

**Author:** Bartosz Mordarski  

## Project Overview

**CharityCollection** is a web application developed as part of the LAT 2025 recruitment task. It is designed to manage collection boxes during fundraising events for charity organizations.

The application provides functionality for:

- Creating and managing fundraising events  
- Registering and managing collection boxes  
- Assigning boxes to fundraising events  
- Adding money in multiple currencies to boxes  
- Transferring funds with automatic currency conversion  
- Generating financial reports for events

---

## Technologies Used

- **Spring Boot**
- **H2 Database**
- **Maven**
- **JUnit 5, Mockito, AssertJ**
- **Spring Boot Test**
- **Jakarta Validator**
- **RestTemplate**
---

## Key Components

### Data Models

- **FundraisingEvent**: Event with name, description, dates, currency, and account balance  
- **CollectionBox**: Box with unique identifier, event assignment, and multi-currency money  
- **CollectionBoxMoney**: Amount and currency in a box  
- **Currency**: Code and name of currency  
- **ExchangeRate**: Exchange rate between currencies  

### Controllers

- `FundraisingEventController` – Create events  
- `CollectionBoxController` – Register, list, assign, remove, add money, empty boxes  
- `ReportController` – Generate financial reports  
- `ExchangeRateController` – Daily exchange rate update with endpoint for manual update

### Services

- `FundraisingEventService` – Fundraising event logic  
- `CollectionBoxService` – Box management logic  
- `ReportService` – Generates reports  
- `ExchangeRateService` – Fetches NBP exchange rates, calculates rates and saves them to the database

---

## ⚠️ Note on Exchange Rates

The app fetches **real-time exchange rates** from the **NBP API** to support currency conversions.  
Rates are:

- **Updated automatically** at **startup** and **daily at 2 PM**  
- **Stored with 6 decimal places**  
- **Cross-rates calculated** using PLN as the base  
- **Manually updateable** via a REST endpoint  

---

## Functionality Overview

### Fundraising Events

- **Create Event**  
  - Validates dates and currency  
  - Initializes balance to 0  

### Collection Boxes

- **Register Box**  
  - Unique identifier  
  - Empty by default  

- **List Boxes**  
  - Returns identifiers, status, and assignment info  

- **Unregister Box**  
  - Deletes the box and clears contents  

- **Assign to Event**  
  - Only empty boxes can be assigned  

- **Add Money**  
  - Multiple supported currencies  
  - Validates inputs  

- **Empty Box**  
  - Transfers funds to event with currency conversion  
  - Clears and resets the box  

### Financial Reports

- **Generate Report**  
  - Lists events with their balances and currencies  

---

## Requirements

- Java 21  
- Maven  

---

## Installation and Running

```bash
# Clone the repository
git clone https://github.com/BartoszMordarski/CharityFundsRaising.git
cd CharityFundsRaising

# Build and run the application
mvn spring-boot:run
```

---

## API Documentation

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/fundraising-events` | Create a fundraising event |
| POST | `/api/collection-boxes` | Register a new collection box |
| GET | `/api/collection-boxes` | List all collection boxes |
| DELETE | `/api/collection-boxes/delete/{identifier}` | Unregister a collection box |
| POST | `/api/collection-boxes/assign` | Assign a collection box to an event |
| POST | `/api/collection-boxes/add` | Add money to a collection box |
| POST | `/api/collection-boxes/empty/{identifier}` | Empty a collection box |
| GET | `/api/reports/financial` | Generate a financial report |
| POST | `/api/exchange-rates/update` | Manually update exchange rates |

---

## Sample Queries

### 1. Create a Fundraising Event

**Request type:** POST  
**Endpoint:** `http://localhost:8080/api/fundraising-events`  

**Request body:**
```json
{
  "name": "Charity One",
  "description": "Annual charity fundraiser",
  "startDate": "2025-01-11",
  "endDate": "2025-01-14",
  "currencyCode": "PLN"
}
```


### 2. Register a Collection Box

**Request type:** POST  
**Endpoint:** `http://localhost:8080/api/collection-boxes`  

**Request body:**
```json
{
  "identifier": "BOX001"
}
```


### 3. List All Collection Boxes

**Request type:** GET  
**Endpoint:** `http://localhost:8080/api/collection-boxes`  


### 4. Unregister a Collection Box

**Request type:** DELETE  
**Endpoint:** `http://localhost:8080/api/collection-boxes/delete/BOX001`  


### 5. Assign a Box to a Fundraising Event

**Request type:** POST  
**Endpoint:** `http://localhost:8080/api/collection-boxes/assign`  

**Request body:**
```json
{
  "collectionBoxIdentifier": "BOX001",
  "fundraisingEventId": 1
}
```


### 6. Add Money to a Collection Box

**Request type:** POST  
**Endpoint:** `http://localhost:8080/api/collection-boxes/add`  

**Request body:**
```json
{
  "collectionBoxIdentifier": "BOX001",
  "amount": 100.50,
  "currencyCode": "PLN"
}
```


### 7. Empty a Collection Box

**Request type:** POST  
**Endpoint:** `http://localhost:8080/api/collection-boxes/empty/BOX001`  


### 8. Generate a Financial Report

**Request type:** GET  
**Endpoint:** `http://localhost:8080/api/reports/financial`  


### 9. Manually Update Exchange Rates

**Request type:** POST  
**Endpoint:** `http://localhost:8080/api/exchange-rates/update`  

---

## Testing

Running tests
```bash
mvn test
```

### Unit Tests for services

- `FundraisingEventServiceTest`
- `CollectionBoxServiceTest`
- `ReportServiceTest`

### Integration Tests for controllers

- `FundraisingEventControllerIntegrationTest`  
- `CollectionBoxControllerIntegrationTest`  
- `ReportControllerIntegrationTest`  

### DTO Validation Tests for DTO classes


**Libraries used for testing:**  
- JUnit 5  
- Mockito  
- AssertJ  
- Spring Boot Test  
- Jakarta Validator  
