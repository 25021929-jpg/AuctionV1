Dưới đây là nội dung toàn bộ kiến thức hệ thống đã được biên soạn và chuẩn hóa dưới dạng file Markdown (`.md`). Bạn có thể sao chép toàn bộ nội dung này để lưu vào kho lưu trữ mã nguồn của dự án (ví dụ: đặt tên là `DATABASE_ARCHITECTURE.md`) làm tài liệu hướng dẫn cho bản thân và đội ngũ phát triển.

---

# TÀI LIỆU KIẾN TRÚC TẦNG DATABASE: JDBC THUẦN & HIKARICP

Tài liệu này giải thích chi tiết các khái niệm cốt lõi, cơ chế vận hành ngầm và lý do tại sao hệ thống xử lý kết nối Database được thiết kế như hiện tại.

---

## 1. Định Nghĩa Gốc Rễ: Connection Là Gì?

Một `Connection` (Kết nối cơ sở dữ liệu) là một đường ống giao tiếp hai chiều được thiết lập giữa Java Server và MySQL. Nó được nhìn nhận dưới 3 tầng cấu trúc:

* **Tầng Vật Lý (Hệ điều hành):** Là một **OS Socket TCP** đang hoạt động (kết nối qua cổng 3306 của MySQL). Nó tiêu tốn tài nguyên mạng (Network I/O) và RAM của cả hai máy để duy trì trạng thái kết nối.
* **Tầng Cơ Sở Dữ Liệu (MySQL):** Đại diện cho một **Phiên làm việc (Session)** độc lập. MySQL sẽ cấp phát một Thread riêng để phục vụ Connection này, lưu giữ ngữ cảnh về quyền hạn, biến môi trường và trạng thái Transaction.
* **Tầng Ứng Dụng (Java):** Là một đối tượng thực thi interface `java.sql.Connection`, cung cấp các phương thức điều khiển (`prepareStatement()`, `commit()`, `close()`) để tương tác với Socket mạng bên dưới.

---

### 1.1. Connection Trong Chuẩn JDBC (`java.sql.Connection`)

Theo định nghĩa JDBC, một **Connection** là **phiên làm việc (session)** với **một database cụ thể**. Mọi câu lệnh SQL được thực thi và kết quả trả về đều nằm **trong ngữ cảnh (context)** của Connection đó.

```
[Java Server]  ←—— Connection ——→  [MySQL auction_db]
                      │
                      ├─ Gửi SQL (SELECT, INSERT, UPDATE...)
                      ├─ Nhận kết quả (ResultSet)
                      └─ Quản lý transaction, auto-commit...
```

#### Phân biệt hai khái niệm trong dự án AuctionV1

| Khái niệm | Vai trò |
|-----------|---------|
| **Class `DatabaseConnection`** | Utility Java — đọc `application.properties`, gọi `DriverManager.getConnection(...)` |
| **Object `Connection` (JDBC)** | Phiên thật với MySQL — interface `java.sql.Connection` |

```java
// Utility — tạo phiên
Connection conn = DatabaseConnection.getConnection();

// conn — session JDBC với auction_db
```

Mỗi lần gọi `getConnection()` ≈ yêu cầu MySQL mở **một phiên mới**. Trong repository, pattern chuẩn:

```java
try (Connection conn = DatabaseConnection.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql)) {
    // ...
} // Hết khối → conn.close() → giải phóng session
```

#### Connection dùng để làm gì?

| Việc | API / ý tưởng |
|------|----------------|
| Tạo câu lệnh SQL | `conn.prepareStatement("SELECT ...")` |
| Thực thi SQL | `executeQuery()`, `executeUpdate()` |
| Mô tả database (metadata) | `conn.getMetaData()` — bảng, cột, version DB, khả năng transaction... |
| Cấu hình phiên | `setAutoCommit()`, `setTransactionIsolation()` |
| Transaction thủ công | `commit()`, `rollback()` (khi tắt auto-commit) |

> **Nguyên tắc JDBC:** Khi đã có API trên `Connection`, **không** nên dùng SQL thuần (`SET autocommit=0`, v.v.) để đổi cấu hình phiên — dùng đúng method JDBC.

