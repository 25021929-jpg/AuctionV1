Static Factory Method — hàm static trả về instance của class, thay thế hoặc bổ sung cho constructor.
Lợi ích 1 — Có tên, nói rõ ý định
Lợi ích 2 — Che giấu implementation
Lợi ích 3 — Kiểm soát dependency
// Tất cả dependency được tạo và quản lý tại đây
// Chỉ sửa 1 chỗ duy nhất trong Factory
// Không đụng Controller, không đụng RegisterValidator
Lợi ích 4 — Có thể trả về subtype
Lợi ích 5 — Tái dùng instance nếu cần
Lý do 1 — FXMLLoader yêu cầu constructor không tham số
    Controller không thể nhận dependency qua constructor
    → Factory tạo dependency thay cho Controller

Lý do 2 — Tập trung logic khởi tạo
    Không có Factory:
        RegisterController tự tạo RegisterValidator
        LoginController    tự tạo LoginValidator
        BidController      tự tạo PlaceBidValidator
        → Logic khởi tạo rải rác khắp nơi

    Có Factory:
        Tất cả logic khởi tạo ở 1 chỗ duy nhất
        → Muốn đổi implementation sửa 1 chỗ

Lý do 3 — Controller không cần biết về dependency
    Không có Factory:
        Controller biết cần UniqueUsernameRule
        Controller biết cần new RegisterValidator(rule)
        → Controller biết quá nhiều

    Có Factory:
        Controller chỉ gọi ValidatorFactory.createRegisterValidator()
        → Controller không biết gì về dependency bên trong

Nó cũng thỏa mãn các nguyên lý như:
SRP      Factory tạo object — Controller xử lý UI
OCP      Đổi implementation chỉ sửa Factory
DIP      Controller phụ thuộc interface, không phụ thuộc class
DRY      Logic khởi tạo tập trung 1 chỗ
Information Hiding  Controller không biết dependency bên trong
Single Point of Change Thay đổi implementation sửa 1 chỗ