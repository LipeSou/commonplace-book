package com.felipe.commonplace.journal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "O jornal de lembranças de um dia — 温故知新")
public record JournalResponse(
        @Schema(description = "O dia de referência (o \"hoje\" de quem perguntou)", example = "2026-07-20")
        LocalDate date,
        @Schema(description = """
                As lembranças achadas, das mais recentes para as mais antigas.
                Dia sem nota não vira seção: o silêncio é honesto.
                """)
        List<JournalSection> sections
) {
}
