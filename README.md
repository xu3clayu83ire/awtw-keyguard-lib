# KeyGuard

KeyGuard 是依照需求文件建立的 Spring Boot JDK 21 專案，提供 API Key 核發、撤銷、SHA-256 雜湊驗證與 IP 白名單保護。

## 技術棧

- JDK 21
- Spring Boot 3
- Spring Web
- Spring Security Filter Chain
- Spring Data JPA
- H2（本地預設）
- SQL Server Driver（正式環境可切換）

## 環境需求

- JDK 21（需含 `javac`，僅 JRE 無法編譯）

macOS 可用下列方式設定：

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
java -version
javac -version
```

若你不想在本機安裝 JDK：

- 專案已提供 GitHub Actions 雲端建置流程：[.github/workflows/build.yml](.github/workflows/build.yml)
- 只要把程式碼 push 到 GitHub，Actions 就會自動使用 JDK 21 執行 compile 與 test
- 你可以在 GitHub 的 Actions 頁面查看建置結果與錯誤明細
- 目前 CI 觸發分支為 main、develop、release/**（也支援手動觸發）
- 失敗通知機制：
  - Pull Request 失敗時，會自動留言在該 PR
- Push 失敗時，會自動建立一張 issue

## API

### 核發 API Key

POST /api/v1/keys

```json
{
  "vendorName": "Vendor A",
  "allowIps": ["127.0.0.1", "10.0.0.1"],
  "createdUser": "systemAdmin",
  "prefix": "nslsk"
}
```

### 撤銷 API Key

PATCH /api/v1/keys/{id}/revoke

```json
{
  "updatedUser": "systemAdmin"
}
```

### 受保護 API

GET /api/v1/todolist/{loginId}

Header:

```text
X-API-KEY: <raw-key>
```

## 執行方式

```bash
./mvnw spring-boot:run
```

若本機沒有 JDK 21，請改走上述 GitHub Actions 雲端建置流程。

編譯專案：

```bash
./mvnw clean compile
```

若要切換 SQL Server，請提供以下環境變數：

```bash
export DB_URL='jdbc:sqlserver://<host>:1433;databaseName=<db>;encrypt=true;trustServerCertificate=true'
export DB_USERNAME='<user>'
export DB_PASSWORD='<password>'
export DB_DRIVER='com.microsoft.sqlserver.jdbc.SQLServerDriver'
```