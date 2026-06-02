package org.chatapp.e2eechatserver.ws.events;

public record GroupMemberRemovedPayload(Long roomId, String removedUsername) {}