#### `getMetaData()` — thông tin mô tả, không phải dữ liệu nghiệp vụ

`Connection.getMetaData()` trả về thông tin **mô tả** database: tên bảng, kiểu cột, stored procedure, driver version, isolation level hỗ trợ… Hữu ích cho tool sinh code hoặc kiểm tra schema; ít dùng trong CRUD đơn giản của auth/đấu giá.

#### Auto-commit và Transaction

**Mặc định:** `auto-commit = true` — sau mỗi `INSERT` / `UPDATE` / `DELETE` thành công, MySQL **commit ngay** (lưu vĩnh viễn). Phù hợp một câu lệnh độc lập (đăng ký user, một bid đơn lẻ…).

**Tắt auto-commit:** `conn.setAutoCommit(false)` — nhiều câu SQL trên **cùng một** `Connection` = **một transaction**:

```
INSERT vào users
INSERT vào wallet
→ conn.commit()      // cả hai cùng lưu
→ hoặc conn.rollback()  // hủy cả hai
```

**Lưu ý quan trọng:**

* Cùng một `Connection` → các lệnh **chia sẻ** trạng thái phiên (auto-commit, transaction, isolation).
* `getConnection()` lần mới → **phiên mới**, không tự gộp transaction với connection cũ.
* Nghiệp vụ đấu giá cần atomicity (trừ ví + ghi bid + cập nhật giá) → **một Connection**, tắt auto-commit, `commit`/`rollback` rõ ràng.

#### Type map (UDT) — ít gặp trong MySQL thông thường

JDBC cho phép map **User-Defined Type** (UDT) trên DB sang class Java tùy chỉnh qua `getTypeMap()` / `setTypeMap()`. Ví dụ trong Javadoc:

```java
java.util.Map map = con.getTypeMap();
map.put("mySchemaName.ATHLETES", Class.forName("Athletes"));
con.setTypeMap(map);
```

Với schema bảng + cột chuẩn (users, bids, auction_sessions…) trong dự án Auction, phần này **có thể bỏ qua** khi mới học JDBC.

#### Điều cần nhớ khi vận hành

1. **Luôn đóng** `Connection`, `PreparedStatement`, `ResultSet` — ưu tiên `try-with-resources`.
2. **Connection không sống mãi** — timeout, MySQL restart, lỗi mạng → cần tạo lại hoặc để Connection Pool (HikariCP — xem mục 3) xử lý.
3. Code hiện tại (`DatabaseConnection.getConnection()` mỗi lần gọi) = **connection mới mỗi lần** — đơn giản cho học tập; production nên dùng pool (object vẫn là `Connection`, chỉ **mượn** từ pool).

**Tóm một câu:** `Connection` là cầu nối **phiên làm việc** giữa app Java và một database; mọi SQL và transaction đều gắn với một object `Connection` — trong AuctionV1 được tạo qua `DriverManager` sau khi đọc `application.properties`.

---

### 1.2. Đọc `application.properties`: ClassLoader & `getResourceAsStream`

Trước khi gọi `DriverManager.getConnection(...)`, class `DatabaseConnection` phải đọc file cấu hình. Đoạn code then chốt:

```java
InputStream inputStream = DatabaseConnection.class
        .getClassLoader()
        .getResourceAsStream(PROPERTIES_FILE);  // "application.properties"

properties.load(inputStream);
```

#### File nằm ở đâu?

| Giai đoạn | Vị trí |
|-----------|--------|
| **Source (khi viết code)** | `Auction-Server/src/main/resources/application.properties` |
| **Runtime (khi chạy server)** | `target/classes/application.properties` hoặc bên trong JAR — **gốc classpath** của process server |

JVM **không** đọc trực tiếp thư mục `src/main/resources` trên ổ đĩa khi chạy app đã build. Maven/IDE copy resources vào classpath; ClassLoader tìm file **trên classpath đó**.

Ví dụ nội dung:

```properties
db.url=jdbc:mysql://localhost:3306/auction_db
db.username=root
db.password=...
```

#### Chuỗi gọi — từng bước

```
DatabaseConnection.class
    → getClassLoader()
    → getResourceAsStream("application.properties")
    → InputStream
    → Properties.load()
    → db.url, db.username, db.password
```

