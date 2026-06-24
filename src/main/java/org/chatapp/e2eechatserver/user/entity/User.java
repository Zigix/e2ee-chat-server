package org.chatapp.e2eechatserver.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String pubEcdhJwk;

    @Column(nullable = false)
    private int vaultVersion;

    @Column(unique = true, nullable = false)
    private String kdfSaltB64;

    @Column(nullable = false)
    private int kdfIterations;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String wrappedMkB64;

    @Column(nullable = false, length = 128)
    private String wrappedMkIvB64;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String wrappedEcdhPrivB64;

    @Column(nullable = false, length = 128)
    private String wrappedEcdhPrivIvB64;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("USER"));
    }
}
