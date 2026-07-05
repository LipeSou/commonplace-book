package com.felipe.commonplace.tag.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Uma tag e quantas notas a carregam.
 */
@Schema(description = "Uma tag e quantas notas a carregam")
public record TagResponse(
        @Schema(description = "Nome da tag, sem o `#`", example = "zen")
        String name,
        @Schema(description = "Quantas notas usam esta tag", example = "3")
        long count
) {
}
