package org.chatapp.e2eechatserver.conversation.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.chatapp.e2eechatserver.common.security.CurrentUserService;
import org.chatapp.e2eechatserver.conversation.dto.*;
import org.chatapp.e2eechatserver.conversation.service.MessageService;
import org.chatapp.e2eechatserver.conversation.service.RoomMemberService;
import org.chatapp.e2eechatserver.conversation.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomsController {

    private final RoomService roomService;
    private final RoomMemberService roomMemberService;
    private final CurrentUserService currentUserService;
    private final MessageService messageService;

    @PostMapping("/dm")
    public RoomDataResponse createOrGetPrivateRoom(@RequestBody @Valid CreateDmRequest req, Principal principal) {
        Long myUserId = currentUserService.getCurrentUserId(principal);
        return roomService.createOrGetDm(myUserId, req.otherUserId());
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDataResponse> getRoomData(@PathVariable Long roomId, Principal principal) {
        return ResponseEntity.ok(roomService.getRoomData(roomId, principal));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<RoomDataResponse>> recentConversations(Principal principal) {
        return ResponseEntity.ok(roomService.getRecentConversations(principal));
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<WsNewMessage>> loadMessages(@PathVariable Long roomId) {
        return ResponseEntity.ok(messageService.loadMessages(roomId));
    }

    @PostMapping("/group")
    public ResponseEntity<RoomDataResponse> createGroupRoom(@RequestBody CreateGroupRoomRequest request, Principal principal) {
        return ResponseEntity.ok(roomService.createGroupRoom(request, principal));
    }

    @PostMapping(value = "/{roomId}/name")
    public ResponseEntity<RoomDataResponse> updateName(@PathVariable Long roomId, @RequestBody ChangeGroupNameRequest request, Principal principal) {
        return ResponseEntity.ok(roomService.updateRoomName(roomId, request.name(), principal));
    }

    @PostMapping("/{roomId}/members")
    public ResponseEntity<RoomDataResponse> addMember(@PathVariable Long roomId, @RequestBody AddNewMemberRequest request, Principal principal) {
        return ResponseEntity.ok(roomMemberService.addMemberToGroup(roomId, request.userId(), principal));
    }

    @DeleteMapping("/{roomId}/members")
    public ResponseEntity<RoomDataResponse> removeMember(@PathVariable Long roomId, @RequestBody RemoveMemberRequest request, Principal principal) {
        return ResponseEntity.ok(roomMemberService.removeMemberFromGroup(roomId, request.userId(), principal));
    }

    @DeleteMapping("/{roomId}/leave")
    public ResponseEntity<RoomDataResponse> leaveGroup(@PathVariable Long roomId, Principal principal) {
        return ResponseEntity.ok(roomMemberService.leaveGroup(roomId, principal));
    }
}
