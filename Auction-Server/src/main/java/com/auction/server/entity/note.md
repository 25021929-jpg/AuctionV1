

---
Việc bạn chủ động tách @ToString(exclude = ...) và @EqualsAndHashCode(of = {"id"}) chính là chìa khóa cứu mạng hệ thống khỏi các lỗi treo RAM / sập bộ nhớ sau này.
## 2. Hiểu rõ cách Lombok hoạt động và cách bạn đã sử dụng nó

Lombok không phải là một "phép thuật" chạy khi ứng dụng đang vận hành (Runtime). Bản chất của Lombok là một **Annotation Processor** hoạt động trong quá trình biên dịch (Compile-time).

Khi bạn nhấn nút Build/Run, Lombok sẽ nhảy vào, đọc các Annotation của bạn, tự động sinh ra mã bytecode cho các hàm `get()`, `set()`, `toString()` thẳng vào file `.class` trước khi đưa cho JVM chạy. Nhờ đó, file mã nguồn `.java` của bạn trông cực kỳ sạch sẽ.

Dưới đây là những vũ khí của Lombok bạn đã dùng và cách kiểm soát chúng:

### 2.1. Bộ ba Constructor: `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`

* **`@NoArgsConstructor`:** Tạo ra constructor rỗng `public User() {}`. Hibernate bắt buộc phải có cái này vì khi nó lấy dữ liệu từ DB lên, nó dùng Reflection để gọi constructor rỗng tạo ra một Object "trần chuồng" trước, sau đó mới dùng các hàm `set()` hoặc field để đắp dữ liệu vào.
* **`@AllArgsConstructor`:** Tạo ra constructor có đầy đủ tham số.
* **`@Builder`:** Tạo ra một Design Pattern giúp bạn khởi tạo Object cực kỳ thanh lịch.
```java
User user = User.builder().username("admin").email("admin@gmail.com").build();

```


* **⚠️ Lưu ý tối quan trọng về `@Builder.Default`:** Khi bạn dùng `@Builder`, nếu bạn viết `private List<Bid> bids = new ArrayList<>();`, Lombok khi build Object sẽ **bỏ qua** lệnh gán `= new ArrayList<>()` và mặc định ném vào đó giá trị `null`. Việc bạn thêm `@Builder.Default` sẽ ép Lombok phải giữ nguyên lệnh khởi tạo list rỗng của bạn, tránh được lỗi `NullPointerException` khi bạn vô tình gọi `user.getBids().add(newBid)`.

### 2.2. Tại sao Tuyệt đối KHÔNG ĐƯỢC dùng `@Data` cho Entity?

Rất nhiều bài hướng dẫn trên mạng dạy người mới viết:

```java
@Entity
@Data // <--- SAI LẦM CHÍ MẠCH TRONG DỰ ÁN THỰC TẾ
public class User { ... }

```

Bạn đã rất tỉnh táo khi **không** dùng `@Data`. Bởi vì `@Data` là một combo bọc sẵn bao gồm: `@Getter + @Setter + @ToString + @EqualsAndHashCode + @RequiredArgsConstructor`.

Hai annotation `@ToString` và `@EqualsAndHashCode` dạng mặc định của `@Data` chính là **"sát thủ phần mềm"** khi đi với Hibernate:

1. **`@ToString` mặc định:** Sẽ in ra TẤT CẢ các field. Nếu `User` in `items` (danh sách món hàng), từng `AuctionItem` lại quay sang in thuộc tính `seller` (User). Hai thằng cứ gọi nhau vô tận $\rightarrow$ `StackOverflowError` (Sập Server).
2. **`@EqualsAndHashCode` mặc định:** Sẽ băm (hash) tất cả các thuộc tính. Khi bạn sửa số dư tài khoản `balance` từ 0 thành 100$, giá trị `hashCode()` của Object `User` đó lập tức bị thay đổi. Nếu Object này đang nằm trong một `HashSet` của Hibernate, Hibernate sẽ không thể tìm thấy nó nữa, dẫn đến việc lưu dữ liệu bị sai lệch hoàn toàn.

> **Bài học nằm lòng:** Với JPA Entity, luôn dùng tách rời: `@Getter`, `@Setter`, và cấu hình tường minh `@ToString(exclude = ...)` kèm `@EqualsAndHashCode(of = {"id"})` chính xác như cách bạn đã làm!

---

