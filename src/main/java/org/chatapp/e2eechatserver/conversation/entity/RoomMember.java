package org.chatapp.e2eechatserver.conversation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "room_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_id","user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="room_id", nullable=false)
    private Long roomId;

    @Column(name="user_id", nullable=false)
    private Long userId;

    private boolean isActive = true;

    private MemberRole memberRole;

    public RoomMember(Long roomId, Long userId, MemberRole memberRole) {
        this.roomId = roomId;
        this.userId = userId;
        this.memberRole = memberRole;
    }
}
