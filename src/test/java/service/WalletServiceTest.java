package service;

import dto.WalletResponse;
import model.Account;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import repository.WalletRepository;

import java.util.Optional;

public class WalletServiceTest {
    //Dependencias simuladas (mocks)
    private WalletRepository walletRepository;
    private RiskClient riskClient;

    //Clase real a probar
    private WalletService walletService;

    @BeforeEach
    void setUp() {
        //ARRANGE común para todos los test
        walletRepository = Mockito.mock(WalletRepository.class);
        riskClient = Mockito.mock(RiskClient.class);

        walletService = new WalletService(walletRepository, riskClient);
    }

    @Test
    void createAccount_validData_shouldSaveAndReturnResponse() {
        //ARRANGE
        String email = "fajimenez4@espe.edu.ec";
        double initialBalance = 100.00;

        Mockito.when(riskClient.isBlocked(email)).thenReturn(false);
        Mockito.when(walletRepository.existsByEmail(email)).thenReturn(false);
        Mockito.when(walletRepository.save(ArgumentMatchers.any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        //ACT
        WalletResponse response = walletService.createAccount(email, initialBalance);

        //ASSERT (resultado)
        Assertions.assertNotNull(response.getId(), "El id no debe ser null");
        Assertions.assertEquals(100.00, response.getBalance(), "El balance inicial debe coincidir");

        //ASSERT (interacciones)
        Mockito.verify(riskClient).isBlocked(email);
        Mockito.verify(walletRepository).existsByEmail(email);
        Mockito.verify(walletRepository).save(ArgumentMatchers.any(Account.class));
    }

    @Test
    void createAccount_invalidEmail_shouldThrowException_andNotCallDependencies() {
        //ARRANGE
        String invalidEmail = "fajimenez4-espe.edu.ec";
        double initialBalance = 100.00;

        //ACT + ASSERT
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> walletService.createAccount(invalidEmail, initialBalance),
                "Debe lanzar IllegalArgumentException por email inválido"
        );

        Assertions.assertEquals("Invalid Email", exception.getMessage());

        //ASSERT no interacciones
        Mockito.verifyNoInteractions(riskClient, walletRepository);
    }

    @Test
    void deposit_accountNotFound_shouldThrowException() {
        //ARRANGE
        String id = "cuenta-inexistente";
        double amount = 50.00;

        Mockito.when(walletRepository.findById(id)).thenReturn(Optional.empty());

        //ACT + ASSERT
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> walletService.deposit(id, amount),
                "Debe lanzar IllegalArgumentException si la cuenta no existe"
        );

        Assertions.assertEquals("Account not found", exception.getMessage());
        Mockito.verify(walletRepository, Mockito.never()).save(ArgumentMatchers.any(Account.class));
    }

    @Test
    void deposit_validAccount_shouldUpdateBalance_andSave_usingCaptor() {
        //ARRANGE
        String email = "fajimenez4@espe.edu.ec";
        Account existingAccount = new Account(email, 100.00);
        double depositAmount = 50.00;

        Mockito.when(walletRepository.findById(existingAccount.getId()))
                .thenReturn(Optional.of(existingAccount));

        //ACT
        double newBalance = walletService.deposit(existingAccount.getId(), depositAmount);

        //ASSERT (resultado)
        Assertions.assertEquals(150.00, newBalance, "El balance debe reflejar el depósito");

        //ASSERT (interacciones + captor)
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        Mockito.verify(walletRepository).save(captor.capture());
        Assertions.assertEquals(150.00, captor.getValue().getBalance(), "El balance guardado debe ser el actualizado");
    }

    // ===================== Feature: withdraw =====================

    @Test
    void withdraw_success() {
        //ARRANGE
        String email = "jrvieira@espe.edu.ec";
        Account existingAccount = new Account(email, 200.00);
        double withdrawAmount = 80.00;

        Mockito.when(walletRepository.findById(existingAccount.getId()))
                .thenReturn(Optional.of(existingAccount));

        //ACT
        double newBalance = walletService.withdraw(existingAccount.getId(), withdrawAmount);

        //ASSERT
        Assertions.assertEquals(120.00, newBalance, "El balance debe reflejar el retiro");

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        Mockito.verify(walletRepository).save(captor.capture());
        Assertions.assertEquals(120.00, captor.getValue().getBalance());
    }

    @Test
    void withdraw_insufficientFunds_shouldThrowException() {
        //ARRANGE
        String email = "jrvieira@espe.edu.ec";
        Account existingAccount = new Account(email, 30.00);
        double withdrawAmount = 100.00;

        Mockito.when(walletRepository.findById(existingAccount.getId()))
                .thenReturn(Optional.of(existingAccount));

        //ACT + ASSERT
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> walletService.withdraw(existingAccount.getId(), withdrawAmount),
                "Debe lanzar IllegalArgumentException si el saldo es insuficiente"
        );

        Assertions.assertEquals("Insufficient funds", exception.getMessage());
        Mockito.verify(walletRepository, Mockito.never()).save(ArgumentMatchers.any(Account.class));
    }
}
