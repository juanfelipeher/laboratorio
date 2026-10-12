package com.universidad;

import java.util.*;

import com.universidad.Service.BoleteriaService;
import com.universidad.model.Compra;
import com.universidad.model.Evento;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        BoleteriaService service = new BoleteriaService();

        int opcion = 0;
        do {
            System.out.println("\n=== SISTEMA DE BOLETERIA UDEA ===");
            System.out.println("1. Ver Eventos y Asientos");
            System.out.println("2. Comprar Boletas");
            System.out.println("3. Reporte de Ventas");
            System.out.println("4. Salir");
            System.out.print("Opción: ");
            
            try {
                opcion = Integer.parseInt(scanner.nextLine());
                switch (opcion) {
                    case 1:
                        for (Evento e : service.getEventos()) System.out.println(e);
                        System.out.print("\nIngrese ID del evento para ver asientos: ");
                        String evId = scanner.nextLine();
                        service.mostrarAsientosEvento(evId);
                        break;
                    case 2:
                        System.out.print("Nombre: "); String nombre = scanner.nextLine();
                        System.out.print("Documento: "); String doc = scanner.nextLine();
                        System.out.print("Correo: "); String correo = scanner.nextLine();
                        System.out.print("ID del Evento: "); String idEve = scanner.nextLine();
                        System.out.print("IDs de Asientos (separados por coma): ");
                        List<String> sillas = Arrays.asList(scanner.nextLine().replace(" ", "").split(","));
                        
                        Compra c = service.procesarCompra(nombre, doc, correo, idEve, sillas);
                        System.out.println("¡COMPRA EXITOSA! Total pagado: $" + c.getTotal());
                        System.out.println("Ticket PDF generado en la carpeta 'tickets'.");
                        break;
                    case 3:
                        service.generarReporte();
                        break;
                    case 4:
                        System.out.println("Saliendo...");
                        break;
                    default:
                        System.out.println("Opción inválida.");
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        } while (opcion != 4);
    }
}