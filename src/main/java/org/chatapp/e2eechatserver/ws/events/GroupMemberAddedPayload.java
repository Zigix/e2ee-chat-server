package org.chatapp.e2eechatserver.ws.events;

import org.chatapp.e2eechatserver.conversation.dto.RoomDataResponse;

public record GroupMemberAddedPayload(RoomDataResponse roomData) {
}
