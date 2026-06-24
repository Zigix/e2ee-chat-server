package org.chatapp.e2eechatserver.user.service;

import org.chatapp.e2eechatserver.user.dto.SearchUserResponse;
import org.chatapp.e2eechatserver.user.entity.User;
import org.chatapp.e2eechatserver.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void loadUserByUsername_givenExistingUsername_whenLoadUserByUsername_thenReturnsUserDetails() {
        // given
        User user = user(1L, "alice");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        // when
        UserDetails userDetails = userService.loadUserByUsername("alice");

        // then
        assertThat(userDetails).isSameAs(user);
        verify(userRepository).findByUsername("alice");
    }

    @Test
    void loadUserByUsername_givenMissingUsername_whenLoadUserByUsername_thenThrowsUsernameNotFoundException() {
        // given
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> userService.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("missing");

        verify(userRepository).findByUsername("missing");
    }

    @Test
    void searchForUsers_givenUsernamePrefix_whenSearchForUsers_thenReturnsMappedUsers() {
        // given
        User alice = user(1L, "alice");
        User alicia = user(2L, "alicia");

        when(userRepository.findAllByUsernameStartsWith("ali")).thenReturn(List.of(alice, alicia));

        // when
        List<SearchUserResponse> response = userService.searchForUsers("ali");

        // then
        assertThat(response)
                .extracting(SearchUserResponse::getId, SearchUserResponse::getUsername)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(1L, "alice"),
                        org.assertj.core.groups.Tuple.tuple(2L, "alicia")
                );
        verify(userRepository).findAllByUsernameStartsWith("ali");
    }

    @Test
    void getPublicEcdhJwkForUser_givenExistingUserId_whenGetPublicEcdhJwkForUser_thenReturnsPublicKey() {
        // given
        User user = user(1L, "alice");
        user.setPubEcdhJwk("alice-public-key");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        String publicKey = userService.getPublicEcdhJwkForUser(1L);

        // then
        assertThat(publicKey).isEqualTo("alice-public-key");
        verify(userRepository).findById(1L);
    }

    @Test
    void getPublicEcdhJwkForUser_givenMissingUserId_whenGetPublicEcdhJwkForUser_thenThrowsNoSuchElementException() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> userService.getPublicEcdhJwkForUser(1L))
                .isInstanceOf(NoSuchElementException.class);

        verify(userRepository).findById(1L);
    }

    @Test
    void getUsernameByUserId_givenExistingUserId_whenGetUsernameByUserId_thenReturnsUsername() {
        // given
        when(userRepository.findUsernameByUserId(1L)).thenReturn(Optional.of("alice"));

        // when
        String username = userService.getUsernameByUserId(1L);

        // then
        assertThat(username).isEqualTo("alice");
        verify(userRepository).findUsernameByUserId(1L);
    }

    @Test
    void getUsernameByUserId_givenMissingUserId_whenGetUsernameByUserId_thenThrowsNoSuchElementException() {
        // given
        when(userRepository.findUsernameByUserId(1L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> userService.getUsernameByUserId(1L))
                .isInstanceOf(NoSuchElementException.class);

        verify(userRepository).findUsernameByUserId(1L);
    }

    @Test
    void getUserIdByUsername_givenExistingUsername_whenGetUserIdByUsername_thenReturnsUserId() {
        // given
        User user = user(1L, "alice");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        // when
        Long userId = userService.getUserIdByUsername("alice");

        // then
        assertThat(userId).isEqualTo(1L);
        verify(userRepository).findByUsername("alice");
    }

    @Test
    void getUserIdByUsername_givenMissingUsername_whenGetUserIdByUsername_thenThrowsNoSuchElementException() {
        // given
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> userService.getUserIdByUsername("missing"))
                .isInstanceOf(NoSuchElementException.class);

        verify(userRepository).findByUsername("missing");
    }

    private static User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setEmail(username + "@example.com");
        user.setUsername(username);
        user.setPassword("encoded-password");
        user.setPubEcdhJwk(username + "-public-key");
        user.setVaultVersion(1);
        user.setKdfSaltB64("salt-" + username);
        user.setKdfIterations(10000);
        user.setWrappedMkB64("wrappedMk");
        user.setWrappedMkIvB64("wrappedMkIv");
        user.setWrappedEcdhPrivB64("wrappedEcdhPriv");
        user.setWrappedEcdhPrivIvB64("wrappedEcdhPrivIv");

        return user;
    }
}
