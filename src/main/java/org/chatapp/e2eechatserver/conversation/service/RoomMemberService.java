package org.chatapp.e2eechatserver.conversation.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.chatapp.e2eechatserver.common.exception.RoomNotFoundException;
import org.chatapp.e2eechatserver.common.security.CurrentUserService;
import org.chatapp.e2eechatserver.conversation.dto.RoomDataResponse;
import org.chatapp.e2eechatserver.conversation.entity.MemberRole;
import org.chatapp.e2eechatserver.conversation.entity.Room;
import org.chatapp.e2eechatserver.conversation.entity.RoomMember;
import org.chatapp.e2eechatserver.conversation.mapper.RoomMapper;
import org.chatapp.e2eechatserver.conversation.repository.RoomMemberRepository;
import org.chatapp.e2eechatserver.conversation.repository.RoomRepository;
import org.chatapp.e2eechatserver.user.entity.User;
import org.chatapp.e2eechatserver.user.service.UserService;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomMemberService {
    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final UserService userService;
    private final RoomAccessService roomAccessService;
    private final RoomMapper roomMapper;
    private final CurrentUserService currentUserService;
    private final RoomNotificationService roomNotificationService;

    @Transactional
    public RoomDataResponse addMemberToGroup(Long roomId, Long userId, Principal principal) {
        Room room = roomRepository.findById(roomId).orElseThrow(() ->
                new RoomNotFoundException("Room with that id has not been found"));

        Long currentUserId = currentUserService.getCurrentUserId(principal);

        roomAccessService.assertActiveMember(roomId, currentUserId);

        Optional<RoomMember> roomMemberOptional = roomMemberRepository.findByRoomIdAndUserId(room.getRoomId(), userId);
        if (roomMemberOptional.isPresent()) {
            RoomMember roomMember = roomMemberOptional.get();
            roomMember.setActive(true);
            roomMemberRepository.save(roomMember);
        } else {
            roomMemberRepository.save(new RoomMember(room.getRoomId(), userId, MemberRole.MEMBER));
        }

        room.setCurrentKeyVersion(room.getCurrentKeyVersion() + 1);

        String currentUsername = currentUserService.getCurrentUsername(principal);
        String addedUsername = userService.getUsernameByUserId(userId);

        RoomDataResponse roomDataResponse = roomMapper.toRoomDataResponse(room, currentUserId);

        roomNotificationService.publishMemberAdded(roomDataResponse, addedUsername, currentUsername);

        return roomDataResponse;
    }

    @Transactional
    public RoomDataResponse removeMemberFromGroup(Long roomId, Long userId, Principal principal) {
        Room room = roomRepository.findById(roomId).orElseThrow(() ->
                new RoomNotFoundException("Room with that id has not been found"));

        Long currentUserId = currentUserService.getCurrentUserId(principal);

        roomAccessService.assertGroupAdmin(roomId, currentUserId);

        RoomMember roomMember = roomMemberRepository.findByRoomIdAndUserId(room.getRoomId(), userId).orElseThrow();
        roomMember.setActive(false);

        room.setCurrentKeyVersion(room.getCurrentKeyVersion() + 1);

        String currentUsername = currentUserService.getCurrentUsername(principal);
        String removedUsername = userService.getUsernameByUserId(userId);

        RoomDataResponse roomDataResponse = roomMapper.toRoomDataResponse(room, currentUserId);

        roomNotificationService.publishMemberRemoved(roomDataResponse, removedUsername, currentUsername);

        return roomDataResponse;
    }

    @Transactional
    public RoomDataResponse leaveGroup(Long roomId, Principal principal) {
        User leavingUser = currentUserService.getCurrentUser(principal);

        Room room = roomRepository.findById(roomId).orElseThrow(() ->
                new RoomNotFoundException("Room with that id has not been found"));

        roomAccessService.assertActiveMember(roomId, leavingUser.getId());

        RoomMember roomMember = roomMemberRepository.findByRoomIdAndUserId(room.getRoomId(), leavingUser.getId()).orElseThrow();
        roomMember.setActive(false);

        room.setRekeyRequired(true);

        RoomDataResponse roomDataResponse = roomMapper.toRoomDataResponse(room, leavingUser.getId());

        roomNotificationService.publishMemberLeft(roomDataResponse, leavingUser.getUsername());

        return roomDataResponse;
    }
}
