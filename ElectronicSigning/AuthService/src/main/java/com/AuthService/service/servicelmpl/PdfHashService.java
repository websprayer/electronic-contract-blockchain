package com.AuthService.service.servicelmpl;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class PdfHashService {

    /**
     * 方法一：读取PDF文件内容
     * @param filePath PDF文件路径
     * @return 提取到的文本内容
     * @throws IOException
     */
    public String readPdfText(String filePath) throws IOException {
        try {
            // 对文件路径进行 URL 解码
            String decodedPath = URLDecoder.decode(filePath, StandardCharsets.UTF_8.name());

            // 将反斜杠替换为正斜杠，以确保兼容性
            decodedPath = decodedPath.replace("\\", "/");

            // 加载 PDF 文件
            File pdfFile = new File(decodedPath);
            if (!pdfFile.exists()) {
                throw new IOException("文件未找到: " + decodedPath);
            }

            try (PDDocument document = PDDocument.load(pdfFile)) {
                PDFTextStripper pdfStripper = new PDFTextStripper();
                return pdfStripper.getText(document);  // 返回 PDF 中的文本内容
            }
        } catch (IOException e) {
            throw new IOException("读取 PDF 文件时出错: " + filePath, e);
        }
    }


    public String readPdfTextFromFile(MultipartFile pdfFile) throws IOException {
        try {
            // 将上传的PDF文件转换为 PDDocument 对象
            try (PDDocument document = PDDocument.load(pdfFile.getInputStream())) {
                PDFTextStripper pdfStripper = new PDFTextStripper();
                return pdfStripper.getText(document);  // 返回 PDF 中的文本内容
            }
        } catch (IOException e) {
            throw new IOException("读取 PDF 文件时出错", e);
        }
    }
    public String generateHash(String input) throws NoSuchAlgorithmException {
        // 默认使用 SHA-256 加密算法
        String algorithm = "SHA-256";

        // 获取指定算法的 MessageDigest 实例
        MessageDigest digest = MessageDigest.getInstance(algorithm);

        // 生成哈希值
        byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

        // 将哈希值转换为16进制字符串
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        // 返回生成的哈希值
        return hexString.toString();
    }
}