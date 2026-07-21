package service;

import dto.WalletResponse;
import model.Account;
import repository.WalletRepository;

import java.util.Optional;

public class WalletService {
    //Inyectar las dependencias
    private final WalletRepository walletRepository;
    private final RiskClient riskClient;

    public WalletService(WalletRepository walletRepository, RiskClient riskClient) {
        this.walletRepository = walletRepository;
        this.riskClient = riskClient;
    }

    //Crear una cuenta aplicando las reglas de negocio
    public WalletResponse createAccount(String email, double initialBalance) {
        //Validaciones
        //Validación de email básica
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid Email");
        }
        //Validación del saldo inicial
        if (initialBalance <= 0) {
            throw new IllegalArgumentException("Initial balance must be > 0");
        }

        //Validación de riesgo
        //Si el cliente está bloqueado no debe poder crear una cuenta
        if (riskClient.isBlocked(email)) {
            throw new IllegalArgumentException("Customer blocked");
        }

        //Validación de cuenta existente
        if (walletRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Account already exists");
        }

        //Crear y guardar la cuenta
        Account account = new Account(email, initialBalance);
        Account saved = walletRepository.save(account);

        //Respuesta del DTO
        return new WalletResponse(saved.getId(), saved.getBalance());
    }

    //Depositar dinero en una cuenta existente
    public double deposit(String id, double amount) {
        //Validación del monto
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be > 0");
        }

        //La cuenta debe existir
        Account account = walletRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        //Usar la función de depositar del modelo
        account.depositar(amount);

        //Persistir el saldo
        walletRepository.save(account);

        //Retornar el balance de la cuenta
        return account.getBalance();
    }
}
