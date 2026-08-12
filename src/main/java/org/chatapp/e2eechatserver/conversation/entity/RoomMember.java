package org.chatapp.e2eechatserver.conversation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.chatapp.e2eechatserver.user.entity.User;

@Entity
@Table(name = "room_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private boolean isActive = true;

    private MemberRole memberRole;

    public RoomMember(Room room, User user, MemberRole memberRole) {
        this.room = room;
        this.user = user;
        this.memberRole = memberRole;
    }
}
