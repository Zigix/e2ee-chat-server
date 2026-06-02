package org.chatapp.e2eechatserver.auth.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VaultDto {

    @Min(1)
    int version;

    @NotBlank
    String kdfSaltB64;

    @Min(10_000)
    int kdfIterations;

    @NotBlank
    String wrappedMkB64;

    @NotBlank
    String wrappedMkIvB64;

    @NotBlank
    String wrappedEcdhPrivB64;

    @NotBlank
    String wrappedEcdhPrivIvB64;
}
