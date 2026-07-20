package com.felipe.commonplace.journal.dto;

import com.felipe.commonplace.note.dto.NoteResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Uma lembrança: o que foi escrito num dia do passado")
public record JournalSection(
        @Schema(description = "Como o dia se chama para quem lê hoje", example = "Há um ano")
        String label,
        @Schema(description = "O dia exato de onde vieram estas notas", example = "2025-07-20")
        LocalDate date,
        @Schema(description = "As notas nascidas naquele dia, da mais antiga para a mais recente")
        List<NoteResponse> notes
) {
}
