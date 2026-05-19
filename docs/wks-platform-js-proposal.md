# wks-platform-js — proposta de arquitetura

Documento de alinhamento para a primeira conversa sobre o `wks-platform-js`: a biblioteca de componentes JS para case management que será compilada para Angular e React, consumida tanto pela UI de referência da WKS quanto por integradores (SIs) embutindo em UIs próprias.

## Objetivo

Uma única base de código de componentes — escritos uma vez, publicados como pacotes idiomáticos para React **e** Angular — que se mantenha em sincronia automática com:

1. Os contratos de dados do backend `wks-platform` (modelos, envelope de API, códigos de erro).
2. As especificações visuais e de comportamento da UI de referência.
3. As expectativas dos consumidores externos (SIs).

A regra-base: **nenhuma das três sincronizações pode depender de disciplina humana.** Toda deriva precisa quebrar um build.

## Estratégia técnica: Stencil + wrappers oficiais

Componentes escritos em **Stencil** (Web Components nativos). O compilador do Stencil gera:

- O pacote base de Web Components (`@wks/components`).
- Wrappers tipados para React (`@wks/components-react`) e Angular (`@wks/components-angular`) — não `<custom-element>` cru, mas componentes idiomáticos com props/eventos corretamente bindados.

**Storybook** entra por cima como ambiente de desenvolvimento, spec executável e galeria. Não é o que compila — é o que documenta e valida.

## Os três contratos

A biblioteca vive na interseção de três contratos. Cada um tem fonte de verdade distinta e mecanismo de validação distinto. Tratá-los como um só é o que apodrece bibliotecas de componentes.

### 1. Contrato de dados — back ↔ componentes

**Fonte de verdade: o backend `wks-platform`.**

O backend já expõe OpenAPI via springdoc e gera JSON Schema a partir dos YAMLs de case-type (ver `docs/architecture.md` e a regra de "Config-driven rendering" no `CLAUDE.md` do repo). Isso é matéria-prima pronta.

Plano:

- Um pacote `@wks/contracts` é **gerado pelo CI do `wks-platform`** a partir do OpenAPI do backend (via `openapi-typescript` ou `orval`).
- Contém types de `Case`, `Task`, envelope `{ data, error, meta }`, códigos de erro `WKS-*`. Tudo gerado — nunca escrito à mão.
- `wks-platform-js` depende de `@wks/contracts` como dependência npm. Quando o back muda um campo, o build da lib quebra. É exatamente o comportamento desejado.
- Componentes config-driven (form, table, filter bar) recebem **JSON Schema como prop**, não shapes hardcoded. O mesmo schema que o back gera a partir do YAML do case-type. Zero duplicação de modelo entre back e front.

### 2. Contrato de componente — lib ↔ consumidores

**Fonte de verdade: o Storybook do `wks-platform-js`.**

Storybook não como galeria visual, mas como **spec executável**:

- Cada componente tem um `.stories.tsx` cobrindo estados (loading, empty, error, populated), variantes (size, density) e edge cases (campo readonly, validação client-side, schema malformado).
- **CSF3 + `play` functions** rodam interações reais dentro da story (digitar, submeter, expandir). Vira teste de comportamento sem custo adicional.
- **Visual regression** com Chromatic ou Playwright snapshots: toda PR compara pixel a pixel contra baseline. Sem isso, "manter sincronia visual com a UI de referência" vira opinião.
- **Addon de a11y** ligado por padrão. Contraste ou role faltando bloqueia o merge.

### 3. Contrato de integração — lib ↔ UI de referência

**Fonte de verdade: a UI de referência (`wks-platform/frontend`).**

A UI de referência é o **canário**:

- Consome a lib via versão publicada no npm, não path link. Bump de versão = PR no `wks-platform` rodando o CI inteiro (incluindo o coverage ratchet do `frontend/` — ver regra no `CLAUDE.md`). Se um componente quebrou o consumo real, descobre-se antes do SI descobrir.
- **Smoke E2E** no `wks-platform`: um Playwright que renderiza um case-type real, preenche um form gerado pelo schema, submete, valida o `201`. Esse teste é o "a lib funciona de verdade" — não a story isolada.
- **Changesets** no `wks-platform-js` para semver disciplinado: `major` quando muda prop, `patch` quando muda CSS. SIs externos vão depender disso.

## Como tudo se amarra

```
┌─ wks-platform (backend) ─────── OpenAPI + JSON Schema (gerado de YAMLs)
│         │
│         │  CI publica
│         ▼
│   @wks/contracts (npm)
│         │
│         │  dependência
│         ▼
├─ wks-platform-js ─────────────── consome contracts, exporta componentes
│   ├── Storybook (spec + visual regression + a11y)
│   ├── Stencil build → @wks/components (web components)
│   └── wrappers → @wks/components-react, @wks/components-angular
│         │
│         │  dependência (versão fixada)
│         ▼
└─ wks-platform/frontend ──────── consome @wks/components-react
    └── Playwright smoke (canário do contrato real ponta-a-ponta)
```

## Decisão tomada: onde mora `@wks/contracts`

`@wks/contracts` é **gerado dentro do `wks-platform`** e publicado pelo pipeline do backend.

Alternativa considerada (repo próprio para os contratos) foi descartada: adicionaria um terceiro fluxo de CI para sincronizar, e abriria janela para deriva entre o que o back retorna e o que o pacote declara.

Com a opção escolhida: um commit no back que muda contrato sai com o pacote novo publicado no mesmo PR. Não há janela de deriva possível.

## O que isso significa para o `wks-platform-js`

- Não definir types de domínio à mão. Importar de `@wks/contracts`.
- Storybook é first-class — stories são parte do "definition of done" de cada componente, não opcional.
- Visual regression + a11y rodando em CI desde o primeiro componente (não retrofit depois).
- Publicar três pacotes a partir do mesmo source: `@wks/components`, `@wks/components-react`, `@wks/components-angular`. Changesets coordena os bumps.
- Componentes config-driven recebem JSON Schema, não shapes específicos. A lib não conhece "Case" — ela conhece "formulário descrito por este schema".


## Próximos passos sugeridos

1. Pipeline no `wks-platform` que publica `@wks/contracts` a cada release do backend.
2. Primeiro componente fim-a-fim cobrindo o ciclo completo: contrato → componente → wrapper React → consumo na UI de referência → Playwright smoke.
3. Definir baseline visual e ligar Chromatic (ou equivalente) antes do segundo componente.
