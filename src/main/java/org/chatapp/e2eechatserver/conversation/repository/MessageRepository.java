package org.chatapp.e2eechatserver.conversation.repository;

import org.chatapp.e2eechatserver.conversation.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    List<MessageEntity> findAllByRoomIdOrderByCreatedAtAsc(Long roomId);
}
