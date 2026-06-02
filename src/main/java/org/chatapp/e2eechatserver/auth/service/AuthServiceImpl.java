package org.chatapp.e2eechatserver.auth.service;

import lombok.RequiredArgsConstructor;
import org.chatapp.e2eechatserver.auth.dto.LoginUserRequest;
import org.chatapp.e2eechatserver.auth.dto.LoginUserResponse;
import org.chatapp.e2eechatserver.auth.dto.RegisterUserRequest;
import org.chatapp.e2eechatserver.auth.mapper.AuthMapper;
import org.chatapp.e2eechatserver.auth.validation.RegisterUserValidator;
import org.chatapp.e2eechatserver.security.jwt.JwtTokenUtil;
import org.chatapp.e2eechatserver.user.entity.User;
import org.chatapp.e2eechatserver.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthMapper authMapper;
    private final RegisterUserValidator registerUserValidator;

    @Transactional
    public void signUp(RegisterUserRequest registerUserRequest) {
        registerUserValidator.validate(registerUserRequest);
        User user = authMapper.toUser(registerUserRequest);

        userRepository.save(user);
    }

    @Override
    public LoginUserResponse login(LoginUserRequest loginUserRequest) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginUserRequest.getUsername(),
                loginUserRequest.getPassword()
        );

        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) authenticationManager.authenticate(authenticationToken);

        User user = (User) auth.getPrincipal();

        return authMapper.toLoginUserResponse(
                user,
                jwtTokenUtil.generateAccessToken(user),
                jwtTokenUtil.generateRefreshToken(user)
        );
    }
}
