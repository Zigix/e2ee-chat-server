package org.chatapp.e2eechatserver.conversation.service;

import org.chatapp.e2eechatserver.common.security.CurrentUserService;
import org.chatapp.e2eechatserver.conversation.dto.MyKeyResponse;
import org.chatapp.e2eechatserver.conversation.dto.RoomDataResponse;
import org.chatapp.e2eechatserver.conversation.dto.UploadRoomKeysRequest;
import org.chatapp.e2eechatserver.conversation.entity.Room;
import org.chatapp.e2eechatserver.conversation.entity.RoomKeyEnvelope;
import org.chatapp.e2eechatserver.conversation.entity.RoomType;
import org.chatapp.e2eechatserver.conversation.mapper.RoomKeyMapper;
import org.chatapp.e2eechatserver.conversation.mapper.RoomMapper;
import org.chatapp.e2eechatserver.conversation.repository.RoomKeyEnvelopeRepository;
import org.chatapp.e2eechatserver.conversation.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomKeyServiceTest {

    @Mock
    private RoomAccessService roomAccessService;

    @Mock
    private RoomNotificationService roomNotificationService;

    @Mock
    private RoomKeyMapper roomKeyMapper;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomKeyEnvelopeRepository roomKeyEnvelopeRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private RoomMapper roomMapper;

    @InjectMocks
    private RoomKeyService roomKeyService;

    @Test
    void uploadKeys_givenRequestWithKeyItems_whenUploadKeys_thenValidatesMembersSavesEnvelopesAndPublishesNotification() {
        // given
        Long myUserId = 1L;
        Long roomId = 10L;
        UploadRoomKeysRequest request = uploadRoomKeysRequest(5, 1L);
        RoomKeyEnvelope firstEnvelope = roomKeyEnvelope(10L, 5, 2L, 1L);
        RoomKeyEnvelope secondEnvelope = roomKeyEnvelope(10L, 5, 3L, 1L);

        when(roomKeyMapper.toKeyEnvelope(request.keyItems().get(0), roomId, request.version(), request.wrappedByUserId()))
                .thenReturn(firstEnvelope);
        when(roomKeyMapper.toKeyEnvelope(request.keyItems().get(1), roomId, request.version(), request.wrappedByUserId()))
                .thenReturn(secondEnvelope);

        // when
        roomKeyService.uploadKeys(myUserId, roomId, request);

        // then
        InOrder inOrder = inOrder(roomAccessService, roomKeyMapper, roomKeyEnvelopeRepository, roomNotificationService);
        inOrder.verify(roomAccessService).assertActiveMember(roomId, myUserId);
        inOrder.verify(roomAccessService).assertActiveMember(roomId, 2L);
        inOrder.verify(roomKeyMapper).toKeyEnvelope(request.keyItems().get(0), roomId, 5, 1L);
        inOrder.verify(roomKeyEnvelopeRepository).save(firstEnvelope);
        inOrder.verify(roomAccessService).assertActiveMember(roomId, 3L);
        inOrder.verify(roomKeyMapper).toKeyEnvelope(request.keyItems().get(1), roomId, 5, 1L);
        inOrder.verify(roomKeyEnvelopeRepository).save(secondEnvelope);
        inOrder.verify(roomNotificationService).publishRoomKeyUpdated(request, roomId);
    }

    @Test
    void uploadPendingRekeyKeys_givenRoomWithoutPendingRekey_whenUploadPendingRekeyKeys_thenReturnsRoomDataWithoutSavingKeys() {
        // given
        Long roomId = 10L;
        Long senderId = 1L;
        Principal principal = () -> "testuser";
        UploadRoomKeysRequest request = uploadRoomKeysRequest(5, 999L);
        Room room = room(roomId, 4, false);
        RoomDataResponse expectedResponse = roomDataResponse(roomId, 4, false);

        when(currentUserService.getCurrentUserId(principal)).thenReturn(senderId);
        when(roomRepository.findByIdForUpdate(roomId)).thenReturn(Optional.of(room));
        when(roomMapper.toRoomDataResponse(room, senderId)).thenReturn(expectedResponse);

        // when
        RoomDataResponse response = roomKeyService.uploadPendingRekeyKeys(roomId, request, principal);

        // then
        assertThat(response).isSameAs(expectedResponse);
        verify(roomAccessService).assertActiveMember(roomId, senderId);
        verify(roomRepository).findByIdForUpdate(roomId);
        verify(roomMapper).toRoomDataResponse(room, senderId);
        verifyNoInteractions(roomKeyMapper, roomKeyEnvelopeRepository, roomNotificationService);
    }

    @Test
    void uploadPendingRekeyKeys_givenRoomWithPendingRekey_whenUploadPendingRekeyKeys_thenSavesKeysUpdatesRoomAndPublishesNotification() {
        // given
        Long roomId = 10L;
        Long senderId = 1L;
        Principal principal = () -> "testuser";
        UploadRoomKeysRequest request = uploadRoomKeysRequest(5, 999L);
        Room room = room(roomId, 4, true);
        RoomKeyEnvelope firstEnvelope = roomKeyEnvelope(roomId, 5, 2L, senderId);
        RoomKeyEnvelope secondEnvelope = roomKeyEnvelope(roomId, 5, 3L, senderId);
        RoomDataResponse expectedResponse = roomDataResponse(roomId, 5, false);

        when(currentUserService.getCurrentUserId(principal)).thenReturn(senderId);
        when(roomRepository.findByIdForUpdate(roomId)).thenReturn(Optional.of(room));
        when(roomKeyMapper.toKeyEnvelope(request.keyItems().get(0), roomId, request.version(), senderId))
                .thenReturn(firstEnvelope);
        when(roomKeyMapper.toKeyEnvelope(request.keyItems().get(1), roomId, request.version(), senderId))
                .thenReturn(secondEnvelope);
        when(roomMapper.toRoomDataResponse(room, senderId)).thenReturn(expectedResponse);

        // when
        RoomDataResponse response = roomKeyService.uploadPendingRekeyKeys(roomId, request, principal);

        // then
        assertThat(response).isSameAs(expectedResponse);
        assertThat(room.getCurrentKeyVersion()).isEqualTo(5);
        assertThat(room.isRekeyRequired()).isFalse();

        verify(currentUserService).getCurrentUserId(principal);
        verify(roomAccessService).assertActiveMember(roomId, senderId);
        verify(roomAccessService).assertActiveMember(roomId, 2L);
        verify(roomAccessService).assertActiveMember(roomId, 3L);
        verify(roomKeyMapper).toKeyEnvelope(request.keyItems().get(0), roomId, 5, senderId);
        verify(roomKeyMapper).toKeyEnvelope(request.keyItems().get(1), roomId, 5, senderId);
        verify(roomKeyEnvelopeRepository).save(firstEnvelope);
        verify(roomKeyEnvelopeRepository).save(secondEnvelope);
        verify(roomMapper).toRoomDataResponse(room, senderId);
        verify(roomNotificationService).publishRoomKeyUpdated(request, roomId);
    }

    @Test
    void getMyKey_givenExistingEnvelope_whenGetMyKey_thenValidatesAccessAndReturnsMappedResponse() {
        // given
        Long myUserId = 1L;
        Long roomId = 10L;
        int version = 5;
        RoomKeyEnvelope envelope = roomKeyEnvelope(roomId, version, myUserId, 2L);
        MyKeyResponse expectedResponse = myKeyResponse(roomId, version, 2L);

        when(roomKeyEnvelopeRepository.findByRoomIdAndVersionAndForUserId(roomId, version, myUserId))
                .thenReturn(Optional.of(envelope));
        when(roomKeyMapper.toMyKeyResponse(envelope)).thenReturn(expectedResponse);

        // when
        MyKeyResponse response = roomKeyService.getMyKey(myUserId, roomId, version);

        // then
        assertThat(response).isSameAs(expectedResponse);
        verify(roomAccessService).assertActiveMember(roomId, myUserId);
        verify(roomKeyEnvelopeRepository).findByRoomIdAndVersionAndForUserId(roomId, version, myUserId);
        verify(roomKeyMapper).toMyKeyResponse(envelope);
    }

    @Test
    void getMyKey_givenMissingEnvelope_whenGetMyKey_thenThrowsNoSuchElementException() {
        // given
        Long myUserId = 1L;
        Long roomId = 10L;
        int version = 5;

        when(roomKeyEnvelopeRepository.findByRoomIdAndVersionAndForUserId(roomId, version, myUserId))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> roomKeyService.getMyKey(myUserId, roomId, version))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("No key envelope");

        verify(roomAccessService).assertActiveMember(roomId, myUserId);
        verify(roomKeyMapper, never()).toMyKeyResponse(org.mockito.ArgumentMatchers.any(RoomKeyEnvelope.class));
    }

    @Test
    void getAllMyKeysForRoom_givenExistingEnvelopes_whenGetAllMyKeysForRoom_thenReturnsMappedResponses() {
        // given
        Long myUserId = 1L;
        Long roomId = 10L;
        RoomKeyEnvelope firstEnvelope = roomKeyEnvelope(roomId, 4, myUserId, 2L);
        RoomKeyEnvelope secondEnvelope = roomKeyEnvelope(roomId, 5, myUserId, 3L);
        MyKeyResponse firstResponse = myKeyResponse(roomId, 4, 2L);
        MyKeyResponse secondResponse = myKeyResponse(roomId, 5, 3L);

        when(roomKeyEnvelopeRepository.findByRoomIdAndForUserId(roomId, myUserId))
                .thenReturn(List.of(firstEnvelope, secondEnvelope));
        when(roomKeyMapper.toMyKeyResponse(firstEnvelope)).thenReturn(firstResponse);
        when(roomKeyMapper.toMyKeyResponse(secondEnvelope)).thenReturn(secondResponse);

        // when
        List<MyKeyResponse> response = roomKeyService.getAllMyKeysForRoom(myUserId, roomId);

        // then
        assertThat(response).containsExactly(firstResponse, secondResponse);
        verify(roomKeyEnvelopeRepository).findByRoomIdAndForUserId(roomId, myUserId);
        verify(roomKeyMapper).toMyKeyResponse(firstEnvelope);
        verify(roomKeyMapper).toMyKeyResponse(secondEnvelope);
    }

    @Test
    void getMyKeyWithVersion_givenExistingEnvelope_whenGetMyKeyWithVersion_thenReturnsMappedResponse() {
        // given
        Long userId = 1L;
        Long roomId = 10L;
        int version = 5;
        RoomKeyEnvelope envelope = roomKeyEnvelope(roomId, version, userId, 2L);
        MyKeyResponse expectedResponse = myKeyResponse(roomId, version, 2L);

        when(roomKeyEnvelopeRepository.findByRoomIdAndVersionAndForUserId(roomId, version, userId))
                .thenReturn(Optional.of(envelope));
        when(roomKeyMapper.toMyKeyResponse(envelope)).thenReturn(expectedResponse);

        // when
        MyKeyResponse response = roomKeyService.getMyKeyWithVersion(userId, roomId, version);

        // then
        assertThat(response).isSameAs(expectedResponse);
        verify(roomKeyEnvelopeRepository).findByRoomIdAndVersionAndForUserId(roomId, version, userId);
        verify(roomKeyMapper).toMyKeyResponse(envelope);
    }

    @Test
    void getMyKeyWithVersion_givenMissingEnvelope_whenGetMyKeyWithVersion_thenThrowsNoSuchElementException() {
        // given
        Long userId = 1L;
        Long roomId = 10L;
        int version = 5;

        when(roomKeyEnvelopeRepository.findByRoomIdAndVersionAndForUserId(roomId, version, userId))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> roomKeyService.getMyKeyWithVersion(userId, roomId, version))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("No key envelope");

        verify(roomKeyMapper, never()).toMyKeyResponse(org.mockito.ArgumentMatchers.any(RoomKeyEnvelope.class));
    }

    private static UploadRoomKeysRequest uploadRoomKeysRequest(int version, Long wrappedByUserId) {
        return new UploadRoomKeysRequest(
                version,
                wrappedByUserId,
                List.of(
                        new UploadRoomKeysRequest.KeyItem(2L, "wrapped-room-key-2", "iv-2", "aad-2"),
                        new UploadRoomKeysRequest.KeyItem(3L, "wrapped-room-key-3", "iv-3", "aad-3")
                )
        );
    }

    private static Room room(Long roomId, int currentKeyVersion, boolean rekeyRequired) {
        Room room = new Room();
        room.setRoomId(roomId);
        room.setType(RoomType.GROUP);
        room.setName("Test room");
        room.setCurrentKeyVersion(currentKeyVersion);
        room.setRekeyRequired(rekeyRequired);

        return room;
    }

    private static RoomKeyEnvelope roomKeyEnvelope(Long roomId, int version, Long forUserId, Long wrappedByUserId) {
        RoomKeyEnvelope envelope = new RoomKeyEnvelope();
        envelope.setRoomId(roomId);
        envelope.setVersion(version);
        envelope.setForUserId(forUserId);
        envelope.setWrappedByUserId(wrappedByUserId);
        envelope.setWrappedRoomKeyB64("wrapped-room-key-" + forUserId);
        envelope.setIvB64("iv-" + forUserId);
        envelope.setAadB64("aad-" + forUserId);

        return envelope;
    }

    private static RoomDataResponse roomDataResponse(Long roomId, int currentKeyVersion, boolean rekeyRequired) {
        return RoomDataResponse.builder()
                .roomId(roomId)
                .roomName("Test room")
                .roomType(RoomType.GROUP.name())
                .currentKeyVersion(currentKeyVersion)
                .rekeyRequired(rekeyRequired)
                .activeMembership(true)
                .roomMembersDataList(List.of())
                .build();
    }

    private static MyKeyResponse myKeyResponse(Long roomId, int version, Long wrappedByUserId) {
        return MyKeyResponse.builder()
                .roomId(roomId)
                .version(version)
                .wrappedByUserId(wrappedByUserId)
                .wrappedRoomKeyB64("wrapped-room-key-1")
                .ivB64("iv-1")
                .aadB64("aad-1")
                .build();
    }
}
