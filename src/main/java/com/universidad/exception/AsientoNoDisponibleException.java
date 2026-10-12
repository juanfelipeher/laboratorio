package com.universidad.exception;

public class AsientoNoDisponibleException extends BoleteriaException {
    public AsientoNoDisponibleException(String asiento) {
        super("El asiento " + asiento + " no es válido o ya se encuentra vendido.");
    }
}