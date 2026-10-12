package com.universidad.Service;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.style.Styler;

import com.universidad.model.Asiento;
import com.universidad.model.Compra;
import com.universidad.model.Evento;

import java.util.*;

public class ReporteService {

    public void generarReporte(List<Evento> eventos, List<Asiento> asientos, List<Compra> compras) {
        List<String> labels = new ArrayList<>();
        List<Double> ingresos = new ArrayList<>();

        for (Evento ev : eventos) {
            double ingresoEvento = compras.stream()
                .filter(c -> c.getIdEvento().equals(ev.getId()))
                .mapToDouble(Compra::getTotal).sum();
            
            long ocupados = asientos.stream()
                .filter(a -> a.getIdEvento().equals(ev.getId()) && !a.isDisponible()).count();
            long totalAsientos = asientos.stream()
                .filter(a -> a.getIdEvento().equals(ev.getId())).count();
            
            double porcentaje = totalAsientos > 0 ? (double)ocupados / totalAsientos * 100 : 0;

            labels.add(ev.getId());
            ingresos.add(ingresoEvento);
            
            System.out.println("Evento: " + ev.getNombre() + " | Ingresos: $" + ingresoEvento + " | Ocupación: " + String.format("%.2f", porcentaje) + "%");
        }

        if(labels.isEmpty() || ingresos.stream().allMatch(i -> i == 0.0)) {
            System.out.println("No hay datos suficientes para graficar.");
            return;
        }

        CategoryChart chart = new CategoryChartBuilder()
                .width(800).height(600)
                .title("Ingresos por Evento")
                .xAxisTitle("Eventos").yAxisTitle("Ingresos ($)")
                .theme(Styler.ChartTheme.GGPlot2).build();

        chart.addSeries("Ingresos", labels, ingresos);

        try {
            BitmapEncoder.saveBitmap(chart, "reportes/ventas_por_evento", BitmapEncoder.BitmapFormat.PNG);
            System.out.println("Gráfica generada en: reportes/ventas_por_evento.png");
        } catch (Exception e) {
            System.out.println("Error generando gráfica: " + e.getMessage());
        }
    }
}