package org.chatapp.e2eechatserver.conversation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.chatapp.e2eechatserver.user.entity.User;

@Entity
@Table(name = "room_keys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomKeyEnvelope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable=false)
    private int version;

    @ManyToOne
    @JoinColumn(name = "for_user_id", nullable = false)
    private User forUser;

    @ManyToOne
    @JoinColumn(name = "wrapped_by_user_id", nullable = false)
    private User wrappedByUser;

    @Lob
    @Column(name="wrapped_room_key_b64", nullable=false)
    private String wrappedRoomKeyB64;

    @Column(name="iv_b64", nullable=false, length=200)
    private String ivB64;

    @Lob
    @Column(name="aad_b64", nullable=false)
    private String aadB64;
}

