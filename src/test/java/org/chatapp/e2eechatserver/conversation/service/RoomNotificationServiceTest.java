package org.chatapp.e2eechatserver.conversation.service;

import org.chatapp.e2eechatserver.conversation.dto.RoomDataResponse;
import org.chatapp.e2eechatserver.conversation.dto.RoomMemberData;
import org.chatapp.e2eechatserver.conversation.dto.UploadRoomKeysRequest;
import org.chatapp.e2eechatserver.conversation.dto.WsNewMessage;
import org.chatapp.e2eechatserver.conversation.entity.MessageEntity;
import org.chatapp.e2eechatserver.conversation.entity.MessageType;
import org.chatapp.e2eechatserver.conversation.entity.RoomType;
import org.chatapp.e2eechatserver.conversation.mapper.MessageMapper;
import org.chatapp.e2eechatserver.user.service.UserService;
import org.chatapp.e2eechatserver.ws.WsEventPublisher;
import org.chatapp.e2eechatserver.ws.WsEventType;
import org.chatapp.e2eechatserver.ws.events.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomNotificationServiceTest {

    @Mock
    private WsEventPublisher eventPublisher;

    @Mock
    private MessageService messageService;

    @Mock
    private UserService userService;

    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private RoomNotificationService roomNotificationService;

    @Test
    void publishPrivateConversationCreated_givenRoomDataByUsername_whenPublishPrivateConversationCreated_thenSendsConversationCreatedEventToEachUser() {
        // given
        RoomDataResponse aliceRoomData = roomDataResponse(10L, "alice-room", false);
        RoomDataResponse bobRoomData = roomDataResponse(10L, "bob-room", false);

        // when
        roomNotificationService.publishPrivateConversationCreated(Map.of(
                "alice", aliceRoomData,
                "bob", bobRoomData
        ));

        // then
        verify(eventPublisher).sendToUser(eq("alice"), argThat(event ->
                WsEventType.CONVERSATION_CREATED.equals(event.type())
                        && event.payload() instanceof ConversationCreatedPayload payload
                        && payload.roomData() == aliceRoomData));
        verify(eventPublisher).sendToUser(eq("bob"), argThat(event ->
                WsEventType.CONVERSATION_CREATED.equals(event.type())
                        && event.payload() instanceof ConversationCreatedPayload payload
                        && payload.roomData() == bobRoomData));
    }

    @Test
    void publishGroupConversationCreated_givenRoomData_whenPublishGroupConversationCreated_thenSendsConversationCreatedEventToRoomMembers() {
        // given
        RoomDataResponse roomData = roomDataResponse(10L, "Test room", false);

        // when
        roomNotificationService.publishGroupConversationCreated(roomData);

        // then
        verify(eventPublisher).sendToUsers(eq(List.of("alice", "bob")), argThat(event ->
                WsEventType.CONVERSATION_CREATED.equals(event.type())
                        && event.payload() instanceof ConversationCreatedPayload payload
                        && payload.roomData() == roomData));
    }

    @Test
    void publishMemberAdded_givenRoomDataAndUsernames_whenPublishMemberAdded_thenNotifiesMembersAndPublishesSystemMessage() {
        // given
        RoomDataResponse roomData = roomDataResponse(10L, "Test room", false);
        MessageEntity systemMessage = systemMessage(100L, roomData.getRoomId());
        WsNewMessage wsNewMessage = wsNewMessage(systemMessage);

        when(messageService.createSystemMessage(10L, "charlie has been added to the group by alice"))
                .thenReturn(systemMessage);
        when(messageMapper.toWsNewMessage(systemMessage)).thenReturn(wsNewMessage);

        // when
        roomNotificationService.publishMemberAdded(roomData, "charlie", "alice");

        // then
        verify(eventPublisher).sendToUsers(eq(List.of("alice", "bob")), argThat(event ->
                WsEventType.GROUP_MEMBER_ADDED.equals(event.type())
                        && event.payload() instanceof GroupMemberAddedPayload payload
                        && payload.roomData() == roomData));
        verify(messageService).createSystemMessage(10L, "charlie has been added to the group by alice");
        verifyNewMessageCreated(systemMessage, wsNewMessage);
    }

    @Test
    void publishMemberRemoved_givenRoomDataAndUsernames_whenPublishMemberRemoved_thenPublishesSystemMessageAndNotifiesMembersAndRemovedUser() {
        // given
        RoomDataResponse roomData = roomDataResponse(10L, "Test room", false);
        MessageEntity systemMessage = systemMessage(100L, roomData.getRoomId());
        WsNewMessage wsNewMessage = wsNewMessage(systemMessage);

        when(messageService.createSystemMessage(10L, "charlie has been removed from the group by alice"))
                .thenReturn(systemMessage);
        when(messageMapper.toWsNewMessage(systemMessage)).thenReturn(wsNewMessage);

        // when
        roomNotificationService.publishMemberRemoved(roomData, "charlie", "alice");

        // then
        verify(messageService).createSystemMessage(10L, "charlie has been removed from the group by alice");
        verifyNewMessageCreated(systemMessage, wsNewMessage);
        verify(eventPublisher).sendToUsers(eq(List.of("alice", "bob")), argThat(event ->
                WsEventType.GROUP_MEMBER_REMOVED.equals(event.type())
                        && event.payload() instanceof GroupMemberRemovedPayload payload
                        && payload.roomId().equals(10L)
                        && payload.removedUsername().equals("charlie")));
        verify(eventPublisher).sendToUser(eq("charlie"), argThat(event ->
                WsEventType.REMOVED_FROM_GROUP.equals(event.type())
                        && event.payload() instanceof RemovedFromGroupPayload payload
                        && payload.roomId().equals(10L)));
    }

    @Test
    void publishMemberLeft_givenRoomDataAndUsername_whenPublishMemberLeft_thenPublishesSystemMessageAndNotifiesMembers() {
        // given
        RoomDataResponse roomData = roomDataResponse(10L, "Test room", true);
        MessageEntity systemMessage = systemMessage(100L, roomData.getRoomId());
        WsNewMessage wsNewMessage = wsNewMessage(systemMessage);

        when(messageService.createSystemMessage(10L, "charlie has left the group"))
                .thenReturn(systemMessage);
        when(messageMapper.toWsNewMessage(systemMessage)).thenReturn(wsNewMessage);

        // when
        roomNotificationService.publishMemberLeft(roomData, "charlie");

        // then
        verify(messageService).createSystemMessage(10L, "charlie has left the group");
        verifyNewMessageCreated(systemMessage, wsNewMessage);
        verify(eventPublisher).sendToUsers(eq(List.of("alice", "bob")), argThat(event ->
                WsEventType.GROUP_MEMBER_LEFT.equals(event.type())
                        && event.payload() instanceof GroupMemberLeftPayload payload
                        && payload.roomId().equals(10L)
                        && payload.leftUsername().equals("charlie")
                        && payload.rekeyRequired()));
    }

    @Test
    void publishGroupNameChanged_givenRoomDataAndInitiator_whenPublishGroupNameChanged_thenPublishesSystemMessageAndNotifiesMembers() {
        // given
        RoomDataResponse roomData = roomDataResponse(10L, "New group name", false);
        MessageEntity systemMessage = systemMessage(100L, roomData.getRoomId());
        WsNewMessage wsNewMessage = wsNewMessage(systemMessage);

        when(messageService.createSystemMessage(10L, "alice has changed group name to New group name"))
                .thenReturn(systemMessage);
        when(messageMapper.toWsNewMessage(systemMessage)).thenReturn(wsNewMessage);

        // when
        roomNotificationService.publishGroupNameChanged(roomData, "alice");

        // then
        verify(messageService).createSystemMessage(10L, "alice has changed group name to New group name");
        verifyNewMessageCreated(systemMessage, wsNewMessage);
        verify(eventPublisher).sendToUsers(eq(List.of("alice", "bob")), argThat(event ->
                WsEventType.GROUP_NAME_CHANGED.equals(event.type())
                        && event.payload() instanceof GroupNameChangedPayload payload
                        && payload.roomId().equals(10L)
                        && payload.newName().equals("New group name")));
    }

    @Test
    void publishRoomKeyUpdated_givenRequest_whenPublishRoomKeyUpdated_thenSendsKeyEnvelopeAvailableEventToEachKeyOwner() {
        // given
        UploadRoomKeysRequest request = new UploadRoomKeysRequest(
                5,
                1L,
                List.of(
                        new UploadRoomKeysRequest.KeyItem(2L, "wrapped-key-2", "iv-2", "aad-2"),
                        new UploadRoomKeysRequest.KeyItem(3L, "wrapped-key-3", "iv-3", "aad-3")
                )
        );

        when(userService.getUsernameByUserId(2L)).thenReturn("bob");
        when(userService.getUsernameByUserId(3L)).thenReturn("charlie");

        // when
        roomNotificationService.publishRoomKeyUpdated(request, 10L);

        // then
        verify(eventPublisher).sendToUser(eq("bob"), argThat(event ->
                WsEventType.KEY_ENVELOPE_AVAILABLE.equals(event.type())
                        && event.payload() instanceof KeyEnvelopeAvailablePayload payload
                        && payload.roomId().equals(10L)
                        && payload.version().equals(5)));
        verify(eventPublisher).sendToUser(eq("charlie"), argThat(event ->
                WsEventType.KEY_ENVELOPE_AVAILABLE.equals(event.type())
                        && event.payload() instanceof KeyEnvelopeAvailablePayload payload
                        && payload.roomId().equals(10L)
                        && payload.version().equals(5)));
    }

    @Test
    void publishNewMessageCreated_givenRoomMembersAndMessage_whenPublishNewMessageCreated_thenSendsMessageToRoomTopicAndInfoToMembers() {
        // given
        MessageEntity message = systemMessage(100L, 10L);
        WsNewMessage wsNewMessage = wsNewMessage(message);

        when(messageMapper.toWsNewMessage(message)).thenReturn(wsNewMessage);

        // when
        roomNotificationService.publishNewMessageCreated(10L, roomMembers(), message);

        // then
        verifyNewMessageCreated(message, wsNewMessage);
    }

    private void verifyNewMessageCreated(MessageEntity message, WsNewMessage wsNewMessage) {
        verify(messageMapper).toWsNewMessage(message);
        verify(eventPublisher).sendToRoomTopic(eq(10L), argThat(event ->
                WsEventType.MESSAGE_CREATED.equals(event.type())
                        && event.payload() == wsNewMessage));
        verify(eventPublisher).sendToUsers(eq(List.of("alice", "bob")), argThat(event ->
                WsEventType.MESSAGE_CREATED_INFO.equals(event.type())
                        && event.payload() instanceof MessageCreatedInfoPayload payload
                        && payload.roomId().equals(10L)));
    }

    private static RoomDataResponse roomDataResponse(Long roomId, String roomName, boolean rekeyRequired) {
        return RoomDataResponse.builder()
                .roomId(roomId)
                .roomName(roomName)
                .roomType(RoomType.GROUP.name())
                .currentKeyVersion(1)
                .rekeyRequired(rekeyRequired)
                .activeMembership(true)
                .roomMembersDataList(roomMembers())
                .build();
    }

    private static List<RoomMemberData> roomMembers() {
        return List.of(
                new RoomMemberData(1L, 1L, "alice", "ADMIN", "alice-public-key"),
                new RoomMemberData(2L, 2L, "bob", "MEMBER", "bob-public-key")
        );
    }

    private static MessageEntity systemMessage(Long id, Long roomId) {
        MessageEntity message = new MessageEntity();
        message.setId(id);
        message.setRoomId(roomId);
        message.setType(MessageType.SYSTEM);
        message.setSystemText("system message");
        message.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z"));

        return message;
    }

    private static WsNewMessage wsNewMessage(MessageEntity message) {
        return WsNewMessage.builder()
                .id(message.getId())
                .roomId(message.getRoomId())
                .createdAt(message.getCreatedAt())
                .type(MessageType.SYSTEM.name())
                .systemText(message.getSystemText())
                .build();
    }
}