##### 1) `DatabaseConnection.class`

* Trả về object `Class<DatabaseConnection>` — metadata của class (đã được JVM load khi `getConnection()` chạy).
* **Không** dùng để “kích hoạt” method `static` — class đã load sẵn vì code đang thực thi trong class đó.
* Chỉ là **điểm neo** để lấy ClassLoader đúng với process đang chạy server.

##### 2) `.getClassLoader()`

* Trả về **ClassLoader đã load class này** — trong app thường là **Application ClassLoader** (classpath: `target/classes` + JAR dependency như `auction-shared`, `mysql-connector-j`…).
* **Không** phải “bất kỳ ClassLoader nào”: phải là loader của **process server** — loader của `Auction-client` (process khác) **không** thấy file chỉ có trên server.

**Maven module vs ClassLoader:**

| Khái niệm | Ý nghĩa |
|-----------|---------|
| Mỗi `pom.xml` module | Đơn vị **build** (artifact riêng) |
| Mỗi lần `java` chạy server | **Một process** → thường **một Application ClassLoader** cho cả stack JAR trên classpath process đó |
| Class bất kỳ trong `Auction-Server` | Cùng loader → `UserRepository.class.getClassLoader()` thường **giống** `DatabaseConnection.class.getClassLoader()` |

##### 3) `.getResourceAsStream(String name)`

* Tìm file **tương đối trên classpath** (dùng `/` như trong JAR).
* Trả về `InputStream` đọc byte, hoặc **`null`** nếu không tìm thấy → code ném `RuntimeException("Cannot find application.properties")`.
* Sau `properties.load(inputStream)` nên **đóng** stream (`try-with-resources`) để tránh leak handle (code hiện tại chưa đóng — có thể cải thiện sau).

#### Classpath — hình dung

```
[JVM — process chạy Auction-Server]
       │
       ▼
 Application ClassLoader
       │
       ├── com/auction/server/database/DatabaseConnection.class
       ├── application.properties          ← getResourceAsStream tìm ở đây
       ├── auction-shared.jar (classes bên trong)
       └── mysql-connector-j.jar
```

#### Quy tắc ủy quyền (parent-first) — liên quan khi đọc resource

Khi tìm resource, ClassLoader cũng **hỏi parent trước**, rồi mới tìm trên classpath của chính nó. File `application.properties` của project **không** nằm trên JDK → Application loader tìm thấy và mở stream.

*(Chi tiết load **class** `.class` qua parent-first — xem mục 2 với `Class.forName()`.)*

#### Cách viết thay thế (cùng mục đích)

```java
// Cách 1 — qua ClassLoader (như DatabaseConnection hiện tại)
DatabaseConnection.class.getClassLoader()
    .getResourceAsStream("application.properties");

// Cách 2 — gọn hơn, vẫn cùng loader của class
DatabaseConnection.class.getResourceAsStream("application.properties");

// Cách 3 — class khác trong cùng module server (thường tương đương)
UserRepository.class.getResourceAsStream("/application.properties");
```

**Không** dùng class/module **client** khi file chỉ nằm trong resources của **server**.

#### Phân biệt: đọc resource vs nạp class

| Hành động | API | Kết quả |
|-----------|-----|---------|
| Đọc file config | `getResourceAsStream("application.properties")` | `InputStream` → `Properties` |
| Nạp bytecode Driver / class Java | ClassLoader load `.class` / `Class.forName(...)` | Class trong RAM để `new` / gọi method |

`DatabaseConnection` chỉ **đọc cấu hình**; việc đăng ký MySQL Driver với `DriverManager` do JDBC SPI / dependency JAR (mục 2).

#### Lưu ý vận hành

1. **Mỗi lần `getConnection()` đều `load()` lại file** — đơn giản nhưng chậm hơn cache `Properties` một lần lúc khởi động.
2. **Mật khẩu trong `application.properties`** — tiện dev; production nên biến môi trường / secret, tránh commit password thật.
3. **Thiếu key** (`db.url` null) → lỗi khó đọc từ `DriverManager` — nên validate sau `getProperty`.
4. Chạy **test** classpath khác process server → có thể `inputStream == null` nếu không copy resources vào test classpath.

