## ERD 설계

### **5.1 User**

- id (BigInt, PK): 사용자 식별자
- email (Varchar, Unique): 로그인 ID
- password (Varchar): 암호화된 비밀번호
- nickname (Varchar): 사용자 활동명
- role (Enum): USER, ADMIN, COMPANY
- created_at / updated_at: 생성 및 수정 시간

---

### **5.2 Company**

- id (BigInt, PK): 기업 식별자
- user_id (BigInt, FK, Unique): 기업 계정과 연결. 한 유저는 하나의 기업만 등록 가능
- name (Varchar): 기업명
- logo_url (Varchar): 브랜드 이미지
- description (Text): 기업 상세 설명
- business_number (Varchar, Unique): 사업자등록번호
- is_active (Boolean): 서비스 이용 승인 여부. 기업 등록 직후 false, 관리자 승인 후 true
- created_at / updated_at: 생성 및 수정 시간

---

### **5.3 Product**

- id (BigInt, PK): 상품 식별자
- company_id (BigInt, FK): 상품을 등록한 기업. Product는 User가 아니라 Company에 소속
- name (Varchar): 상품명
- description (Text): 상품 설명
- price (Int): 판매 가격
- stock_quantity (Int): 현재 재고 수량
- initial_stock_quantity (Int): 최초 등록 재고 수량
- sale_date (Date, Index): 판매일
- status (Enum): READY, APPROVED, REJECTED, ON_SALE, SOLD_OUT, CLOSED
- created_at / updated_at: 생성 및 수정 시간

권장 제약:

- (company_id, sale_date) Unique: 하나의 기업은 특정 판매일에 상품 1개만 등록 가능
- Product 등록 시에는 DailySaleSlot을 생성하지 않고, 관리자 상품 승인 시 DailySaleSlot을 생성

---

### **5.4 DailySaleSlot**

- id (BigInt, PK): 슬롯 식별자
- sale_date (Date): 판매일
- slot_number (Int): 1~10번 슬롯
- company_id (BigInt, FK): 참여 기업
- product_id (BigInt, FK): 판매 상품
- status (Enum): RESERVED, CANCELLED
- created_at / updated_at: 생성 및 수정 시간

생성 시점:

- Product 등록 시점이 아니라 관리자 상품 승인 시점에 생성
- 승인된 상품만 판매일 슬롯을 점유

---

### **5.5 Order**

- id (BigInt, PK): 주문 식별자
- user_id (BigInt, FK): 주문 유저
- product_id (BigInt, FK): 주문 상품
- sale_date (Date): 판매일
- order_status (Enum): PENDING, PAID, FAILED, EXPIRED, CANCELLED, REFUNDED
- total_price (Int): 서버 기준 주문 금액
- ordered_at (DateTime): 주문 생성 시각
- expired_at (DateTime): 결제 만료 시각
- paid_at (DateTime): 결제 완료 시각

---

### **5.6 Payment**

- id (BigInt, PK): 결제 식별자
- order_id (BigInt, FK): 연결된 주문
- payment_key (Varchar, Unique): PG사 결제 고유 키
- order_id_for_pg (Varchar): PG사에 전달한 주문 번호
- amount (Int): 결제 승인 금액
- payment_status (Enum): READY, APPROVED, FAILED, CANCELLED
- approved_at (DateTime): 결제 승인 시각
- fail_reason (Varchar): 실패 사유
- created_at / updated_at: 생성 및 수정 시간

---

### **5.7 Subscription**

- id (BigInt, PK): 구독 식별자
- user_id (BigInt, FK): 구독한 유저
- company_id (BigInt, FK): 구독 대상 기업
- created_at: 구독 시작일

---

### **5.8 StockHistory**

- id (BigInt, PK): 재고 이력 식별자
- product_id (BigInt, FK): 상품
- order_id (BigInt, FK, Nullable): 관련 주문
- change_quantity (Int): 재고 변화량
- reason (Enum): RESERVED, EXPIRED_RESTORE, PAYMENT_FAILED_RESTORE, CANCEL_RESTORE, ADMIN_ADJUST
- created_at: 생성 시각
