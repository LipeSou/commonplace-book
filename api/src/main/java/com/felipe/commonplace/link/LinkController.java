package com.felipe.commonplace.link;

import com.felipe.commonplace.link.dto.GraphResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/graph")
@RequiredArgsConstructor
@Tag(name = "Grafo", description = "As notas e os links entre elas, derivados dos `[[wikilinks]]`.")
public class LinkController {

    private final LinkService service;

    @GetMapping
    @Operation(summary = "O grafo inteiro",
            description = "Nós (notas, com a contagem de backlinks) e arestas (links resolvidos).")
    public GraphResponse graph() {
        return service.graph();
    }
}