**Tóm một câu:** `getClassLoader().getResourceAsStream(...)` mở file config từ **classpath của process server**, không đọc đường dẫn cứng trên ổ đĩa — neo qua `DatabaseConnection.class` để dùng đúng Application ClassLoader đang chạy app.

---


## 2. Vòng Đời Nạp Thư Viện: Dependency $\rightarrow$ Import $\rightarrow$ Class.forName()

Để nạp file Bytecode của Driver MySQL vào RAM (Process), hệ thống trải qua 3 giai đoạn độc lập:

1. **Cài Dependency (Môi trường/Ổ cứng):** Khai báo trong `pom.xml` (Maven) để tải file `.jar` chứa các file `.class` của MySQL về máy.
2. **Lệnh `import` (Trình biên dịch - Compile-time):** Chỉ có giá trị khi viết code để viết tắt đường dẫn lớp. Khi biên dịch xong sang file `.class`, lệnh `import` sẽ bị xóa bỏ hoàn toàn.
3. **`Class.forName()` (Thời gian chạy - Runtime):** * **Cơ chế thông thường:** JVM nạp Class theo kiểu lười biếng (**Lazy Loading**), chỉ khi nào gọi lệnh `new` thì mới bốc file `.class` vào RAM.
* **Bản chất `Class.forName()`:** Chủ động ép buộc **ClassLoader** nhìn vào **ClassPath** (bản đồ đường dẫn file JAR) để tìm kiếm và nạp thủ công file bytecode `com.mysql.cj.jdbc.Driver` vào RAM ngay lập tức (**Eager Loading**).
* **Mục đích:** Khi Driver được nạp vào RAM, khối mã tĩnh `static {}` bên trong nó tự động kích hoạt để đăng ký sự hiện diện của mình với `DriverManager` của Java.



> **Lưu ý kỹ thuật:** Từ JDBC 4.0 (Java 6) trở đi, Java sử dụng cơ chế SPI (Service Provider Interface) để tự động quét file ẩn trong JAR và nạp Driver, giúp lập trình viên không còn phải gõ dòng `Class.forName()` một cách thủ công.

---

## 3. Bản Chất Của HikariProxyConnection (Mẫu Thiết Kế Proxy)

Khi ứng dụng gọi `dataSource.getConnection()` từ HikariCP, đối tượng nhận được không phải là kết nối gốc của MySQL (`ConnectionImpl`), mà là một **`HikariProxyConnection` (Kẻ mạo danh hoàn hảo)**.

Nếu đưa thẳng kết nối gốc cho lập trình viên, Connection Pool sẽ mất hoàn toàn quyền kiểm soát. Do đó, Proxy đứng ra làm trung gian bọc lót với các logic "gác cổng" tinh vi:

* **Chặn đứng lệnh hủy kết nối (`.close()`):** Khi code gọi `conn.close()`, Proxy không ngắt mạng TCP vật lý. Nó lén lút dọn dẹp trạng thái rồi đưa kết nối thật trở lại tủ chứa đồ (Pool) để Thread khác tái sử dụng.
* **Tẩy rửa trạng thái (Sanitization):** Tự động khôi phục `setAutoCommit(true)`, `setReadOnly(false)` và gọi `rollback()` nếu lập trình viên mở Transaction nhưng quên chốt trước khi trả kết nối.
* **Cách ly kết nối nhiễm độc (Eviction):** Nếu phát hiện lỗi sập mạng vật lý (`Fatal Exception`), Proxy sẽ đánh dấu kết nối này đã chết (`EVICTED`), hủy hoàn toàn Socket TCP và yêu cầu Pool bù vào một kết nối mới tinh.
* **Bảo vệ trùng lặp (Double-Close):** Nếu code gọi `close()` 2 lần, Proxy sẽ bỏ qua lần gọi thứ 2 thay vì làm crash ứng dụng.

---

## 4. Cơ Chế Chống Rò Rỉ Bộ Nhớ (Anti-Leak Statements) & Cấu Trúc FastList

