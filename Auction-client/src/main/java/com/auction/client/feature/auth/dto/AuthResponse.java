//package com.auction.client.feature.auth.dto;
//
//
//public class AuthResponse {
//    private boolean success;
//    private String message;
//    private com.auction.model.User user; // Sử dụng User của Authshared
//
//    // Hàm login sẽ trả về một đối tượng AuthResponse với đầy đủ tính chất
//    //Sử dụng cách Refractoring code Replace Data Value with Object
//    //Hàm khởi tạo khi đăng nhập v trả về object không có user (có thể là khi đăng nhập thất bại)
//    public AuthResponse(boolean success, String message) {
//        this.success = success;
//        this.message = message;
//        this.user = null;
//    }
//    //Hàm khởi tạo khi đăng nhập và trả về có user (có thể là thành công)
//    public AuthResponse(boolean success, String message, com.auction.model.User User){
//        this.success = success;
//        this.message = message;
//        this.user = User;
//    }
//    //Encapsulate Fields
//    //Controller lấy dữ liệu ra
//    public boolean isSuccess(){
//        return success;
//    }
//    public String getMessage(){
//        return message;
//    }
//    public com.auction.model.User getUser(){
//        return user;
//    }
//
//}
