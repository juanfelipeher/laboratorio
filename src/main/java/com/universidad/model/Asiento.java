package com.universidad.model;

public class Asiento {
    private String idAsiento;
    private String idEvento;
    private String localidad;
    private double precio;
    private boolean disponible;

    public Asiento(String idAsiento, String idEvento, String localidad, double precio, boolean disponible) {
        this.idAsiento = idAsiento;
        this.idEvento = idEvento;
        this.localidad = localidad;
        this.precio = precio;
        this.disponible = disponible;
    }

    public String getIdAsiento() { 
        return idAsiento; 
    }

    public String getIdEvento() { 
        return idEvento; 
    }
    
    public String getLocalidad() { 
        return localidad;
    }

    public double getPrecio() { 
        return precio; 
    }

    public boolean isDisponible() { 
        return disponible; 
    }
    
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    @Override
    public String toString() {
        return idAsiento + " [" + localidad + "] - $" + precio + " - " + (disponible ? "DISPONIBLE" : "VENDIDO");
    }
}
