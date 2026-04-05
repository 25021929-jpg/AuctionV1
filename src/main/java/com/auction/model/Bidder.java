package src.main.java.com.auction.model;

public class Bidder extends User {
    private double balance; // Số dư tài khoản

    public Bidder(String username, String password, String fullName) {
        // Dùng 'super' để truyền thông tin lên class Cha, gán sẵn role là BIDDER
        super(username, password, fullName, Role.BIDDER);
        this.balance = 0.0; // Mặc định tài khoản mới tạo có 0 đồng
    }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
}
