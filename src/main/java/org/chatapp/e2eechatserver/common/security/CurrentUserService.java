package org.chatapp.e2eechatserver.common.security;

import lombok.RequiredArgsConstructor;
import org.chatapp.e2eechatserver.user.entity.User;
import org.chatapp.e2eechatserver.user.service.UserService;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final UserService userService;

    public User getCurrentUser(Principal principal) {
        return (User) userService.loadUserByUsername(principal.getName());
    }

    public Long getCurrentUserId(Principal principal) {
        return getCurrentUser(principal).getId();
    }

    public String getCurrentUsername(Principal principal) {
        return principal.getName();
    }
}
