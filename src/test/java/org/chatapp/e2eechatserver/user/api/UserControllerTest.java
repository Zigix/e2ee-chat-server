package org.chatapp.e2eechatserver.user.api;

import org.chatapp.e2eechatserver.user.dto.SearchUserResponse;
import org.chatapp.e2eechatserver.user.entity.User;
import org.chatapp.e2eechatserver.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestTestClient
class UserControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @MockitoBean
    private UserService userService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_givenAuthenticatedUser_whenGetCurrentUser_thenReturnsCurrentUser() {
        // given
        authenticatedUser();

        // when / then
        restTestClient.get()
                .uri("/api/users/me")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .value(currentUser -> {
                    org.assertj.core.api.Assertions.assertThat(currentUser.getId()).isEqualTo(1L);
                    org.assertj.core.api.Assertions.assertThat(currentUser.getEmail()).isEqualTo("alice@example.com");
                    org.assertj.core.api.Assertions.assertThat(currentUser.getUsername()).isEqualTo("alice");
                    org.assertj.core.api.Assertions.assertThat(currentUser.getPubEcdhJwk()).isEqualTo("alice-public-key");
                });
    }

    @Test
    void search_givenQuery_whenSearch_thenReturnsMatchingUsers() {
        // given
        List<SearchUserResponse> response = List.of(
                new SearchUserResponse(1L, "alice"),
                new SearchUserResponse(2L, "alicia")
        );

        when(userService.searchForUsers("ali")).thenReturn(response);

        // when / then
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/users/search")
                        .queryParam("q", "ali")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<SearchUserResponse>>() {})
                .isEqualTo(response);

        verify(userService).searchForUsers("ali");
    }

    @Test
    void getPublicEcdhJwkForUser_givenUserId_whenGetPublicEcdhJwkForUser_thenReturnsPublicKey() {
        // given
        when(userService.getPublicEcdhJwkForUser(1L)).thenReturn("alice-public-key");

        // when / then
        restTestClient.get()
                .uri("/api/users/{userId}/public-key", 1L)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("alice-public-key");

        verify(userService).getPublicEcdhJwkForUser(1L);
    }

    private static User authenticatedUser() {
        User user = user();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
        );

        return user;
    }

    private static User user() {
        User user = new User();
        user.setId(1L);
        user.setEmail("alice@example.com");
        user.setUsername("alice");
        user.setPassword("encoded-password");
        user.setPubEcdhJwk("alice-public-key");
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
