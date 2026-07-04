# DESIGN.md — Sistema de Design · Commonplace Book

> **Claude Code: leia este arquivo antes de gerar ou editar qualquer UI.**
> Toda tela do app deve seguir estas regras. Os valores vivem em `desktop/src/styles/tokens.css`.
> No `CLAUDE.md` da raiz, aponte para este arquivo na seção de frontend.

A direção é **tinta sobre papel** (sumi-e) com inspiração no Zen, taoísmo e bushido — porque o app é
sobre **conhecimento**, e essas tradições foram sistemas de busca de conhecimento. Não é tema decorativo:
cada regra abaixo tem um porquê funcional.

---

## Regra zero — tokens, nunca cor crua

- Use **só tokens semânticos**: `var(--bg)`, `var(--text)`, `var(--accent)`, `var(--link)`, etc.
- **Nunca** escreva `#hex` num componente. **Nunca** use pigmento (`--p-*`) direto — pigmento é matéria-prima dos tokens, não cor de uso.
- Trocar tinta↔papel deve funcionar só mudando `data-theme` no `<html>`. Se um componente quebra ao trocar de tema, ele furou a regra zero.

---

## Os 6 princípios (cada um vira decisão concreta)

1. **間 Ma — espaço é elemento.** Margens largas, poucos itens por tela, respiro generoso. Na dúvida, mais espaço. Use a escala `--s*` (base 4). Um caderno precisa de silêncio visual pra pensar.
2. **陰陽 Yin-yang — claro e escuro são iguais.** Não existe "tema padrão". Tinta e papel são duas faces. Todo componente nasce funcionando nos dois.
3. **円相 Ensō — a marca.** Círculo de um traço, **aberto**. Logo, ícone, splash. Nunca feche o círculo (a abertura = aprendizado nunca acaba).
4. **墨絵 Sumi-e — um só acento.** Monocromático (tinta + papel + cinzas). O vermelhão (`--accent`) é **raro**: no máximo um elemento de destaque por tela. Links usam `--link` (índigo), não o acento.
5. **武士道 Bushido — restrição.** Cada elemento se justifica. Sem sombra gratuita, sem gradiente, sem cantos muito redondos (`--r-sm`/`--r-md` só). Se não serve, corta.
6. **緣起 Interdependência — o grafo é o tema.** Backlinks e grafo não são features secundárias; são a ideia central virando imagem. Trate-os com capricho.

---

## Cor

- `--accent` (shu, vermelhão) = **ações e seleção**, um por tela. É o carimbo.
- `--link` (ai, índigo) = wikilinks e backlinks. **Nunca** use o acento pra link.
- `--gold` = destaque raríssimo (quase nunca). Reservado.
- Texto: `--text` (principal) e `--text-muted` (meta, legenda, secundário).
- Estrutura: `--surface` (cartão/painel), `--border` (linha/divisor).

## Tipografia (3 vozes, não misture papéis)

- **Display** (`--f-display`, Shippori Mincho) → títulos de tela e de nota. A voz do pincel. Use com peso 600.
- **Corpo** (`--f-body`, Zen Kaku Gothic, peso 300) → leitura, parágrafos, labels.
- **Mono** (`--f-mono`, JetBrains Mono) → **o markdown bruto no editor**, sempre. O editor mostra o `content` exatamente como será salvo — nada renderizado escondendo o cru.

## Espaçamento

- Só a escala `--s1`…`--s9`. Não invente valores soltos (nada de `padding:18px`).
- Prefira o lado generoso. Densidade é o oposto do que queremos.

---

## Componentes (convenções — construa só os que têm tela de verdade)

**Botões**
- `.btn-primary` (fundo `--accent`) → **um por tela**. Verbo de ação direto: "Salvar nota", nunca "Enviar".
- `.btn-secondary` (contorno) → ação secundária.
- `.btn-text` → ação terciária/discreta (ex.: "Exportar").

**Campos**
- Input: fundo `--bg`, borda `--border`, foco vira `--accent` com anel `--focus`.
- **Busca é cidadã de primeira classe** (full-text é core). Campo de busca com glifo `⌕` à esquerda.
- **Editor de markdown**: `textarea` em `--f-mono`, mostra o `content` bruto. Sagrado.

**Selos (tags)**
- Tags são **carimbos hanko**, não pílulas: contorno `--accent`, canto `--r-sm`, sombra-carimbo sutil (`1.5px 1.5px 0`).
- Variante `.muted` (contorno `--border`, sem sombra) para tags sem destaque.
- Tags são **derivadas** dos `#hashtags` do conteúdo — a UI só exibe, nunca é a fonte.

