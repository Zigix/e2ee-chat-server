package org.chatapp.e2eechatserver.conversation.service;

import lombok.RequiredArgsConstructor;
import org.chatapp.e2eechatserver.conversation.dto.RoomDataResponse;
import org.chatapp.e2eechatserver.conversation.dto.RoomMemberData;
import org.chatapp.e2eechatserver.conversation.dto.UploadRoomKeysRequest;
import org.chatapp.e2eechatserver.conversation.dto.WsNewMessage;
import org.chatapp.e2eechatserver.conversation.entity.MessageEntity;
import org.chatapp.e2eechatserver.conversation.mapper.MessageMapper;
import org.chatapp.e2eechatserver.user.service.UserService;
import org.chatapp.e2eechatserver.ws.WsEvent;
import org.chatapp.e2eechatserver.ws.WsEventPublisher;
import org.chatapp.e2eechatserver.ws.WsEventType;
import org.chatapp.e2eechatserver.ws.events.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoomNotificationService {

    private final WsEventPublisher eventPublisher;
    private final MessageService messageService;
    private final UserService userService;
    private final MessageMapper messageMapper;

    public void publishPrivateConversationCreated(Map<String, RoomDataResponse> roomDataMap) {
        for (Map.Entry<String, RoomDataResponse> entry : roomDataMap.entrySet()) {
            notifyUser(entry.getKey(), WsEventType.CONVERSATION_CREATED, new ConversationCreatedPayload(entry.getValue()));
        }
    }

    public void publishGroupConversationCreated(RoomDataResponse roomData) {
        notifyUsers(roomData.getRoomMembersDataList(), WsEventType.CONVERSATION_CREATED, new ConversationCreatedPayload(roomData));
    }

    public void publishMemberAdded(RoomDataResponse roomData, String addedUsername, String initiatorUsername) {
        GroupMemberAddedPayload groupMemberAddedPayload = new GroupMemberAddedPayload(roomData);
        notifyUsers(roomData.getRoomMembersDataList(), WsEventType.GROUP_MEMBER_ADDED, groupMemberAddedPayload);

        MessageEntity systemMessage = messageService.createSystemMessage(
                roomData.getRoomId(),
                String.format("%s has been added to the group by %s", addedUsername, initiatorUsername));
        publishNewMessageCreated(roomData.getRoomId(), roomData.getRoomMembersDataList(), systemMessage);
    }

    public void publishMemberRemoved(RoomDataResponse roomData, String removedUsername, String initiatorUsername) {
        MessageEntity systemMessage = messageService.createSystemMessage(
                roomData.getRoomId(),
                String.format("%s has been removed from the group by %s", removedUsername, initiatorUsername));
        publishNewMessageCreated(roomData.getRoomId(), roomData.getRoomMembersDataList(), systemMessage);

        GroupMemberRemovedPayload groupMemberRemovedPayload = new GroupMemberRemovedPayload(roomData.getRoomId(), removedUsername);
        notifyUsers(roomData.getRoomMembersDataList(), WsEventType.GROUP_MEMBER_REMOVED, groupMemberRemovedPayload);

        RemovedFromGroupPayload removedFromGroupPayload = new RemovedFromGroupPayload(roomData.getRoomId());
        notifyUser(removedUsername, WsEventType.REMOVED_FROM_GROUP, removedFromGroupPayload);
    }

    public void publishMemberLeft(RoomDataResponse roomData, String leftUsername) {
        MessageEntity systemMessage = messageService.createSystemMessage(
                roomData.getRoomId(),
                String.format("%s has left the group", leftUsername));
        publishNewMessageCreated(roomData.getRoomId(), roomData.getRoomMembersDataList(), systemMessage);

        GroupMemberLeftPayload groupMemberLeftPayload = new GroupMemberLeftPayload(roomData.getRoomId(), leftUsername, roomData.isRekeyRequired());
        notifyUsers(roomData.getRoomMembersDataList(), WsEventType.GROUP_MEMBER_LEFT, groupMemberLeftPayload);
    }

    public void publishGroupNameChanged(RoomDataResponse roomData, String initiatorUsername) {
        MessageEntity systemMessage = messageService.createSystemMessage(
                roomData.getRoomId(),
                String.format("%s has changed group name to %s", initiatorUsername, roomData.getRoomName())
        );
        publishNewMessageCreated(roomData.getRoomId(), roomData.getRoomMembersDataList(), systemMessage);

        GroupNameChangedPayload groupNameChangedPayload = new GroupNameChangedPayload(roomData.getRoomId(), roomData.getRoomName());
        notifyUsers(roomData.getRoomMembersDataList(), WsEventType.GROUP_NAME_CHANGED, groupNameChangedPayload);
    }

    public void publishRoomKeyUpdated(UploadRoomKeysRequest request, Long roomId) {
        for (UploadRoomKeysRequest.KeyItem keyItem : request.keyItems()) {
            KeyEnvelopeAvailablePayload payload = new KeyEnvelopeAvailablePayload(roomId, request.version());

            eventPublisher.sendToUser(userService.getUsernameByUserId(keyItem.userId()),
                    new WsEvent<>(WsEventType.KEY_ENVELOPE_AVAILABLE, payload));
        }
    }

    public void publishNewMessageCreated(Long roomId, List<RoomMemberData> members, MessageEntity messageEntity) {
        WsNewMessage wsNewMessage = messageMapper.toWsNewMessage(messageEntity);

        eventPublisher.sendToRoomTopic(roomId, new WsEvent<>(WsEventType.MESSAGE_CREATED, wsNewMessage));

        notifyUsers(members, WsEventType.MESSAGE_CREATED_INFO, new MessageCreatedInfoPayload(roomId));
    }

    private <T> void notifyUsers(List<RoomMemberData> members, String eventType, T event) {
        eventPublisher.sendToUsers(
                members
                        .stream()
                        .map(RoomMemberData::getUsername)
                        .toList(),
                new WsEvent<>(eventType, event));
    }

    private <T> void notifyUser(String username, String eventType, T event) {
        eventPublisher.sendToUser(username, new WsEvent<>(eventType, event));
    }
}
