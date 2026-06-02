package org.chatapp.e2eechatserver.conversation.api;

import org.chatapp.e2eechatserver.common.security.CurrentUserService;
import org.chatapp.e2eechatserver.conversation.dto.*;
import org.chatapp.e2eechatserver.conversation.entity.MessageType;
import org.chatapp.e2eechatserver.conversation.entity.RoomType;
import org.chatapp.e2eechatserver.conversation.service.MessageService;
import org.chatapp.e2eechatserver.conversation.service.RoomMemberService;
import org.chatapp.e2eechatserver.conversation.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.security.Principal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(RoomsController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestTestClient
class RoomsControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @MockitoBean
    private RoomService roomService;

    @MockitoBean
    private RoomMemberService roomMemberService;

    @MockitoBean
    private CurrentUserService currentUserService;

    @MockitoBean
    private MessageService messageService;

    @Test
    void createOrGetPrivateRoom_givenRequest_whenCreateOrGetPrivateRoom_thenReturnsRoomDataResponse() {
        // given
        CreateDmRequest request = new CreateDmRequest(2L);
        RoomDataResponse response = roomDataResponse(10L, "bob", RoomType.PRIVATE);

        when(currentUserService.getCurrentUserId(nullable(Principal.class))).thenReturn(1L);
        when(roomService.createOrGetDm(1L, 2L)).thenReturn(response);

        // when / then
        restTestClient.post()
                .uri("/api/rooms/dm")
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RoomDataResponse.class)
                .isEqualTo(response);

        verify(currentUserService).getCurrentUserId(nullable(Principal.class));
        verify(roomService).createOrGetDm(1L, 2L);
    }

    @Test
    void getRoomData_givenRoomId_whenGetRoomData_thenReturnsRoomDataResponse() {
        // given
        RoomDataResponse response = roomDataResponse(10L, "Test room", RoomType.GROUP);

        when(roomService.getRoomData(eq(10L), nullable(Principal.class))).thenReturn(response);

        // when / then
        restTestClient.get()
                .uri("/api/rooms/{roomId}", 10L)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RoomDataResponse.class)
                .isEqualTo(response);

        verify(roomService).getRoomData(eq(10L), nullable(Principal.class));
    }

    @Test
    void recentConversations_givenRequest_whenRecentConversations_thenReturnsRoomDataResponseList() {
        // given
        List<RoomDataResponse> response = List.of(
                roomDataResponse(10L, "bob", RoomType.PRIVATE),
                roomDataResponse(11L, "Team", RoomType.GROUP)
        );

        when(roomService.getRecentConversations(nullable(Principal.class))).thenReturn(response);

        // when / then
        restTestClient.get()
                .uri("/api/rooms/recent")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<RoomDataResponse>>() {})
                .isEqualTo(response);

        verify(roomService).getRecentConversations(nullable(Principal.class));
    }

    @Test
    void loadMessages_givenRoomId_whenLoadMessages_thenReturnsMessages() {
        // given
        List<WsNewMessage> response = List.of(wsNewMessage(100L, 10L));

        when(messageService.loadMessages(10L)).thenReturn(response);

        // when / then
        restTestClient.get()
                .uri("/api/rooms/{roomId}/messages", 10L)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<WsNewMessage>>() {})
                .isEqualTo(response);

        verify(messageService).loadMessages(10L);
    }

    @Test
    void createGroupRoom_givenRequest_whenCreateGroupRoom_thenReturnsRoomDataResponse() {
        // given
        CreateGroupRoomRequest request = new CreateGroupRoomRequest("Team", List.of(2L, 3L));
        RoomDataResponse response = roomDataResponse(10L, "Team", RoomType.GROUP);

        when(roomService.createGroupRoom(eq(request), nullable(Principal.class))).thenReturn(response);

        // when / then
        restTestClient.post()
                .uri("/api/rooms/group")
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RoomDataResponse.class)
                .isEqualTo(response);

        verify(roomService).createGroupRoom(eq(request), nullable(Principal.class));
    }

    @Test
    void updateName_givenRequest_whenUpdateName_thenReturnsRoomDataResponse() {
        // given
        ChangeGroupNameRequest request = new ChangeGroupNameRequest("New name");
        RoomDataResponse response = roomDataResponse(10L, "New name", RoomType.GROUP);

        when(roomService.updateRoomName(eq(10L), eq("New name"), nullable(Principal.class))).thenReturn(response);

        // when / then
        restTestClient.post()
                .uri("/api/rooms/{roomId}/name", 10L)
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RoomDataResponse.class)
                .isEqualTo(response);

        verify(roomService).updateRoomName(eq(10L), eq("New name"), nullable(Principal.class));
    }

    @Test
    void addMember_givenRequest_whenAddMember_thenReturnsRoomDataResponse() {
        // given
        AddNewMemberRequest request = new AddNewMemberRequest(2L);
        RoomDataResponse response = roomDataResponse(10L, "Team", RoomType.GROUP);

        when(roomMemberService.addMemberToGroup(eq(10L), eq(2L), nullable(Principal.class))).thenReturn(response);

        // when / then
        restTestClient.post()
                .uri("/api/rooms/{roomId}/members", 10L)
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RoomDataResponse.class)
                .isEqualTo(response);

        verify(roomMemberService).addMemberToGroup(eq(10L), eq(2L), nullable(Principal.class));
    }

    @Test
    void removeMember_givenRequest_whenRemoveMember_thenReturnsRoomDataResponse() {
        // given
        RemoveMemberRequest request = new RemoveMemberRequest(2L);
        RoomDataResponse response = roomDataResponse(10L, "Team", RoomType.GROUP);

        when(roomMemberService.removeMemberFromGroup(eq(10L), eq(2L), nullable(Principal.class))).thenReturn(response);

        // when / then
        restTestClient.method(HttpMethod.DELETE)
                .uri("/api/rooms/{roomId}/members", 10L)
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RoomDataResponse.class)
                .isEqualTo(response);

        verify(roomMemberService).removeMemberFromGroup(eq(10L), eq(2L), nullable(Principal.class));
    }

    @Test
    void leaveGroup_givenRoomId_whenLeaveGroup_thenReturnsRoomDataResponse() {
        // given
        RoomDataResponse response = roomDataResponse(10L, "Team", RoomType.GROUP);
        response.setRekeyRequired(true);

        when(roomMemberService.leaveGroup(eq(10L), nullable(Principal.class))).thenReturn(response);

        // when / then
        restTestClient.delete()
                .uri("/api/rooms/{roomId}/leave", 10L)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RoomDataResponse.class)
                .isEqualTo(response);

        verify(roomMemberService).leaveGroup(eq(10L), nullable(Principal.class));
    }

    private static RoomDataResponse roomDataResponse(Long roomId, String roomName, RoomType roomType) {
        return RoomDataResponse.builder()
                .roomId(roomId)
                .roomName(roomName)
                .roomType(roomType.name())
                .currentKeyVersion(1)
                .rekeyRequired(false)
                .activeMembership(true)
                .roomMembersDataList(List.of())
                .build();
    }

    private static WsNewMessage wsNewMessage(Long id, Long roomId) {
        return WsNewMessage.builder()
                .id(id)
                .roomId(roomId)
                .senderId(1L)
                .sender("alice")
                .createdAt(Instant.parse("2026-01-01T10:00:00Z"))
                .keyVersion(1)
                .ciphertextB64("ciphertext")
                .ivB64("iv")
                .aadB64("aad")
                .type(MessageType.CHAT.name())
                .build();
    }
}
