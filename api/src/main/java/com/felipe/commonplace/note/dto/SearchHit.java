package com.felipe.commonplace.note.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Uma nota encontrada pela busca, com o trecho onde deu match")
public record SearchHit(
        @Schema(description = "A nota inteira, como em qualquer outro endpoint")
        NoteResponse note,
        @Schema(description = "Trecho do content em volta do match; o achado vem entre « e »",
                example = "o silêncio entre as «coisas»")
        String snippet
) {
}
