# plan.md — Commonplace Book (decisões & estratégia)

> Este é o documento de **pensamento**: o *porquê* do projeto ser como é.
> Muda raramente. As tarefas do dia a dia vivem em `tasks.md`. As regras de UI em `DESIGN.md`.
> O `CLAUDE.md` carrega o essencial (modo de trabalho, princípios) toda sessão.

---

## O que é

Um **Commonplace Book**: app desktop pessoal para coletar, conectar e recuperar conhecimento em markdown.
O nome tem história: de John Locke a Darwin, commonplace books foram sistemas de busca de conhecimento
por séculos. É literalmente isso que estou construindo — o meu, com a minha cara, crescendo aos poucos.
Não pretende substituir o Obsidian — moro no meu app.

**Modo de construção:** o Claude Code implementa tudo, ponta a ponta. Eu sou o dono do produto —
decido a direção, uso, aprovo e peço ajustes. Velocidade máxima: MVP em dias, não semanas.

---

## Decisões fechadas (a stack)

- **Backend:** Java 21 (LTS) + Spring Boot 3.x
- **Build:** Maven
- **Banco:** PostgreSQL 16
- **Acesso a dados:** Spring Data JPA + Hibernate
- **Migrations:** Flyway
- **Validação:** Bean Validation (Jakarta)
- **Boilerplate:** Lombok
- **Parsing markdown:** commonmark-java
- **Desktop:** Electron + Vue (UI em stack que já conheço, caso eu queira mexer)
- **Infra local:** Docker Compose (API + Postgres) — sobe com um comando
- **Repositório:** monorepo único (`api/` + `desktop/` como pastas irmãs)
- **Design:** direção visual tinta sumi-e / yin-yang (Zen, taoísmo, bushido). Tokens em `tokens.css`, regras em `DESIGN.md`. Sistema vivo de referência, não design system pesado.

### Por que essas escolhas (resumo das conversas)

- **Java/Spring** é o habitat natural pra CRUD + banco: maduro, estável, o CC domina bem. Nota honesta: a stack foi originalmente escolhida por um objetivo de aprendizado que deixei de lado; ela continua ótima pro caso, mas se um dia a manutenção pesar, migrar o backend pra Node/TS (minha stack) é uma opção aberta.
- **Electron** porque a UI fica em JS/Vue — se eu quiser mexer em algo da interface, é território meu. O app pesado não me custa nada num uso pessoal.
- **Opção A — markdown como texto no Postgres** (e não arquivos em disco) porque decidi morar no meu app, não editar junto no Obsidian. Isso elimina a complexidade de reconciliação/file-watching. A exportação vira recurso futuro, não obrigação.
- **Monorepo** porque é solo, as peças andam juntas, e o "um comando" pede um lugar só.

---

## 🔒 Princípio de modelagem (inegociável)

> **O markdown bruto é sagrado e imutável.** Fica numa coluna `content`, exatamente como digitei.
> Todo o resto (tags, links, índice de busca) é **derivado** dele — descartável e recalculável.
>
> A regra mental: **o `content` cru é sagrado; todo o resto é índice que posso jogar fora e reconstruir.**
> É isso que mantém a porta da exportação (.md pra outros apps) aberta sem custo nenhum no presente.

---

## Arquitetura

```
┌─────────────────────────┐
│  Electron + Vue (desktop)│   ← rodo fora do Docker (app nativo)
└───────────┬─────────────┘
            │ HTTP REST (localhost:8080)
┌───────────▼─────────────┐
│   Spring Boot (Java)    │   ← regras, parsing, busca
└───────────┬─────────────┘
            │ JPA / SQL
┌───────────▼─────────────┐
│   PostgreSQL 16         │   ← content bruto + índices derivados
└─────────────────────────┘
     (API + banco dentro do Docker Compose)
```

---

## As fases — o porquê da ordem

A ordem segue a dependência natural dos dados: primeiro o dado sagrado, depois as derivações por cima.
As checklists detalhadas estão no `tasks.md`.

- **FASE 0 — Setup.** Infra completa: um comando e tudo vivo.
- **FASE 1 — CRUD de Notas.** A fundação: o `content` bruto entrando e saindo intacto.
- **FASE 2 — Tags.** Primeira derivação: `#hashtags` extraídas do conteúdo.
- **FASE 3 — Links e backlinks.** O grafo: `[[wikilinks]]` derivados, notas conectadas.
- **FASE 4 — Busca full-text.** `tsvector` + índice GIN no Postgres, derivados do `content`.
- **FASE 5 — Frontend.** As telas, fiéis ao `DESIGN.md`. Aqui entra minha aprovação de UX.
- **FASE 6 — Exportação.** A recompensa do content sagrado: exportar `.md` vira trivial.
- **FASE 7 — Home & jornal de lembranças.** A tela que alimenta o hábito: timeline por data + o passado voltando na hora certa.

> **MVP rápido:** com o CC fazendo tudo, FASE 0–1 + uma tela simples é questão de dias.
> Lançar simples e enriquecer > fazer tudo perfeito de primeira. O markdown-bruto-sagrado é o que permite isso.

**Decisão (2026-07-20) — a home é um ritual, não um menu.** A FASE 7 nasce do lembrete de que
o hábito é o desafio real: abrir o app passa a mostrar uma **home** com o jornal de lembranças
(温故知新, *onkochishin* — "revisitar o velho para conhecer o novo") no topo e uma **timeline por
`created_at`** embaixo (diário de captura; a sidebar já cobre `updated_at`). As lembranças casam o
**dia exato** — há 1 semana, mesmo dia do mês passado, mesmo dia/mês de cada ano anterior — e dia
sem lembrança fica em silêncio, sem janela aproximada. O visual do jornal é shinbun, não broadsheet:
Mincho, filetes, monocromia e um selo — jornal É tinta sobre papel, o app sendo ele mesmo
(spec no `DESIGN.md`).

**Decisão (2026-07-04) — frontend acompanha cada fase.** A FASE 5 deixou de ser um bloco no
fim: a primeira tela (lista + editor + tema) foi antecipada logo após a FASE 1, e daqui em diante
**toda fase de backend entrega junto sua contraparte na UI** (tags → selos, links → backlinks/grafo,
busca → campo). O porquê: sempre ter algo real pra usar e testar — o hábito de usar o app é o
desafio real, e ele só se forma com o app vivo na mão.

---

## Design (resumo — detalhe em `DESIGN.md`)

Direção **tinta sobre papel** (sumi-e), inspirada em Zen, taoísmo e bushido — porque o app é sobre
**conhecimento**, e essas tradições foram sistemas de busca de conhecimento. Cada princípio é decisão
concreta: 間 ma (espaço é elemento), 陰陽 yin-yang (claro e escuro são iguais), 円相 ensō (a marca, círculo
aberto), 墨絵 sumi-e (um só acento vivo), 武士道 bushido (restrição), 緣起 interdependência (o grafo é o tema).
Tokens em `tokens.css`; regras de uso e componentes em `DESIGN.md`. É o sistema **vivo** — cresce com o app,
não é um design system pesado pra manter à parte.

---

## Lembretes pra mim mesmo

- **Meu papel é dono do produto:** usar de verdade, aprovar UX, pedir ajustes até ficar com a minha cara. O valor do app depende de eu alimentar ele — a construção é rápida, o hábito é o desafio real.
- **MVP em dias.** CRUD + uma tela já é um caderno usável. Começar a usar cedo é o que valida tudo.
- **Lançar simples e enriquecer** > tentar fazer tudo perfeito de primeira.
- **Começar junto (monorepo), separar depois** só se a dor aparecer.
