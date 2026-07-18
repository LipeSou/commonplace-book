-- Busca não deve cobrar acento de quem está com pressa: "pratica" acha "prática".
-- unaccent() é STABLE (depende do dicionário ativo); fixando o dicionário na chamada,
-- a função vira IMMUTABLE e pode ser usada em coluna gerada.
CREATE EXTENSION IF NOT EXISTS unaccent;

CREATE FUNCTION cpb_unaccent(text) RETURNS text
    LANGUAGE sql IMMUTABLE STRICT PARALLEL SAFE
AS $$ SELECT public.unaccent('public.unaccent', $1) $$;

-- recria o índice derivado passando o texto pelo unaccent (o content segue intocado)
ALTER TABLE notes DROP COLUMN search;

ALTER TABLE notes
    ADD COLUMN search tsvector
        GENERATED ALWAYS AS (
            setweight(to_tsvector('portuguese', cpb_unaccent(title)), 'A') ||
            setweight(to_tsvector('portuguese', cpb_unaccent(content)), 'B')
        ) STORED;

CREATE INDEX idx_notes_search ON notes USING GIN (search);
