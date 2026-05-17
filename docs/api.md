## MVP API 문서

### **7.1 공통 응답 규칙**

공통 에러 응답

```json
{
  "code": "OUT_OF_STOCK",
  "message": "상품 재고가 없습니다.",
  "status": 409
}
```

공통 HTTP 상태

```json
400 Bad Request      // 요청 값 오류
401 Unauthorized     // 로그인 필요
403 Forbidden        // 권한 없음
404 Not Found        // 리소스 없음
409 Conflict         // 정책 충돌, 재고 부족, 중복 구매, 슬롯 마감
500 Server Error     // 서버 오류
```

---

### 7.2 Auth API

### **회원가입**

`POST /api/auth/signup`

Request

```json
{
  "email": "user@example.com",
  "password": "password123!",
  "nickname": "whiteuser"
}
```

Response `201`

```json
{
  "userId": 1,
  "email": "user@example.com",
  "nickname": "whiteuser"
}
```

### **로그인**

`POST /api/auth/login`

Request

```json
{
  "email": "user@example.com",
  "password": "password123!"
}
```

Response `200`

```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "jwt-refresh-token"
}
```

---

### **7.3 Company API**

### **기업 등록**

`POST /api/companies`

권한: 로그인 유저

정책:
- 기업 등록 성공 시 `User.role`은 `COMPANY`로 변경된다.
- 기업 등록 직후 `isActive`는 `false`이며, 관리자 승인 전까지 상품 등록은 불가하다.

Request

```json
{
  "name": "White Brand",
  "logoUrl": "https://example.com/logo.png",
  "description": "브랜드 소개",
  "businessNumber": "123-45-67890"
}
```

Response `201`

```json
{
  "companyId": 1,
  "name": "White Brand",
  "isActive": false
}
```

### **내 기업 정보 조회**

`GET /api/companies/me`

Response 200

```json
{
  "companyId": 1,
  "name": "White Brand",
  "logoUrl": "https://example.com/logo.png",
  "description": "브랜드 소개",
  "isActive": true
}
```

---

### **7.4 Product API**

### **상품 등록**

`POST /api/company/products`

권한: COMPANY

정책:
- 현재 로그인 유저와 연결된 기업 기준으로 상품을 등록한다.
- 연결된 기업의 `isActive`가 `true`일 때만 상품 등록이 가능하다.
- 상품 등록 직후 상태는 `READY`다.
- 상품 등록 시에는 `DailySaleSlot`을 확보하지 않는다.
- `DailySaleSlot`은 관리자 상품 승인 시 확보한다.
- 상품 재고는 1~3개만 허용한다.
- 상품은 User가 아니라 Company에 연결된다.

Request

```json
{
  "name": "Limited White Hoodie",
  "description": "한정 수량 후드",
  "imageUrl": "https://example.com/products/hoodie.jpg",
  "price": 59000,
  "stockQuantity": 3,
  "saleDate": "2026-05-10"
}
```

Response `201`

```json
{
  "productId": 1,
  "saleDate": "2026-05-10",
  "status": "READY"
}
```

### **기업 상품 목록 조회**

`GET /api/company/products`

권한: COMPANY

정책:
- 현재 로그인 유저와 연결된 기업의 상품 목록을 조회한다.
- 판매일 내림차순, 상품 ID 내림차순으로 정렬한다.

Response `200`

```json
{
  "products": [
    {
      "productId": 1,
      "companyId": 1,
      "companyName": "White Brand",
      "name": "Limited White Hoodie",
      "description": "한정 수량 후드",
      "imageUrl": "https://example.com/products/hoodie.jpg",
      "price": 59000,
      "stockQuantity": 3,
      "saleDate": "2026-05-10",
      "status": "READY"
    }
  ]
}
```

### **오늘의 상품 목록 조회**

`GET /api/products/today`

Response `200`

```json
{
  "saleDate": "2026-05-05",
  "products": [
    {
      "productId": 1,
      "companyName": "White Brand",
      "name": "Limited White Hoodie",
      "imageUrl": "https://example.com/products/hoodie.jpg",
      "price": 59000,
      "stockQuantity": 3,
      "status": "ON_SALE"
    }
  ]
}
```

### **특정 날짜 상품 목록 조회**

`GET /api/products?saleDate=2026-05-10`

Response `200`

```json
{
  "saleDate": "2026-05-10",
  "products": [
    {
      "productId": 1,
      "companyName": "White Brand",
      "name": "Limited White Hoodie",
      "imageUrl": "https://example.com/products/hoodie.jpg",
      "price": 59000,
      "stockQuantity": 3,
      "status": "APPROVED"
    }
  ]
}
```

### **상품 상세 조회**

`GET /api/products/{productId}`

Response `200`

```json
{
  "productId": 1,
  "companyId": 1,
  "companyName": "White Brand",
  "name": "Limited White Hoodie",
  "description": "한정 수량 후드",
  "imageUrl": "https://example.com/products/hoodie.jpg",
  "price": 59000,
  "stockQuantity": 3,
  "saleDate": "2026-05-10",
  "status": "ON_SALE"
}
```

---

### **7.5 Order API**

### **주문 생성 / 재고 선점**

`POST /api/orders`

권한: USER

