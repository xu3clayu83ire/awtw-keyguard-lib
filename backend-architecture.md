# KeyGuard 後端架構文件

## 1. 專案概述

KeyGuard 是一套企業級 API Key 安全管理服務，提供 API Key 的核發、撤銷、SHA-256 雜湊驗證與 IP 白名單保護，以 Spring Boot 3 實作，採用整潔架構（Clean Architecture）分層設計。

### 技術棧

| 項目 | 版本 / 說明 |
|------|-------------|
| JDK | 21（啟用虛擬執行緒） |
| Spring Boot | 3.3.5 |
| Spring Security | Filter Chain（無狀態模式） |
| Spring Data JPA | Hibernate |
| 資料庫（本地）| H2 in-memory（MSSQLServer 相容模式） |
| 資料庫（正式）| Microsoft SQL Server |
| 建置工具 | Maven |
| 容器化 | Docker（多階段建置） |

---

## 2. 分層架構

專案採用四層整潔架構，依賴方向由外向內，內層不依賴外層。

```
┌─────────────────────────────────────────────┐
│            interfaces（接口層）               │
│   REST Controllers / Security Filter         │
├─────────────────────────────────────────────┤
│           application（應用層）               │
│   Use Cases / DTOs                           │
├─────────────────────────────────────────────┤
│             domain（領域層）                  │
│   Domain Model / Domain Service Interface    │
├─────────────────────────────────────────────┤
│          infrastructure（基礎設施層）          │
│   JPA Entity / Repository Adapter / Config   │
└─────────────────────────────────────────────┘
```

### 層級說明

| 層級 | 套件 | 職責 |
|------|------|------|
| **接口層** | `interfaces.filter`、`interfaces.rest` | 接收 HTTP 請求、執行 API Key 驗證、回傳 HTTP 回應 |
| **應用層** | `application.usecase`、`application.dto` | 協調領域物件完成業務流程，定義輸入／輸出資料格式 |
| **領域層** | `domain.model`、`domain.service` | 核心業務規則與領域物件，完全不依賴框架 |
| **基礎設施層** | `infrastructure.config`、`infrastructure.repository` | 資料庫操作、Spring Security 設定、屬性設定 |

---

## 3. 套件結構

```
com.keyguard
├── KeyguardApplication.java              # 應用程式入口
│
├── domain/
│   ├── model/
│   │   ├── ApiKey.java                   # 領域模型（Java Record，不可變）
│   │   └── KeyStatus.java                # 金鑰狀態列舉（ACTIVE / REVOKED）
│   └── service/
│       ├── ApiKeyCryptographyService.java # 加密服務（金鑰生成 / SHA-256 雜湊）
│       └── ApiKeyRecordStore.java         # 持久化操作介面（Port）
│
├── application/
│   ├── dto/
│   │   ├── ApiKeyResponse.java           # 核發回應 DTO
│   │   ├── CreateApiKeyRequest.java      # 核發請求 DTO
│   │   ├── RevokeApiKeyRequest.java      # 撤銷請求 DTO
│   │   └── RevokeApiKeyResponse.java     # 撤銷回應 DTO
│   └── usecase/
│       ├── ApiKeyAuthenticationService.java # 驗證 Use Case
│       ├── KeyIssuanceService.java           # 核發 Use Case
│       └── RevokeApiKeyService.java          # 撤銷 Use Case
│
├── infrastructure/
│   ├── config/
│   │   ├── BeanConfig.java               # 通用 Bean 設定（Clock）
│   │   ├── KeyGuardProperties.java       # 自訂屬性設定（@ConfigurationProperties）
│   │   └── SecurityConfig.java           # Spring Security 設定
│   └── repository/
│       ├── ApiKeyEntity.java             # JPA 實體
│       ├── ApiKeyRepositoryAdapter.java  # ApiKeyRecordStore 實作（Adapter）
│       └── SpringDataApiKeyRepository.java # Spring Data JPA Repository
│
└── interfaces/
    ├── filter/
    │   └── ApiKeyAuthenticationFilter.java # API Key 驗證過濾器
    └── rest/
        ├── KeyManagementController.java    # 金鑰管理 REST 控制器
        ├── RestExceptionHandler.java       # 全域例外處理器
        └── TodoController.java             # 受保護路徑示範控制器
```

---

