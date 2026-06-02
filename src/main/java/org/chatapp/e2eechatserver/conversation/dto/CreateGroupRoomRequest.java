package org.chatapp.e2eechatserver.conversation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupRoomRequest {
    private String name;
    private List<Long> userIds;
}
