package org.chatapp.e2eechatserver.auth.api;

import org.chatapp.e2eechatserver.auth.dto.LoginUserRequest;
import org.chatapp.e2eechatserver.auth.dto.LoginUserResponse;
import org.chatapp.e2eechatserver.auth.dto.RegisterUserRequest;
import org.chatapp.e2eechatserver.auth.dto.VaultDto;
import org.chatapp.e2eechatserver.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestTestClient
class AuthControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @MockitoBean
    private AuthService authService;

    @Test
    void signUp_givenValidRequest_whenSignUp_thenReturnsCreated() {
        RegisterUserRequest request = registerUserRequest();

        restTestClient.post()
                .uri("/api/auth/sign-up")
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody().isEmpty();

        verify(authService).signUp(request);
    }

    @Test
    void login_givenValidRequest_whenLogin_thenReturnsLoginResponse() {
        // given
        LoginUserRequest request = new LoginUserRequest("testuser", "password123");
        LoginUserResponse response = new LoginUserResponse(
                "access-token",
                1L,
                "testuser",
                "{\"kty\":\"EC\"}",
                vaultDto()
        );

        when(authService.login(request)).thenReturn(response);

        // when / then
        restTestClient.post()
                .uri("/api/auth/login")
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginUserResponse.class)
                .isEqualTo(response);

        verify(authService).login(request);
    }

    private static RegisterUserRequest registerUserRequest() {
        return new RegisterUserRequest(
                "test@example.com",
                "testuser",
                "password123",
                "password123",
                "{\"kty\":\"EC\"}",
                vaultDto()
        );
    }

    private static VaultDto vaultDto() {
        return new VaultDto(
                1,
                "salt",
                10000,
                "wrappedMk",
                "wrappedMkIv",
                "wrappedEcdhPriv",
                "wrappedEcdhPrivIv"
        );
    }
}
