package org.chatapp.e2eechatserver.conversation.mapper;

import lombok.RequiredArgsConstructor;
import org.chatapp.e2eechatserver.conversation.dto.RoomDataResponse;
import org.chatapp.e2eechatserver.conversation.dto.RoomMemberData;
import org.chatapp.e2eechatserver.conversation.entity.Room;
import org.chatapp.e2eechatserver.conversation.entity.RoomMember;
import org.chatapp.e2eechatserver.conversation.entity.RoomType;
import org.chatapp.e2eechatserver.conversation.repository.RoomMemberRepository;
import org.chatapp.e2eechatserver.user.service.UserService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RoomMapper {
    private final RoomMemberRepository roomMemberRepository;
    private final UserService userService;

    public RoomDataResponse toRoomDataResponse(Room room, Long currentUserId) {
        List<RoomMember> roomMembers = roomMemberRepository.findByRoomId(room.getRoomId())
                .stream()
                .filter(RoomMember::isActive)
                .toList();

        boolean activeMembership = roomMembers.stream()
                .anyMatch(rm -> rm.getUserId().equals(currentUserId));

        return RoomDataResponse.builder()
                .roomId(room.getRoomId())
                .roomName(getRoomNameForConversation(room, currentUserId))
                .roomType(room.getType().name())
                .currentKeyVersion(room.getCurrentKeyVersion())
                .rekeyRequired(room.isRekeyRequired())
                .activeMembership(activeMembership)
                .roomMembersDataList(roomMembers.stream().map(this::toRoomMemberData).toList())
                .build();
    }

    public RoomMemberData toRoomMemberData(RoomMember roomMember) {
        return new RoomMemberData(
                roomMember.getId(),
                roomMember.getUserId(),
                userService.getUsernameByUserId(roomMember.getUserId()),
                roomMember.getMemberRole().name(),
                userService.getPublicEcdhJwkForUser(roomMember.getUserId())
        );
    }

    private String getRoomNameForConversation(Room room, Long currentUserId) {
        if (room.getType() == RoomType.PRIVATE) {
            Long otherUserId = roomMemberRepository.findByRoomId(room.getRoomId())
                    .stream()
                    .filter(member -> !member.getUserId().equals(currentUserId))
                    .findFirst()
                    .orElseThrow()
                    .getUserId();

            return userService.getUsernameByUserId(otherUserId);
        }

        return room.getName();
    }
}
