# AlocaSenac — Evidência de TDD (BDD Parte 2)

> **Fonte dos cenários:** `docs/testes/bdd-aprovacao-bloqueio.md`.
> **Regra do ciclo:** um cenário por vez. Um único teste JUnit 5, `./mvnw -B test` mostrando a falha **por asserção** (nunca por compilação ou configuração), commit `test(red)`; o mínimo para a **suíte inteira** ficar verde, commit `feat(green)`; refatoração sem mudar comportamento, commit `refactor`.
> **Tempo:** sempre pelo `Clock` injetado (`ReservaService`, `Reserva.aplicar`, `Ocupacao.bloqueio`; `RelogioDeTeste` nos testes). Nenhum `Instant.now()` escondido nem `Thread.sleep`.
> **Situação:** **parcial**, 13 de 37 cenários, com merge antecipado a pedido da equipe. Os outros 24 continuam numa branch seguinte.

## 0. Commits de preparação (aprovados pelo grupo, `bdd-aprovacao-bloqueio.md` §7)
| Commit | O quê | Por quê |
|---|---|---|
| `c995369` | `test: ajusta RN-E5-C01 à decisão D-05` | Mudança de requisito: não restrito passa a ser aprovado pelo sistema; o caso usa recurso restrito, com a mesma asserção |
| `f6dea08` | `test: Clock ajustável na IntegracaoBase (D-12)` | Infraestrutura de teste: nenhum teste depende do relógio real |

## 1. Cenário → commits → saída do teste vermelho
| Cenário | Commit red | Commit green | Commit refactor | Trecho da saída do `./mvnw -B test` no vermelho |
|---|---|---|---|---|
| B4-01 | `6c8f73d` | `bc82491` | — | expected: APROVADA but was: SOLICITADA (3 falhas de asserção; 93 verdes) |
| B4-02 | `edad39a` | `2fe9869` | — | expected: REJEITADA but was: SOLICITADA (4 falhas de asserção; 96 verdes) |
| B4-03 | `15b9b6e` | `e20b80d` | — | Expecting code to raise a throwable. (15 falhas de asserção; 100 verdes) |
| B4-09 | `2ecd181` | `fec1bd5` | — | [08:59:59] Expecting code to raise a throwable. (1 falha de asserção; 117 verdes) |
| B4-10 | `3736ed5` | `e2f320b` | `78b8349` | [08:59:59] Expecting code to raise a throwable. (1 falha de asserção; 120 verdes) |
| B3-04 | `f7e9b94` | `e3a9560` | — | [cruza o início por 1 segundo] Expecting actual: [] to contain exactly [...] (4 falhas de asserção: as 4 recusas; as 4 aceitações passam) |
| B3-12 | `ab1ca86` | `5b870b7` | — | [07:59:59] Expecting code to raise a throwable. (1 falha de asserção; 131 verdes) |
| B1-08 | `d96e45c` | `9638e58` | — | expected: APROVADA but was: SOLICITADA (1 falha de asserção; 132 verdes) |
| B1-09 | `6dacef1` | `cd5c02e` | — | Expected size: 1 but was: 0 in: [] (1 falha de asserção; 133 verdes) |
| B1-01 | — (verde na escrita: `5a038e5`) | — | — | testes=135 falhas=0 erros=0: passou na primeira execução |
| B1-07 | — (verde na escrita: `1dfa648`) | — | — | testes=136 falhas=0 erros=0: passou na primeira execução |
| B1-02 | `f3457c1` | `8534708` | — | expected: APROVADA but was: SOLICITADA (1 falha de asserção; 136 verdes) |
| B4-04 | `f5577ef` | `530805f` | `fa69eae` | Expected size: 1 but was: 0 in: [] (1 falha de asserção; 137 verdes) |

**Verde na escrita (B1-01, B1-07):** o comportamento já existia antes do ciclo, porque toda reserva nasce SOLICITADA desde a Aula 06 e a D-05 só aprova quando **nenhum** recurso é restrito. Os testes foram escritos, rodaram verdes na primeira execução e foram mantidos como regressão. Não houve commit `test(red)` porque não houve vermelho, e nenhum código de produção foi quebrado para forçar um.

## 2. Integridade
- Nenhum teste existente foi enfraquecido. A única mudança em teste antigo é a do commit `c995369`, uma mudança de requisito aprovada, sem mudar a asserção.
- Em todo commit `test(red)`, a única falha da suíte é o teste novo, sempre `AssertionError` ou `AssertionFailedError`, com 0 erros.

## 3. Faltam (próxima branch)
B1-03, B1-04, B1-05, B1-06, B2-01, B2-02, B2-03, B3-01, B3-02, B3-03, B3-05, B3-06, B3-07, B3-08, B3-09, B3-10, B3-11, B3-13, B4-05, B4-06, B4-07, B4-08, B4-11, B4-12.
