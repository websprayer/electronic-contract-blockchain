package com.AuthService.controller;

import com.AuthService.domain.FabricIdentity;
import com.AuthService.domain.User;
import com.AuthService.service.FabricService;
import com.AuthService.service.UserService;
import com.AuthService.service.servicelmpl.PdfHashService;
import com.AuthService.utils.JwtUtil;
import com.AuthService.utils.Result;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/auth")
public class UserController {

    @Resource
    private UserService userServiceImp;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private FabricService FabricServiceImp;

    @Resource
    private PdfHashService pdfHashService;

    @PostMapping("/login")
    public Result<Map> loginController(@RequestParam String uname, @RequestParam String password) {
        User user = userServiceImp.loginService(uname, password);
        if (user != null) {
            String token = jwtUtil.generateToken(user.getUid(),user.getUname());
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("user", user);
            resultMap.put("token", token);
            return Result.success(resultMap, "登录成功");
        } else {
            return Result.error("123", "密码错误");
        }
    }

    @PostMapping("/register")
    public Result<User> registerController(@RequestBody User newUser) {
        FabricIdentity identity = FabricServiceImp.registerUser(newUser.getUname(), newUser.getPassword());
        User user = userServiceImp.registerService(newUser);
        if (user != null) {
            return Result.success(user, "注册成功");
        } else {
            return Result.error("456", "用户名已存在");
        }
    }
    @PostMapping("/findByUname")
    public Result<Map<String, Object>> findByUname(@RequestParam String uname) {
        User existingUser = userServiceImp.findByUname(uname);
        if (existingUser != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("uid", existingUser.getUid());
            return Result.success(data, "用户存在");
        } else {
            return Result.error("404", "用户名不存在");
        }
    }


    @GetMapping("/getId")

    public Result<Long> getUserId(@RequestHeader("Authorization") String authHeader) {
        // 校验 Authorization 头部
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            return Result.error("401", "未提供有效的令牌");
        }

        // 提取 JWT 令牌
        String token = authHeader.substring(7);
        String userIdStr = jwtUtil.extractUid(token).toString();

        if (userIdStr == null) {
            return Result.error("403", "无效的令牌");
        }

        try {
            Long userId = Long.parseLong(userIdStr); // 确保 ID 为 Long 类型
            return Result.success(userId, "用户 ID 获取成功");
        } catch (NumberFormatException e) {
            return Result.error("500", "令牌解析失败");
        }
    }
    @GetMapping("/readtest")
    public String readPdf(@RequestParam String path) {
        try {
            return pdfHashService.readPdfText(path);
        } catch (Exception e) {
            return "Error reading PDF: " + e.getMessage();
        }
    }

    // 方法2：对文本生成hash
//    @PostMapping("/hash")
//    public String hashText(@RequestParam String text
//                           ) {
//        try {
//            return pdfHashService.generateHash(text);
//        } catch (Exception e) {
//            return "Error generating hash: " + e.getMessage();
//        }
//    }
//    @PostMapping("/read")
//    public ResponseEntity<String> readPdf(@RequestParam("file") MultipartFile pdfFile) {
//        try {
//            // 调用 PdfHashService 读取 PDF 文件文本
//            String pdfText = pdfHashService.readPdfTextFromFile(pdfFile);
//            return ResponseEntity.ok(pdfText);
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("读取 PDF 文件时出错: " + e.getMessage());
//        }
//    }

    @PostMapping("/readcontract")
    public Result<String> readPdfAndGenerateHash(@RequestParam("file") MultipartFile file) {
        try {
            // 读取PDF文本内容
            String text = pdfHashService.readPdfTextFromFile(file);

            // 生成Hash值
            String hash = pdfHashService.generateHash(text);

            return Result.success(hash, "PDF 哈希生成成功");
        } catch (IOException | NoSuchAlgorithmException e) {
            return Result.error("500", "读取或生成哈希失败: " + e.getMessage());
        }
    }

}
