package com.universidad.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.universidad.model.Compra;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PdfQrUtil {

    public static void generarTicket(Compra compra, String nombreEvento) throws Exception {
        generarTicket(compra, nombreEvento, Paths.get("tickets"));
    }

    public static void generarTicket(Compra compra, String nombreEvento, Path directorioTickets) throws Exception {
        Files.createDirectories(directorioTickets);
        Path ruta = directorioTickets.resolve(compra.getIdCompra() + ".pdf");
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(ruta.toFile()));
        document.open();

        Font fontTitulo = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font fontTexto = new Font(Font.HELVETICA, 12, Font.NORMAL);

        document.add(new Paragraph("TICKET DE ENTRADA - BOLETERIA UDEA", fontTitulo));
        document.add(new Paragraph("--------------------------------------------------"));
        document.add(new Paragraph("ID Compra: " + compra.getIdCompra(), fontTexto));
        document.add(new Paragraph("Titular: " + compra.getTitular(), fontTexto));
        document.add(new Paragraph("Documento: " + compra.getDocumento(), fontTexto));
        document.add(new Paragraph("Evento: " + nombreEvento, fontTexto));
        document.add(new Paragraph("Asientos: " + String.join(", ", compra.getAsientos()), fontTexto));
        document.add(new Paragraph("Total Pagado: $" + compra.getTotal(), fontTexto));
        document.add(new Paragraph("--------------------------------------------------"));

        // Generar QR en memoria y añadirlo al PDF
        String datosQr = String.format("ID_COMPRA=%s | TITULAR=%s | EVENTO=%s | ASIENTOS %s | TOTAL=%.0f",
                compra.getIdCompra(), compra.getTitular(), compra.getIdEvento(), 
                String.join(",", compra.getAsientos()), compra.getTotal());

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(datosQr, BarcodeFormat.QR_CODE, 200, 200);
        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        Image qrImage = Image.getInstance(baos.toByteArray());
        document.add(qrImage);

        document.close();
    }
}
