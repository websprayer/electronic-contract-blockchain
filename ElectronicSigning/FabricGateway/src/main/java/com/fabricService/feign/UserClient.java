package com.fabricService.feign;

import com.fabricService.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@FeignClient(name = "Auth")  // 与用户服务在 Nacos 中注册的名称一致
public interface UserClient {

    @PostMapping("auth/findByUname")
    Result<Map<String, Object>> findByUname(@RequestParam("uname") String uname);

    @PostMapping(value = "auth/readcontract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result<String> readPdfAndGenerateHash(@RequestPart("file") MultipartFile file);
}