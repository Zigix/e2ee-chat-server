package org.chatapp.e2eechatserver.conversation.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.chatapp.e2eechatserver.common.exception.RoomNotFoundException;
import org.chatapp.e2eechatserver.common.security.CurrentUserService;
import org.chatapp.e2eechatserver.conversation.dto.CreateGroupRoomRequest;
import org.chatapp.e2eechatserver.conversation.dto.RoomDataResponse;
import org.chatapp.e2eechatserver.conversation.entity.MemberRole;
import org.chatapp.e2eechatserver.conversation.entity.Room;
import org.chatapp.e2eechatserver.conversation.entity.RoomMember;
import org.chatapp.e2eechatserver.conversation.entity.RoomType;
import org.chatapp.e2eechatserver.conversation.mapper.RoomMapper;
import org.chatapp.e2eechatserver.conversation.repository.RoomMemberRepository;
import org.chatapp.e2eechatserver.conversation.repository.RoomRepository;
import org.chatapp.e2eechatserver.user.entity.User;
import org.chatapp.e2eechatserver.user.service.UserService;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final UserService userService;
    private final RoomMapper roomMapper;
    private final RoomAccessService roomAccessService;
    private final RoomNotificationService roomNotificationService;
    private final CurrentUserService currentUserService;

    @Transactional
    public RoomDataResponse createOrGetDm(Long myUserId, Long otherUserId) {
        Optional<Room> roomOptional = roomRepository.findPrivateRoomForTwoUsers(RoomType.PRIVATE, myUserId, otherUserId);


        if (roomOptional.isPresent()) {
            Room room = roomOptional.get();
            return roomMapper.toRoomDataResponse(room, myUserId);
        }

        User myUser = userService.getUserByUserId(myUserId);
        User otherUser = userService.getUserByUserId(otherUserId);

        Room room = new Room(RoomType.PRIVATE);
        roomRepository.save(room);

        roomMemberRepository.save(new RoomMember(room, myUser, MemberRole.MEMBER));
        roomMemberRepository.save(new RoomMember(room, otherUser, MemberRole.MEMBER));

        Map<String, RoomDataResponse> roomDataResponsesForParticularUsers = new HashMap<>();
        roomDataResponsesForParticularUsers.put(myUser.getUsername(), roomMapper.toRoomDataResponse(room, myUserId));
        roomDataResponsesForParticularUsers.put(otherUser.getUsername(), roomMapper.toRoomDataResponse(room, otherUserId));

        roomNotificationService.publishPrivateConversationCreated(roomDataResponsesForParticularUsers);

        return roomMapper.toRoomDataResponse(room, myUserId);
    }

    @Transactional
    public RoomDataResponse getRoomData(Long roomId, Principal principal) {
        Room room = roomRepository.findById(roomId).orElseThrow(() ->
                new RoomNotFoundException("Room with that id has not been found"));

        Long currentUserId = currentUserService.getCurrentUserId(principal);

        return roomMapper.toRoomDataResponse(room, currentUserId);
    }

    @Transactional
    public RoomDataResponse updateRoomName(Long roomId, String name, Principal principal) {
        Room room = roomRepository.findById(roomId).orElseThrow(() ->
                new RoomNotFoundException("Room with that id has not been found"));

        Long currentUserId = currentUserService.getCurrentUserId(principal);
        String currentUsername = currentUserService.getCurrentUsername(principal);

        roomAccessService.assertGroupAdmin(roomId, currentUserId);

        room.setName(name);
        roomRepository.save(room);

        RoomDataResponse roomDataResponse = roomMapper.toRoomDataResponse(room, currentUserId);

        roomNotificationService.publishGroupNameChanged(roomDataResponse, currentUsername);

        return roomDataResponse;
    }

    public List<RoomDataResponse> getRecentConversations(Principal principal) {
        Long currentUserId = currentUserService.getCurrentUserId(principal);
        return roomRepository.findByMemberUserId(currentUserId)
                .stream()
                .map(room -> roomMapper.toRoomDataResponse(room, currentUserId))
                .toList();
    }

    @Transactional
    public RoomDataResponse createGroupRoom(CreateGroupRoomRequest request, Principal principal) {
        User groupOwner = currentUserService.getCurrentUser(principal);

        Room room = new Room(RoomType.GROUP);
        room.setName(request.getName());

        roomRepository.save(room);

        roomMemberRepository.save(new RoomMember(room, groupOwner, MemberRole.ADMIN));

        for (Long userId : request.getUserIds()) {
            User user = userService.getUserByUserId(userId);
            roomMemberRepository.save(new RoomMember(room, user, MemberRole.MEMBER));
        }

        RoomDataResponse roomDataResponse = roomMapper.toRoomDataResponse(room, groupOwner.getId());

        roomNotificationService.publishGroupConversationCreated(roomDataResponse);

        return roomDataResponse;
    }
}

