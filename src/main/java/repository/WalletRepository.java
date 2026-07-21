package repository;

import model.Account;

import java.util.Optional;

public interface WalletRepository {
    //Guardar una cuenta y devolver la cuenta guardada
    Account save(Account account);

    //Buscar una cuenta con un id específico
    Optional<Account> findById(String id);

    //Saber si existe una cuenta con el email del dueño
    boolean existsByEmail(String email);
}
