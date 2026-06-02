package org.chatapp.e2eechatserver.auth.validation;

import lombok.AllArgsConstructor;
import org.chatapp.e2eechatserver.auth.dto.RegisterUserRequest;
import org.chatapp.e2eechatserver.common.dto.FieldErrorDto;
import org.chatapp.e2eechatserver.common.exception.ValidationException;
import org.chatapp.e2eechatserver.user.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class RegisterUserValidator {
    private final UserRepository userRepository;

    public void validate(RegisterUserRequest registerUserRequest) {
        List<FieldErrorDto> errorsList = new ArrayList<>();

        if (userRepository.existsByEmail(registerUserRequest.getEmail())) {
            errorsList.add(new FieldErrorDto("email", "Email already exists"));
        }

        if (userRepository.existsByUsername(registerUserRequest.getUsername())) {
            errorsList.add(new FieldErrorDto("username", "Username already exists"));
        }

        if (!registerUserRequest.getPassword().equals(registerUserRequest.getRePassword())) {
            errorsList.add(new FieldErrorDto("rePassword", "Passwords do not match"));
        }

        if (!errorsList.isEmpty()) {
            throw new ValidationException("Validation failed", errorsList);
        }
    }
}
