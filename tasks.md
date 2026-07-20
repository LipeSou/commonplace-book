# tasks.md — Commonplace Book · o trabalho

> As checklists do projeto. O *porquê* de cada fase está no `plan.md`.
> CC: trabalhe de cima pra baixo, ponta a ponta, teste antes de entregar. Marque `[x]` ao terminar.

**Marcadores:** ⚡ CC faz 100% · 👤 ponto de aprovação/uso do Felipe

---

## FASE 0 — Setup

- [x] ⚡ Criar estrutura do monorepo (`api/`, `desktop/`, raiz)
- [x] ⚡ Gerar projeto Spring via Initializr (Web, JPA, Postgres Driver, Flyway, Validation, Lombok)
- [x] ⚡ Scaffold Electron + Vue na pasta `desktop/` com nome **"Commonplace Book"** (`productName` no `package.json` e título do `BrowserWindow`)
- [x] ⚡ Escrever `docker-compose.yml` (postgres:16 + api + adminer opcional)
- [x] ⚡ Escrever `Dockerfile` do Spring Boot (build multi-stage)
- [x] ⚡ Escrever `start.sh` / `start.bat` (o "clica e sobe tudo")
- [x] ⚡ `.gitignore` na raiz cobrindo `target/` e `node_modules/`
- [x] ⚡ Validar que `docker-compose up` sobe API + banco limpo
- [x] 👤 Rodar o start script na minha máquina e confirmar que sobe

**Meta:** um comando e tudo vivo.

---

## FASE 1 — CRUD de Notas (a fundação)

- [x] ⚡ Entidade `Note` (`id`, `title`, `content` bruto, `created_at`, `updated_at`)
- [x] ⚡ Migration Flyway da tabela `notes`
- [x] ⚡ `NoteRepository`, `NoteService`, `NoteController` — CRUD completo
- [x] ⚡ DTOs de entrada/saída (nunca expor a entidade direto)
- [x] ⚡ Bean Validation nos DTOs + tratamento de erros / respostas HTTP padronizadas
- [x] ⚡ Testar todos os endpoints (curl) e confirmar `content` preservado byte a byte no banco

**Meta:** CRUD funcionando, `content` intacto.

---

## FASE 2 — Tags

- [x] ⚡ Tabelas `tags` e `note_tags` (many-to-many) + migration
- [x] ⚡ Mapeamento JPA do relacionamento
- [x] ⚡ Derivar tags do `content` (extrair `#hashtags`) ao salvar — sem nunca alterar o bruto
- [x] ⚡ Recalcular tags quando o `content` muda
- [x] ⚡ Endpoint de listar notas por tag + listar todas as tags
- [x] ⚡ UI: selos hanko de tags nos cartões e na tela da nota (carimbos, não pílulas) — clicar no selo filtra a lista
- [x] ⚡ UI: painel de tags na sidebar (lista todas + contagem, via `GET /api/tags`); tag ativa vira o único acento, clicar de novo limpa
- [ ] 👤 Testar no app: escrever `#tags` numa nota e ver os selos aparecerem

**Meta:** tags como dado derivado, sempre em sincronia com o conteúdo.

---

## FASE 3 — Links e backlinks (o grafo)

- [x] ⚡ Tabela `links` (`source_note_id`, `target_title`, `target_note_id`) + migration — título guardado para o link quebrado religar sozinho quando a nota alvo nascer
- [x] ⚡ Parsear `[[wikilinks]]` do `content` (regex própria, como o `TagExtractor`) e popular `links` ao salvar — aceita `[[Título|apelido]]` e `[[Título#seção]]`
- [x] ⚡ Recalcular links quando o `content` muda (e religar/soltar os que chegam quando o **título** muda)
- [x] ⚡ Endpoint de backlinks (`GET /api/notes/{id}/backlinks`)
- [x] ⚡ Endpoint do grafo (`GET /api/graph` — nós com contagem de backlinks + arestas)
- [x] ⚡ Reindex na subida: notas escritas antes da tabela `links` entram no grafo sozinhas
- [x] ⚡ UI: backlinks na tela da nota ("← N notas apontam para esta") + grafo (`縁 ver o grafo`) + contador `← N` no cartão + Ctrl/⌘+clique num `[[wikilink]]` abre a nota
- [ ] 👤 Testar no app: criar notas com `[[wikilinks]]` e navegar pelos backlinks/grafo

**Meta:** notas conectadas, backlinks funcionando.

---

## FASE 4 — Busca full-text

- [x] ⚡ Coluna `notes.search` (`tsvector` **gerada** pelo Postgres: título peso A, content peso B) + índice GIN — migration V4
- [x] ⚡ Recalcular o índice quando o `content` muda — de graça: coluna gerada nunca sai de sincronia
- [x] ⚡ Busca tolerante a acento ("pratica" acha "prática") via `cpb_unaccent` — migration V5
- [x] ⚡ Query nativa com `websearch_to_tsquery` (`"frase exata"`, `or`, `-excluir`), ranking `ts_rank` e trecho `ts_headline`
- [x] ⚡ Endpoint `GET /api/notes/search?q=&page=&size=` com paginação
- [x] ⚡ UI: campo de busca com glifo `⌕` (debounce de 250ms), resultados na lista com o trecho destacado, "carregar mais"
- [ ] 👤 Testar no app: buscar entre várias notas e sentir a relevância

