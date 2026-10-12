package com.universidad.model;

public class Evento {
    private String id;
    private String nombre;
    private String fecha;
    private String lugar;

    public Evento(String id, String nombre, String fecha, String lugar) {
        this.id = id; this.nombre = nombre; this.fecha = fecha; this.lugar = lugar;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getFecha() { return fecha; }
    public String getLugar() { return lugar; }

    @Override
    public String toString() {
        return id + " | " + nombre + " (" + fecha + ") - " + lugar;
    }
}
