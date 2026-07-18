-- Índice de busca derivado do content — coluna gerada pelo próprio Postgres:
-- ninguém precisa lembrar de recalcular, ela nunca sai de sincronia com o texto.
-- Peso A no título (o que a nota diz que é), peso B no corpo.
ALTER TABLE notes
    ADD COLUMN search tsvector
        GENERATED ALWAYS AS (
            setweight(to_tsvector('portuguese', title), 'A') ||
            setweight(to_tsvector('portuguese', content), 'B')
        ) STORED;

CREATE INDEX idx_notes_search ON notes USING GIN (search);
