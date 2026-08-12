package org.chatapp.e2eechatserver.user.service;

import lombok.RequiredArgsConstructor;
import org.chatapp.e2eechatserver.common.exception.UserNotFoundException;
import org.chatapp.e2eechatserver.user.dto.SearchUserResponse;
import org.chatapp.e2eechatserver.user.entity.User;
import org.chatapp.e2eechatserver.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
    }

    @Override
    public List<SearchUserResponse> searchForUsers(String q) {
        List<User> foundUsers = userRepository.findAllByUsernameStartsWith(q);
        return foundUsers.stream().map(user -> new SearchUserResponse(user.getId(), user.getUsername())).toList();
    }

    @Override
    public String getPublicEcdhJwkForUser(Long userId) {
        User user =  userRepository.findById(userId).orElseThrow();
        return user.getPubEcdhJwk();
    }

    @Override
    public String getUsernameByUserId(Long userId) {
        return userRepository.findUsernameByUserId(userId).orElseThrow();
    }

    @Override
    public Long getUserIdByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow().getId();
    }

    @Override
    public User getUserByUserId(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new UserNotFoundException(String.format("User with id %d not found", userId)));
    }
}
