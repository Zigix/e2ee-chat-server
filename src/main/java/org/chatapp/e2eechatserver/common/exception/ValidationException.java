package org.chatapp.e2eechatserver.common.exception;

import lombok.Getter;
import org.chatapp.e2eechatserver.common.dto.FieldErrorDto;

import java.util.List;

@Getter
public class ValidationException extends RuntimeException {
    private List<FieldErrorDto> errors;

    public ValidationException(String message, List<FieldErrorDto> errors) {
        super(message);
        this.errors = errors;
    }

}
