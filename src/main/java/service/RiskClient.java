package service;

public interface RiskClient {
    //Retorne true si el cliente esta bloqueado por riesgo
    boolean isBlocked(String email);
}