## 4. 核心業務流程

### 4.1 核發 API Key

```
POST /api/v1/keys
       │
       ▼
KeyManagementController.create()
       │  @Valid 驗證請求格式
       ▼
KeyIssuanceService.generate()
       ├─ 解析 prefix（請求提供 or 系統預設）
       ├─ ApiKeyCryptographyService.generateRawKey()  → 生成 64 位元隨機原始金鑰
       ├─ ApiKeyCryptographyService.hash()            → SHA-256 雜湊
       └─ ApiKeyRecordStore.issue()                   → 儲存至資料庫（僅儲存雜湊）
       │
       ▼
回傳 ApiKeyResponse（含 rawKey，僅此一次）
```

### 4.2 撤銷 API Key

```
PATCH /api/v1/keys/{id}/revoke
       │
       ▼
KeyManagementController.revoke()
       │
       ▼
RevokeApiKeyService.revoke()
       └─ ApiKeyRecordStore.revoke()  → 將狀態改為 REVOKED
       │
       ▼
回傳 RevokeApiKeyResponse
```

### 4.3 API Key 驗證流程（每次受保護請求）

```
HTTP 請求（受保護路徑）
       │
       ▼
ApiKeyAuthenticationFilter（OncePerRequestFilter）
       ├─ shouldNotFilter()  → 非保護路徑則跳過
       ├─ 從 Header 讀取 X-API-KEY
       ├─ 擷取請求 IP（X-Forwarded-For or RemoteAddr）
       └─ ApiKeyAuthenticationService.authenticate()
               ├─ SHA-256 雜湊原始金鑰
               ├─ 查詢資料庫（findActiveByHash）
               └─ 驗證 IP 白名單（ApiKey.isAccessibleFrom）
       │
       ├─ 驗證失敗 → 回傳 401 JSON
       └─ 驗證成功 → 帶入 apiKeyId、vendorName 至 Request Attribute → 放行
```

---

## 5. 安全設計

### 5.1 金鑰儲存策略

- 資料庫**只儲存 SHA-256 雜湊後的金鑰**，原始金鑰不落地
- 原始金鑰僅於核發當下回傳一次，之後無法再取得
- 雜湊使用 `SecureRandom` 生成 32 位元組隨機值（產生 64 字元十六進位字串）

### 5.2 IP 白名單

- 每個 API Key 可綁定多個允許 IP（逗號分隔儲存於資料庫）
- 白名單為空時，允許所有 IP 存取
- 驗證時比對 IP 前先正規化（去除首尾空白）

### 5.3 Spring Security 設定

| 項目 | 設定 |
|------|------|
| Session | STATELESS（無狀態） |
| CSRF | 停用 |
| HTTP Basic | 停用 |
| 表單登入 | 停用 |
| H2 Console | 允許同源 iframe |
| 管理路徑 | `/api/v1/keys/**` 不需 API Key（核發／撤銷自身不受保護） |

### 5.4 自訂過濾器插入位置

```
AnonymousAuthenticationFilter（前方）
       ↑
ApiKeyAuthenticationFilter  ← 插入於此
```

---

## 6. 資料模型

### 6.1 資料庫表格：`api_keys`

| 欄位 | 型別 | 說明 |
|------|------|------|
| `id` | BIGINT（自增） | 主鍵 |
| `vendor_name` | NVARCHAR(100) | 廠商名稱 |
| `key_hash` | CHAR(64) | SHA-256 雜湊金鑰（唯一鍵） |
| `allow_ip` | NVARCHAR(500) | IP 白名單，逗號分隔 |
| `status` | VARCHAR(10) | 金鑰狀態（ACTIVE / REVOKED） |
| `created_user` | NVARCHAR(20) | 建立者 |
| `created_at` | TIMESTAMP | 建立時間 |
| `updated_user` | NVARCHAR(20) | 最後更新者 |
| `updated_at` | TIMESTAMP | 最後更新時間 |

### 6.2 領域模型：`ApiKey`（Java Record）

```java
ApiKey(
    Long id,
    String vendorName,
    String keyHash,
    List<String> allowIps,
    KeyStatus status,
    String createdUser,
    LocalDateTime createdAt,
    String updatedUser,
    LocalDateTime updatedAt
)
```

