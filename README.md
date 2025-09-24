# Transaction Manager

Transaction Manager is a Spring Boot application designed to manage purchases. It provide API endpoints to storing purchases and retrieve them with currency conversion based on exchange rates.

## Features

- **Create Purchase**: Add a purchase with description, date, and amount.
- **Retrieve Purchase**: Fetch purchase details by ID and convert the amount to a requested currency.

---

## Building the Application

You can build the application using Maven or Docker.

### Build with Maven

1. **Install Java 21 and Maven 3.9+**
2. Clone the repository:
    ```sh
    git clone https://github.com/marcosbrazz/transaction-manager.git
    cd transaction-manager
    ```
3. Build the application:
    ```sh
    mvn clean package
    ```

This will generate a JAR file under the `target/` directory.

---

### Build and Run with Docker

The repository includes a multi-stage `Dockerfile`:

```sh
docker build -t transaction-manager .
docker run -p 8080:8080 -e TREASURY_API_URL="optional treasure api url" -e ALLOW_H2_CONSOLE=true|false transaction-manager 
```
- TREASURY_API_URL is optional. A default value is provided in source code.
- ALLOW_H2_CONSOLE is optional. Set to true to allow access to H2 database web console.

---

### Build and Run with Docker Compose

```sh
docker compose up
```

## Running Locally

If you built with Maven:

```sh
java -jar target/*.jar
```

The application will start on port **8080** by default.

---

## Configuration

The source code already define the URL `https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange`
to be used for exchange rate retrieval. 
It can be overriden by setting the environment variable `TREASURY_API_URL`.

_Examples:_

```sh
export TREASURY_API_URL="https://example.com/api"
```

or in docker-compose.yml;

---

## Database H2
This application uses H2 in memory database.
Database may be inspected by using its web console

`http://localhost:8080/h2-console`

Fill the field "JDBC URL" with `jdbc:h2:mem:transactiondb`

Hit "Connect"

## API Usage

### 1. Create a Purchase

- **Endpoint:** `POST /purchase`
- **Request Body:**
    ```json
    {
      "description": "Office supplies",
      "transactionDate": "2025-09-18",
      "amount": 100.50
    }
    ```
- **Response:**
    ```json
    {
      "id": "the purchase id"
    }
    ```

### 2. Get Purchase by ID with Currency Conversion

- **Endpoint:** `GET /purchase/{id}?currency=Real`
- **Response Example:**
    ```json
    {
      "id": "generated_purchase_id",
      "description": "Office supplies",
      "transactionDate": "2025-09-18",
      "amount": 100.50,
      "currency": "Real",
      "rate": 1.07,
      "convertedAmount": 107.54
    }
    ```

---

## Error Handling

The API returns structured error responses. Example:

```json
{
  "errors": [
    {
      "field": "amount",
      "message": "Purchase amount must be positive"
    }
  ]
}
```

---

## Running Tests

To run unit and integration tests:

```sh
mvn test
```


