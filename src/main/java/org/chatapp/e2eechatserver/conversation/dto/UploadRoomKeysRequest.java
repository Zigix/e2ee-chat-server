package org.chatapp.e2eechatserver.conversation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UploadRoomKeysRequest(
        @Min(1) int version,
        Long wrappedByUserId,
        @NotEmpty List<KeyItem> keyItems
) {
    public record KeyItem(
            Long userId,
            @NotBlank String wrappedRoomKeyB64,
            @NotBlank String ivB64,
            @NotBlank String aadB64
    ) {}
}