package org.chatapp.e2eechatserver.conversation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDataResponse {
    private Long roomId;
    private String roomName;
    private String roomType;
    private int currentKeyVersion;
    private boolean rekeyRequired;
    private boolean activeMembership;
    private List<RoomMemberData> roomMembersDataList;
}
