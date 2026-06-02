package org.chatapp.e2eechatserver.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@ToString
@Data
@NoArgsConstructor
public class ApiValidationFailedResponse {
    private String message;
    private List<FieldErrorDto> errors;
}
