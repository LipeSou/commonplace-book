package com.felipe.commonplace.note;

import com.felipe.commonplace.note.dto.NoteRequest;
import com.felipe.commonplace.note.dto.NoteResponse;
import com.felipe.commonplace.note.dto.SearchPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
@Tag(name = "Notas", description = "CRUD das notas. O `content` é o markdown bruto, preservado byte a byte.")
public class NoteController {

    private final NoteService service;

    @PostMapping
    @Operation(summary = "Cria uma nota", description = "Salva o markdown bruto e deriva as tags do content.")
    @ApiResponse(responseCode = "201", description = "Nota criada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos (título em branco ou content nulo)")
    public ResponseEntity<NoteResponse> create(@Valid @RequestBody NoteRequest request) {
        NoteResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(summary = "Lista as notas",
            description = "Todas as notas (mais recentes primeiro). Com `?tag=`, filtra pelas que carregam a tag.")
    public List<NoteResponse> findAll(
            @Parameter(description = "Filtra pelas notas que têm esta tag (sem o `#`)")
            @RequestParam(required = false) String tag) {
        return (tag == null || tag.isBlank()) ? service.findAll() : service.findByTag(tag);
    }

    @GetMapping("/search")
    @Operation(summary = "Busca full-text nas notas",
            description = """
                    Procura no título (peso maior) e no content, com ranking de relevância.
                    A consulta aceita a gramática de buscador: palavras soltas, `"frase exata"`,
                    `or`, `-excluir`. Cada resultado vem com o trecho onde deu match.
                    """)
    public SearchPage search(
            @Parameter(description = "O que procurar", example = "vazio zen")
            @RequestParam String q,
            @Parameter(description = "Página, base zero") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Resultados por página (1 a 100)") @RequestParam(defaultValue = "20") int size) {
        if (q.isBlank()) {
            return new SearchPage(List.of(), 0, page, size, false);
        }
        return service.search(q, Math.max(page, 0), Math.min(Math.max(size, 1), 100));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma nota pelo id")
    @ApiResponse(responseCode = "200", description = "Nota encontrada")
    @ApiResponse(responseCode = "404", description = "Nota não encontrada")
    public NoteResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/{id}/backlinks")
    @Operation(summary = "As notas que apontam para esta",
            description = "Quem escreveu um `[[wikilink]]` para o título desta nota. A própria não conta.")
    @ApiResponse(responseCode = "200", description = "Backlinks encontrados (pode vir lista vazia)")
    @ApiResponse(responseCode = "404", description = "Nota não encontrada")
    public List<NoteResponse> backlinks(@PathVariable Long id) {
        return service.findBacklinks(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma nota", description = "Reescreve título e content, e recalcula as tags.")
    @ApiResponse(responseCode = "200", description = "Nota atualizada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "404", description = "Nota não encontrada")
    public NoteResponse update(@PathVariable Long id, @Valid @RequestBody NoteRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma nota")
    @ApiResponse(responseCode = "204", description = "Nota excluída")
    @ApiResponse(responseCode = "404", description = "Nota não encontrada")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
