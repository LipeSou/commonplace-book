package com.felipe.commonplace.note;

public class NoteNotFoundException extends RuntimeException {

    public NoteNotFoundException(Long id) {
        super("Nota " + id + " não encontrada");
    }
}