Khi tạo ra một `PreparedStatement`, MySQL Server sẽ cấp phát RAM để biên dịch và lưu trữ con trỏ quản lý câu lệnh đó. Nếu lập trình viên quên gọi `pstmt.close()`, qua thời gian MySQL sẽ cạn kiệt bộ nhớ đệm và từ chối mọi truy vấn mới.

### Cách Proxy giải quyết rò rỉ:

* Mỗi khi một `PreparedStatement` được tạo ra qua Proxy, nó âm thầm bị ghi sổ vào một danh sách nội bộ của Connection gọi là **`FastList`**.
* Khi bạn gọi `conn.close()`, trước khi trả kết nối về Pool, Proxy sẽ chủ động duyệt qua `FastList` này và **đóng hộ** tất cả các Statement mà bạn lỡ quên không đóng.

### Chuyện gì xảy ra nếu đóng gói Statement KHÔNG theo cơ chế LIFO (Last In First Out)?

`FastList` là một cấu trúc mảng (Array) tùy chỉnh của HikariCP được viết lại vòng lặp tìm kiếm để tối ưu hóa hiệu năng:

* **Nếu đóng đúng thứ tự LIFO (Chuẩn):** Lệnh đóng Statement sẽ quét `FastList` ngược từ dưới đuôi lên. Nó sẽ tìm thấy Statement cần đóng ngay lập tức ở vị trí cuối cùng $\rightarrow$ Đạt tốc độ tối đa $O(1)$.
* **Nếu đóng sai thứ tự (hoặc đóng lộn xộn):** Vòng lặp phải quét lùi sâu hơn vào trong mảng mới tìm thấy $\rightarrow$ Tốc độ trở về mức bình thường $O(N)$ giống như `ArrayList`.
* **Kết luận:** Hệ thống **không bị lỗi và vẫn hoàn toàn an toàn** nếu đóng sai thứ tự LIFO. Hệ thống chỉ chạy ở mức bình thường thay vì đạt tốc độ tối đa (Nano-giây) mà tác giả đã tối ưu.

---

## 5. Bảo Mật: Tại Sao Statement Bị SQL Injection Nhưng PreparedStatement Thì Không?

Hệ quản trị CSDL (MySQL) xử lý câu lệnh thông qua hai bước độc lập: **Biên dịch cú pháp (Dựng cây AST)** và **Thực thi dữ liệu**.

### Statement thông thường (Bị SQL Injection):

* **Cơ chế:** Buộc phải cộng chuỗi chữ dữ liệu của người dùng trực tiếp vào câu lệnh SQL và gửi xuống cùng một lượt.
* **Rủi ro:** MySQL coi toàn bộ chuỗi nhận được là **MÃ LỆNH**. Nếu Hacker nhập mã độc chứa các ký tự điều khiển (như dấu nháy đơn `'`, lệnh `OR '1'='1'`), cấu trúc của cây cú pháp SQL sẽ bị thay đổi hoàn toàn, làm thay đổi logic thực thi của hệ thống (bỏ qua mật khẩu, xóa bảng dữ liệu...).
* **Thời điểm gửi mạng:** Hàm `conn.createStatement()` chỉ diễn ra trên RAM của Java. Toàn bộ chuỗi SQL thô chỉ được đẩy qua mạng khi gọi `.executeQuery(sql)`.

### PreparedStatement (Chống SQL Injection tuyệt đối):

* **Cơ chế:** Gửi khung câu lệnh chứa các dấu hỏi chấm giữ chỗ (`?`) xuống MySQL trước thông qua hàm `conn.prepareStatement(sql)`.
* **Thời điểm gửi mạng:** **Khung câu lệnh được gửi đi ngay lập tức từ bước này**. MySQL nhận khung lệnh, tiến hành **Biên dịch trước (Pre-compile)** và đóng băng cấu trúc cây cú pháp, chừa sẵn các hốc trống (Placeholder).
* **An toàn tuyệt đối:** Khi gọi `.execute()`, Java không gửi lại câu lệnh nữa mà chỉ gửi dữ liệu thuần nhét vào các hốc trống. Dẫu dữ liệu của Hacker có chứa mã độc, MySQL cũng chỉ coi đó là một chuỗi ký tự chữ thuần túy nằm trong tầm kiểm soát, không cách nào phá vỡ cấu trúc câu lệnh đã đóng băng.