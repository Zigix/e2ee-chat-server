package org.chatapp.e2eechatserver.ws.events;

public record GroupNameChangedPayload(Long roomId, String newName) {
}
