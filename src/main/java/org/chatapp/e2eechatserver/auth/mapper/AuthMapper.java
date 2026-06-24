package org.chatapp.e2eechatserver.auth.mapper;

import lombok.RequiredArgsConstructor;
import org.chatapp.e2eechatserver.auth.dto.LoginUserResponse;
import org.chatapp.e2eechatserver.auth.dto.RegisterUserRequest;
import org.chatapp.e2eechatserver.auth.dto.VaultDto;
import org.chatapp.e2eechatserver.user.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthMapper {
    private final PasswordEncoder passwordEncoder;

    public User toUser(RegisterUserRequest registerUserRequest) {
        User user = new User();
        user.setEmail(registerUserRequest.getEmail());
        user.setUsername(registerUserRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerUserRequest.getPassword()));
        user.setPubEcdhJwk(registerUserRequest.getPubEcdhJwk());

        VaultDto vaultDto = registerUserRequest.getVault();
        user.setVaultVersion(vaultDto.getVersion());
        user.setKdfSaltB64(vaultDto.getKdfSaltB64());
        user.setKdfIterations(vaultDto.getKdfIterations());
        user.setWrappedMkB64(vaultDto.getWrappedMkB64());
        user.setWrappedMkIvB64(vaultDto.getWrappedMkIvB64());
        user.setWrappedEcdhPrivB64(vaultDto.getWrappedEcdhPrivB64());
        user.setWrappedEcdhPrivIvB64(vaultDto.getWrappedEcdhPrivIvB64());

        return user;
    }

    public LoginUserResponse toLoginUserResponse(User user, String accessToken) {
        LoginUserResponse response = new LoginUserResponse();

        response.setAccessToken(accessToken);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setPubEcdhJwk(user.getPubEcdhJwk());
        response.setVaultDto(toVaultDto(user));

        return response;
    }

    private VaultDto toVaultDto(User user) {
        VaultDto vaultDto = new VaultDto();

        vaultDto.setVersion(user.getVaultVersion());
        vaultDto.setKdfSaltB64(user.getKdfSaltB64());
        vaultDto.setKdfIterations(user.getKdfIterations());
        vaultDto.setWrappedMkB64(user.getWrappedMkB64());
        vaultDto.setWrappedMkIvB64(user.getWrappedMkIvB64());
        vaultDto.setWrappedEcdhPrivB64(user.getWrappedEcdhPrivB64());
        vaultDto.setWrappedEcdhPrivIvB64(user.getWrappedEcdhPrivIvB64());

        return vaultDto;
    }
}
