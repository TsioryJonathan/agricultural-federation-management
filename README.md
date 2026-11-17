# Agricultural Federation API

REST API to manage an agricultural federation: members, collectivities (local groups), their financial accounts, membership fees, transactions and activity attendance.

Team project built at HEI.

## What it does

- Create members and collectivities, update a collectivity's information
- Membership fees and member payments
- Financial accounts per collectivity (cash, bank, mobile money) and their transactions
- Activities and attendance, plus statistics per collectivity and for the whole federation
- API key authentication

The contract is in [`docs/spec.yaml`](docs/spec.yaml) (OpenAPI 3.1, version 0.0.7). The running API serves it at `/api-spec` and a Swagger page at `/swagger`.

## Stack

Java 21, Spring Boot 4, PostgreSQL (JDBC repositories), Lombok, JUnit.

## Run

```bash
# 1. create a PostgreSQL database and load the schema
psql "$DB_URL" -f sql/schema.sql

# 2. create a .env file
DB_URL=jdbc:postgresql://localhost:5432/federation
API_KEY=change-me

# 3. start
./mvnw spring-boot:run
```

## Test

```bash
./mvnw test
```
