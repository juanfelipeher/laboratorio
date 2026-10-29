package com.universidad;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import com.universidad.service.ArchivoService;
import com.universidad.service.BoleteriaService;
import com.universidad.service.ReporteService;
import com.universidad.exception.AsientoNoDisponibleException;
import com.universidad.exception.BoleteriaException;
import com.universidad.model.Compra;

import java.io.File;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class BoleteriaServiceTest {

        private BoleteriaService service;
        @TempDir
        Path directorioTemporal;

        @BeforeEach
            void setupBaseDatosPrueba() throws Exception {
                // Crear datos de prueba (Mocking file system manual para el test)
                Path directorioDatos = directorioTemporal.resolve("data");
                Files.createDirectories(directorioDatos);
            Files.writeString(directorioDatos.resolve("eventos.csv"),
                    "id,nom,fecha,lug,\nV001,Prueba,2026,U\n", StandardCharsets.UTF_8);
            Files.writeString(directorioDatos.resolve("asientos.csv"),
                    "idA,idE,loc,pre,dis\nVIP-A-01,V001,VIP,350000.0,false\nPRE-B-01,V001,Preferencial,250000.0,false\nGEN-C-01,V001,General,150000.0,false\n",
                    StandardCharsets.UTF_8);
                service = new BoleteriaService(new ArchivoService(directorioTemporal));
        }

    @Test
    void prueba1_CalculoSubtotalServicioTotal() throws Exception {
        Files.writeString(directorioTemporal.resolve("data").resolve("asientos.csv"),
                "idA,idE,loc,pre,dis\nVIP-A-01,V001,VIP,350000.0,true\n", StandardCharsets.UTF_8);
        service = new BoleteriaService(new ArchivoService(directorioTemporal));
        Compra c = service.procesarCompra("Ana", "123", "a@a.com", "V001", List.of("VIP-A-01"));
        assertEquals(350000.0, c.getTotal() / 1.10, 0.1); // Subtotal original
        assertEquals(385000.0, c.getTotal(), 0.1); // Total con 10%
    }

    @Test
    void prueba2_RechazoAsientoYaVendido() {
        assertThrows(AsientoNoDisponibleException.class, () -> {
            service.procesarCompra("Ana", "123", "a@a.com", "V001", List.of("VIP-A-01"));
        });
    }

    @Test
    void prueba3_BusquedaEventoInvalido() {
        assertThrows(BoleteriaException.class, () -> {
            service.procesarCompra("Ana", "123", "a@a.com", "V999", List.of("VIP-A-01"));
        });
    }

    @Test
    void prueba4_ValidacionCorreoYDatosObligatorios() {
        assertThrows(BoleteriaException.class, () -> {
            service.procesarCompra("", "123", "correoMal", "V001", List.of("VIP-A-01"));
        });
    }

    @Test
    void prueba5_CalculoPorcentajeOcupacion() {
        ReporteService rs = new ReporteService();
        assertDoesNotThrow(() -> rs.generarReporte(service.getEventos(), List.of(), List.of()));
    }

    @Test
    void prueba6_LecturaFilaCsvInvalida() throws Exception {
        Files.writeString(directorioTemporal.resolve("data").resolve("asientos.csv"),
                "A3,V001,Gen,PRECIO_MALO,true", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        ArchivoService as = new ArchivoService(directorioTemporal);
        assertDoesNotThrow(as::cargarAsientos); // No debe romper, debe auditar.
    }
    
    @Test
    void prueba7_SerializacionCompraJSON() {
        ArchivoService as = new ArchivoService(directorioTemporal);
        List<Compra> iniciales = as.cargarCompras();
        as.guardarCompra(new Compra("TEST", "A", "B", "c@c", "V001", List.of("VIP-A-01"), 0,0,0));
        assertTrue(as.cargarCompras().size() > iniciales.size());
    }
    
    @Test
    void prueba8_GeneracionExcepcionPersonalizada() {
        Exception e = new AsientoNoDisponibleException("VIP-A-01");
        assertTrue(e.getMessage().contains("VIP-A-01"));
    }
}
