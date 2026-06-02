package org.chatapp.e2eechatserver.conversation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomMemberData {
    private Long memberId;
    private Long userId;
    private String username;
    private String role;
    private String publicEcdhJwk;
}
