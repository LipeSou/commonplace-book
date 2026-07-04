package com.felipe.commonplace.note;

import com.felipe.commonplace.note.dto.NoteRequest;
import com.felipe.commonplace.note.dto.NoteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository repository;

    @Transactional
    public NoteResponse create(NoteRequest request) {
        Note note = new Note(request.title(), request.content());
        return NoteResponse.from(repository.save(note));
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> findAll() {
        return repository.findAllByOrderByUpdatedAtDesc().stream()
                .map(NoteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public NoteResponse findById(Long id) {
        return NoteResponse.from(getOrThrow(id));
    }

    @Transactional
    public NoteResponse update(Long id, NoteRequest request) {
        Note note = getOrThrow(id);
        note.setTitle(request.title());
        note.setContent(request.content());
        return NoteResponse.from(note);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getOrThrow(id));
    }

    private Note getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException(id));
    }
}
