package com.felipe.commonplace.note.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Entrada de criação/atualização. O content aceita vazio (nota pode nascer
 * em branco), mas nunca null — e jamais é transformado.
 */
@Schema(description = "Dados para criar ou atualizar uma nota")
public record NoteRequest(
        @Schema(description = "Título da nota", example = "Sobre o vazio")
        @NotBlank(message = "O título não pode ficar em branco")
        String title,

        @Schema(description = "Markdown bruto, preservado byte a byte (vazio é válido)",
                example = "# Sobre o vazio\n\nToda prática começa no vazio. #zen")
        @NotNull(message = "O conteúdo não pode ser nulo (vazio é válido)")
        String content
) {
}
