package com.felipe.commonplace.note;

import com.felipe.commonplace.link.LinkService;
import com.felipe.commonplace.note.dto.NoteRequest;
import com.felipe.commonplace.note.dto.NoteResponse;
import com.felipe.commonplace.tag.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
