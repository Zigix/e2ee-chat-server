package org.chatapp.e2eechatserver.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUserRequest {

    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Username may not be blank")
    @Size(min = 5, max = 20, message = "Username must be between 5 and 20 characters long")
    private String username;

    @NotBlank(message = "Password may not be blank")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters long")
    private String password;

    private String rePassword;

    private String pubEcdhJwk;

    private VaultDto vault;
}
