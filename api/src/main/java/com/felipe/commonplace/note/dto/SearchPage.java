package com.felipe.commonplace.note.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Uma página de resultados, em ordem de relevância")
public record SearchPage(
        @Schema(description = "Os resultados desta página")
        List<SearchHit> results,
        @Schema(description = "Quantas notas casaram com a busca ao todo", example = "12")
        long total,
        @Schema(description = "Página pedida (base zero)", example = "0")
        int page,
        @Schema(description = "Tamanho da página", example = "20")
        int size,
        @Schema(description = "Se ainda há resultado depois desta página", example = "true")
        boolean hasMore
) {
}
