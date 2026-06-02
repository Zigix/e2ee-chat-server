package org.chatapp.e2eechatserver.user.api;

import lombok.RequiredArgsConstructor;
import org.chatapp.e2eechatserver.user.dto.SearchUserResponse;
import org.chatapp.e2eechatserver.user.entity.User;
import org.chatapp.e2eechatserver.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(currentUser);
    }

    @GetMapping("/search")
    public ResponseEntity<List<SearchUserResponse>> search(@RequestParam("q") String q) {
        return ResponseEntity.ok(userService.searchForUsers(q));
    }

    @GetMapping("/{userId}/public-key")
    public ResponseEntity<String> getPublicEcdhJwkForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getPublicEcdhJwkForUser(userId));
    }
}
