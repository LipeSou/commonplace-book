package com.felipe.commonplace.link;

import com.felipe.commonplace.link.dto.GraphResponse;
import com.felipe.commonplace.note.Note;
import com.felipe.commonplace.note.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository repository;
    private final NoteRepository notes;
    private final WikiLinkExtractor extractor;

    /**
     * Recalcula os links que SAEM da nota, a partir do content. Índice descartável:
     * joga fora os antigos e deriva tudo de novo. O content não é tocado.
     */
    @Transactional
    public void rebuildFor(Note note) {
        repository.deleteBySource(note);
        repository.flush();
        for (String title : extractor.extract(note.getContent())) {
            Note target = notes.findFirstByTitleIgnoreCaseOrderByIdAsc(title).orElse(null);
            repository.save(new Link(note, title, target));
        }
    }

    /**
     * Recalcula os links que CHEGAM na nota: solta os que apontavam para ela (caso o título
     * tenha mudado) e religa os quebrados que pediam o título atual.
     */
    @Transactional
    public void resolveIncoming(Note note) {
        repository.detachTarget(note);
        repository.attachTarget(note, note.getTitle());
    }

    /**
     * A nota vai sumir: os links dela morrem junto, os que apontavam para ela viram quebrados
     * (guardam o título, e religam se a nota renascer).
     */
    @Transactional
    public void removeFor(Note note) {
        repository.detachTarget(note);
        repository.deleteBySource(note);
    }

    /**
     * Recalcula o grafo inteiro a partir do content de todas as notas. Devolve quantas notas
     * passaram pelo parser. Como cada link resolve o alvo pelo título na hora, uma varredura
     * só já religa tudo.
     */
    @Transactional
    public int reindexAll() {
        List<Note> all = notes.findAll();
        all.forEach(this::rebuildFor);
        return all.size();
    }

    @Transactional(readOnly = true)
    public List<Note> findBacklinks(Note note) {
        return repository.findSourcesByTarget(note);
    }

    @Transactional(readOnly = true)
    public GraphResponse graph() {
        return new GraphResponse(repository.graphNodes(), repository.graphEdges());
    }
}
