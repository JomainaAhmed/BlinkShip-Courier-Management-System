package com.capg.delivery.service;

import com.capg.delivery.entity.Delivery;
import com.capg.delivery.repository.DeliveryRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class InvoiceService {

    private final DeliveryRepository deliveryRepository;

    public InvoiceService(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    public byte[] generateInvoice(Long deliveryId) throws Exception {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        // Generate QR Code
        String trackingUrl = "http://localhost:4200/track?id=" + deliveryId;
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(trackingUrl, BarcodeFormat.QR_CODE, 200, 200);
        
        ByteArrayOutputStream qrStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", qrStream);
        String qrBase64 = Base64.getEncoder().encodeToString(qrStream.toByteArray());

        // Simple HTML Template
        String html = "<html><body style='font-family: Arial, sans-serif; padding: 40px;'>" +
                "<div style='text-align: center; margin-bottom: 40px;'>" +
                "<h1 style='color: #FF6B00; margin: 0;'>BLINKSHIP</h1>" +
                "<p style='color: #666; font-size: 12px; margin-top: 5px;'>SMART LOGISTICS SOLUTIONS</p>" +
                "</div>" +
                "<div style='margin-bottom: 30px;'>" +
                "<h2>INVOICE</h2>" +
                "<p><strong>Shipment ID:</strong> #" + delivery.getId() + "</p>" +
                "<p><strong>Status:</strong> " + delivery.getStatus() + "</p>" +
                "<p><strong>Date:</strong> " + delivery.getCreatedAt() + "</p>" +
                "</div>" +
                "<div style='display: flex; justify-content: space-between; margin-bottom: 30px;'>" +
                "<div>" +
                "<h3>SENDER</h3>" +
                "<p>" + delivery.getSender().getName() + "<br>" + delivery.getSender().getCity() + ", " + delivery.getSender().getState() + "</p>" +
                "</div>" +
                "<div>" +
                "<h3>RECEIVER</h3>" +
                "<p>" + delivery.getReceiver().getName() + "<br>" + delivery.getReceiver().getCity() + ", " + delivery.getReceiver().getState() + "</p>" +
                "</div>" +
                "</div>" +
                "<table style='width: 100%; border-collapse: collapse; margin-bottom: 30px;'>" +
                "<thead><tr style='background: #f4f4f4;'>" +
                "<th style='padding: 10px; border: 1px solid #ddd;'>Description</th>" +
                "<th style='padding: 10px; border: 1px solid #ddd;'>Weight (KG)</th>" +
                "<th style='padding: 10px; border: 1px solid #ddd;'>Price</th>" +
                "</tr></thead>" +
                "<tbody><tr>" +
                "<td style='padding: 10px; border: 1px solid #ddd;'>" + delivery.getPackageEntity().getDescription() + "</td>" +
                "<td style='padding: 10px; border: 1px solid #ddd; text-align: center;'>" + delivery.getPackageEntity().getWeight() + "</td>" +
                "<td style='padding: 10px; border: 1px solid #ddd; text-align: right;'>\u20B9" + String.format("%.2f", delivery.getPackageEntity().getPrice()) + "</td>" +
                "</tr></tbody>" +
                "</table>" +
                "<div style='text-align: right; margin-bottom: 40px;'>" +
                "<h2 style='color: #FF6B00;'>Total: \u20B9" + String.format("%.2f", delivery.getPackageEntity().getPrice()) + "</h2>" +
                "</div>" +
                "<div style='text-align: center; border-top: 1px solid #eee; padding-top: 30px;'>" +
                "<p style='font-size: 10px; color: #999; margin-bottom: 10px;'>SCAN TO TRACK SHIPMENT</p>" +
                "<img src='data:image/png;base64," + qrBase64 + "' width='100' height='100' />" +
                "</div>" +
                "</body></html>";

        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html, pdfStream);
        return pdfStream.toByteArray();
    }
}