`ApiKeyEntity`（JPA）與 `ApiKey`（Domain）透過 `toDomain()` 方法轉換，確保領域層不依賴 JPA 框架。

---

## 7. API 端點

| 方法 | 路徑 | 說明 | 需要 API Key |
|------|------|------|:---:|
| `POST` | `/api/v1/keys` | 核發 API Key | 否 |
| `PATCH` | `/api/v1/keys/{id}/revoke` | 撤銷 API Key | 否 |
| `GET` | `/api/v1/keys/{loginId}` | 受保護資源範例 | **是** |

### 驗證 Header

```
X-API-KEY: <rawKey>
```

### 錯誤回應格式（RFC 9457 ProblemDetail）

| HTTP 狀態 | 情境 |
|-----------|------|
| `400 Bad Request` | 請求格式驗證失敗 |
| `401 Unauthorized` | API Key 無效、已撤銷或 IP 不在白名單 |
| `404 Not Found` | 指定 ID 的 API Key 不存在 |

---

## 8. 設定屬性

### 環境變數

| 環境變數 | 預設值 | 說明 |
|----------|--------|------|
| `PORT` | `8080` | 服務埠號 |
| `DB_URL` | H2 in-memory | JDBC 連線字串 |
| `DB_USERNAME` | `sa` | 資料庫帳號 |
| `DB_PASSWORD` | （空） | 資料庫密碼 |
| `DB_DRIVER` | `org.h2.Driver` | JDBC Driver |
| `KEYGUARD_PREFIX` | `amsk` | API Key 預設字首 |

### `keyguard.*` 自訂屬性（`KeyGuardProperties`）

| 屬性 | 預設值 | 說明 |
|------|--------|------|
| `keyguard.prefix` | `amsk` | 金鑰字首 |
| `keyguard.protected-path-prefix` | `/api/v1/keys` | 需驗證的路徑前綴 |
| `keyguard.forwarded-for-header` | `X-Forwarded-For` | 取得真實 IP 的 Header |
| `keyguard.api-key-header` | `X-API-KEY` | 金鑰 Header 名稱 |

---

## 9. 容器化

### Dockerfile 多階段建置

```
Stage 1（builder）: maven:3.9.9-eclipse-temurin-21-alpine
  └─ mvn package -DskipTests → 產生 JAR

Stage 2（runtime）: eclipse-temurin:21-jre-alpine
  ├─ 建立非 root 使用者（appuser / appgroup）
  ├─ 複製 JAR
  └─ ENTRYPOINT ["java", "-jar", "app.jar"]
```

**安全考量**：執行階段使用非特權使用者，最小化容器攻擊面。

### 執行指令

```bash
# 建置 image
docker build -t awtw-keyguard:latest .

# 本地執行（H2）
docker run -p 8080:8080 awtw-keyguard:latest

# 正式環境（SQL Server）
docker run -p 8080:8080 \
  -e DB_URL='jdbc:sqlserver://<host>:1433;databaseName=<db>;encrypt=true;trustServerCertificate=true' \
  -e DB_USERNAME='<user>' \
  -e DB_PASSWORD='<password>' \
  -e DB_DRIVER='com.microsoft.sqlserver.jdbc.SQLServerDriver' \
  awtw-keyguard:latest
```

---

## 10. 設計決策

| 決策 | 說明 |
|------|------|
| **Java Record 作為領域模型** | 不可變性確保領域物件的一致性，避免意外修改狀態 |
| **Port & Adapter（Hexagonal）模式** | `ApiKeyRecordStore` 為 Port，`ApiKeyRepositoryAdapter` 為 Adapter，領域層不依賴 JPA |
| **SHA-256 雜湊儲存** | 資料庫洩漏時原始金鑰仍受保護，符合最小敏感資料原則 |
| **SecureRandom 生成金鑰** | 確保加密強度的隨機性，避免可預測的金鑰 |
| **Clock Bean 注入** | 時間取得透過注入而非直接呼叫，方便單元測試中替換固定時間 |
| **OncePerRequestFilter** | 保證同一請求的驗證過濾器只執行一次 |
| **虛擬執行緒** | 啟用 `spring.threads.virtual.enabled=true`，提升 I/O 密集場景下的吞吐量 |
| **ProblemDetail 錯誤格式** | 符合 RFC 9457 標準，提供一致的錯誤回應結構 |
