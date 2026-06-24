package org.chatapp.e2eechatserver.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserResponse {
    private String accessToken;
    private Long userId;
    private String username;
    private String pubEcdhJwk;
    private VaultDto vaultDto;
}
