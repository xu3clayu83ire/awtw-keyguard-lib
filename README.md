# KeyGuard

企業級 API Key 安全管理服務，提供 API Key 核發、撤銷、SHA-256 雜湊驗證與 IP 白名單保護。

## 技術棧

| 項目 | 版本 |
|------|------|
| JDK | 21 |
| Spring Boot | 3.3.5 |
| Spring Security | Filter Chain |
| Spring Data JPA | - |
| 資料庫（本地）| H2 in-memory |
| 資料庫（正式）| SQL Server |

## 環境需求

本地執行需安裝 JDK 21（需含 `javac`，僅 JRE 無法編譯）。

**macOS 設定：**

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

**Windows 設定：**

```powershell
$env:JAVA_HOME = "C:\path\to\jdk-21"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
java -version
```

## 本地執行

```bash
mvn spring-boot:run
```

預設使用 H2 in-memory 資料庫，無需任何額外設定，服務啟動於 `http://localhost:8080`。

## Docker 執行

```bash
# 建置 image
docker build -t keyguard .

# 執行（使用 H2）
docker run -p 8080:8080 keyguard

# 執行（使用 SQL Server）
docker run -p 8080:8080 \
  -e DB_URL='jdbc:sqlserver://<host>:1433;databaseName=<db>;encrypt=true;trustServerCertificate=true' \
  -e DB_USERNAME='<user>' \
  -e DB_PASSWORD='<password>' \
  -e DB_DRIVER='com.microsoft.sqlserver.jdbc.SQLServerDriver' \
  keyguard
```

## 環境變數

| 變數 | 預設值 | 說明 |
|------|--------|------|
| `PORT` | `8080` | 服務埠號 |
| `DB_URL` | H2 in-memory | JDBC 連線字串 |
| `DB_USERNAME` | `sa` | 資料庫帳號 |
| `DB_PASSWORD` | （空）| 資料庫密碼 |
| `DB_DRIVER` | `org.h2.Driver` | JDBC Driver |
| `KEYGUARD_PREFIX` | `amsk` | API Key 前綴 |

## API

### 核發 API Key

```
POST /api/v1/keys
```

Request：

```json
{
  "vendorName": "Vendor A",
  "allowIps": ["127.0.0.1", "10.0.0.1"],
  "createdUser": "systemAdmin",
  "prefix": "amsk"
}
```

Response：

```json
{
  "id": 1,
  "vendorName": "Vendor A",
  "rawKey": "amsk-xxxxxxxxxxxxxxxx",
  "keyHash": "sha256-hash",
  "allowIps": ["127.0.0.1", "10.0.0.1"],
  "status": "ACTIVE",
  "createdUser": "systemAdmin",
  "createdAt": "2026-01-01T00:00:00"
}
```

> **注意**：`rawKey` 僅於核發當下回傳一次，請妥善保存。

### 撤銷 API Key

```
PATCH /api/v1/keys/{id}/revoke
```

Request：

```json
{
  "updatedUser": "systemAdmin"
}
```

### 受保護 API（需驗證）

```
GET /api/v1/keys/{loginId}
```

Header：

```
X-API-KEY: <rawKey>
```

驗證失敗時回傳：

```json
{
  "message": "API key invalid, revoked, or request IP is not allowed"
}
```

## CI/CD

專案已整合 GitHub Actions（[.github/workflows/build.yml](.github/workflows/build.yml)）：

- 觸發分支：`main`、`develop`、`release/**`（支援手動觸發）
- 執行項目：compile → test
- PR 失敗時自動留言；Push 失敗時自動建立 Issue

## H2 Console（本地除錯）

瀏覽器開啟 `http://localhost:8080/h2-console`，JDBC URL 填入：

```
jdbc:h2:mem:keyguard
```
H2 Console 預設禁止遠端連線，從 Docker 外部存取會被擋。