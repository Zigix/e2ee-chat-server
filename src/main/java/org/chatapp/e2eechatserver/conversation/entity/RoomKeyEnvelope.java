package org.chatapp.e2eechatserver.conversation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "room_keys",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_id","version","for_user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomKeyEnvelope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="room_id", nullable=false)
    private Long roomId;

    @Column(nullable=false)
    private int version;

    @Column(name="for_user_id", nullable=false)
    private Long forUserId;

    @Column(name="wrapped_by_user_id", nullable=false)
    private Long wrappedByUserId;

    @Lob
    @Column(name="wrapped_room_key_b64", nullable=false)
    private String wrappedRoomKeyB64;

    @Column(name="iv_b64", nullable=false, length=200)
    private String ivB64;

    @Lob
    @Column(name="aad_b64", nullable=false)
    private String aadB64;
}

