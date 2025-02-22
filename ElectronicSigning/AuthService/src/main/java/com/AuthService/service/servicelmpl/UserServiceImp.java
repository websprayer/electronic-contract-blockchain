package com.AuthService.service.servicelmpl;

import com.AuthService.domain.User;
import com.AuthService.repository.UserDao;
import com.AuthService.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImp implements UserService {
    @Resource
    UserDao userDao;
    @Override
    public User loginService(String uname, String password) {
        User user = userDao.findByUnameAndPassword(uname, password);
        if(user!=null){
            user.setPassword("");
        }
        return user;
    }

    @Override
    public User registerService(User user) {
        // 检查用户名是否已存在
        if (userDao.findByUname(user.getUname()) != null) {
            return null;
        }
        User newUser = userDao.save(user);
        // 隐藏密码以提高安全性
        newUser.setPassword("");
        return newUser;
    }

    @Override
    public long getUserId(User user) {
        return user.getUid();
    }

    @Override
    public User findByUname(String uname) {
        return userDao.findByUname(uname);
    }
}
