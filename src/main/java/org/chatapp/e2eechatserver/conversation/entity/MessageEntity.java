package org.chatapp.e2eechatserver.conversation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "key_version")
    private int keyVersion;

    @Enumerated(EnumType.STRING)
    private MessageType type;

    @Column(name = "system_text")
    private String systemText;

    @Lob
    @Column(name = "ciphertext_b64")
    private String ciphertextB64;

    @Column(name = "iv_b64", length = 200)
    private String ivB64;

    @Lob
    @Column(name = "aad_b64")
    private String aadB64;

    @Column(name = "client_message_id", length = 100)
    private String clientMessageId;
}

