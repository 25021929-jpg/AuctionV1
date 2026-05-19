package com.auction.shared.dto;

public class Request {

    private String action;   // tiêu đề: "AUCTION_GET_ALL", "AUTH_LOGIN"...
    private String body;     // nội dung: JSON string chứa dữ liệu

    public Request() {
    }

    public Request(String action, String body) {
        this.action = action;
        this.body = body;   //body là JSON dạng text, chưa phải object.
    }

    public String getAction() {
        return action;
    }

    public String getBody() {
        return body;
    }
}