package org.chatapp.e2eechatserver.conversation.service;

import org.chatapp.e2eechatserver.common.exception.RoomNotFoundException;
import org.chatapp.e2eechatserver.common.security.CurrentUserService;
import org.chatapp.e2eechatserver.conversation.dto.CreateGroupRoomRequest;
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
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomMemberRepository roomMemberRepository;

    @Mock
    private UserService userService;

    @Mock
    private RoomMapper roomMapper;

    @Mock
    private RoomAccessService roomAccessService;

    @Mock
    private RoomNotificationService roomNotificationService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private RoomService roomService;

    @Test
    void createOrGetDm_givenExistingPrivateRoom_whenCreateOrGetDm_thenReturnsMappedRoomData() {
        // given
        Long myUserId = 1L;
        Long otherUserId = 2L;
        Room room = room(10L, RoomType.PRIVATE, null);
        RoomDataResponse expectedResponse = roomDataResponse(10L, "bob", RoomType.PRIVATE);

        when(roomRepository.findPrivateRoomForTwoUsers(RoomType.PRIVATE, myUserId, otherUserId))
                .thenReturn(Optional.of(room));
        when(roomMapper.toRoomDataResponse(room, myUserId)).thenReturn(expectedResponse);

        // when
        RoomDataResponse response = roomService.createOrGetDm(myUserId, otherUserId);

        // then
        assertThat(response).isSameAs(expectedResponse);
        verify(roomMapper).toRoomDataResponse(room, myUserId);
        verify(roomRepository, never()).save(any(Room.class));
        verifyNoInteractions(roomMemberRepository, roomNotificationService);
    }

    @Test
    void createOrGetDm_givenMissingPrivateRoom_whenCreateOrGetDm_thenCreatesMembersPublishesNotificationAndReturnsCurrentUserRoomData() {
        // given
        Long myUserId = 1L;
        Long otherUserId = 2L;
        RoomDataResponse myResponse = roomDataResponse(10L, "bob", RoomType.PRIVATE);
        RoomDataResponse otherResponse = roomDataResponse(10L, "alice", RoomType.PRIVATE);
        RoomDataResponse returnedResponse = roomDataResponse(10L, "bob", RoomType.PRIVATE);

        when(roomRepository.findPrivateRoomForTwoUsers(RoomType.PRIVATE, myUserId, otherUserId))
                .thenReturn(Optional.empty());
        when(userService.getUsernameByUserId(myUserId)).thenReturn("alice");
        when(userService.getUsernameByUserId(otherUserId)).thenReturn("bob");
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> {
            Room room = invocation.getArgument(0);
            room.setRoomId(10L);
            return room;
        });
        when(roomMapper.toRoomDataResponse(any(Room.class), org.mockito.ArgumentMatchers.eq(myUserId)))
                .thenReturn(myResponse, returnedResponse);
        when(roomMapper.toRoomDataResponse(any(Room.class), org.mockito.ArgumentMatchers.eq(otherUserId)))
                .thenReturn(otherResponse);

        // when
        RoomDataResponse response = roomService.createOrGetDm(myUserId, otherUserId);

        // then
        assertThat(response).isSameAs(returnedResponse);

        ArgumentCaptor<RoomMember> roomMemberCaptor = ArgumentCaptor.forClass(RoomMember.class);
        verify(roomMemberRepository, org.mockito.Mockito.times(2)).save(roomMemberCaptor.capture());
        assertThat(roomMemberCaptor.getAllValues())
                .extracting(RoomMember::getRoomId, RoomMember::getUserId, RoomMember::getMemberRole)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(10L, myUserId, MemberRole.MEMBER),
                        org.assertj.core.groups.Tuple.tuple(10L, otherUserId, MemberRole.MEMBER)
                );

        ArgumentCaptor<Map<String, RoomDataResponse>> notificationCaptor = ArgumentCaptor.forClass(Map.class);
        verify(roomNotificationService).publishPrivateConversationCreated(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue())
                .containsEntry("alice", myResponse)
                .containsEntry("bob", otherResponse);
    }

    @Test
    void getRoomData_givenExistingRoom_whenGetRoomData_thenReturnsMappedRoomDataForCurrentUser() {
        // given
        Long roomId = 10L;
        Long currentUserId = 1L;
        Principal principal = () -> "alice";
        Room room = room(roomId, RoomType.GROUP, "Test room");
        RoomDataResponse expectedResponse = roomDataResponse(roomId, "Test room", RoomType.GROUP);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(currentUserService.getCurrentUserId(principal)).thenReturn(currentUserId);
        when(roomMapper.toRoomDataResponse(room, currentUserId)).thenReturn(expectedResponse);

        // when
        RoomDataResponse response = roomService.getRoomData(roomId, principal);

        // then
        assertThat(response).isSameAs(expectedResponse);
        verify(roomMapper).toRoomDataResponse(room, currentUserId);
    }

    @Test
    void getRoomData_givenMissingRoom_whenGetRoomData_thenThrowsRoomNotFoundException() {
        // given
        Long roomId = 10L;
        Principal principal = () -> "alice";

        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> roomService.getRoomData(roomId, principal))
                .isInstanceOf(RoomNotFoundException.class)
                .hasMessage("Room with that id has not been found");

        verifyNoInteractions(currentUserService, roomMapper);
    }

    @Test
    void updateRoomName_givenExistingRoomAndGroupAdmin_whenUpdateRoomName_thenUpdatesNameSavesRoomPublishesNotificationAndReturnsRoomData() {
        // given
        Long roomId = 10L;
        Long currentUserId = 1L;
        Principal principal = () -> "alice";
        Room room = room(roomId, RoomType.GROUP, "Old name");
        RoomDataResponse expectedResponse = roomDataResponse(roomId, "New name", RoomType.GROUP);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(currentUserService.getCurrentUserId(principal)).thenReturn(currentUserId);
        when(currentUserService.getCurrentUsername(principal)).thenReturn("alice");
        when(roomMapper.toRoomDataResponse(room, currentUserId)).thenReturn(expectedResponse);

        // when
        RoomDataResponse response = roomService.updateRoomName(roomId, "New name", principal);

        // then
        assertThat(response).isSameAs(expectedResponse);
        assertThat(room.getName()).isEqualTo("New name");
        verify(roomAccessService).assertGroupAdmin(roomId, currentUserId);
        verify(roomRepository).save(room);
        verify(roomNotificationService).publishGroupNameChanged(expectedResponse, "alice");
    }

    @Test
    void getRecentConversations_givenCurrentUser_whenGetRecentConversations_thenReturnsMappedRoomsForCurrentUser() {
        // given
        Long currentUserId = 1L;
        Principal principal = () -> "alice";
        Room firstRoom = room(10L, RoomType.PRIVATE, null);
        Room secondRoom = room(11L, RoomType.GROUP, "Team");
        RoomDataResponse firstResponse = roomDataResponse(10L, "bob", RoomType.PRIVATE);
        RoomDataResponse secondResponse = roomDataResponse(11L, "Team", RoomType.GROUP);

        when(currentUserService.getCurrentUserId(principal)).thenReturn(currentUserId);
        when(roomRepository.findByMemberUserId(currentUserId)).thenReturn(List.of(firstRoom, secondRoom));
        when(roomMapper.toRoomDataResponse(firstRoom, currentUserId)).thenReturn(firstResponse);
        when(roomMapper.toRoomDataResponse(secondRoom, currentUserId)).thenReturn(secondResponse);

        // when
        List<RoomDataResponse> response = roomService.getRecentConversations(principal);

        // then
        assertThat(response).containsExactly(firstResponse, secondResponse);
        verify(roomRepository).findByMemberUserId(currentUserId);
    }

    @Test
    void createGroupRoom_givenRequestAndPrincipal_whenCreateGroupRoom_thenCreatesRoomMembersPublishesNotificationAndReturnsRoomData() {
        // given
        Principal principal = () -> "alice";
        User groupOwner = user(1L, "alice");
        CreateGroupRoomRequest request = new CreateGroupRoomRequest("Team", List.of(2L, 3L));
        RoomDataResponse expectedResponse = roomDataResponse(10L, "Team", RoomType.GROUP);

        when(currentUserService.getCurrentUser(principal)).thenReturn(groupOwner);
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> {
            Room room = invocation.getArgument(0);
            room.setRoomId(10L);
            return room;
        });
        when(roomMapper.toRoomDataResponse(any(Room.class), org.mockito.ArgumentMatchers.eq(groupOwner.getId())))
                .thenReturn(expectedResponse);

        // when
        RoomDataResponse response = roomService.createGroupRoom(request, principal);

        // then
        assertThat(response).isSameAs(expectedResponse);

        ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(roomCaptor.capture());
        assertThat(roomCaptor.getValue().getType()).isEqualTo(RoomType.GROUP);
        assertThat(roomCaptor.getValue().getName()).isEqualTo("Team");

        ArgumentCaptor<RoomMember> roomMemberCaptor = ArgumentCaptor.forClass(RoomMember.class);
        verify(roomMemberRepository, org.mockito.Mockito.times(3)).save(roomMemberCaptor.capture());
        assertThat(roomMemberCaptor.getAllValues())
                .extracting(RoomMember::getRoomId, RoomMember::getUserId, RoomMember::getMemberRole)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(10L, 1L, MemberRole.ADMIN),
                        org.assertj.core.groups.Tuple.tuple(10L, 2L, MemberRole.MEMBER),
                        org.assertj.core.groups.Tuple.tuple(10L, 3L, MemberRole.MEMBER)
                );
        verify(roomNotificationService).publishGroupConversationCreated(expectedResponse);
    }

    private static Room room(Long roomId, RoomType roomType, String name) {
        Room room = new Room();
        room.setRoomId(roomId);
        room.setType(roomType);
        room.setName(name);
        room.setCurrentKeyVersion(1);

        return room;
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

    private static User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setEmail(username + "@example.com");
        user.setUsername(username);
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
