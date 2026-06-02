package org.chatapp.e2eechatserver.conversation.service;

import lombok.RequiredArgsConstructor;
import org.chatapp.e2eechatserver.conversation.dto.WsNewMessage;
import org.chatapp.e2eechatserver.conversation.dto.WsSendMessage;
import org.chatapp.e2eechatserver.conversation.entity.MessageEntity;
import org.chatapp.e2eechatserver.conversation.entity.MessageType;
import org.chatapp.e2eechatserver.conversation.mapper.MessageMapper;
import org.chatapp.e2eechatserver.conversation.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messages;
    private final MessageMapper messageMapper;

    @Transactional
    public MessageEntity saveCiphertext(Long senderId, Long roomId, WsSendMessage msg) {
        MessageEntity message = MessageEntity.builder()
                .roomId(roomId)
                .senderId(senderId)
                .createdAt(Instant.now())
                .keyVersion(msg.keyVersion())
                .ciphertextB64(msg.ciphertextB64())
                .ivB64(msg.ivB64())
                .aadB64(msg.aadB64())
                .clientMessageId(msg.clientMessageId())
                .type(MessageType.CHAT)
                .systemText(null)
                .build();

        return messages.save(message);
    }

    public List<WsNewMessage> loadMessages(Long roomId) {
        return messages.findAllByRoomIdOrderByCreatedAtAsc(roomId).stream().map(messageMapper::toWsNewMessage).toList();
    }

    @Transactional
    public MessageEntity createSystemMessage(Long roomId, String text) {
        MessageEntity message = new MessageEntity();
        message.setRoomId(roomId);
        message.setType(MessageType.SYSTEM);
        message.setSystemText(text);
        message.setCreatedAt(Instant.now());
        return messages.save(message);
    }
}

