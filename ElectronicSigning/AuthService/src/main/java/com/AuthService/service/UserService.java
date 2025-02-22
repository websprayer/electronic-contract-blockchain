package com.AuthService.service;
import com.AuthService.domain.User;

public interface UserService {
    User loginService(String uname, String password);
    User registerService(User user);
    User findByUname(String uname);
    long getUserId(User user);
}
