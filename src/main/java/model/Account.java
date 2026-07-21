package model;

import java.util.UUID;

public class    Account {
    private String id;
    private String ownerEmail;
    private double balance;

    //Constructor lleno: el id se genera de forma randómica (UUID)
    public Account(String ownerEmail, double balance) {
        this.id = UUID.randomUUID().toString();
        this.ownerEmail = ownerEmail;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public double getBalance() {
        return balance;
    }

    //Depositar dinero en la cuenta
    public void depositar(double amount) {
        this.balance = this.balance + amount;
    }

    //Retirar dinero de la cuenta
    public void retirar(double amount) {
        this.balance = this.balance - amount;
    }
}
