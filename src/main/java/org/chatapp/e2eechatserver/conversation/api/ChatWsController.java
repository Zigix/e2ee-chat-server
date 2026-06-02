package org.chatapp.e2eechatserver.conversation.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.chatapp.e2eechatserver.common.security.CurrentUserService;
import org.chatapp.e2eechatserver.conversation.dto.RoomMemberData;
import org.chatapp.e2eechatserver.conversation.dto.WsSendMessage;
import org.chatapp.e2eechatserver.conversation.entity.MessageEntity;
import org.chatapp.e2eechatserver.conversation.mapper.RoomMapper;
import org.chatapp.e2eechatserver.conversation.repository.RoomMemberRepository;
import org.chatapp.e2eechatserver.conversation.service.MessageService;
import org.chatapp.e2eechatserver.conversation.service.RoomAccessService;
import org.chatapp.e2eechatserver.conversation.service.RoomNotificationService;
import org.chatapp.e2eechatserver.user.entity.User;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatWsController {
    private final MessageService messageService;
    private final RoomMapper roomMapper;
    private final RoomNotificationService roomNotificationService;
    private final RoomAccessService roomAccessService;
    private final CurrentUserService currentUserService;
    private final RoomMemberRepository roomMemberRepository;

    @MessageMapping("/rooms/{roomId}/send")
    public void send(@DestinationVariable Long roomId,
                     @Payload @Valid WsSendMessage msg,
                     Principal principal) {

        User user = currentUserService.getCurrentUser(principal);

        roomAccessService.assertActiveMember(roomId, user.getId());

        MessageEntity savedMessage = messageService.saveCiphertext(user.getId(), roomId, msg);

        List<RoomMemberData> roomMembersData = roomMemberRepository.findByRoomId(roomId)
                .stream()
                .map(roomMapper::toRoomMemberData)
                .toList();

        roomNotificationService.publishNewMessageCreated(roomId, roomMembersData, savedMessage);
    }
}
