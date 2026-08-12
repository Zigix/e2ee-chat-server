package org.chatapp.e2eechatserver.conversation.mapper;

import lombok.RequiredArgsConstructor;
import org.chatapp.e2eechatserver.conversation.dto.MyKeyResponse;
import org.chatapp.e2eechatserver.conversation.dto.UploadRoomKeysRequest;
import org.chatapp.e2eechatserver.conversation.entity.Room;
import org.chatapp.e2eechatserver.conversation.entity.RoomKeyEnvelope;
import org.chatapp.e2eechatserver.user.entity.User;
import org.chatapp.e2eechatserver.user.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomKeyMapper {
    private final UserRepository userRepository;

    public RoomKeyEnvelope toKeyEnvelope(UploadRoomKeysRequest.KeyItem keyItem, Room room, int version, Long senderId) {
        User forUser = userRepository.findById(keyItem.userId()).orElse(null);
        User wrappedByUser = userRepository.findById(senderId).orElse(null);

        RoomKeyEnvelope roomKeyEnvelope = new RoomKeyEnvelope();
        roomKeyEnvelope.setRoom(room);
        roomKeyEnvelope.setVersion(version);
        roomKeyEnvelope.setForUser(forUser);
        roomKeyEnvelope.setWrappedByUser(wrappedByUser);
        roomKeyEnvelope.setWrappedRoomKeyB64(keyItem.wrappedRoomKeyB64());
        roomKeyEnvelope.setIvB64(keyItem.ivB64());
        roomKeyEnvelope.setAadB64(keyItem.aadB64());

        return roomKeyEnvelope;
    }

    public MyKeyResponse toMyKeyResponse(RoomKeyEnvelope envelope) {
        return MyKeyResponse.builder()
                .roomId(envelope.getRoom().getId())
                .version(envelope.getVersion())
                .wrappedByUserId(envelope.getWrappedByUser().getId())
                .wrappedRoomKeyB64(envelope.getWrappedRoomKeyB64())
                .ivB64(envelope.getIvB64())
                .aadB64(envelope.getAadB64())
                .build();
    }
}
