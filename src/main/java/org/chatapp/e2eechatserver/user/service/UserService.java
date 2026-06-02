package org.chatapp.e2eechatserver.user.service;

import org.chatapp.e2eechatserver.user.dto.SearchUserResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    List<SearchUserResponse> searchForUsers(String q);

    String getPublicEcdhJwkForUser(Long userId);

    String getUsernameByUserId(Long userId);

    Long getUserIdByUsername(String username);
}
