package com.auction.shared.dto;

public class Request {

  private String action;
  private String body;

  public Request() {}

  public Request(String action, String body) {
    this.action = action;
    this.body = body;
  }

  public String getAction() {
    return action;
  }

  public String getBody() {
    return body;
  }
}
