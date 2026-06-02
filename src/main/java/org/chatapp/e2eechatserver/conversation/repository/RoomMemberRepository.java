package org.chatapp.e2eechatserver.conversation.repository;

import org.chatapp.e2eechatserver.conversation.entity.MemberRole;
import org.chatapp.e2eechatserver.conversation.entity.RoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {
    boolean existsByRoomIdAndUserId(Long roomId, Long userId);

    List<RoomMember> findByRoomId(Long roomId);

    Optional<RoomMember> findByRoomIdAndUserId(Long roomId, Long userId);

    boolean existsByRoomIdAndUserIdAndIsActiveTrue(Long roomId, Long userId);

    boolean existsByRoomIdAndUserIdAndIsActiveTrueAndMemberRole(Long roomId, Long userId, MemberRole memberRole);
}
