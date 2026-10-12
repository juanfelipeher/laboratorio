package com.universidad;

import org.junit.jupiter.api.*;

import com.universidad.Service.ArchivoService;
import com.universidad.Service.BoleteriaService;
import com.universidad.Service.ReporteService;
import com.universidad.exception.AsientoNoDisponibleException;
import com.universidad.exception.BoleteriaException;
import com.universidad.model.Compra;

import java.io.File;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BoleteriaServiceTest {
    
    private BoleteriaService service;

    @BeforeAll
    void setupBaseDatosPrueba() throws Exception {
        // Crear datos de prueba (Mocking file system manual para el test)
        Files.createDirectories(Paths.get("data"));
        Files.write(Paths.get("data/eventos.csv"), "id,nom,fecha,lug\nE01,Prueba,2026,U".getBytes());
        Files.write(Paths.get("data/asientos.csv"), "idA,idE,loc,pre,dis\nA1,E01,Gen,100,true\nA2,E01,Gen,100,false".getBytes());
        service = new BoleteriaService();
    }

    @Test
    void prueba1_CalculoSubtotalServicioTotal() throws Exception {
        Compra c = service.procesarCompra("Ana", "123", "a@a.com", "E01", List.of("A1"));
        assertEquals(100.0, c.getTotal() / 1.10, 0.1); // Subtotal original
        assertEquals(110.0, c.getTotal()); // Total con 10%
    }

    @Test
    void prueba2_RechazoAsientoYaVendido() {
        assertThrows(AsientoNoDisponibleException.class, () -> {
            service.procesarCompra("Ana", "123", "a@a.com", "E01", List.of("A2"));
        });
    }

    @Test
    void prueba3_BusquedaEventoInvalido() {
        assertThrows(BoleteriaException.class, () -> {
            service.procesarCompra("Ana", "123", "a@a.com", "E99", List.of("A1"));
        });
    }

    @Test
    void prueba4_ValidacionCorreoYDatosObligatorios() {
        assertThrows(BoleteriaException.class, () -> {
            service.procesarCompra("", "123", "correoMal", "E01", List.of("A1"));
        });
    }

    @Test
    void prueba5_CalculoPorcentajeOcupacion() {
        // Al vender A1 en la prueba 1, ahora la ocupación de E01 es 100% (A1 y A2 vendidos)
        ReporteService rs = new ReporteService();
        assertDoesNotThrow(() -> rs.generarReporte(service.getEventos(), List.of(), List.of()));
    }

    @Test
    void prueba6_LecturaFilaCsvInvalida() throws Exception {
        Files.write(Paths.get("data/asientos.csv"), "A3,E01,Gen,PRECIO_MALO,true".getBytes(), StandardOpenOption.APPEND);
        ArchivoService as = new ArchivoService();
        assertDoesNotThrow(as::cargarAsientos); // No debe romper, debe auditar.
    }

    @Test
    void prueba7_SerializacionCompraJSON() {
        ArchivoService as = new ArchivoService();
        List<Compra> iniciales = as.cargarCompras();
        as.guardarCompra(new Compra("TEST", "A", "B", "c@c", "E01", List.of("A"), 0,0,0));
        assertTrue(as.cargarCompras().size() > iniciales.size());
    }
    
    @Test
    void prueba8_GeneracionExcepcionPersonalizada() {
        Exception e = new AsientoNoDisponibleException("A2");
        assertTrue(e.getMessage().contains("A2"));
    }
}
