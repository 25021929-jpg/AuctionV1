package com.auction.shared.model;

import com.auction.shared.domain.UserRole;
import java.io.Serializable;

/**
 * Shared user model (minimal) to be reused by client/server when needed.
 *
 * <p>NOTE: We keep it minimal to avoid forcing a schema; extend only when the server model is
 * finalized.
 */
public class User implements Serializable {
  private long id;
  private String username;
  private String email;
  private UserRole role;

  public User() {}

  public User(long id, String username, String email, UserRole role) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.role = role;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public UserRole getRole() {
    return role;
  }

  public void setRole(UserRole role) {
    this.role = role;
  }
}
