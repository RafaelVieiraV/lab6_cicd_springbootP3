package dto;

public class WalletResponse {
    private String id;
    private double balance;

    public WalletResponse(String id, double balance) {
        this.id = id;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public double getBalance() {
        return balance;
    }
}
