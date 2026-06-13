# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
./gradlew build          # Full build with tests
./gradlew bootJar        # Build Spring Boot JAR (no tests)
./gradlew test           # Run all tests
./gradlew test --tests "koza.licensemanagementservice.domain.qna.QnaServiceTest"  # Single test class
./gradlew test --tests "*.QnaServiceTest.methodName"  # Single test method
```

- Java 17, Spring Boot 3.4.2, Gradle
- QueryDSL 5.0.0 (jakarta), Testcontainers 1.20.4 (MySQL), jjwt 0.12.5, BouncyCastle 1.78.1 (HKDF), springdoc-openapi 2.7.0
- No linting or formatting tools configured
- Tests use Testcontainers MySQL 8.0 (Docker must be running; `TC_REUSABLE=true`, Hibernate `ddl-auto: create` for the test schema). Redis is hardcoded to `localhost:6379` in test config (`src/test/resources/application-tc.yaml`) — start Redis locally before running tests
- Production JPA DDL-auto is `none` — schema changes are manual (no Flyway/Liquibase)
- CI (`ci.yml`) skips tests (`-x test`); `deploy.yml` builds an ARM64 image to ghcr.io on push to `main`. Run tests locally before pushing

## Architecture

Backend-only REST API service for software license management with SDK verification.

### Key Packages (`src/main/java/koza/licensemanagementservice/`)

- **domain/** — Core business logic, organized by bounded context:
  - `license/` — License CRUD, key generation, status management, ban scheduling
  - `software/` — Software registration, key generation
  - `member/` — User management, withdrawal scheduling
  - `session/` — Active software session tracking
  - `qna/`, `faq/` — Support system with priority/event-driven notifications
  - `billing/`, `payment/`, `paymentmethod/` — Toss Payments integration
  - `plan/`, `subscription/` — Subscription plans and lifecycle management
  - `audit/` — Admin audit logging
- **sdk/** — Client SDK verification endpoints (base path `/api/sdk`). Crypto refactored in `35344b6` to **X25519 → HKDF-SHA256 → AES-256-GCM** (see `sdk/security/`: `ECDHExchange`, `HKDFUtil`, `AESEncryption`, `Ed25519Signer`). The canonical protocol spec lives in `sdk-guide/` (root `SDK_SPEC.md` is **deprecated**):
  - `/init` — version + file-hash check, Ed25519 signature over response; stateless, no session
  - `/verify` — per-request server X25519 keypair; HKDF derives two directional keys (`keyC2S`, `keyS2C`, salt = clientPubKey‖serverPubKey); AES-256-GCM-encrypts response; Ed25519-signs it; creates Redis session (replaces any existing session for the license — single active session per license)
  - `/hb` (heartbeat) — decrypts C2S payload, extends session TTL (60s), increments server sequence
  - `/release`, `/lv` (changeLocalVariables) — verify C2S encrypted payload before acting
  - Replay protection is **sequence-number-derived nonces** (12-byte IV: 4 zero bytes + 8-byte seq), not key rotation. Session state (both keys, seq counter, reverse `license:{id}→sessionId` lookup, `trigger:` TTL key) lives in Redis
- **auth/** — JWT + OAuth2 (Google, GitHub, Naver) authentication
- **dashboard/**, **stat/** — Analytics and dashboard aggregation
- **global/config/** — SecurityConfig, RedisConfig, QueryDslConfig, SwaggerConfig
- **global/error/** — BusinessException with centralized handling

### Patterns

- Controllers return `ApiResponse<T>` wrapper (`global/common/ApiResponse.java`)
- Base entity with JPA auditing (`global/common/BaseEntity.java`)
- Pagination via `PageResponse` wrapper
- Admin endpoints use separate controller/service classes (e.g., `AdminLicenseController`)
- QueryDSL for dynamic queries (custom repository implementations)
- Async event publishing for cross-domain communication (e.g., `QnaAnsweredEvent`)
- Scheduled tasks (`@Scheduled`):
  - `SessionScheduler` (every 10 min) — ghost-session cleanup: releases sessions inactive >120s (TTL 60s + 60s grace) with `ReleaseType.SYSTEM_ERROR`
  - `LicenseScheduler` — expire licenses (every 5 min) + process status/ban expirations (every min)
  - `SoftwareScheduler` (every min) — software ban/maintenance expiry
  - `MemberWithdrawScheduler` (daily 4 AM) — anonymize members past withdrawal grace period
  - `SubscriptionScheduler` — renewal (daily 9 AM) + expiration (daily midnight)

## Infrastructure

- **Docker Compose**: Caddy (reverse proxy + SSL) + App + Redis
- **Deployment**: Terraform in `deploy/terraform/`, scripts in `deploy/scripts/`
- **Environment variables**: DB_HOST, DB_PORT, DB_USERNAME, DB_PASSWORD, JWT_SECRET_KEY, ED25519_PRIVATE_KEY, ENCRYPT_SECRET_KEY, TOSS_SECRET_KEY, OAuth credentials (see `deploy/.env.example`)
