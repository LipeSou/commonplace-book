package com.felipe.commonplace.link.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "O grafo inteiro: todas as notas e os links resolvidos entre elas")
public record GraphResponse(
        @Schema(description = "As notas")
        List<GraphNode> nodes,
        @Schema(description = "Os links que encontraram sua nota alvo (links quebrados ficam de fora)")
        List<GraphEdge> edges
) {
}
