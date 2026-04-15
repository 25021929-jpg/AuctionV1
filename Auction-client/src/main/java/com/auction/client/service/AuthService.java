package com.auction.client.service;
public class AuthService {
    public boolean login(String username, String password){
        String validationResult = AuthValidator.validateLogin(username, password);
        if(!validationResult.equals("Valid")){
            System.out.println("Lỗi đăng nhập:" + validationResult);
            return false;
        }
        //... Vẫn còn logic
        return true;
    }


}