**Citação (blockquote) — a joia da coroa**
- Um commonplace book é, historicamente, um **livro de citações** (Locke, Darwin). A citação é o elemento mais nobre da tipografia do app — trate com reverência.
- Estilo: fonte display (`--f-display`), corpo maior (`--fs-h2`), respiro generoso (`--s6` vertical), colchete japonês `「` em `--accent` abrindo, atribuição em `--text-muted` menor embaixo.
- Sem fundo, sem caixa, sem borda esquerda genérica de markdown. A citação se destaca pelo **espaço ao redor**, não por decoração.

**Cartão de nota**
- Título em display, meta em mono pequena, excerto em `--text-muted`, selos embaixo, contador de backlinks ("← N notas apontam para esta"). Hover: borda vira `--accent`.
- **Kintsugi:** nota com muitas edições (ex.: 10+) ganha um fio superior dourado (`border-top: 2px solid var(--gold)`) e a meta "consertada com ouro · N revisões". Kintsugi é reparar com ouro: a nota retrabalhada vale **mais**, não menos. É o único uso permitido do `--gold`.

**Navegação**
- Item ativo marcado por **pincelada vermelha à esquerda** (`border-left` em `--accent`), não fundo preenchido. Um traço, não um bloco.

**Estado vazio**
- É **convite à ação**, não vácuo. Ensō pequeno + frase na voz do app ("O Commonplace Book está em branco. Toda prática começa no vazio.") + um `.btn-primary` ("Escrever a primeira nota").
- Erros explicam o que houve e como resolver, sem pedir desculpa. Voz do app, não de pessoa.

---

## O ar de sabedoria — detalhes que fazem (e a regra que segura tudo)

> **Kanso (簡素): sabedoria é o que se remove.** Nenhum item abaixo é ornamento empilhado — cada um é
> discreto, sentido mais que visto. Se algum dia a tela parecer "restaurante temático", corte o detalhe
> mais visível. A estética falha por excesso, nunca por falta.

- **Movimento calmo (静寂 seijaku).** Nada no app se move com pressa. Use os tokens de movimento: `--ease-ink` com `--dur-fast` (hover/foco), `--dur-calm` (troca de tema, painéis), `--dur-slow` (momentos raros: ensō desenhando no splash, confirmação de save). Movimento apressado quebra a atmosfera inteira.
- **Textura washi.** Classe `.washi-grain` no container raiz — grão de papel quase imperceptível. Se dá pra "ver" a textura, está forte demais.
- **Marcar texto é pincelar tinta.** `::selection` já vem em `--accent` do tokens.css. Não sobrescrever.
- **Scrollbar discreta.** Já vem fina e apagada do tokens.css. Barras grossas e chamativas são o oposto de ma.
- **Rótulo vertical (縦書き).** Classe `.tategaki` para um pequeno marcador de seção em kanji na vertical — **no máximo um por tela**, em `--text-muted`. É tempero raro, não padrão.
- **Divisor-pincelada.** Em vez de `<hr>` de 1px em telas especiais (vazio, splash), um traço SVG de pincel curto (~80px), `--border`, centralizado. Nas telas comuns, linha fina normal — a pincelada é reservada.
- **O save como carimbo.** Ao salvar uma nota, o feedback pode ser um hanko: o selo vermelho "aparece" com `--dur-slow` e assenta. Único momento de cerimônia da UI — por isso funciona.

---

## Piso de qualidade (sempre, sem anunciar)

- Responsivo até mobile.
- Foco de teclado visível (`:focus-visible` com `--accent`).
- `prefers-reduced-motion` respeitado (já está no tokens.css).
- Contraste legível nos dois temas.

## Checklist antes de entregar uma tela

- [ ] Zero `#hex` e zero `--p-*` nos componentes?
- [ ] Funciona em tinta **e** papel?
- [ ] No máximo um `--accent` de destaque na tela?
- [ ] Espaçamento só da escala `--s*`?
- [ ] Foco de teclado visível?
- [ ] Tags são carimbos, não pílulas?
- [ ] Transições usam `--ease-ink` + `--dur-*` (nada se move com pressa)?
- [ ] Citações com reverência (display, `「`, espaço) — não blockquote genérico?
- [ ] Teste do restaurante temático: se algo grita "oriental", corte o mais visível?
