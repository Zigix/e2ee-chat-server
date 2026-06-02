package org.chatapp.e2eechatserver.conversation.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record WsNewMessage(
        Long id,
        Long roomId,
        Long senderId,
        String sender,
        Instant createdAt,

        Integer keyVersion,
        String ciphertextB64,
        String ivB64,
        String aadB64,
        String type,
        String systemText
) {}
