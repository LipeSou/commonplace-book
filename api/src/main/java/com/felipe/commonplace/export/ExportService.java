package com.felipe.commonplace.export;

import com.felipe.commonplace.note.Note;
import com.felipe.commonplace.note.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final NoteRepository repository;
    private final MarkdownExporter exporter;

    /**
     * Todas as notas num zip de arquivos `.md` — um por nota, com frontmatter no topo
     * e o content intacto embaixo. Títulos repetidos ganham sufixo para ninguém sumir.
     */
    @Transactional(readOnly = true)
    public byte[] zipAll() {
        List<Note> notes = repository.findAllByOrderByUpdatedAtDesc();
        Map<String, Integer> seen = new HashMap<>();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        try (ZipOutputStream zip = new ZipOutputStream(bytes, StandardCharsets.UTF_8)) {
            for (Note note : notes) {
                zip.putNextEntry(new ZipEntry(uniqueName(exporter.fileNameFor(note), seen)));
                zip.write(exporter.documentFor(note).getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao montar o zip da exportação", e);
        }
        return bytes.toByteArray();
    }

    private String uniqueName(String name, Map<String, Integer> seen) {
        int count = seen.merge(name.toLowerCase(), 1, Integer::sum);
        if (count == 1) {
            return name;
        }
        return name.substring(0, name.length() - ".md".length()) + " (" + count + ").md";
    }
}
