package com.auction.client.service;

public class AuthValidator {
    //Một class riêng biệt để kiểm tra format người dùng nhập vào đăng nhập có hợp lệ hay không
    public static String validateLogin(String username, String password){
        if (username == null || username.trim().isBlank()){
            return "Tên đăng nhập không được để trống";
        }
        if (password == null || password.trim().isBlank()){
            return "Mật khẩu không được để trống";
        }
        return "Valid";
    }
}
