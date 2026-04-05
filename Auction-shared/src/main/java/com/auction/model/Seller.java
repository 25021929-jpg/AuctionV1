package src.main.java.com.auction.model;

public class Seller extends User {
    private double rating; // Điểm uy tín của người bán

    public Seller(String username, String password, String fullName) {
        super(username, password, fullName, Role.SELLER);
        this.rating = 5.0; // Mặc định shop mới có 5 sao
    }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
}
