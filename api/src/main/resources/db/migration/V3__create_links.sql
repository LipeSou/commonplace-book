-- Links derivados dos [[wikilinks]] do content. Índice descartável: recalculado a cada save.
-- target_note_id nulo = link quebrado (a nota alvo ainda não existe); o título fica guardado
-- para religar sozinho quando alguém criar (ou renomear para) esse título.
CREATE TABLE links (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    source_note_id BIGINT NOT NULL REFERENCES notes (id) ON DELETE CASCADE,
    target_title   TEXT   NOT NULL,
    target_note_id BIGINT REFERENCES notes (id) ON DELETE SET NULL,
    UNIQUE (source_note_id, target_title)
);

CREATE INDEX idx_links_target_note ON links (target_note_id);
CREATE INDEX idx_links_target_title ON links (lower(target_title));
