package org.chatapp.e2eechatserver.conversation.service;

import org.chatapp.e2eechatserver.common.exception.RoomNotFoundException;
import org.chatapp.e2eechatserver.common.security.CurrentUserService;
import org.chatapp.e2eechatserver.conversation.dto.RoomDataResponse;
import org.chatapp.e2eechatserver.conversation.entity.MemberRole;
import org.chatapp.e2eechatserver.conversation.entity.Room;
import org.chatapp.e2eechatserver.conversation.entity.RoomMember;
import org.chatapp.e2eechatserver.conversation.entity.RoomType;
import org.chatapp.e2eechatserver.conversation.mapper.RoomMapper;
import org.chatapp.e2eechatserver.conversation.repository.RoomMemberRepository;
import org.chatapp.e2eechatserver.conversation.repository.RoomRepository;
import org.chatapp.e2eechatserver.user.entity.User;
import org.chatapp.e2eechatserver.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomMemberServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomMemberRepository roomMemberRepository;

    @Mock
    private UserService userService;

    @Mock
    private RoomAccessService roomAccessService;

    @Mock
    private RoomMapper roomMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private RoomNotificationService roomNotificationService;

    @InjectMocks
    private RoomMemberService roomMemberService;

    @Test
    void addMemberToGroup_givenExistingInactiveMember_whenAddMemberToGroup_thenReactivatesMemberIncrementsKeyVersionAndPublishesNotification() {
        // given
        Long roomId = 10L;
        Long addedUserId = 2L;
        Long currentUserId = 1L;
        Principal principal = () -> "admin";
        Room room = room(roomId, 4);
        RoomMember existingMember = new RoomMember(roomId, addedUserId, MemberRole.MEMBER);
        existingMember.setActive(false);
        RoomDataResponse expectedResponse = roomDataResponse(roomId, 5);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(currentUserService.getCurrentUserId(principal)).thenReturn(currentUserId);
        when(roomMemberRepository.findByRoomIdAndUserId(roomId, addedUserId)).thenReturn(Optional.of(existingMember));
        when(currentUserService.getCurrentUsername(principal)).thenReturn("admin");
        when(userService.getUsernameByUserId(addedUserId)).thenReturn("new-member");
        when(roomMapper.toRoomDataResponse(room, currentUserId)).thenReturn(expectedResponse);

        // when
        RoomDataResponse response = roomMemberService.addMemberToGroup(roomId, addedUserId, principal);

        // then
        assertThat(response).isSameAs(expectedResponse);
        assertThat(existingMember.isActive()).isTrue();
        assertThat(room.getCurrentKeyVersion()).isEqualTo(5);

        verify(roomAccessService).assertActiveMember(roomId, currentUserId);
        verify(roomMemberRepository).save(existingMember);
        verify(roomNotificationService).publishMemberAdded(expectedResponse, "new-member", "admin");
    }

    @Test
    void addMemberToGroup_givenNewMember_whenAddMemberToGroup_thenCreatesMemberIncrementsKeyVersionAndPublishesNotification() {
        // given
        Long roomId = 10L;
        Long addedUserId = 2L;
        Long currentUserId = 1L;
        Principal principal = () -> "admin";
        Room room = room(roomId, 4);
        RoomDataResponse expectedResponse = roomDataResponse(roomId, 5);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(currentUserService.getCurrentUserId(principal)).thenReturn(currentUserId);
        when(roomMemberRepository.findByRoomIdAndUserId(roomId, addedUserId)).thenReturn(Optional.empty());
        when(currentUserService.getCurrentUsername(principal)).thenReturn("admin");
        when(userService.getUsernameByUserId(addedUserId)).thenReturn("new-member");
        when(roomMapper.toRoomDataResponse(room, currentUserId)).thenReturn(expectedResponse);

        // when
        RoomDataResponse response = roomMemberService.addMemberToGroup(roomId, addedUserId, principal);

        // then
        assertThat(response).isSameAs(expectedResponse);
        assertThat(room.getCurrentKeyVersion()).isEqualTo(5);

        ArgumentCaptor<RoomMember> roomMemberCaptor = ArgumentCaptor.forClass(RoomMember.class);
        verify(roomMemberRepository).save(roomMemberCaptor.capture());

        RoomMember savedMember = roomMemberCaptor.getValue();
        assertThat(savedMember.getRoomId()).isEqualTo(roomId);
        assertThat(savedMember.getUserId()).isEqualTo(addedUserId);
        assertThat(savedMember.getMemberRole()).isEqualTo(MemberRole.MEMBER);
        assertThat(savedMember.isActive()).isTrue();

        verify(roomAccessService).assertActiveMember(roomId, currentUserId);
        verify(roomNotificationService).publishMemberAdded(expectedResponse, "new-member", "admin");
    }

    @Test
    void addMemberToGroup_givenMissingRoom_whenAddMemberToGroup_thenThrowsRoomNotFoundException() {
        // given
        Long roomId = 10L;
        Principal principal = () -> "admin";

        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> roomMemberService.addMemberToGroup(roomId, 2L, principal))
                .isInstanceOf(RoomNotFoundException.class)
                .hasMessage("Room with that id has not been found");

        verifyNoInteractions(roomAccessService, roomMemberRepository, userService, roomMapper, roomNotificationService);
        verify(currentUserService, never()).getCurrentUserId(principal);
    }

    @Test
    void removeMemberFromGroup_givenExistingMember_whenRemoveMemberFromGroup_thenDeactivatesMemberIncrementsKeyVersionAndPublishesNotification() {
        // given
        Long roomId = 10L;
        Long removedUserId = 2L;
        Long currentUserId = 1L;
        Principal principal = () -> "admin";
        Room room = room(roomId, 4);
        RoomMember removedMember = new RoomMember(roomId, removedUserId, MemberRole.MEMBER);
        RoomDataResponse expectedResponse = roomDataResponse(roomId, 5);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(currentUserService.getCurrentUserId(principal)).thenReturn(currentUserId);
        when(roomMemberRepository.findByRoomIdAndUserId(roomId, removedUserId)).thenReturn(Optional.of(removedMember));
        when(currentUserService.getCurrentUsername(principal)).thenReturn("admin");
        when(userService.getUsernameByUserId(removedUserId)).thenReturn("removed-member");
        when(roomMapper.toRoomDataResponse(room, currentUserId)).thenReturn(expectedResponse);

        // when
        RoomDataResponse response = roomMemberService.removeMemberFromGroup(roomId, removedUserId, principal);

        // then
        assertThat(response).isSameAs(expectedResponse);
        assertThat(removedMember.isActive()).isFalse();
        assertThat(room.getCurrentKeyVersion()).isEqualTo(5);

        verify(roomAccessService).assertGroupAdmin(roomId, currentUserId);
        verify(roomNotificationService).publishMemberRemoved(expectedResponse, "removed-member", "admin");
    }

    @Test
    void leaveGroup_givenCurrentMember_whenLeaveGroup_thenDeactivatesMemberRequiresRekeyAndPublishesNotification() {
        // given
        Long roomId = 10L;
        User leavingUser = user(1L, "leaving-user");
        Principal principal = () -> "leaving-user";
        Room room = room(roomId, 4);
        RoomMember leavingMember = new RoomMember(roomId, leavingUser.getId(), MemberRole.MEMBER);
        RoomDataResponse expectedResponse = roomDataResponse(roomId, 4);
        expectedResponse.setRekeyRequired(true);

        when(currentUserService.getCurrentUser(principal)).thenReturn(leavingUser);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomMemberRepository.findByRoomIdAndUserId(roomId, leavingUser.getId())).thenReturn(Optional.of(leavingMember));
        when(roomMapper.toRoomDataResponse(room, leavingUser.getId())).thenReturn(expectedResponse);

        // when
        RoomDataResponse response = roomMemberService.leaveGroup(roomId, principal);

        // then
        assertThat(response).isSameAs(expectedResponse);
        assertThat(leavingMember.isActive()).isFalse();
        assertThat(room.isRekeyRequired()).isTrue();
        assertThat(room.getCurrentKeyVersion()).isEqualTo(4);

        verify(roomAccessService).assertActiveMember(roomId, leavingUser.getId());
        verify(roomNotificationService).publishMemberLeft(expectedResponse, "leaving-user");
    }

    private static Room room(Long roomId, int currentKeyVersion) {
        Room room = new Room();
        room.setRoomId(roomId);
        room.setType(RoomType.GROUP);
        room.setName("Test room");
        room.setCurrentKeyVersion(currentKeyVersion);

        return room;
    }

    private static RoomDataResponse roomDataResponse(Long roomId, int currentKeyVersion) {
        return RoomDataResponse.builder()
                .roomId(roomId)
                .roomName("Test room")
                .roomType(RoomType.GROUP.name())
                .currentKeyVersion(currentKeyVersion)
                .rekeyRequired(false)
                .activeMembership(true)
                .roomMembersDataList(List.of())
                .build();
    }

    private static User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setEmail(username + "@example.com");
        user.setUsername(username);
        user.setPassword("encoded-password");
        user.setPubEcdhJwk("{\"kty\":\"EC\"}");
        user.setVaultVersion(1);
        user.setVaultSaltBase64("salt");
        user.setVaultIterations(10000);
        user.setWrappedMkB64("wrappedMk");
        user.setWrappedMkIvB64("wrappedMkIv");
        user.setWrappedEcdhPrivB64("wrappedEcdhPriv");
        user.setWrappedEcdhPrivIvB64("wrappedEcdhPrivIv");

        return user;
    }
}
