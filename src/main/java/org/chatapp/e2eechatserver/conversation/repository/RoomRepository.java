package org.chatapp.e2eechatserver.conversation.repository;

import jakarta.persistence.LockModeType;
import org.chatapp.e2eechatserver.conversation.entity.Room;
import org.chatapp.e2eechatserver.conversation.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("""
            select distinct room
            from Room room
            join RoomMember member on member.roomId = room.roomId
            where member.userId = :userId
            """)
    List<Room> findByMemberUserId(@Param("userId") Long userId);

    @Query("""
            select room
            from Room room
            where room.type = :type
              and :userId1 <> :userId2
              and exists (
                  select 1
                  from RoomMember member
                  where member.roomId = room.roomId
                    and member.userId = :userId1
              )
              and exists (
                  select 1
                  from RoomMember member
                  where member.roomId = room.roomId
                    and member.userId = :userId2
              )
              and (
                  select count(distinct member.userId)
                  from RoomMember member
                  where member.roomId = room.roomId
              ) = 2
            """)
    Optional<Room> findPrivateRoomForTwoUsers(
            @Param("type") RoomType type,
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.roomId = :roomId")
    Optional<Room> findByIdForUpdate(@Param("roomId") Long roomId);
}
