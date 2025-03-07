package com.AuthService.service.servicelmpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileService {

    @Value("${app.storage.unsigned-path:${user.home}/contracts/unsigned/}")
    private String UNSIGNED_PATH;

    @Value("${app.storage.signed-path:${user.home}/contracts/signed/}")
    private String SIGNED_PATH;

    @Value("${app.storage.unsigned-path:${user.home}/contracts/unsigned/}")
    private String BASE_DIR;


    // 接口1：返回 unsigned/uname 下所有 pdf 文件名
    public List<Map<String, Object>> listPdfFiles(String uname,int judge) {
        File dir = null;
        if(judge == 1){
             dir = new File(SIGNED_PATH + uname);
        }
        else if(judge == 0) {
             dir = new File(UNSIGNED_PATH + uname);
        }
        List<Map<String, Object>> result = new ArrayList<>();

        if (!dir.exists() || !dir.isDirectory()) return result;
        // 获取所有 .pdf 文件
        File[] pdfFiles = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".pdf"));
        if (pdfFiles == null || pdfFiles.length == 0) return result;

        for (File pdfFile : pdfFiles) {
            String pdfName = pdfFile.getName();
            if (!pdfName.endsWith(".pdf")) continue;

            // 构造对应的 info 文件名
            String baseName = pdfName.substring(0, pdfName.length() - 4); // 去掉 ".pdf"
            String infoFileName = baseName + "_info.txt";
            File infoFile = new File(dir, infoFileName);

            if (!infoFile.exists()) continue;

            Map<String, String> infoMap = new HashMap<>();
            boolean isUserPartyB = false;

            try (BufferedReader reader = new BufferedReader(new FileReader(infoFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(":")) {
                        String[] parts = line.split(":", 2);
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        infoMap.put(key, value);

                        if (key.equals("PartyB") && value.equals(uname)) {
                            isUserPartyB = true;
                        }
                    }
                }
            } catch (IOException e) {
                continue; // 读取失败就跳过这个文件
            }

            if (!isUserPartyB&&judge == 0) continue;

            Map<String, Object> map = new HashMap<>();
            map.put("filename", pdfName);
            map.put("info", infoMap);
            result.add(map);
        }

        // 打印到控制台
        System.out.println("Unsigned contracts for user: " + uname);
        for (Map<String, Object> item : result) {
            System.out.println(item);
        }
        return result;
    }

    // 接口2：获取 unsigned PDF 文件
    public Resource getUnsignedPdfFile(String uname, String filename) {
        File file = new File(UNSIGNED_PATH + uname + "/" + filename);
        if (!file.exists()) throw new RuntimeException("文件不存在");
        return new FileSystemResource(file);
    }
    public Resource getSignedPdfFile(String uname, String filename) {
        File file = new File(SIGNED_PATH + uname + "/" + filename);
        if (!file.exists()) throw new RuntimeException("文件不存在");
        return new FileSystemResource(file);
    }

    // 接口3：保存 signed PDF 文件
    public void saveSignedPdf(String uname, MultipartFile file) {
        File dir = new File(SIGNED_PATH + uname);
        if (!dir.exists()) dir.mkdirs();

        File dest = new File(dir, file.getOriginalFilename());
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        }
    }
    public void saveContractToBothParties(String contractId, String partyA, String partyB,String signTime, MultipartFile file) throws IOException {
        saveToPartyDir(contractId, partyA, partyA, partyB,signTime, file);
        saveToPartyDir(contractId, partyB, partyA, partyB,signTime, file);
    }


    private void saveToPartyDir(String contractId, String targetDir, String partyA, String partyB,String signTime, MultipartFile file) throws IOException {
        String dirPath = SIGNED_PATH + targetDir;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 保存 PDF 文件
        File pdfFile = new File(dir, file.getOriginalFilename());
        try (InputStream inputStream = file.getInputStream();
             OutputStream outputStream = new FileOutputStream(pdfFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
        // 创建合同信息文件
        String infoFileName = file.getOriginalFilename().replaceAll("\\.pdf$", "") + "_info.txt";
        File infoFile = new File(dir, infoFileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(infoFile))) {
            writer.write("ContractID: " + contractId + "\n");
            writer.write("PartyA: " + partyA + "\n");
            writer.write("PartyB: " + partyB + "\n");
            writer.write("signTime: " + signTime + "\n");
        }
    }

    public void saveContractToBothParties(String partyA, String partyB, MultipartFile file) throws IOException {
        saveToPartyDir(partyA, partyA, partyB, file);
        saveToPartyDir(partyB, partyA, partyB, file);
    }

    private void saveToPartyDir(String targetDir, String partyA, String partyB, MultipartFile file) throws IOException {
        String dirPath = BASE_DIR + targetDir;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 手动从 MultipartFile 输入流复制文件
        File pdfFile = new File(dir, file.getOriginalFilename());
        try (InputStream inputStream = file.getInputStream();
             OutputStream outputStream = new FileOutputStream(pdfFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
        // 创建合同信息文件
        String infoFileName = file.getOriginalFilename().replaceAll("\\.pdf$", "") + "_info.txt";
        File infoFile = new File(dir, infoFileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(infoFile))) {
            writer.write("PartyA: " + partyA + "\n");
            writer.write("PartyB: " + partyB + "\n");
        }
    }


    public boolean deleteUnsignedContractForBothParties(String partyA, String partyB, String filename) {
        boolean aDeleted = deleteFromUserUnsignedDir(partyA, filename);
        boolean bDeleted = deleteFromUserUnsignedDir(partyB, filename);
        return aDeleted || bDeleted;
    }

    private boolean deleteFromUserUnsignedDir(String uname, String filename) {
        File userDir = new File(UNSIGNED_PATH + uname);
        if (!userDir.exists() || !userDir.isDirectory()) return false;

        boolean pdfDeleted = false;
        boolean infoDeleted = false;

        // 删除 PDF 文件
        File pdfFile = new File(userDir, filename);
        if (pdfFile.exists()) {
            pdfDeleted = pdfFile.delete();
        }

        // 删除对应 info 文件
        String infoFileName = filename.replaceAll("\\.pdf$", "") + "_info.txt";
        File infoFile = new File(userDir, infoFileName);
        if (infoFile.exists()) {
            infoDeleted = infoFile.delete();
        }

        return pdfDeleted || infoDeleted;
    }



}
