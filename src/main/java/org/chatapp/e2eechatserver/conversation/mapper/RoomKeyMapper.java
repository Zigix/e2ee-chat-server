package org.chatapp.e2eechatserver.conversation.mapper;

import org.chatapp.e2eechatserver.conversation.dto.MyKeyResponse;
import org.chatapp.e2eechatserver.conversation.dto.UploadRoomKeysRequest;
import org.chatapp.e2eechatserver.conversation.entity.RoomKeyEnvelope;
import org.springframework.stereotype.Component;

@Component
public class RoomKeyMapper {

    public RoomKeyEnvelope toKeyEnvelope(UploadRoomKeysRequest.KeyItem keyItem, Long roomId, int version, Long senderId) {
        RoomKeyEnvelope roomKeyEnvelope = new RoomKeyEnvelope();
        roomKeyEnvelope.setRoomId(roomId);
        roomKeyEnvelope.setVersion(version);
        roomKeyEnvelope.setForUserId(keyItem.userId());
        roomKeyEnvelope.setWrappedByUserId(senderId);
        roomKeyEnvelope.setWrappedRoomKeyB64(keyItem.wrappedRoomKeyB64());
        roomKeyEnvelope.setIvB64(keyItem.ivB64());
        roomKeyEnvelope.setAadB64(keyItem.aadB64());

        return roomKeyEnvelope;
    }

    public MyKeyResponse toMyKeyResponse(RoomKeyEnvelope envelope) {
        return MyKeyResponse.builder()
                .roomId(envelope.getRoomId())
                .version(envelope.getVersion())
                .wrappedByUserId(envelope.getWrappedByUserId())
                .wrappedRoomKeyB64(envelope.getWrappedRoomKeyB64())
                .ivB64(envelope.getIvB64())
                .aadB64(envelope.getAadB64())
                .build();
    }
}