**Meta:** busca rápida e relevante em todas as notas.

---

## FASE 5 — Frontend Electron + Vue

> **Decisão (2026-07-04):** o frontend evolui **junto** com cada fase de backend — cada fase
> tem seu item de UI e um 👤 de uso real. A FASE 5 guarda a fundação (feita) e o que sobrou.

**Design primeiro (a fundação visual) — feito:**
- [x] ⚡ Portar `tokens.css` pro `desktop/src/styles/` e importar uma vez, global
- [x] ⚡ Carregar as fontes (Shippori Mincho, Zen Kaku Gothic, JetBrains Mono)
- [x] ⚡ Implementar o toggle tinta↔papel (`data-theme` no root)

**Telas (seguindo o `DESIGN.md` — rodar o checklist dele antes de entregar cada uma):**
- [x] ⚡ Config de CORS / comunicação com `localhost:8080`
- [x] ⚡ Tela de lista de notas (cartões) + editor de markdown (mono, `content` bruto)
- [x] ⚡ Preview do markdown renderizado — virou **live preview estilo Obsidian** (CodeMirror 6: doc é o cru intacto, formatação é decoração; linha do cursor revela as marcas) + toggle 素 cru
- [x] ⚡ Permitir importar markdown para ele — arquivos avulsos **e** pasta inteira (`.md`/`.markdown`); título = nome do arquivo, `content` importado byte a byte
- [ ] 👤 **Usar o app de verdade e aprovar a UX** — um só acento por tela, espaço (ma), tags como carimbos. Pedir ajustes até ficar com a minha cara.

*(busca e backlinks/grafo migraram pras suas fases: 4 e 3)*

**Meta:** o Commonplace Book utilizável, aprovado por mim, fiel ao `DESIGN.md`.

---

## FASE 6 — Exportação

- [x] ⚡ `GET /api/export`: um `.md` por nota, empacotados num `.zip` — a API roda em container, então quem escolhe a pasta é o app (o zip baixa e você salva onde quiser). Títulos repetidos ganham sufixo `(2)`
- [x] ⚡ Frontmatter reconstruído no topo (`title`, `created`, `updated`, `tags`) — o `content` continua byte a byte embaixo
- [x] ⚡ UI: "Exportar" como `.btn-text` ao lado dos importes (o `ImportNotes.vue` virou `FileActions.vue`)
- [ ] 👤 Validar que os `.md` exportados abrem certo em outro app (Obsidian, etc.)

**Meta:** liberdade total — minhas notas saem pra qualquer lugar quando eu quiser.

---

## FASE 7 — Home: timeline por data + jornal de lembranças (温故知新)

> **Decisão (2026-07-20):** a home é a tela inicial. Timeline segue `created_at` (diário de
> captura — a sidebar já cobre `updated_at`). Lembranças casam o dia exato; dia sem lembrança
> fica em silêncio. O porquê está no `plan.md`; a cara do jornal está no `DESIGN.md`.

**Backend:**
- [ ] ⚡ Endpoint de timeline: `GET /api/notes/timeline?page=&size=` — notas por `created_at desc`,
      paginação no padrão do search (`hasMore`); agrupamento por dia é do cliente
- [ ] ⚡ Endpoint do jornal: `GET /api/journal?date=&zone=` — seções "há uma semana" (−7d),
      "há um mês" (mesmo dia, mês anterior) e "há N anos" (mesmo dia/mês de cada ano anterior
      com nota), calculadas em `Instant` com o fuso do cliente; seções vazias ficam de fora

**Frontend:**
- [ ] ⚡ Navegação por views no `App.vue`: ref único `view: 'home' | 'note' | 'graph'`,
      home como inicial; "início" na sidebar ao lado do grafo
- [ ] ⚡ `HomeView.vue`: jornal no topo + timeline embaixo; clicar em qualquer nota abre o editor
- [ ] ⚡ `JournalCard.vue` — o shinbun (spec no `DESIGN.md`): masthead 温故知新 com selo hanko
      (o único acento da tela), filetes, manchetes em display, colunas quando 2+ lembranças;
      vazio = convite ("Escreva algo que o seu eu de daqui a um ano vai reler.")
- [ ] ⚡ `Timeline.vue`: cabeçalhos de dia discretos (mono, muted) + `NoteCard` reutilizado +
      scroll infinito (IntersectionObserver na sentinela)
- [ ] 👤 Usar a home por alguns dias: o jornal convida a reler? A timeline dá vontade de scrollar?
      Ajustar até ficar com a minha cara

**Meta:** abrir o app virou ritual — o dia de hoje na frente, o passado voltando na hora certa.

---

## Backlog (sem ordem; cada um vira fase quando der vontade)

- [ ] Autenticação (Spring Security + JWT) — se eu quiser proteger ou sincronizar
- [ ] Módulo de hábitos/estudos (inglês, leitura, Anki) com streaks
- [ ] Módulo de saúde/treino
- [ ] Módulo de projetos e tarefas
- [ ] Versionamento/histórico de edição das notas
- [ ] Sincronização entre dispositivos
