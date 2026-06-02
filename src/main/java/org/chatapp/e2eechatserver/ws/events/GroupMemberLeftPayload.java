package org.chatapp.e2eechatserver.ws.events;

public record GroupMemberLeftPayload(Long roomId, String leftUsername, boolean rekeyRequired) {
}
