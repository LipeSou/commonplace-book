package com.felipe.commonplace.note.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Uma página de notas, na ordem pedida pelo endpoint")
public record NotePage(
        @Schema(description = "As notas desta página")
        List<NoteResponse> notes,
        @Schema(description = "Quantas notas existem ao todo", example = "137")
        long total,
        @Schema(description = "Página pedida (base zero)", example = "0")
        int page,
        @Schema(description = "Tamanho da página", example = "30")
        int size,
        @Schema(description = "Se ainda há nota depois desta página", example = "true")
        boolean hasMore
) {
}
