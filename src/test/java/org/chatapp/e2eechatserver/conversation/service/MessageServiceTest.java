package org.chatapp.e2eechatserver.conversation.service;

import org.chatapp.e2eechatserver.conversation.dto.WsNewMessage;
import org.chatapp.e2eechatserver.conversation.dto.WsSendMessage;
import org.chatapp.e2eechatserver.conversation.entity.MessageEntity;
import org.chatapp.e2eechatserver.conversation.entity.MessageType;
import org.chatapp.e2eechatserver.conversation.mapper.MessageMapper;
import org.chatapp.e2eechatserver.conversation.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messages;

    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private MessageService messageService;

    @Test
    void saveCiphertext_givenSenderRoomAndMessage_whenSaveCiphertext_thenSavesChatMessageAndReturnsSavedEntity() {
        // given
        Long senderId = 1L;
        Long roomId = 2L;
        WsSendMessage message = new WsSendMessage(
                3,
                "ciphertext",
                "iv",
                "aad"
        );
        MessageEntity savedMessage = new MessageEntity();
        savedMessage.setId(10L);
        Instant beforeSave = Instant.now();

        when(messages.save(org.mockito.ArgumentMatchers.any(MessageEntity.class))).thenReturn(savedMessage);

        // when
        MessageEntity result = messageService.saveCiphertext(senderId, roomId, message);

        // then
        assertThat(result).isSameAs(savedMessage);

        ArgumentCaptor<MessageEntity> messageCaptor = ArgumentCaptor.forClass(MessageEntity.class);
        verify(messages).save(messageCaptor.capture());

        MessageEntity messageToSave = messageCaptor.getValue();
        assertThat(messageToSave.getRoomId()).isEqualTo(roomId);
        assertThat(messageToSave.getSenderId()).isEqualTo(senderId);
        assertThat(messageToSave.getCreatedAt()).isBetween(beforeSave, Instant.now());
        assertThat(messageToSave.getKeyVersion()).isEqualTo(3);
        assertThat(messageToSave.getCiphertextB64()).isEqualTo("ciphertext");
        assertThat(messageToSave.getIvB64()).isEqualTo("iv");
        assertThat(messageToSave.getAadB64()).isEqualTo("aad");
        assertThat(messageToSave.getType()).isEqualTo(MessageType.CHAT);
        assertThat(messageToSave.getSystemText()).isNull();
    }

    @Test
    void loadMessages_givenRoomId_whenLoadMessages_thenLoadsMessagesOrderedByCreatedAtAndMapsThem() {
        // given
        Long roomId = 2L;
        MessageEntity firstMessage = messageEntity(1L);
        MessageEntity secondMessage = messageEntity(2L);
        WsNewMessage firstResponse = wsNewMessage(1L);
        WsNewMessage secondResponse = wsNewMessage(2L);

        when(messages.findAllByRoomIdOrderByCreatedAtAsc(roomId))
                .thenReturn(List.of(firstMessage, secondMessage));
        when(messageMapper.toWsNewMessage(firstMessage)).thenReturn(firstResponse);
        when(messageMapper.toWsNewMessage(secondMessage)).thenReturn(secondResponse);

        // when
        List<WsNewMessage> result = messageService.loadMessages(roomId);

        // then
        assertThat(result).containsExactly(firstResponse, secondResponse);
        verify(messages).findAllByRoomIdOrderByCreatedAtAsc(roomId);
        verify(messageMapper).toWsNewMessage(firstMessage);
        verify(messageMapper).toWsNewMessage(secondMessage);
    }

    @Test
    void createSystemMessage_givenRoomIdAndText_whenCreateSystemMessage_thenSavesSystemMessageAndReturnsSavedEntity() {
        // given
        Long roomId = 2L;
        String text = "User joined the room";
        MessageEntity savedMessage = new MessageEntity();
        savedMessage.setId(10L);
        Instant beforeSave = Instant.now();

        when(messages.save(org.mockito.ArgumentMatchers.any(MessageEntity.class))).thenReturn(savedMessage);

        // when
        MessageEntity result = messageService.createSystemMessage(roomId, text);

        // then
        assertThat(result).isSameAs(savedMessage);

        ArgumentCaptor<MessageEntity> messageCaptor = ArgumentCaptor.forClass(MessageEntity.class);
        verify(messages).save(messageCaptor.capture());

        MessageEntity messageToSave = messageCaptor.getValue();
        assertThat(messageToSave.getRoomId()).isEqualTo(roomId);
        assertThat(messageToSave.getType()).isEqualTo(MessageType.SYSTEM);
        assertThat(messageToSave.getSystemText()).isEqualTo(text);
        assertThat(messageToSave.getCreatedAt()).isBetween(beforeSave, Instant.now());
        assertThat(messageToSave.getSenderId()).isNull();
        assertThat(messageToSave.getCiphertextB64()).isNull();
        assertThat(messageToSave.getIvB64()).isNull();
        assertThat(messageToSave.getAadB64()).isNull();
        assertThat(messageToSave.getClientMessageId()).isNull();
    }

    private static MessageEntity messageEntity(Long id) {
        MessageEntity message = new MessageEntity();
        message.setId(id);
        message.setRoomId(2L);
        message.setSenderId(1L);
        message.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z").plusSeconds(id));
        message.setKeyVersion(3);
        message.setType(MessageType.CHAT);
        message.setCiphertextB64("ciphertext-" + id);
        message.setIvB64("iv-" + id);
        message.setAadB64("aad-" + id);

        return message;
    }

    private static WsNewMessage wsNewMessage(Long id) {
        return WsNewMessage.builder()
                .id(id)
                .roomId(2L)
                .senderId(1L)
                .sender("testuser")
                .createdAt(Instant.parse("2026-01-01T10:00:00Z").plusSeconds(id))
                .keyVersion(3)
                .ciphertextB64("ciphertext-" + id)
                .ivB64("iv-" + id)
                .aadB64("aad-" + id)
                .type(MessageType.CHAT.name())
                .build();
    }
}
