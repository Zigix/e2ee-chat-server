package org.chatapp.e2eechatserver.ws;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WsEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public <T> void sendToUser(String username, WsEvent<T> event) {
        log.info("WS SEND TO USER = {}", username);
        log.info("WS EVENT = {}", event);

        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/events",
                event
        );
    }

    public <T> void sendToUsers(Iterable<String> usernames, WsEvent<T> event) {
        for (String username : usernames) {
            sendToUser(username, event);
        }
    }

    public <T> void sendToRoomTopic(Long roomId, WsEvent<T> event) {
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + roomId,
                event
        );
    }
}
