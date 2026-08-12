package org.chatapp.e2eechatserver.conversation.mapper;

import lombok.RequiredArgsConstructor;
import org.chatapp.e2eechatserver.conversation.dto.WsNewMessage;
import org.chatapp.e2eechatserver.conversation.entity.MessageEntity;
import org.chatapp.e2eechatserver.user.service.UserService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageMapper {
    public WsNewMessage toWsNewMessage(MessageEntity messageEntity) {
        Long senderId = messageEntity.getSender() == null ? null : messageEntity.getSender().getId();

        WsNewMessage.WsNewMessageBuilder wsNewMessageBuilder = WsNewMessage.builder()
                .id(messageEntity.getId())
                .roomId(messageEntity.getRoom().getId())
                .senderId(senderId)
                .createdAt(messageEntity.getCreatedAt())
                .keyVersion(messageEntity.getKeyVersion())
                .ciphertextB64(messageEntity.getCiphertextB64())
                .ivB64(messageEntity.getIvB64())
                .aadB64(messageEntity.getAadB64())
                .type(messageEntity.getType().name())
                .systemText(messageEntity.getSystemText());

        if (senderId == null) {
            wsNewMessageBuilder.senderUsername(null);
            return wsNewMessageBuilder.build();
        }

        wsNewMessageBuilder.senderUsername(messageEntity.getSender().getUsername());

        return wsNewMessageBuilder.build();
    }
}
