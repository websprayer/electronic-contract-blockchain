package com.AuthService.controller;

import com.AuthService.service.servicelmpl.FileService;
import com.AuthService.utils.JwtUtil;
import com.AuthService.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/listunsigned")
    public Result<List<Map<String, Object>>> listUnsignedFiles(HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            return Result.error("401", "未提供有效的 token");
        }

        token = token.substring(7); // 去掉 "Bearer "
        String uname = jwtUtil.extractUname(token);
        if (uname == null) {
            return Result.error("401", "token 无效或未包含用户名");
        }

        List<Map<String, Object>> filesWithInfo = fileService.listPdfFiles(uname,0);
        return Result.success(filesWithInfo, "获取PDF及合同信息成功");
    }
    @GetMapping("/listsigned")
    public Result<List<Map<String, Object>>> listsignedFiles(HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            return Result.error("401", "未提供有效的 token");
        }

        token = token.substring(7); // 去掉 "Bearer "
        String uname = jwtUtil.extractUname(token);
        if (uname == null) {
            return Result.error("401", "token 无效或未包含用户名");
        }

        List<Map<String, Object>> filesWithInfo = fileService.listPdfFiles(uname,1);
        return Result.success(filesWithInfo, "获取PDF及合同信息成功");
    }

    // 接口2：根据 uid 和文件名返回文件内容（PDF）
    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadPdf(
            @PathVariable String filename,
            HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        token = token.substring(7); // 去掉 "Bearer "
        String uname = jwtUtil.extractUname(token);

        if (uname == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Resource file = fileService.getUnsignedPdfFile(uname, filename);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(file);
    }
    @GetMapping("/showpdf/{filename}")
    public ResponseEntity<Resource> showPdf(
            @PathVariable String filename,
            HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        token = token.substring(7); // 去掉 "Bearer "
        String uname = jwtUtil.extractUname(token);

        if (uname == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Resource file = fileService.getSignedPdfFile(uname, filename);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(file);
    }


    // 接口3：上传文件并保存到 signed/uid 目录下
    @PostMapping("/savesginedContract")
    public Result<String> saveContract(@RequestParam String ContractID,
                                       @RequestParam String partyA,
                                       @RequestParam String partyB,
                                       @RequestParam("file") MultipartFile file,
                                       @RequestParam String signTime) {
        try {
            fileService.saveContractToBothParties( ContractID,partyA, partyB, signTime,file);
            fileService.deleteUnsignedContractForBothParties(partyA, partyB, file.getOriginalFilename());

            return Result.success("保存成功", "PDF和合同信息已存储");
        } catch (IOException e) {
            return Result.error("500", "保存失败: " + e.getMessage());
        }
    }


    @PostMapping("/saveUnsginedContract")
    public Result<String> saveContract(@RequestParam String partyA,
                                       @RequestParam String partyB,
                                       @RequestParam("file") MultipartFile file) {
        try {
            fileService.saveContractToBothParties( partyA, partyB, file);
            return Result.success("保存成功", "PDF和合同信息已存储");
        } catch (IOException e) {
            return Result.error("500", "保存失败: " + e.getMessage());
        }
    }


}