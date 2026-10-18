package com.universidad.model;
import java.util.List;

public class Compra {
    private String idCompra;
    private String titular;
    private String documento;
    private String correo;
    private String idEvento;
    private List<String> asientos;
    private double subtotal;
    private double servicio;
    private double total;

    public Compra(String idCompra, String titular, String documento, String correo, String idEvento, 
                List<String> asientos, double subtotal, double servicio, double total) {
        this.idCompra = idCompra; this.titular = titular; this.documento = documento;
        this.correo = correo; this.idEvento = idEvento; this.asientos = asientos;
        this.subtotal = subtotal; this.servicio = servicio; this.total = total;
    }

    // Getters
    public String getIdCompra() { 
        return idCompra; 
    }

    public String getTitular() { 
        return titular; 
    }

    public String getDocumento() { 
        return documento; 
    }

    public String getIdEvento() {
        return idEvento; 
    }

    public List<String> getAsientos() { 
        return asientos; 
    }

    public double getTotal() { 
        return total; 
    }
}
