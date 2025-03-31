# Blockchain Electronic Contract Signing System

A distributed electronic contract signing system built on **Hyperledger Fabric**, with a Spring Boot microservices backend.

## Architecture

```
┌─────────────────┐       ┌──────────────────┐       ┌─────────────────────┐
│   Frontend      │──────▶│  AuthService      │       │   FabricGateway     │
│  (Vue/React)    │       │  Port: 8082       │◀─────▶│   Port: 8084        │
└─────────────────┘       │  - User auth      │ Feign │  - Fabric SDK       │
                          │  - File upload    │       │  - Contract CRUD    │
                          │  - PDF hashing    │       │  - Verification     │
                          └──────────────────┘       └─────────────────────┘
                                    │                           │
                                    ▼                           ▼
                              ┌──────────┐             ┌──────────────┐
                              │  MySQL   │             │  Hyperledger │
                              │blockchain│             │    Fabric    │
                              └──────────┘             └──────────────┘
```

## Modules

| Module | Description |
|--------|-------------|
| `fabric-contract/` | Hyperledger Fabric chaincode (Java) |
| `ElectronicSigning/AuthService/` | User authentication & file management (port 8082) |
| `ElectronicSigning/FabricGateway/` | Fabric blockchain gateway service (port 8084) |
| `fabric-samples/` | Fabric test network configuration |

## Prerequisites

- Java 21
- Maven 3.8+
- MySQL 8.0
- Nacos 2.x (service discovery)
- Hyperledger Fabric 2.5 test network

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:mysql://localhost:3306/blockchain` | Database URL |
| `DB_USERNAME` | `root` | Database username |
| `DB_PASSWORD` | *(empty)* | Database password |
| `STORAGE_PATH` | `~/contracts/unsigned/` | Unsigned contracts directory |
| `STORAGE_PATH_SIGNED` | `~/contracts/signed/` | Signed contracts directory |
| `FABRIC_PEER_ENDPOINT` | `localhost:7051` | Fabric peer gRPC endpoint |
| `FABRIC_CHANNEL_NAME` | `mychannel` | Fabric channel name |
| `FABRIC_CHAINCODE_NAME` | `contractsigning` | Chaincode name |

## Quick Start

### 1. Start Hyperledger Fabric Test Network

```bash
cd fabric-samples/test-network
./network.sh up createChannel -c mychannel -ca
./network.sh deployCC -ccn contractsigning -ccp ../../fabric-contract -ccl java
```

### 2. Configure Fabric crypto material

Copy the admin certificate and private key to:
```
ElectronicSigning/FabricGateway/src/main/resources/crypto/cert.pem
ElectronicSigning/FabricGateway/src/main/resources/crypto/private_key.pem
ElectronicSigning/FabricGateway/src/main/resources/crypto/ca.crt
```

### 3. Create MySQL database

```sql
CREATE DATABASE blockchain CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 4. Start Nacos

```bash
# Download and start Nacos 2.x
sh startup.sh -m standalone
```

### 5. Build and run services

```bash
cd ElectronicSigning

# Set credentials
export DB_PASSWORD=your_password

# Build all modules
mvn clean package -DskipTests

# Start AuthService
java -jar AuthService/target/AuthService-*.jar

# Start FabricGateway
java -jar FabricGateway/target/FabricGateway-*.jar
```

## API Endpoints

### AuthService (port 8082)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/auth/register` | Register new user |
| POST | `/auth/login` | User login, returns JWT |
| GET  | `/auth/getId` | Get current user ID |
| POST | `/auth/readcontract` | Upload PDF and get SHA-256 hash |
| GET  | `/file/listunsigned` | List unsigned contracts |
| GET  | `/file/listsigned` | List signed contracts |
| GET  | `/file/download/{filename}` | Download contract PDF |
| POST | `/file/savesginedContract` | Save signed contract |

### FabricGateway (port 8084)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/contracts/create` | Create contract on blockchain |
| GET  | `/api/contracts/read/{id}` | Read contract from blockchain |
| PUT  | `/api/contracts/update` | Update contract |
| DELETE | `/api/contracts/delete/{id}` | Delete contract |
| GET  | `/api/contracts/all` | Get all contracts |
| POST | `/api/contracts/verify` | Verify contract integrity |

## Smart Contract (Chaincode)

Located in `fabric-contract/`, implements:
- `CreateContract(contractID, partyA, partyB, contentHash, signTime)`
- `ReadContract(contractID)`
- `UpdateContract(contractID, partyA, partyB, contentHash, signTime)`
- `DeleteContract(contractID)`
- `ContractExists(contractID)`
- `GetAllContracts()`

## Security

- JWT authentication (RSA-256) on all protected endpoints
- Per-user EC P-256 Fabric identities generated at registration
- SHA-256 content hashing for contract integrity verification
- Contract immutability guaranteed by blockchain ledger

## Author

Liu Bai — Undergraduate Thesis Project
