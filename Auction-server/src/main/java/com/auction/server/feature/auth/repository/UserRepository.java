package com.auction.server.feature.auth.repository;

import com.auction.server.entity.User;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
  Optional<User> findById(Long id);

  Optional<User> findByIdWithLock(Long id);

  Optional<User> findByEmail(String email);

  Optional<User> findByUsername(String username);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);

  List<User> findByRole(User.Role role, int page, int size);

  User save(User user);

  boolean updateBalance(Long userId, BigDecimal newBalance);

  /**
   * "Sử dụng getReference khi cần tham chiếu đến một thực thể đã tồn tại để thực hiện các thao tác
   * ghi (Insert/Update) mà không có nhu cầu truy xuất thông tin chi tiết của thực thể đó, nhằm tối
   * ưu hóa hiệu suất hệ thống."
   *
   * @param id
   * @return
   */
  User getReference(Long id);
}
