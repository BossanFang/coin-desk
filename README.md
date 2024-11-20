
# coindesk Data Integration

## 專案簡介

本專案採用 **Spring Boot** 框架開發，並使用 **H2 資料庫** 實現以下功能：

1. 呼叫 coindesk API，解析並轉換其內容。
2. 提供幣別對應表的 CRUD 操作 API。
3. 實現自定義的 API，整合 coindesk 資料與幣別中文名稱。

---

## 環境需求

- **Java**：OpenJDK 8
- **Maven**：3.8.0 或以上版本
- **資料庫**：內嵌 H2 Database

---

## 專案功能

### 1. 幣別對應表管理 API
提供幣別對應表的 CRUD 功能：
- **查詢所有幣別**：`GET /api/currency`
- **新增幣別**：`POST /api/currency`
- **查詢幣別**：`GET /api/currency/{id}`
- **更新幣別**：`PUT /api/currency/{id}`
- **刪除幣別**：`DELETE /api/currency/{id}`

### 2. 呼叫 coindesk API
- **呼叫 coindesk API**：`GET /api/coindesk`
  - 呼叫並顯示 coindesk 提供的原始資料。

### 3. 自定義 API
- **轉換 coindesk 資料**：`GET /api/coindesk/convert`
  - 整合 coindesk 資料與幣別對應表，回傳以下內容：
    - 更新時間（格式：`YYYY/MM/DD HH:mm:ss`）
    - 幣別資訊（幣別代碼、中文名稱、匯率）

---

## 資料表結構

資料表名稱：`currency`

### 建立 SQL
```sql
CREATE TABLE currency (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL
);
```

### 範例資料
| ID  | 幣別代碼 | 中文名稱  |
|-----|----------|-----------|
| 1   | USD      | 美元      |
| 2   | GBP      | 英鎊      |
| 3   | EUR      | 歐元      |

---

## 專案結構

```
src/
├── main/
│   ├── java/
│   │   └── com/sunway/app/
│   │       ├── controller/
│   │       ├── service/
│   │       ├── model/
│   │       ├── repository/
│   │       ├── exception/
│   │       └── App.java
│   └── resources/
│       ├── application.properties
│       └── schema.sql 
├── test/
│   └── java/
│       └── com/sunway/app/
│           ├── controller/
│           ├── service/
│   │       └── AppTest.java
└── pom.xml
```

---

## 安裝與執行

1. **Clone 專案**
   ```bash
   git clone https://github.com/BossanFang/coindesk-data-integration.git
   cd coindesk-data-integration
   ```

2. **安裝依賴**
   ```bash
   mvn clean install
   ```

3. **啟動專案**
   ```bash
   mvn spring-boot:run
   ```

---

## 單元測試

使用 JUnit 和 Spring Boot Test 實現以下單元測試：

1. 測試查詢幣別對應表資料 API。
2. 測試新增幣別對應表資料 API。
3. 測試更新幣別對應表資料 API。
4. 測試刪除幣別對應表資料 API。
5. 測試呼叫 Coindesk API。
6. 測試資料轉換 API。

執行單元測試：
```bash
mvn test
```

---

## 注意事項

- **資料庫重置**：專案啟動時會自動初始化資料表與範例資料。
- **H2 Console**：
  - URL: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:testdb`
  - 使用者名稱：`sa`
  - 密碼：`<空白>`

---
