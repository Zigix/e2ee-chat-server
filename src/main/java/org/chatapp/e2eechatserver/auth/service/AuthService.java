package org.chatapp.e2eechatserver.auth.service;

import org.chatapp.e2eechatserver.auth.dto.LoginUserRequest;
import org.chatapp.e2eechatserver.auth.dto.LoginUserResponse;
import org.chatapp.e2eechatserver.auth.dto.RegisterUserRequest;

public interface AuthService {
    void signUp(RegisterUserRequest registerUserRequest);
    LoginUserResponse login(LoginUserRequest loginUserRequest);
}
