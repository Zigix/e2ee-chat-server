package org.chatapp.e2eechatserver.conversation.service;

import lombok.RequiredArgsConstructor;
import org.chatapp.e2eechatserver.common.exception.RoomNotFoundException;
import org.chatapp.e2eechatserver.common.exception.UserNotFoundException;
import org.chatapp.e2eechatserver.conversation.dto.WsNewMessage;
import org.chatapp.e2eechatserver.conversation.dto.WsSendMessage;
import org.chatapp.e2eechatserver.conversation.entity.MessageEntity;
import org.chatapp.e2eechatserver.conversation.entity.MessageType;
import org.chatapp.e2eechatserver.conversation.entity.Room;
import org.chatapp.e2eechatserver.conversation.mapper.MessageMapper;
import org.chatapp.e2eechatserver.conversation.repository.MessageRepository;
import org.chatapp.e2eechatserver.conversation.repository.RoomRepository;
import org.chatapp.e2eechatserver.user.entity.User;
import org.chatapp.e2eechatserver.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messages;
    private final MessageMapper messageMapper;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @Transactional
    public MessageEntity saveCiphertext(Long senderId, Long roomId, WsSendMessage msg) {
        Room room =  roomRepository.findById(roomId).orElseThrow(() ->
                new RoomNotFoundException(String.format("Room with id %d not found", roomId)));

        User sender = userRepository.findById(senderId).orElseThrow(() ->
                new UserNotFoundException(String.format("User with id %d not found", senderId)));

        MessageEntity message = MessageEntity.builder()
                .room(room)
                .sender(sender)
                .createdAt(Instant.now())
                .keyVersion(msg.keyVersion())
                .ciphertextB64(msg.ciphertextB64())
                .ivB64(msg.ivB64())
                .aadB64(msg.aadB64())
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
        Room room =  roomRepository.findById(roomId).orElseThrow(() ->
                new RoomNotFoundException(String.format("Room with id %d not found", roomId)));

        MessageEntity message = new MessageEntity();
        message.setRoom(room);
        message.setType(MessageType.SYSTEM);
        message.setSystemText(text);
        message.setCreatedAt(Instant.now());
        return messages.save(message);
    }
}

