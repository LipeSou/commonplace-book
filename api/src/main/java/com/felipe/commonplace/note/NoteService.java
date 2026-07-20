package com.felipe.commonplace.note;

import com.felipe.commonplace.link.LinkService;
import com.felipe.commonplace.note.dto.NotePage;
import com.felipe.commonplace.note.dto.NoteRequest;
import com.felipe.commonplace.note.dto.NoteResponse;
import com.felipe.commonplace.note.dto.SearchHit;
import com.felipe.commonplace.note.dto.SearchPage;
import com.felipe.commonplace.tag.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository repository;
    private final TagService tagService;
    private final LinkService linkService;

    @Transactional
    public NoteResponse create(NoteRequest request) {
        Note note = new Note(request.title(), request.content());
        note.setTags(tagService.resolveFrom(note.getContent()));
        Note saved = repository.save(note);
        reindex(saved);
        return NoteResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> findAll() {
        return repository.findAllByOrderByUpdatedAtDesc().stream()
                .map(NoteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> findByTag(String tag) {
        return repository.findByTags_NameOrderByUpdatedAtDesc(tag).stream()
                .map(NoteResponse::from)
                .toList();
    }

    /**
     * A linha do tempo da home: as notas na ordem em que nasceram. Quem agrupa por dia
     * é a tela — aqui sai a página crua, do mais novo para o mais velho.
     */
    @Transactional(readOnly = true)
    public NotePage timeline(int page, int size) {
        Page<Note> found = repository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        return new NotePage(
                found.map(NoteResponse::from).toList(),
                found.getTotalElements(),
                page,
                size,
                found.hasNext()
        );
    }

    @Transactional(readOnly = true)
    public NoteResponse findById(Long id) {
        return NoteResponse.from(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> findBacklinks(Long id) {
        return linkService.findBacklinks(getOrThrow(id)).stream()
                .map(NoteResponse::from)
                .toList();
    }

    /**
     * Busca full-text. A ordem vem do ranking do Postgres; as notas são carregadas
     * depois e reordenadas para respeitá-la.
     */
    @Transactional(readOnly = true)
    public SearchPage search(String query, int page, int size) {
        Page<SearchProjection> hits = repository.search(query, PageRequest.of(page, size));
        Map<Long, Note> byId = repository.findAllById(hits.map(SearchProjection::getId).toList()).stream()
                .collect(Collectors.toMap(Note::getId, note -> note));

        List<SearchHit> results = hits.stream()
                .filter(hit -> byId.containsKey(hit.getId()))
                .map(hit -> new SearchHit(NoteResponse.from(byId.get(hit.getId())), hit.getSnippet()))
                .toList();

        return new SearchPage(results, hits.getTotalElements(), page, size, hits.hasNext());
    }

    @Transactional
    public NoteResponse update(Long id, NoteRequest request) {
        Note note = getOrThrow(id);
        note.setTitle(request.title());
        note.setContent(request.content());
        note.setTags(tagService.resolveFrom(note.getContent()));
        reindex(note);
        return NoteResponse.from(note);
    }

    @Transactional
    public void delete(Long id) {
        Note note = getOrThrow(id);
        linkService.removeFor(note);
        repository.delete(note);
    }

    /** Recalcula o grafo em volta da nota: o que sai dela e o que chega nela. */
    private void reindex(Note note) {
        linkService.rebuildFor(note);
        linkService.resolveIncoming(note);
    }

    private Note getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException(id));
    }
}
