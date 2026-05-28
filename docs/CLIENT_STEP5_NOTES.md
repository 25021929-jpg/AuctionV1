# CLIENT_STEP5_NOTES - Error handling mini

## Mục tiêu

Step 5 mini không thêm chức năng mới, chỉ làm lỗi client rõ ràng hơn để chuẩn bị tích hợp server.

## Đã thêm

- `core/error/ErrorHandler.java`
- `core/security/AccessGuard.java`
- Nhóm exception phân loại lỗi client/network/business.

## Nguyên tắc sử dụng

Controller không nên tự phân tích exception sâu. Khi cần hiển thị lỗi:

```java
AlertHelper.showException("Lỗi", throwable);
```

hoặc với Toast:

```java
Toast.show(root, ErrorHandler.getUserMessage(error), Toast.Type.ERROR, 3, null);
```

## Guard quyền

- Màn live bidding yêu cầu login.
- Màn seller yêu cầu `SELLER` hoặc `ADMIN`.
- Màn admin yêu cầu `ADMIN`.

## Lưu ý

Server vẫn chưa được sửa. Khi server được migrate, server nên trả message nghiệp vụ rõ ràng để client hiển thị qua `ApiException`/`ServerBusinessException`.
