package org.chatapp.e2eechatserver.conversation.dto;

import lombok.Builder;

@Builder
public record MyKeyResponse(
        Long roomId,
        int version,
        Long wrappedByUserId,
        String wrappedRoomKeyB64,
        String ivB64,
        String aadB64
) {}