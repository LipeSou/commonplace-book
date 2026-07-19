package com.felipe.commonplace.export;

import com.felipe.commonplace.note.Note;
import com.felipe.commonplace.tag.Tag;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Transforma uma nota num arquivo `.md` de exportação.
 *
 * O `content` sai **byte a byte**, sem tocar em nada — o frontmatter é acrescentado
 * acima dele, como metadado reconstruído (título, datas, tags). Nada aqui reescreve
 * o markdown do Felipe.
 */
@Component
public class MarkdownExporter {

    /** Caracteres proibidos em nome de arquivo no Windows (o mais restrito dos três sistemas). */
    private static final Pattern ILLEGAL = Pattern.compile("[\\\\/:*?\"<>|\\p{Cntrl}]");
    private static final Pattern SPACES = Pattern.compile("\\s+");
    private static final int MAX_NAME = 120;

    public String fileNameFor(Note note) {
        String name = SPACES.matcher(ILLEGAL.matcher(note.getTitle()).replaceAll("-")).replaceAll(" ").trim();
        // Windows não aceita nome terminado em ponto ou espaço
        name = name.replaceAll("[. ]+$", "");
        if (name.length() > MAX_NAME) {
            name = name.substring(0, MAX_NAME).trim();
        }
        return (name.isEmpty() ? "sem-titulo" : name) + ".md";
    }

    public String documentFor(Note note) {
        StringBuilder doc = new StringBuilder();
        doc.append("---\n");
        doc.append("title: ").append(yaml(note.getTitle())).append('\n');
        doc.append("created: ").append(note.getCreatedAt()).append('\n');
        doc.append("updated: ").append(note.getUpdatedAt()).append('\n');
        List<String> tags = note.getTags().stream().map(Tag::getName).sorted().toList();
        if (!tags.isEmpty()) {
            doc.append("tags: [").append(String.join(", ", tags)).append("]\n");
        }
        doc.append("---\n\n");
        doc.append(note.getContent());   // sagrado: nem trim, nem newline a mais
        return doc.toString();
    }

    /** Escalar YAML entre aspas, com o que precisa ser escapado escapado. */
    private String yaml(String value) {
        return '"' + value.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }
}
