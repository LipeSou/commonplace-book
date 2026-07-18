package com.felipe.commonplace.link.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Uma aresta do grafo: uma nota apontando para outra")
public record GraphEdge(
        @Schema(description = "Id da nota que escreveu o [[wikilink]]", example = "1")
        Long source,
        @Schema(description = "Id da nota apontada", example = "2")
        Long target
) {
}
