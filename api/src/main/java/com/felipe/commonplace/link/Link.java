package com.felipe.commonplace.link;

import com.felipe.commonplace.note.Note;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Uma aresta do grafo, derivada de um {@code [[wikilink]]} do content.
 * {@code target} nulo = link quebrado: a nota alvo ainda não existe, mas o
 * {@code targetTitle} fica guardado para religar quando ela nascer.
 */
@Entity
@Table(name = "links")
@Getter
@Setter
@NoArgsConstructor
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_note_id", nullable = false)
    private Note source;

    @Column(name = "target_title", nullable = false)
    private String targetTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_note_id")
    private Note target;

    public Link(Note source, String targetTitle, Note target) {
        this.source = source;
        this.targetTitle = targetTitle;
        this.target = target;
    }
}
