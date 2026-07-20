package com.felipe.commonplace.note;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findAllByOrderByUpdatedAtDesc();

    List<Note> findByTags_NameOrderByUpdatedAtDesc(String name);

    /**
     * A linha do tempo: as notas na ordem em que nasceram, da mais nova para a mais velha.
     * É `created_at` (e não `updated_at`) porque a home é um diário de captura — reler uma
     * nota antiga não a traz de volta para hoje.
     */
    Page<Note> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * As notas nascidas dentro de um dia — o intervalo vem em {@code Instant}, já resolvido
     * no fuso de quem perguntou. Início incluído, fim excluído.
     */
    List<Note> findByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtAsc(
            Instant from, Instant to);

    /**
     * Quando nasceu a nota mais antiga — diz até que ano o jornal precisa procurar.
     * Vazio se ainda não há nota nenhuma.
     */
    @Query("select min(n.createdAt) from Note n")
    Optional<Instant> findOldestCreatedAt();

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
