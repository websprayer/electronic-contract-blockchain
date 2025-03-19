package com.fabricService.Service;

import com.fabricService.feign.UserClient;
import com.fabricService.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class UserRemoteService {

    @Autowired
    private UserClient userClient;

    public Long getUidByUname(String uname) {
        Result<Map<String, Object>> result = userClient.findByUname(uname);
        if ("0".equals(result.getCode()) && result.getData() != null) {
            return Long.valueOf(result.getData().get("uid").toString());
        }
        throw new RuntimeException("用户名不存在或服务调用失败");
    }
    public String getHashFromPdf(MultipartFile file) {
        Result<String> result = userClient.readPdfAndGenerateHash(file);
        if ("0".equals(result.getCode()) && result.getData() != null) {
            return result.getData();
        }
        throw new RuntimeException("文件哈希生成失败或服务调用异常: " + result.getMsg());
    }

}

