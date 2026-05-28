# Auction Client (JavaFX)

Client-side module for the Online Auction System (LTNC 2026).

## Requirements
- JDK 17+ (recommend JDK 21)
- Maven 3.8+
- JavaFX is managed via Maven dependencies (no manual SDK setup needed).

## Run (IntelliJ)
1. Open the root project `AuctionV1_login` in IntelliJ.
2. Reload Maven projects.
3. Run `com.auction.client.MainClient`.

## Run (CLI)
From the repo root:

```bash
mvn -pl Auction-client -am clean test
mvn -pl Auction-client -am javafx:run
```

## Notes (Scope)
- The project focuses on required features: login/register, auction list/detail, live bidding (realtime via socket events), seller CRUD.
- The "Forgot password" screen exists in codebase but is **not linked from UI** to keep scope aligned with assignment requirements.

## Socket contract
See: `Auction-client/docs/CLIENT_SERVER_CONTRACT_FINAL.md`
