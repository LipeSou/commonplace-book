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
- [ ] 👤 Testar no app: escrever `#tags` numa nota e ver os selos aparecerem

**Meta:** tags como dado derivado, sempre em sincronia com o conteúdo.

---

## FASE 3 — Links e backlinks (o grafo)

- [ ] ⚡ Tabela `links` (`source_note_id`, `target_note_id`) + migration
- [ ] ⚡ Parsear `[[wikilinks]]` do `content` (commonmark-java) e popular `links` ao salvar
- [ ] ⚡ Recalcular links quando o `content` muda
- [ ] ⚡ Endpoint de backlinks ("quais notas apontam pra esta?")
- [ ] ⚡ Endpoint do grafo (nós + arestas) pro frontend desenhar
- [ ] ⚡ UI: backlinks na tela da nota ("← N notas apontam para esta") + visualização do grafo
- [ ] 👤 Testar no app: criar notas com `[[wikilinks]]` e navegar pelos backlinks/grafo

**Meta:** notas conectadas, backlinks funcionando.

---

## FASE 4 — Busca full-text

- [ ] ⚡ Coluna/índice `tsvector` derivado do `content` + índice GIN, via migration
- [ ] ⚡ Recalcular o índice quando o `content` muda
- [ ] ⚡ Query de busca (nativa) com ranking de relevância
- [ ] ⚡ Endpoint de busca com paginação
- [ ] ⚡ UI: campo de busca cidadão de primeira classe (glifo `⌕`, resultados na lista)
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

- [ ] ⚡ Endpoint que pega o `content` de cada nota e escreve arquivos `.md` numa pasta
- [ ] ⚡ Incluir frontmatter (tags, datas) reconstruído no topo de cada arquivo
- [ ] ⚡ UI: ação "Exportar" discreta (`.btn-text`) na tela
- [ ] 👤 Validar que os `.md` exportados abrem certo em outro app (Obsidian, etc.)

**Meta:** liberdade total — minhas notas saem pra qualquer lugar quando eu quiser.

---

## Backlog (sem ordem; cada um vira fase quando der vontade)

- [ ] Autenticação (Spring Security + JWT) — se eu quiser proteger ou sincronizar
- [ ] Módulo de hábitos/estudos (inglês, leitura, Anki) com streaks
- [ ] Módulo de saúde/treino
- [ ] Módulo de projetos e tarefas
- [ ] Versionamento/histórico de edição das notas
- [ ] Sincronização entre dispositivos
