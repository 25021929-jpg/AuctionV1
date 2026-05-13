Dù như ForgotPasswordValidator ngắn và có thể được viết trực tiếp ở controller nhưng ta vẫn viiết riêng ra một class vì nguyên lý SRP và controller không cần phải biết về cách validate.
Controller thay đổi khi:  UI flow thay đổi
Validator thay đổi khi:   Quy tắc validate thay đổi

→ Độc lập hoàn toàn

Lợi ích thực tế thấy ngay
Muốn thêm rule "email không được dùng .ru domain":
    → Chỉ mở ForgotPasswordValidator
    → Thêm rule mới
    → Controller không biết gì — không cần sửa

Muốn đổi animation chuyển bước:
    → Chỉ mở Controller
    → Sửa goTo()
    → ForgotPasswordValidator không biết gì — không cần sửa