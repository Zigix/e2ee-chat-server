package org.chatapp.e2eechatserver.user.repository;

import org.chatapp.e2eechatserver.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);

    List<User> findAllByUsernameStartsWith(String q);

    @Query("""
                select u.username from User u where u.id = :userId
            """)
    Optional<String> findUsernameByUserId(Long userId);
}
