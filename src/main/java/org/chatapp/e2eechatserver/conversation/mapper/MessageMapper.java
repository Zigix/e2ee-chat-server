package org.chatapp.e2eechatserver.conversation.mapper;

import lombok.RequiredArgsConstructor;
import org.chatapp.e2eechatserver.conversation.dto.WsNewMessage;
import org.chatapp.e2eechatserver.conversation.entity.MessageEntity;
import org.chatapp.e2eechatserver.user.service.UserService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageMapper {
    private final UserService userService;

    public WsNewMessage toWsNewMessage(MessageEntity messageEntity) {
        WsNewMessage.WsNewMessageBuilder wsNewMessageBuilder = WsNewMessage.builder()
                .id(messageEntity.getId())
                .roomId(messageEntity.getRoomId())
                .senderId(messageEntity.getSenderId())
                .createdAt(messageEntity.getCreatedAt())
                .keyVersion(messageEntity.getKeyVersion())
                .ciphertextB64(messageEntity.getCiphertextB64())
                .ivB64(messageEntity.getIvB64())
                .aadB64(messageEntity.getAadB64())
                .type(messageEntity.getType().name())
                .systemText(messageEntity.getSystemText());

        if (messageEntity.getSenderId() == null) {
            wsNewMessageBuilder.sender(null);
            return wsNewMessageBuilder.build();
        }

        final String username = userService.getUsernameByUserId(messageEntity.getSenderId());
        wsNewMessageBuilder.sender(username);

        return wsNewMessageBuilder.build();
    }
}
