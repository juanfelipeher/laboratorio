package com.universidad.Service;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.universidad.model.Asiento;
import com.universidad.model.Compra;
import com.universidad.model.Evento;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

public class ArchivoService {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public ArchivoService() {
        crearCarpetasBase();
    }

    private void crearCarpetasBase() {
        String[] carpetas = {"data", "logs", "tickets", "reportes"};
        for (String c : carpetas) {
            try { Files.createDirectories(Paths.get(c)); } catch (IOException ignored) {}
        }
        File json = new File("data/compras.json");
        if (!json.exists()) {
            try (FileWriter fw = new FileWriter(json)) { fw.write("[]"); } catch (Exception ignored) {}
        }
    }

    public List<Evento> cargarEventos() throws IOException {
        List<Evento> eventos = new ArrayList<>();
        List<String> lineas = Files.readAllLines(Paths.get("data/eventos.csv"));
        for (int i = 1; i < lineas.size(); i++) { // Evitar encabezado
            String[] p = lineas.get(i).split(",");
            if(p.length >= 4) eventos.add(new Evento(p[0], p[1], p[2], p[3]));
        }
        return eventos;
    }

    public List<Asiento> cargarAsientos() throws IOException {
        List<Asiento> asientos = new ArrayList<>();
        List<String> lineas = Files.readAllLines(Paths.get("data/asientos.csv"));
        for (int i = 1; i < lineas.size(); i++) {
            String[] p = lineas.get(i).split(",");
            if(p.length >= 5) {
                try {
                    asientos.add(new Asiento(p[0], p[1], p[2], Double.parseDouble(p[3]), Boolean.parseBoolean(p[4])));
                } catch(NumberFormatException e) {
                    auditar("ERROR al cargar fila CSV: " + lineas.get(i));
                }
            }
        }
        return asientos;
    }

    public void actualizarAsientos(List<Asiento> asientos) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter("data/asientos.csv"))) {
            pw.println("idAsiento,idEvento,localidad,precio,disponible");
            for (Asiento a : asientos) {
                pw.println(String.format("%s,%s,%s,%s,%s", 
                    a.getIdAsiento(), a.getIdEvento(), a.getLocalidad(), a.getPrecio(), a.isDisponible()));
            }
        }
    }

    public List<Compra> cargarCompras() {
        try (Reader reader = new FileReader("data/compras.json")) {
            List<Compra> lista = GSON.fromJson(reader, new TypeToken<List<Compra>>(){}.getType());
            return lista == null ? new ArrayList<>() : lista;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void guardarCompra(Compra compra) {
        List<Compra> compras = cargarCompras();
        compras.add(compra);
        try (Writer writer = new FileWriter("data/compras.json")) {
            GSON.toJson(compras, writer);
        } catch (IOException e) {
            auditar("ERROR guardando compra JSON: " + e.getMessage());
        }
    }

    public void auditar(String mensaje) {
        String log = LocalDateTime.now().toString() + " | " + mensaje + "\n";
        try {
            Files.write(Paths.get("logs/auditoria.txt"), log.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {}
    }
}