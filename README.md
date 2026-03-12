# Electronic Contract Signing System

A Java-based electronic contract signing system built with `Spring Boot` and `Hyperledger Fabric`.

This repository is a cleaned-up public snapshot of a course / thesis-style project. The public repo was organized and published later than the original development work, so the GitHub repository creation date reflects the portfolio publishing time rather than the first day the project was developed.

## Project Overview

The system is designed for contract creation, signing, storage, and integrity verification. The off-chain services handle user management, file storage, and business APIs, while contract metadata and hashes are anchored on a Fabric blockchain network.

Core capabilities:

- user registration and login with JWT-based authentication
- PDF upload and content hashing
- contract creation and query APIs
- blockchain-backed contract verification
- separation between business service and Fabric gateway service

## Architecture

```text
Client
  -> AuthService
       - authentication
       - file upload / contract file management
       - PDF hash generation
       - user identity management
  -> FabricGateway
       - contract CRUD on Fabric
       - contract integrity verification
       - blockchain interaction through Fabric SDK

Persistence / infrastructure
  - MySQL
  - Nacos
  - Hyperledger Fabric test network
```

## Repository Structure

```text
ElectronicSigning/
  AuthService/        Spring Boot service for auth, file handling and user-related APIs
  FabricGateway/      Spring Boot service for blockchain interaction
  pom.xml             parent Maven build

fabric-contract/
  Java chaincode for contract records

fabric-samples/
  Local Fabric test-network dependency used during development
```

## Tech Stack

- Java 21
- Spring Boot
- Spring Security
- Spring Cloud OpenFeign
- MySQL
- Nacos
- Hyperledger Fabric 2.5
- Maven

## Main Modules

### AuthService

Responsibilities:

- user registration and login
- JWT issuance and authentication
- contract file upload and download
- unsigned / signed contract file management
- PDF hashing before blockchain submission

Example endpoints:

- `POST /auth/register`
- `POST /auth/login`
- `GET /auth/getId`
- `POST /auth/readcontract`
- `GET /file/listunsigned`
- `GET /file/listsigned`
- `GET /file/download/{filename}`
- `POST /file/savesginedContract`

### FabricGateway

Responsibilities:

- submit contract records to Fabric
- query contract records from chaincode
- verify on-chain hash against uploaded file hash
- provide blockchain-facing CRUD APIs to upper-layer services

Example endpoints:

- `POST /api/contracts/create`
- `GET /api/contracts/read/{id}`
- `PUT /api/contracts/update`
- `DELETE /api/contracts/delete/{id}`
- `GET /api/contracts/all`
- `POST /api/contracts/verify`

### Chaincode

Located in `fabric-contract/`, the chaincode manages contract records on the ledger, including:

- create contract
- read contract
- update contract
- delete contract
- check existence
- list all contracts

## Local Setup

### Prerequisites

- Java 21
- Maven 3.8+
- MySQL 8.0
- Nacos 2.x
- Hyperledger Fabric 2.5 test network

### 1. Start Fabric test network

```bash
cd fabric-samples/test-network
./network.sh up createChannel -c mychannel -ca
./network.sh deployCC -ccn contractsigning -ccp ../../fabric-contract -ccl java
```

### 2. Prepare local secrets and certificates

This repository does not include real private keys or Fabric certificates.

You need to provide:

- `ElectronicSigning/AuthService/src/main/resources/private_key.pem`
- `ElectronicSigning/AuthService/src/main/resources/public_key.pem`
- `ElectronicSigning/FabricGateway/src/main/resources/public_key.pem`
- `ElectronicSigning/FabricGateway/src/main/resources/crypto/cert.pem`
- `ElectronicSigning/FabricGateway/src/main/resources/crypto/private_key.pem`
- `ElectronicSigning/FabricGateway/src/main/resources/crypto/ca.crt`

### 3. Create database

```sql
CREATE DATABASE blockchain CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 4. Configure environment variables

| Variable | Default |
| --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/blockchain?serverTimezone=UTC` |
| `DB_USERNAME` | `root` |
| `DB_PASSWORD` | empty |
| `STORAGE_PATH` | `${user.home}/contracts/unsigned/` |
| `STORAGE_PATH_SIGNED` | `${user.home}/contracts/signed/` |
| `FABRIC_PEER_ENDPOINT` | `localhost:7051` |
| `FABRIC_OVERRIDE_AUTHORITY` | `peer0.org1.example.com` |
| `FABRIC_MSP_ID` | `Org1MSP` |
| `FABRIC_CHANNEL_NAME` | `mychannel` |
| `FABRIC_CHAINCODE_NAME` | `contractsigning` |

### 5. Start dependencies

```bash
sh startup.sh -m standalone
```

### 6. Build and run

```bash
cd ElectronicSigning
mvn clean package -DskipTests

java -jar AuthService/target/AuthService-*.jar
java -jar FabricGateway/target/FabricGateway-*.jar
```

## Notes

- This repository is intended as a portfolio / learning project snapshot, not a production-ready deployment template.
- Some local environment assumptions from the original coursework setup have been preserved.
- The `fabric-samples/` directory is kept here because it was used during local development and integration testing.

## Author

Liu Qiyuan
