package org.chatapp.e2eechatserver.conversation.api;

import jakarta.validation.Valid;
import org.chatapp.e2eechatserver.conversation.dto.MyKeyResponse;
import org.chatapp.e2eechatserver.conversation.dto.RoomDataResponse;
import org.chatapp.e2eechatserver.conversation.dto.UploadRoomKeysRequest;
import org.chatapp.e2eechatserver.conversation.service.RoomKeyService;
import org.chatapp.e2eechatserver.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/rooms/{roomId}")
public class RoomKeysController {
    private final RoomKeyService roomKeyService;

    public RoomKeysController(RoomKeyService roomKeyService) {
        this.roomKeyService = roomKeyService;
    }

    @PostMapping("/keys/upload")
    public ResponseEntity<String> upload(@PathVariable Long roomId,
                                         @RequestBody @Valid UploadRoomKeysRequest req,
                                         @AuthenticationPrincipal User user) {
        roomKeyService.uploadKeys(user.getId(), roomId, req);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/keys/rekey")
    public ResponseEntity<RoomDataResponse> rekey(@PathVariable Long roomId,
                                                  @RequestBody @Valid UploadRoomKeysRequest req,
                                                  Principal principal) {
        RoomDataResponse roomDataResponse = roomKeyService.uploadPendingRekeyKeys(roomId, req, principal);
        return ResponseEntity.ok(roomDataResponse);
    }

    @GetMapping("/my-key")
    public MyKeyResponse myKey(@PathVariable Long roomId,
                               @RequestParam int version,
                               @AuthenticationPrincipal User user) {
        return roomKeyService.getMyKeyWithVersion(user.getId(), roomId, version);
    }

    @GetMapping("/my-keys")
    public List<MyKeyResponse> myKeys(@PathVariable Long roomId,
                                      @AuthenticationPrincipal User user) {
        return roomKeyService.getAllMyKeysForRoom(user.getId(), roomId);
    }
}