package com.felipe.commonplace.export;

import com.felipe.commonplace.note.Note;
import com.felipe.commonplace.tag.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownExporterTest {

    private final MarkdownExporter exporter = new MarkdownExporter();

    @Test
    void escreveFrontmatterAcimaDoContentIntacto() {
        String content = "# Sobre o vazio\n\n  linha com espaços à esquerda\n\n#zen\n";
        String doc = exporter.documentFor(note("O vazio", content, "zen", "mu"));

        assertThat(doc).isEqualTo("""
                ---
                title: "O vazio"
                created: 2026-07-01T10:00:00Z
                updated: 2026-07-02T10:00:00Z
                tags: [mu, zen]
                ---

                """ + content);
    }

    @Test
    void notaSemTagNaoGanhaChaveVazia() {
        assertThat(exporter.documentFor(note("Só isso", "texto"))).doesNotContain("tags:");
    }

    @Test
    void contentSaiByteAByte() {
        String content = "sem newline no fim, com \"aspas\" e \\barra";
        assertThat(exporter.documentFor(note("Cru", content))).endsWith(content);
    }

    @Test
    void aspasNoTituloSaemEscapadasNoYaml() {
        assertThat(exporter.documentFor(note("O \"vazio\"", "x")))
                .contains("title: \"O \\\"vazio\\\"\"");
    }

    @Test
    void nomeDeArquivoSobreviveATituloHostil() {
        assertThat(exporter.fileNameFor(note("dia 1/2: notas?", "x"))).isEqualTo("dia 1-2- notas-.md");
        assertThat(exporter.fileNameFor(note("   ", "x"))).isEqualTo("sem-titulo.md");
        assertThat(exporter.fileNameFor(note("ponto no fim.", "x"))).isEqualTo("ponto no fim.md");
        assertThat(exporter.fileNameFor(note("Zen", "x"))).isEqualTo("Zen.md");
    }

    private Note note(String title, String content, String... tags) {
        Note note = new Note(title, content);
        note.setCreatedAt(Instant.parse("2026-07-01T10:00:00Z"));
        note.setUpdatedAt(Instant.parse("2026-07-02T10:00:00Z"));
        Set<Tag> set = new LinkedHashSet<>();
        for (String tag : tags) {
            set.add(new Tag(tag));
        }
        note.setTags(set);
        return note;
    }
}
