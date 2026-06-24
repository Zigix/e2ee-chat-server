package org.chatapp.e2eechatserver.conversation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record WsSendMessage(
        @Min(1) int keyVersion,
        @NotBlank String ciphertextB64,
        @NotBlank String ivB64,
        @NotBlank String aadB64
) {}
