package com.felipe.commonplace.note;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findAllByOrderByUpdatedAtDesc();

    List<Note> findByTags_NameOrderByUpdatedAtDesc(String name);

    /**
     * Resolve um {@code [[wikilink]]} pelo título, sem ligar para maiúsculas.
     * Títulos repetidos são permitidos: vence a nota mais antiga (a primeira a usar o nome).
     */
    Optional<Note> findFirstByTitleIgnoreCaseOrderByIdAsc(String title);

    /**
     * Busca full-text no índice `search` (coluna gerada: título com peso A, content com peso B,
     * ambos sem acento). A consulta é `websearch_to_tsquery`, a mesma gramática dos buscadores:
     * palavras soltas, {@code "frase exata"}, {@code or}, {@code -excluir}. Ordena por relevância
     * e, no empate, pela nota mais recente.
     *
     * O trecho (`ts_headline`) é tirado do content ORIGINAL — o que aparece na tela é o que está
     * escrito, com acento e tudo.
     */
    @Query(value = """
            select n.id as id,
                   ts_headline('portuguese', n.content,
                       websearch_to_tsquery('portuguese', cpb_unaccent(:q)),
                       'StartSel=«, StopSel=», MaxWords=28, MinWords=12, MaxFragments=1') as snippet
            from notes n
            where n.search @@ websearch_to_tsquery('portuguese', cpb_unaccent(:q))
            order by ts_rank(n.search, websearch_to_tsquery('portuguese', cpb_unaccent(:q))) desc,
                     n.updated_at desc
            """,
            countQuery = """
                    select count(*) from notes n
                    where n.search @@ websearch_to_tsquery('portuguese', cpb_unaccent(:q))
                    """,
            nativeQuery = true)
    Page<SearchProjection> search(@Param("q") String q, Pageable pageable);
}
