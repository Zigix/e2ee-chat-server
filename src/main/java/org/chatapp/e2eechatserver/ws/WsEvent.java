package org.chatapp.e2eechatserver.ws;

public record WsEvent<T>(
        String type,
        T payload
) {}
