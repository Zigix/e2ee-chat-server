package org.chatapp.e2eechatserver.conversation.service;

import lombok.RequiredArgsConstructor;
import org.chatapp.e2eechatserver.common.security.CurrentUserService;
import org.chatapp.e2eechatserver.conversation.dto.MyKeyResponse;
import org.chatapp.e2eechatserver.conversation.dto.RoomDataResponse;
import org.chatapp.e2eechatserver.conversation.dto.UploadRoomKeysRequest;
import org.chatapp.e2eechatserver.conversation.entity.Room;
import org.chatapp.e2eechatserver.conversation.entity.RoomKeyEnvelope;
import org.chatapp.e2eechatserver.conversation.mapper.RoomKeyMapper;
import org.chatapp.e2eechatserver.conversation.mapper.RoomMapper;
import org.chatapp.e2eechatserver.conversation.repository.RoomKeyEnvelopeRepository;
import org.chatapp.e2eechatserver.conversation.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RoomKeyService {
    private final RoomAccessService roomAccessService;
    private final RoomNotificationService roomNotificationService;
    private final RoomKeyMapper roomKeyMapper;
    private final RoomRepository roomRepository;
    private final RoomKeyEnvelopeRepository roomKeyEnvelopeRepository;
    private final CurrentUserService currentUserService;
    private final RoomMapper roomMapper;

    @Transactional
    public void uploadKeys(Long myUserId, Long roomId, UploadRoomKeysRequest req) {
        roomAccessService.assertActiveMember(roomId, myUserId);

        for (UploadRoomKeysRequest.KeyItem keyItem : req.keyItems()) {
            roomAccessService.assertActiveMember(roomId, keyItem.userId());
            RoomKeyEnvelope roomKeyEnvelope = roomKeyMapper.toKeyEnvelope(keyItem, roomId, req.version(), req.wrappedByUserId());
            roomKeyEnvelopeRepository.save(roomKeyEnvelope);
        }

        roomNotificationService.publishRoomKeyUpdated(req, roomId);
    }

    @Transactional
    public RoomDataResponse uploadPendingRekeyKeys(
            Long roomId,
            UploadRoomKeysRequest req,
            Principal principal
    ) {
        Long senderId = currentUserService.getCurrentUserId(principal);

        roomAccessService.assertActiveMember(roomId, senderId);

        Room room = roomRepository.findByIdForUpdate(roomId).orElseThrow();

        if (!room.isRekeyRequired()) {
            return roomMapper.toRoomDataResponse(room, senderId);
        }

        for (UploadRoomKeysRequest.KeyItem keyItem : req.keyItems()) {
            roomAccessService.assertActiveMember(roomId, keyItem.userId());
            RoomKeyEnvelope keyEnvelope = roomKeyMapper.toKeyEnvelope(keyItem, room.getRoomId(), req.version(), senderId);
            roomKeyEnvelopeRepository.save(keyEnvelope);
        }

        room.setCurrentKeyVersion(req.version());
        room.setRekeyRequired(false);

        RoomDataResponse roomDataResponse = roomMapper.toRoomDataResponse(room, senderId);

        roomNotificationService.publishRoomKeyUpdated(req, roomId);

        return roomDataResponse;
    }

    public MyKeyResponse getMyKey(Long myUserId, Long roomId, int version) {
        roomAccessService.assertActiveMember(roomId, myUserId);
        RoomKeyEnvelope envelope = roomKeyEnvelopeRepository.findByRoomIdAndVersionAndForUserId(roomId, version, myUserId)
                .orElseThrow(() -> new NoSuchElementException("No key envelope"));

        return roomKeyMapper.toMyKeyResponse(envelope);
    }

    public List<MyKeyResponse> getAllMyKeysForRoom(Long myUserId, Long roomId) {
        List<RoomKeyEnvelope> keyEnvelopes = roomKeyEnvelopeRepository.findByRoomIdAndForUserId(roomId, myUserId);

        return keyEnvelopes.stream().map(roomKeyMapper::toMyKeyResponse).toList();
    }

    public MyKeyResponse getMyKeyWithVersion(Long userId, Long roomId, int version) {
        RoomKeyEnvelope envelope = roomKeyEnvelopeRepository.findByRoomIdAndVersionAndForUserId(roomId, version, userId)
                .orElseThrow(() -> new NoSuchElementException("No key envelope"));

        return roomKeyMapper.toMyKeyResponse(envelope);
    }
}

