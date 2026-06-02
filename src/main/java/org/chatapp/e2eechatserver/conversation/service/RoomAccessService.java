package org.chatapp.e2eechatserver.conversation.service;

import lombok.RequiredArgsConstructor;
import org.chatapp.e2eechatserver.conversation.entity.MemberRole;
import org.chatapp.e2eechatserver.conversation.repository.RoomMemberRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomAccessService {
    private final RoomMemberRepository roomMemberRepository;

    public void assertActiveMember(Long roomId, Long userId) {
        boolean isActiveMember = roomMemberRepository.existsByRoomIdAndUserIdAndIsActiveTrue(roomId, userId);

        if (!isActiveMember) {
            throw new AccessDeniedException("User is not a member of this room");
        }
    }

    public void assertGroupAdmin(Long roomId, Long userId) {
        boolean isActiveGroupAdmin = roomMemberRepository.existsByRoomIdAndUserIdAndIsActiveTrueAndMemberRole(roomId, userId, MemberRole.ADMIN);

        if (!isActiveGroupAdmin) {
            throw new AccessDeniedException("User is not a group admin");
        }
    }
}
