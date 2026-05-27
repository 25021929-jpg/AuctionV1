# 📋 Next Steps - JSON Parsing Implementation

## Tình Hình Hiện Tại

Refactor hoàn tất ✅ nhưng **JSON parsing còn chưa implement**:

```java
// AuctionController.java (line ~60)
private CreateAuctionRequest parseCreateAuctionRequest(String json) {
    throw new UnsupportedOperationException("JSON parsing not implemented yet");
}

// BidController.java (line ~70)
private PlaceBidRequest parsePlaceBidRequest(String json) {
    throw new UnsupportedOperationException("JSON parsing not implemented yet");
}
```

---

## 🎯 Hướng Dẫn Tiếp Theo

### STEP 1: Thêm Jackson Dependency vào pom.xml

**File:** `Auction-Server/pom.xml`

Thêm vào `<dependencies>`:

```xml
<!-- Jackson for JSON parsing -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
</dependency>
```

---

### STEP 2: Implement JSON Parsing trong AuctionController

**File:** `feature/auction/controller/AuctionController.java`

```java
package com.auction.server.feature.auction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.auction.server.feature.auction.dto.CreateAuctionRequest;
// ... other imports ...

public class AuctionController {
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // ... existing methods ...
    
    /**
     * Helper method để parse CreateAuctionRequest từ JSON string.
     */
    private CreateAuctionRequest parseCreateAuctionRequest(String json) 
            throws Exception {
        return objectMapper.readValue(json, CreateAuctionRequest.class);
    }
}
```

---

### STEP 3: Implement JSON Parsing trong BidController

**File:** `feature/bidding/controller/BidController.java`

```java
package com.auction.server.feature.bidding.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.auction.server.feature.bidding.dto.PlaceBidRequest;
// ... other imports ...

public class BidController {
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // ... existing methods ...
    
    /**
     * Helper method để parse PlaceBidRequest từ JSON string.
     */
    private PlaceBidRequest parsePlaceBidRequest(String json) 
            throws Exception {
        return objectMapper.readValue(json, PlaceBidRequest.class);
    }
}
```

---

### STEP 4: Cập Nhật Exception Handling trong Controllers

**AuctionController:**
```java
public Response<AuctionSession> createAuction(String requestBody) {
    try {
        CreateAuctionRequest request = parseCreateAuctionRequest(requestBody);
        AuctionSession auction = auctionService.createAuction(request);
        return Response.success(auction);
    } catch (AuctionException e) {
        return Response.fail(e.getMessage());
    } catch (Exception e) {  // ← Catch JSON parsing errors
        return Response.fail("Invalid request format: " + e.getMessage());
    }
}
```

**BidController:**
```java
public Response<BidResponse> placeBid(String requestBody) {
    try {
        PlaceBidRequest request = parsePlaceBidRequest(requestBody);
        BidResponse response = bidService.placeBid(request);
        return Response.success(response);
    } catch (BidException e) {
        return Response.fail(e.getMessage());
    } catch (Exception e) {  // ← Catch JSON parsing errors
        return Response.fail("Invalid request format: " + e.getMessage());
    }
}
```

---

### STEP 5: Test JSON Parsing

**Example Request (AUCTION_CREATE):**

```json
{
  "sellerId": 1,
  "categoryId": 5,
  "itemName": "iPhone 15 Pro",
  "description": "Brand new, sealed box",
  "startingPrice": 15000000,
  "startTime": "2026-05-27T14:00:00",
  "endTime": "2026-05-29T14:00:00"
}
```

**Example Request (BID_PLACE):**

```json
{
  "auctionSessionId": 123,
  "bidderId": 456,
  "bidAmount": 16000000
}
```

---

## 📝 DTO Fields for Reference

### CreateAuctionRequest
```java
private int sellerId;              // int (Map to Long in service)
private int categoryId;             // int
private String itemName;            // String
private String description;         // String
private BigDecimal startingPrice;   // BigDecimal
private LocalDateTime startTime;    // LocalDateTime (ISO 8601)
private LocalDateTime endTime;      // LocalDateTime (ISO 8601)
```

### PlaceBidRequest
```java
private int auctionSessionId;  // int
private int bidderId;          // int
private double bidAmount;      // double
```

---

## 🧪 Integration Test Checklist

- [ ] Compile project successfully
- [ ] CreateAuction endpoint works with valid JSON
- [ ] PlaceBid endpoint works with valid JSON
- [ ] Invalid JSON returns proper error message
- [ ] Missing required fields returns validation error
- [ ] Pessimistic lock blocks concurrent bids (manual test)
- [ ] Bid updates AuctionSession correctly

---

## 🔍 Troubleshooting

### Issue: `NoSuchMethodError: ObjectMapper`
**Solution:** Ensure Jackson is in pom.xml and Maven download completed

### Issue: `InvalidFormatException` on LocalDateTime
**Solution:** Ensure dates are in ISO 8601 format: `2026-05-27T14:00:00`

### Issue: `JsonMappingException` on BigDecimal
**Solution:** Use quoted string in JSON: `"startingPrice": 15000000` (NOT "15000000.00")

---

## 📚 Reference Files

- AuctionController: `feature/auction/controller/AuctionController.java`
- BidController: `feature/bidding/controller/BidController.java`
- CreateAuctionRequest: `feature/auction/dto/CreateAuctionRequest.java`
- PlaceBidRequest: `feature/bidding/dto/PlaceBidRequest.java`

---

## ⏱️ Estimated Time

- Jackson dependency: 2 minutes
- JSON parsing implementation: 10 minutes
- Testing and debugging: 15 minutes
- **Total: ~30 minutes**

---

## 🚀 After Completion

1. Build & run project: `mvn clean install`
2. Run JUnit tests to verify
3. Manual API testing with real JSON
4. Then move to integration testing

---

**Status: Ready for JSON Parsing Implementation**
**Estimated Completion: ~30 minutes**

