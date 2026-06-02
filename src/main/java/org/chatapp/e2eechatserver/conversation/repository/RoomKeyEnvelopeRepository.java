package org.chatapp.e2eechatserver.conversation.repository;

import org.chatapp.e2eechatserver.conversation.entity.RoomKeyEnvelope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomKeyEnvelopeRepository extends JpaRepository<RoomKeyEnvelope, Long> {
    List<RoomKeyEnvelope> findByRoomIdAndForUserId(Long roomId, Long forUserId);
    Optional<RoomKeyEnvelope> findByRoomIdAndVersionAndForUserId(Long roomId, int version, Long forUserId);
}
