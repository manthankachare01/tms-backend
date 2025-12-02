package com.tms.restapi.toolsmanagement.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class QRCodeGenerator {

    public static String generateQRCode(String text, String fileName) throws WriterException, IOException {
        int width = 300;
        int height = 300;

        // Folder path inside your project directory
        String folderPath = System.getProperty("user.dir") + "/qrcodes/";

        // Create folder if it doesn’t exist
        Files.createDirectories(Path.of(folderPath));

        // Create the file path
        String filePath = folderPath + fileName.replace("/", "_") + ".png";
        Path path = FileSystems.getDefault().getPath(filePath);

        // Generate QR code
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        // Write QR code image to the path
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

        return filePath;
    }
}