package com.universidad;

import com.universidad.exception.AsientoNoDisponibleException;
import com.universidad.exception.BoleteriaException;
import com.universidad.model.Compra;
import com.universidad.service.ArchivoService;
import com.universidad.service.BoleteriaService;
import com.universidad.service.ReporteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BoleteriaServiceTest {

    private static final String EVENTOS_PRUEBA =
            "id,nom,fecha,hora,lug,tipoEvento\n"
                    + "EV001,Rock Medellin 2026,20/10/2026,19:00,Estadio Atanasio Girardot,Concierto\n"
                    + "EV002,Final Nacional 2026,15/11/2026,17:00,Estadio Metropolitano,Deportivo\n"
                    + "EV003,Noche de Teatro,3/12/2026,20:00,Teatro Metropolitano,Teatro\n";
    private static final String ASIENTOS_PRUEBA =
            "idAsiento,idEvento,localidad,precio,disponible\n"
                    + "VIP-A-01,EV001,VIP A 1,350000.0,true\n"
                    + "VIP-A-02,EV001,VIP A 2,350000.0,true\n"
                    + "GEN-C-01,EV001,General C 1,150000.0,true\n"
                    + "OCC-A-01,EV002,Occidental A 1,180000.0,false\n";

    @TempDir
    Path directorioTemporal;

    private ArchivoService archivos;
    private BoleteriaService service;

    @BeforeEach
    void prepararDatosDePrueba() throws Exception {
        Path directorioDatos = Files.createDirectories(directorioTemporal.resolve("data"));
        Files.write(directorioDatos.resolve("eventos.csv"), EVENTOS_PRUEBA.getBytes(StandardCharsets.UTF_8));
        Files.write(directorioDatos.resolve("asientos.csv"), ASIENTOS_PRUEBA.getBytes(StandardCharsets.UTF_8));
        archivos = new ArchivoService(directorioTemporal);
        service = new BoleteriaService(archivos);
    }

    @Test
    void compraCalculaSubtotalServicioYTotalConAsientoActual() throws Exception {
        Compra compra = service.procesarCompra(
                "Ana", "123", "a@a.com", "EV001", List.of("VIP-A-01"));

        assertEquals(350000.0, compra.getTotal() / 1.10, 0.01);
        assertEquals(385000.0, compra.getTotal(), 0.01);
        assertFalse(archivos.cargarAsientos().stream()
                .filter(asiento -> asiento.getIdAsiento().equals("VIP-A-01"))
                .findFirst()
                .orElseThrow()
                .isDisponible());
        assertTrue(Files.exists(directorioTemporal.resolve("tickets")
                .resolve(compra.getIdCompra() + ".pdf")));
    }

    @Test
    void rechazaAsientoQueYaEstaVendido() {
        assertThrows(AsientoNoDisponibleException.class, () ->
                service.procesarCompra(
                        "Ana", "123", "a@a.com", "EV002", List.of("OCC-A-01")));
    }

    @Test
    void rechazaEventoInexistente() {
        assertThrows(BoleteriaException.class, () ->
                service.procesarCompra(
                        "Ana", "123", "a@a.com", "EV999", List.of("VIP-A-01")));
    }

    @Test
    void validaCorreoYDatosObligatorios() {
        assertThrows(BoleteriaException.class, () ->
                service.procesarCompra(
                        "", "123", "correoMal", "EV001", List.of("VIP-A-01")));
    }

    @Test
    void generaReporteConEventosYAsientosActuales() {
        ReporteService reporte = new ReporteService();

        assertDoesNotThrow(() -> reporte.generarReporte(
                service.getEventos(), archivos.cargarAsientos(), archivos.cargarCompras()));
    }

    @Test
    void ignoraFilaCsvInvalidaYLaRegistraEnAuditoria() throws Exception {
        Path archivoAsientos = directorioTemporal.resolve("data").resolve("asientos.csv");
        Files.write(archivoAsientos,
                "GEN-C-99,EV001,General C 99,PRECIO_MALO,true\n".getBytes(StandardCharsets.UTF_8),
                java.nio.file.StandardOpenOption.APPEND);

        assertEquals(4, archivos.cargarAsientos().size());
        assertTrue(Files.readAllLines(directorioTemporal.resolve("logs").resolve("auditoria.txt"),
                StandardCharsets.UTF_8).stream().anyMatch(linea -> linea.contains("GEN-C-99")));
    }

    @Test
    void serializaCompraEnJson() {
        Compra compra = new Compra(
                "TEST", "Ana", "123", "a@a.com", "EV001",
                List.of("VIP-A-01"), 350000.0, 35000.0, 385000.0);

        archivos.guardarCompra(compra);

        assertEquals(1, archivos.cargarCompras().size());
        assertEquals("TEST", archivos.cargarCompras().get(0).getIdCompra());
    }

    @Test
    void generaExcepcionPersonalizadaConIdDelAsiento() {
        Exception excepcion = new AsientoNoDisponibleException("VIP-A-01");

        assertTrue(excepcion.getMessage().contains("VIP-A-01"));
    }
}
