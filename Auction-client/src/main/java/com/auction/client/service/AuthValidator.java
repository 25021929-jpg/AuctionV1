package com.auction.client.service;
import com.auction.model.*;

public final class AuthValidator {
    //Một class riêng biệt để kiểm tra format người dùng nhập vào đăng nhập có hợp lệ hay không
    //-> Thỏa mãn SRP và high Cohesion
    //Đây là một lớp tiện ích(không cần tạo interface cho nó)
    private AuthValidator(){
        throw new UnsupportedOperationException("Đây là lớp tiện ích, không được khởi tạo");
    }

    //Hàm dùng chung(hàm chính)
    public static String validateLogin(String username, String password){
        String usernameError = AuthValidator.checkUserName(username);
        if (usernameError != null) return usernameError;
        String passwordError = AuthValidator.checkPassword(password);
        if (passwordError != null) return passwordError;
        return "Valid"; //Mọi thứ đều ổn
    }
    //Sử dụng Extract method để kiểm tra UserName và Password riêng ra
    //-> Đảm bảo SRP, OCP và code không bị dài, đảm bảo dễ hiểu phương thức làm g

    //Kiểm tra Username
    private static String checkUserName(String username){
        if (username == null || username.trim().isBlank()){
            return "Tên đăng nhập không được để trống";
        }
        if (username.contains(" ")){
            return "Tên đăng nhập không được chứa khoảng trống";
        }
        return null; //Không có lỗi
    }

    //Kiểm tra mật khẩu
    private static String checkPassword(String password){
        if (password == null || password.trim().isBlank()){
            return "Mật khẩu không được để trống";
        }
        if (password.contains(" ")){
            return "Mật khẩu không được có khoảng trống";
        }
        if (password.length() < 6){
            return "Mật khẩu phải có ít nhất 6 ký tự";
        }
        return null; //Không có lỗi
    }
}
//Note: -Isblank() là phiên bản xịn hơn của isEmpty() khi nó vừa kiểm tra có ký tự nào hay không
//vừa kiểm tra xem ca ký tự có hoàn toàn là khoảng trắng hay không
//-Trong Java, việc so sánh chuỗi .equals("Valid") đôi khi dễ bị gõ nhầm chữ hoa/chữ thường.
// Cách phổ biến hơn trong các hàm validate là: Nếu có lỗi thì trả về chuỗi báo lỗi, nếu không có lỗi thì trả về null.