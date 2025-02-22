package com.AuthService.service;

import com.AuthService.domain.FabricIdentity;

public interface FabricService {
    public FabricIdentity registerUser(String uname, String password);
}
