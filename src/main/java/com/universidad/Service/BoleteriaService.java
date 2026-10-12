package com.universidad.Service;

import java.util.*;

import com.universidad.exception.AsientoNoDisponibleException;
import com.universidad.exception.BoleteriaException;
import com.universidad.model.Asiento;
import com.universidad.model.Compra;
import com.universidad.model.Evento;
import com.universidad.util.PdfQrUtil;

public class BoleteriaService {
    private ArchivoService archivoService;
    private ReporteService reporteService;
    private List<Evento> eventos;
    private List<Asiento> asientos;

    public BoleteriaService() {
        archivoService = new ArchivoService();
        reporteService = new ReporteService();
        try {
            eventos = archivoService.cargarEventos();
            asientos = archivoService.cargarAsientos();
            archivoService.auditar("Sistema inicializado. Datos cargados.");
        } catch (Exception e) {
            archivoService.auditar("Error crítico al cargar datos: " + e.getMessage());
        }
    }

    public List<Evento> getEventos() { return eventos; }

    public void mostrarAsientosEvento(String idEvento) throws BoleteriaException {
        if(eventos.stream().noneMatch(e -> e.getId().equals(idEvento))) {
            throw new BoleteriaException("El evento seleccionado no existe.");
        }
        System.out.println("--- ASIENTOS DEL EVENTO " + idEvento + " ---");
        asientos.stream()
            .filter(a -> a.getIdEvento().equals(idEvento))
            .forEach(System.out::println);
    }

    public Compra procesarCompra(String titular, String doc, String correo, String idEvento, List<String> asientosSolicitados) throws Exception {
        // Validaciones Básicas (RF4)
        if (titular == null || titular.trim().isEmpty() || correo == null || !correo.contains("@")) {
            throw new BoleteriaException("Datos de comprador inválidos.");
        }
        if (asientosSolicitados.isEmpty()) {
            throw new BoleteriaException("Debe seleccionar al menos un asiento.");
        }
        Evento evento = eventos.stream().filter(e -> e.getId().equals(idEvento)).findFirst()
            .orElseThrow(() -> new BoleteriaException("Evento inexistente."));

        double subtotal = 0;
        List<Asiento> asientosAComprar = new ArrayList<>();

        for (String idAsiento : asientosSolicitados) {
            Asiento asientoObj = asientos.stream()
                .filter(a -> a.getIdAsiento().equals(idAsiento) && a.getIdEvento().equals(idEvento))
                .findFirst()
                .orElseThrow(() -> new AsientoNoDisponibleException(idAsiento));

            if (!asientoObj.isDisponible()) {
                throw new AsientoNoDisponibleException(idAsiento);
            }
            asientoObj.setDisponible(false); // Reseva
            subtotal += asientoObj.getPrecio();
            asientosAComprar.add(asientoObj);
        }

        double servicio = subtotal * 0.10;
        double total = subtotal + servicio;
        String idCompra = "CMP-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Compra compra = new Compra(idCompra, titular, doc, correo, idEvento, asientosSolicitados, subtotal, servicio, total);
        
        // Persistencia y reportes (RF5, RF6, RF7)
        archivoService.actualizarAsientos(this.asientos);
        archivoService.guardarCompra(compra);
        PdfQrUtil.generarTicket(compra, evento.getNombre());
        archivoService.auditar("Compra exitosa: " + idCompra + " | Asientos: " + asientosSolicitados.size());

        return compra;
    }

    public void generarReporte() {
        reporteService.generarReporte(eventos, asientos, archivoService.cargarCompras());
    }
}
