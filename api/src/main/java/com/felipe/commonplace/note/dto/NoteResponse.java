package com.felipe.commonplace.note.dto;

import com.felipe.commonplace.note.Note;
import com.felipe.commonplace.tag.Tag;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Uma nota e suas tags derivadas")
public record NoteResponse(
        @Schema(description = "Identificador", example = "1")
        Long id,
        @Schema(description = "Título", example = "Sobre o vazio")
        String title,
        @Schema(description = "Markdown bruto, exatamente como foi salvo")
        String content,
        @Schema(description = "Quando a nota foi criada")
        Instant createdAt,
        @Schema(description = "Quando a nota foi editada pela última vez")
        Instant updatedAt,
        @Schema(description = "Tags derivadas do content (minúsculas, sem o `#`)", example = "[\"zen\"]")
        List<String> tags
) {

    public static NoteResponse from(Note note) {
        List<String> tags = note.getTags().stream()
                .map(Tag::getName)
                .sorted()
                .toList();
        return new NoteResponse(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getCreatedAt(),
                note.getUpdatedAt(),
                tags
        );
    }
}
