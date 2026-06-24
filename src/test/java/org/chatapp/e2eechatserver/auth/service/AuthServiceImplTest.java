package org.chatapp.e2eechatserver.auth.service;

import org.chatapp.e2eechatserver.auth.dto.LoginUserRequest;
import org.chatapp.e2eechatserver.auth.dto.LoginUserResponse;
import org.chatapp.e2eechatserver.auth.dto.RegisterUserRequest;
import org.chatapp.e2eechatserver.auth.dto.VaultDto;
import org.chatapp.e2eechatserver.auth.mapper.AuthMapper;
import org.chatapp.e2eechatserver.auth.validation.RegisterUserValidator;
import org.chatapp.e2eechatserver.common.dto.FieldErrorDto;
import org.chatapp.e2eechatserver.common.exception.ValidationException;
import org.chatapp.e2eechatserver.security.jwt.JwtTokenUtil;
import org.chatapp.e2eechatserver.user.entity.User;
import org.chatapp.e2eechatserver.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private RegisterUserValidator registerUserValidator;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void signUp_givenValidRegisterRequest_whenSignUp_thenValidatesMapsAndSavesUser() {
        // given
        RegisterUserRequest request = registerUserRequest();
        User user = user();

        when(authMapper.toUser(request)).thenReturn(user);

        // when
        authService.signUp(request);

        // then
        verify(registerUserValidator).validate(request);
        verify(authMapper).toUser(request);
        verify(userRepository).save(user);
    }

    @Test
    void signUp_givenInvalidRegisterRequest_whenSignUp_thenThrowsValidationExceptionAndDoesNotSaveUser() {
        // given
        RegisterUserRequest request = registerUserRequest();
        ValidationException exception = new ValidationException(
                "Validation failed",
                List.of(new FieldErrorDto("email", "Email already exists"))
        );

        doThrow(exception).when(registerUserValidator).validate(request);

        // when / then
        assertThatThrownBy(() -> authService.signUp(request))
                .isSameAs(exception);

        verify(authMapper, never()).toUser(any(RegisterUserRequest.class));
        verify(userRepository, never()).save(any(User.class));
    }

    /*@Test
    void login_givenValidLoginRequest_whenLogin_thenAuthenticatesGeneratesTokensAndReturnsMappedResponse() {
        // given
        LoginUserRequest request = new LoginUserRequest("testuser", "password123");
        User user = user();
        LoginUserResponse expectedResponse = loginUserResponse();

        UsernamePasswordAuthenticationToken authenticatedToken =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticatedToken);
        when(jwtTokenUtil.generateAccessToken(user)).thenReturn("access-token");
        when(jwtTokenUtil.generateRefreshToken(user)).thenReturn("refresh-token");
        when(authMapper.toLoginUserResponse(user, "access-token"))
                .thenReturn(expectedResponse);

        // when
        LoginUserResponse response = authService.login(request);

        // then
        assertThat(response).isSameAs(expectedResponse);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(tokenCaptor.capture());

        UsernamePasswordAuthenticationToken token = tokenCaptor.getValue();
        assertThat(token.getPrincipal()).isEqualTo("testuser");
        assertThat(token.getCredentials()).isEqualTo("password123");

        verify(jwtTokenUtil).generateAccessToken(user);
        verify(authMapper).toLoginUserResponse(user, "access-token");
    }*/

    @Test
    void login_givenAuthenticationFailure_whenLogin_thenPropagatesExceptionAndDoesNotGenerateTokens() {
        // given
        LoginUserRequest request = new LoginUserRequest("testuser", "wrong-password");
        RuntimeException exception = new RuntimeException("Bad credentials");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(exception);

        // when / then
        assertThatThrownBy(() -> authService.login(request))
                .isSameAs(exception);

        verifyNoInteractions(jwtTokenUtil, authMapper);
    }

    private static RegisterUserRequest registerUserRequest() {
        return new RegisterUserRequest(
                "test@example.com",
                "testuser",
                "password123",
                "password123",
                "{\"kty\":\"EC\"}",
                new VaultDto(
                        1,
                        "salt",
                        10000,
                        "wrappedMk",
                        "wrappedMkIv",
                        "wrappedEcdhPriv",
                        "wrappedEcdhPrivIv"
                )
        );
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

    private static LoginUserResponse loginUserResponse() {
        return new LoginUserResponse(
                "access-token",
                1L,
                "testuser",
                "{\"kty\":\"EC\"}",
                new VaultDto(
                        1,
                        "salt",
                        10000,
                        "wrappedMk",
                        "wrappedMkIv",
                        "wrappedEcdhPriv",
                        "wrappedEcdhPrivIv"
                )
        );
    }
}
