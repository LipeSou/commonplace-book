package com.felipe.commonplace.link.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Um nó do grafo: uma nota")
public record GraphNode(
        @Schema(description = "Id da nota", example = "1")
        Long id,
        @Schema(description = "Título da nota", example = "Sobre o vazio")
        String title,
        @Schema(description = "Quantas notas apontam para esta", example = "3")
        Long backlinks
) {
}