정책:
- 판매 중(`ON_SALE`)인 상품만 주문할 수 있다.
- 주문 생성 시 재고를 1개 선점하고 주문 상태는 `PENDING`이 된다.
- 같은 판매일에 `PENDING` 또는 `PAID` 주문이 있으면 추가 주문할 수 없다.
- `FAILED`, `EXPIRED`, `CANCELLED` 주문은 당일 구매 제한에 포함하지 않는다.

Request

```json
{
  "productId": 1
}
```

Response `201`

```json
{
  "orderId": 100,
  "productId": 1,
  "orderStatus": "PENDING",
  "totalPrice": 59000,
  "orderedAt": "2026-05-05T00:00:01.123",
  "expiredAt": "2026-05-05T00:10:01.123"
}
```

### **내 주문 목록 조회**

`GET /api/orders/me`

권한: USER

Response `200`

```json
{
  "orders": [
    {
      "orderId": 100,
      "productName": "Limited White Hoodie",
      "totalPrice": 59000,
      "orderStatus": "PAID",
      "orderedAt": "2026-05-05T00:00:01.123"
    }
  ]
}
```

### **주문 상세 조회**

`GET /api/orders/{orderId}`

권한: 주문자 또는 ADMIN

Response `200`

```json
{
  "orderId": 100,
  "productId": 1,
  "productName": "Limited White Hoodie",
  "orderStatus": "PENDING",
  "totalPrice": 59000,
  "orderedAt": "2026-05-05T00:00:01.123",
  "expiredAt": "2026-05-05T00:10:01.123",
  "paidAt": null
}
```

### **주문 취소**

`POST /api/orders/{orderId}/cancel`

권한: 주문자

Response `200`

```json
{
  "orderId": 100,
  "orderStatus": "CANCELLED"
}
```

---

### **7.6 Payment API**

### **결제 승인**

`POST /api/payments/confirm`

권한: USER

정책:
- 주문자 본인의 `PENDING` 주문만 승인할 수 있다.
- 주문의 `expiredAt`이 지난 경우 승인할 수 없다.
- 서버에 저장된 주문 금액과 요청 금액이 일치해야 한다.
- Toss Payments 승인 성공 후 `Payment`는 `APPROVED`, `Order`는 `PAID`가 된다.

Request

```json
{
  "orderId": 100,
  "paymentKey": "tgen_20260505_xxx",
  "orderIdForPg": "ORDER_100_20260505",
  "amount": 59000
}
```

Response `200`

```json
{
  "orderId": 100,
  "paymentKey": "tgen_20260505_xxx",
  "paymentStatus": "APPROVED",
  "orderStatus": "PAID",
  "approvedAt": "2026-05-05T00:03:21.456"
}
```

### **결제 실패 처리**

`POST /api/payments/fail`

권한: USER

정책:
- 주문자 본인의 `PENDING` 주문만 실패 처리할 수 있다.
- 실패 처리 시 `Order`는 `FAILED`가 되고 선점한 상품 재고를 복구한다.
- 이미 Payment가 저장된 주문이면 중복 Payment를 저장하지 않는다.

Request

```json
{
  "orderId": 100,
  "paymentKey": "tgen_20260505_xxx",
  "orderIdForPg": "ORDER_100_20260505",
  "reason": "USER_CANCELLED"
}
```

Response `200`

```json
{
  "orderId": 100,
  "orderStatus": "FAILED"
}
```

---

### **7.7 Admin API**

### **기업 승인**

`PATCH /api/admin/companies/{companyId}/approve`

권한: ADMIN

Response `200`

```json
{
  "companyId": 1,
  "name": "White Brand",
  "isActive": true
}
```

### **상품 승인**

`PATCH /api/admin/products/{productId}/approve`

권한: ADMIN

정책:
- `READY` 상태의 상품만 승인할 수 있다.
- 승인 시 해당 상품의 판매일에 `DailySaleSlot`을 확보한다.
- 해당 판매일의 슬롯이 10개 모두 찼으면 승인할 수 없다.
- 같은 기업이 같은 판매일에 이미 승인된 상품이 있으면 승인할 수 없다.

Response `200`

```json
{
  "productId": 1,
  "status": "APPROVED"
}
```

### **상품 반려**

`PATCH /api/admin/products/{productId}/reject`

권한: ADMIN

Request

```json
{
  "reason": "상품 정보가 부족합니다."
}
```

Response `200`

```json
{
  "productId": 1,
  "status": "REJECTED"
}
```

### **판매 상품 오픈 처리**

`PATCH /api/admin/sales/{saleDate}/open`

권한: ADMIN 또는 Scheduler

Response `200`

```json
{
  "saleDate": "2026-05-05",
  "openedProductCount": 10
}
```

---

## **8. Scheduler / Batch**

### **8.1 판매 오픈 배치**

```json
매일 00:00 실행
APPROVED 상태의 오늘 상품을 ON_SALE로 변경
```

### **8.2 주문 만료 배치**

```json
1분마다 실행
expired_at < now 이고 order_status = PENDING인 주문 조회
주문 상태를 EXPIRED로 변경
재고 복구
StockHistory 기록
```

### **8.3 판매 종료 배치**
매일 23:59:59 실행
오늘 판매 상품 중 ON_SALE 상태를 CLOSED로 변경
