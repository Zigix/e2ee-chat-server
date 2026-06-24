package org.chatapp.e2eechatserver.conversation.api;

import org.chatapp.e2eechatserver.conversation.dto.MyKeyResponse;
import org.chatapp.e2eechatserver.conversation.dto.RoomDataResponse;
import org.chatapp.e2eechatserver.conversation.dto.UploadRoomKeysRequest;
import org.chatapp.e2eechatserver.conversation.entity.RoomType;
import org.chatapp.e2eechatserver.conversation.service.RoomKeyService;
import org.chatapp.e2eechatserver.user.entity.User;
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

import java.security.Principal;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(RoomKeysController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestTestClient
class RoomKeysControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @MockitoBean
    private RoomKeyService roomKeyService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void upload_givenValidRequestAndAuthenticatedUser_whenUpload_thenReturnsNoContentAndUploadsKeys() {
        // given
        User user = authenticatedUser();
        UploadRoomKeysRequest request = uploadRoomKeysRequest();

        // when / then
        restTestClient.post()
                .uri("/api/rooms/{roomId}/keys/upload", 10L)
                .body(request)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        verify(roomKeyService).uploadKeys(user.getId(), 10L, request);
    }

    @Test
    void rekey_givenValidRequest_whenRekey_thenReturnsRoomDataResponseAndUploadsPendingRekeyKeys() {
        // given
        UploadRoomKeysRequest request = uploadRoomKeysRequest();
        RoomDataResponse response = roomDataResponse();

        when(roomKeyService.uploadPendingRekeyKeys(eq(10L), eq(request), nullable(Principal.class)))
                .thenReturn(response);

        // when / then
        restTestClient.post()
                .uri("/api/rooms/{roomId}/keys/rekey", 10L)
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RoomDataResponse.class)
                .isEqualTo(response);

        verify(roomKeyService).uploadPendingRekeyKeys(eq(10L), eq(request), nullable(Principal.class));
    }

    @Test
    void myKey_givenAuthenticatedUserAndVersion_whenMyKey_thenReturnsMyKeyResponse() {
        // given
        User user = authenticatedUser();
        MyKeyResponse response = myKeyResponse(5);

        when(roomKeyService.getMyKeyWithVersion(user.getId(), 10L, 5)).thenReturn(response);

        // when / then
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/rooms/{roomId}/my-key")
                        .queryParam("version", 5)
                        .build(10L))
                .exchange()
                .expectStatus().isOk()
                .expectBody(MyKeyResponse.class)
                .isEqualTo(response);

        verify(roomKeyService).getMyKeyWithVersion(user.getId(), 10L, 5);
    }

    @Test
    void myKeys_givenAuthenticatedUser_whenMyKeys_thenReturnsAllMyKeysForRoom() {
        // given
        User user = authenticatedUser();
        List<MyKeyResponse> response = List.of(myKeyResponse(4), myKeyResponse(5));

        when(roomKeyService.getAllMyKeysForRoom(user.getId(), 10L)).thenReturn(response);

        // when / then
        restTestClient.get()
                .uri("/api/rooms/{roomId}/my-keys", 10L)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<MyKeyResponse>>() {})
                .isEqualTo(response);

        verify(roomKeyService).getAllMyKeysForRoom(user.getId(), 10L);
    }

    private static User authenticatedUser() {
        User user = user();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
        );

        return user;
    }

    private static UploadRoomKeysRequest uploadRoomKeysRequest() {
        return new UploadRoomKeysRequest(
                5,
                1L,
                List.of(
                        new UploadRoomKeysRequest.KeyItem(2L, "wrapped-room-key-2", "iv-2", "aad-2"),
                        new UploadRoomKeysRequest.KeyItem(3L, "wrapped-room-key-3", "iv-3", "aad-3")
                )
        );
    }

    private static MyKeyResponse myKeyResponse(int version) {
        return MyKeyResponse.builder()
                .roomId(10L)
                .version(version)
                .wrappedByUserId(1L)
                .wrappedRoomKeyB64("wrapped-room-key")
                .ivB64("iv")
                .aadB64("aad")
                .build();
    }

    private static RoomDataResponse roomDataResponse() {
        return RoomDataResponse.builder()
                .roomId(10L)
                .roomName("Test room")
                .roomType(RoomType.GROUP.name())
                .currentKeyVersion(5)
                .rekeyRequired(false)
                .activeMembership(true)
                .roomMembersDataList(List.of())
                .build();
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
