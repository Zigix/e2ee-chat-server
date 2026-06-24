package org.chatapp.e2eechatserver.common.security;

import org.chatapp.e2eechatserver.user.entity.User;
import org.chatapp.e2eechatserver.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private CurrentUserService currentUserService;


    @Test
    void getCurrentUser_givenPrincipal_whenGetCurrentUser_thenReturnsUserLoadedByPrincipalName() {
        // given
        Principal principal = principal("testuser");
        User user = user();

        when(userService.loadUserByUsername("testuser")).thenReturn(user);

        // when
        User currentUser = currentUserService.getCurrentUser(principal);

        // then
        assertThat(currentUser).isSameAs(user);
        verify(userService).loadUserByUsername("testuser");
    }

    @Test
    void getCurrentUserId_givenPrincipal_whenGetCurrentUserId_thenReturnsCurrentUserId() {
        // given
        Principal principal = principal("testuser");
        User user = user();

        when(userService.loadUserByUsername("testuser")).thenReturn(user);

        // when
        Long currentUserId = currentUserService.getCurrentUserId(principal);

        // then
        assertThat(currentUserId).isEqualTo(1L);
        verify(userService).loadUserByUsername("testuser");
    }

    @Test
    void getCurrentUsername_givenPrincipal_whenGetCurrentUsername_thenReturnsPrincipalName() {
        // given
        Principal principal = principal("testuser");

        // when
        String currentUsername = currentUserService.getCurrentUsername(principal);

        // then
        assertThat(currentUsername).isEqualTo("testuser");
        verifyNoInteractions(userService);
    }

    private static Principal principal(String username) {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(username);

        return principal;
    }

    private static User user() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword("encoded-password");
        user.setPubEcdhJwk("{\"kty\":\"EC\"}");
        user.setVaultVersion(1);
        user.setKdfSaltB64("salt");
        user.setKdfIterations(10000);
        user.setWrappedMkB64("wrappedMk");
        user.setWrappedMkIvB64("wrappedMkIv");
        user.setWrappedEcdhPrivB64("wrappedEcdhPriv");
        user.setWrappedEcdhPrivIvB64("wrappedEcdhPrivIv");

        return user;
    }
}