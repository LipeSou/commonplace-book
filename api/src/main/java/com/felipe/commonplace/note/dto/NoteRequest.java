package com.felipe.commonplace.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Entrada de criação/atualização. O content aceita vazio (nota pode nascer
 * em branco), mas nunca null — e jamais é transformado.
 */
public record NoteRequest(
        @NotBlank(message = "O título não pode ficar em branco")
        String title,

        @NotNull(message = "O conteúdo não pode ser nulo (vazio é válido)")
        String content
) {
}
