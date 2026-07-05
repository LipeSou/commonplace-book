# CLAUDE.md — Commonplace Book

> **Este arquivo é carregado em toda sessão. Leia antes de qualquer coisa.**
> É a lei permanente do projeto. O *porquê* das decisões está em `plan.md`.
> O trabalho atual está em `tasks.md`. As regras de UI estão em `DESIGN.md`.

---

## O projeto

Um **Commonplace Book**: app desktop pessoal para coletar, conectar e recuperar conhecimento em markdown. A tradição do caderno de ideias — de John Locke a Darwin — com backend próprio.
É o app do Felipe, com a cara dele. Não pretende substituir o Obsidian — ele mora no app dele.

---

## ⚖️ Modo de trabalho (lei — vale em toda sessão)

**Você (Claude Code) constrói. O Felipe é o dono do produto.**

- **Você implementa tudo**: backend, banco, migrations, frontend, infra. Autonomia máxima.
- **O Felipe decide e aprova**: direção do produto, prioridades, como o app deve se comportar, se a UI ficou boa.
- **Não pergunte permissão pra detalhes técnicos** — decida pelo padrão mais simples e idiomático e siga. Explique só quando a decisão afetar o produto (algo que o Felipe vai ver ou sentir usando).
- **Entregue funcionando**: cada tarefa termina com o app rodando, testado por você (curl nos endpoints, app abrindo). Nunca entregue quebrado pro Felipe descobrir.
- **Nunca commite.** Quem commita é o Felipe. Ao terminar uma parte, avise que terminou e sugira a mensagem de commit pronta pra ele copiar (ex.: `feat: ...`).
- Quando terminar uma fase, resuma em 3-5 linhas o que foi feito e o que vem a seguir.

---

## Legenda de responsabilidade (aplicada em cada tarefa do `tasks.md`)

| Marca | Quem faz | O quê |
|---|---|---|
| ⚡ **CC** | Você faz 100%, ponta a ponta | Todo o código, config, infra, testes |
| 👤 **Felipe** | Só ele pode fazer | Usar o app, aprovar UX, decidir direção do produto |

---

## 🔒 Princípio inegociável de dados

**O markdown bruto é sagrado e imutável.** Fica numa coluna `content`, exatamente como foi digitado.
Tudo o mais — tags, links, índice de busca — é **derivado** dele, descartável e recalculável.
Nunca altere o `content` pra caber em alguma estrutura. Isso mantém a exportação futura (.md) trivial.

---

## Stack

- **Backend:** Java 21 + Spring Boot 3.x · Maven
- **Banco:** PostgreSQL 16 · Spring Data JPA + Hibernate · Flyway (migrations) · Bean Validation · Lombok
- **Parsing markdown:** commonmark-java
- **Desktop:** Electron + Vue
- **Infra local:** Docker Compose (API + Postgres), sobe com um comando
- **Repo:** monorepo único — `api/` (Spring) + `desktop/` (Electron+Vue)

---

## Como rodar (depois da FASE 0)

- Tudo sobe com **um comando**: `docker-compose up` (API + Postgres; Adminer opcional).
- API em `http://localhost:8080`. O desktop (Electron) roda **fora** do Docker e fala com a API por HTTP.
- **Doc da API (Swagger UI):** `http://localhost:8080/swagger-ui.html` — endpoints interativos, gerados do código.
- **Doc do banco:** `docs/DATABASE.md` (diagrama ER em Mermaid + dicionário das tabelas).
- O `content` das notas é a fonte da verdade no Postgres; nunca mexer nele "por fora".

---

## Frontend / Design

- **Antes de gerar ou editar QUALQUER UI, leia `DESIGN.md`.**
- Cores, tipo e espaçamento vêm **só** de `tokens.css` (tokens semânticos). Nunca `#hex` cru num componente.
- Princípios que você tende a furar sozinho: **um só acento (`--accent`) por tela**, espaço generoso (間 ma), tags são **carimbos** (não pílulas). Rode o checklist do `DESIGN.md` antes de entregar cada tela — a aprovação final da UI é do Felipe.

---

## Mapa dos arquivos (onde está o quê)

| Arquivo | O que é | Frequência de mudança |
|---|---|---|
| `CLAUDE.md` | Esta lei. Modo de trabalho, princípios, como rodar. | Raro |
| `plan.md` | O **porquê**: decisões, arquitetura. | Raro |
| `tasks.md` | O **trabalho**: checklists por fase. | Toda sessão |
| `DESIGN.md` | Regras de UI. Ler antes de gerar tela. | Ocasional |
| `tokens.css` | Tokens de design (cor/tipo/espaço). Fonte única. | Ocasional |
| `docs/DATABASE.md` | Diagrama ER (Mermaid) + dicionário das tabelas. | A cada migration |

---

## Como trabalhamos juntos

1. Comece toda sessão olhando o `tasks.md` — pegue a próxima tarefa não concluída, de cima pra baixo.
2. Implemente ponta a ponta e **teste antes de entregar** (endpoints com curl, app abrindo, tema trocando).
3. Mantenha o `tasks.md` atualizado: marque `[x]`, adicione tarefas que surgirem.
4. Decisões grandes (stack, arquitetura) vão pro `plan.md`, não pro `tasks.md`.
5. Nas tarefas 👤, pare e espere o Felipe: são os pontos de aprovação dele (UX, direção, uso real).
6. Código limpo e idiomático mesmo sem revisão humana — o Felipe pode querer ler qualquer parte um dia.
