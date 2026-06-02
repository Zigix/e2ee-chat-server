package org.chatapp.e2eechatserver.ws.events;

public record KeyEnvelopeAvailablePayload(
        Long roomId,
        Integer version
) {}
