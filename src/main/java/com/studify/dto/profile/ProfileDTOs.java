package com.studify.dto.profile;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class ProfileDTOs {

    public record ProfileResponse(
            Long id,
            String name,
            String email,
            String profilePicture,
            String course,
            String institution,
            Integer semester,
            String timezone,
            LocalDateTime createdAt
    ) {}

    public record UpdateRequest(
            @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
            String name,

            @Size(max = 150, message = "Curso deve ter no máximo 150 caracteres")
            String course,

            @Size(max = 150, message = "Instituição deve ter no máximo 150 caracteres")
            String institution,

            @Min(value = 1, message = "Semestre mínimo é 1")
            @Max(value = 20, message = "Semestre máximo é 20")
            Integer semester,

            @Pattern(regexp = "^[A-Za-z]+/[A-Za-z_]+$",
                     message = "Timezone inválido (ex: America/Sao_Paulo)")
            String timezone
    ) {}

    public record ChangePasswordRequest(
            @NotBlank(message = "Senha atual é obrigatória")
            String currentPassword,

            @NotBlank(message = "Nova senha é obrigatória")
            @Size(min = 8, message = "Nova senha deve ter no mínimo 8 caracteres")
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                     message = "Nova senha deve conter ao menos uma letra e um número")
            String newPassword
    ) {}
}
