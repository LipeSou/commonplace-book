package com.felipe.commonplace.note;

import org.springframework.data.jpa.repository.JpaRepository;

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
}
