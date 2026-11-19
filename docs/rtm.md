# AlocaSenac — RTM (Matriz de Rastreabilidade)

> **Entregável:** "RTM.md ligando requisito, risco, teste e evidência" (`docs/requisitos.md` §7). Meta **MT-3**: 100% dos requisitos críticos rastreados.
> **Fontes:** `docs/requisitos.md` (requisitos e IDs), `docs/arquitetura.md` (riscos R-*, ADRs, componentes, matriz §27), `docs/avaliacao-arquitetural/ciclo-01.md` (riscos R1-*), `docs/testes/plano-tdd.md` (casos e decisões D-*), `docs/defeitos.md` (defeitos).
> **Atualizada em:** 2026-11-19, a partir da `main` em `7aad1d3`.

## 0. Como ler

| Coluna | Significado |
|---|---|
| **Crítico** | ✱ = regra da seção "Regras críticas" da especificação (`docs/requisitos.md` §4) ou ligada a um limitador de nota (LIM-1 a LIM-4). Esse critério de "crítico" foi definido pela equipe a partir desses dois pontos da especificação |
| **Risco** | IDs de risco: `R-*` em `docs/arquitetura.md` §9, §21 e §22.7; `R1-*` em `docs/avaliacao-arquitetural/ciclo-01.md` §7.3 e §10.3 |
| **Decisão / componente** | ADR (`docs/arquitetura.md` §26), decisão da equipe (`D-*`, `docs/testes/plano-tdd.md` §4.1) e classe de produção |
| **Teste** | Arquivo e método. O nome do método começa com o ID do caso (ex.: `RN_T3_C01_…` = caso RN-T3-C01 do plano de TDD) |
| **Commit** | `red → green` = commit do teste que falhou → commit que o deixou verde. `gap` = teste de lacuna de cobertura (Aula 07), escrito sobre código já existente |
| **Status** | ✅ verificado (teste verde no CI) · 🟡 parcial (parte da regra pendente) · ⏳ pendente (ciclo BDD ou pergunta aberta) |

**Commits citados:** `0ce1526` fase red (Aula 05) · `18af400` fase green (Aula 06) · `96cd4d1` lacunas de risco (Aula 07).

## 1. Evidência global

| Evidência | Valor | Onde |
|---|---|---|
| Suíte | 93 testes, 0 falhas, 0 erros | `./mvnw -B clean verify`; relatórios no artifact `relatorios-de-teste` |
| CI da `main` | Run [`44623554449`](https://github.com/GuilhermeAFSantos/aloca-senac/actions/runs/44623554449) verde em `7aad1d3` (testes, gate do JaCoCo e Quality Gate) | [Actions](https://github.com/GuilhermeAFSantos/aloca-senac/actions/workflows/ci.yml) |
| Cobertura (JaCoCo) | 95,1% linhas · 92,4% branches; gate 80%/70% | Artifact `relatorio-jacoco`; `docs/testes/plano-tdd.md` §7 |
| Análise estática (SonarCloud) | `main` analisada inteira pelo CI (1.256 linhas): Quality Gate **OK**; 0 bugs, 0 vulnerabilidades, 0 code smells, 0 hotspots | [Projeto no SonarCloud](https://sonarcloud.io/dashboard?id=GuilhermeAFSantos_aloca-senac); `ciclo-01.md` §10.1 |
| Defeitos | Nenhum defeito registrado | `docs/defeitos.md` §8 |

`T/` abaixo = [`src/test/java/br/senac/alocasenac/`](../src/test/java/br/senac/alocasenac/).

## 2. Regras críticas e requisitos funcionais

### 2.1 Tempo e concorrência
| Requisito | Crítico | Risco | Decisão / componente | Teste | Commit | Status |
|---|---|---|---|---|---|---|
| **RN-T1** término posterior ao início | ✱ | — | `Intervalo.de` | [T/reserva/IntervaloTest.java](../src/test/java/br/senac/alocasenac/reserva/IntervaloTest.java#L19) :: `RN_T1_C01`, `RN_T1_C02`, `RN_T1_C04`, `RN_T1_C05`<br>[T/reserva/ReservaApiTest.java](../src/test/java/br/senac/alocasenac/reserva/ReservaApiTest.java#L117) :: `RN_T1_C06` (API, 400) | `0ce1526 → 18af400` | ✅ (C03 aguarda Q-03) |
| **RN-T2** sem sobreposição no mesmo recurso | ✱ | R-01, R-02, R-15 | ADR-000, ADR-003, D-01, D-02 · `VerificadorDisponibilidade`, restrição `ocupacao_sem_sobreposicao` | [T/reserva/VerificadorDisponibilidadeTest.java](../src/test/java/br/senac/alocasenac/reserva/VerificadorDisponibilidadeTest.java#L37) :: `RN_T2_C01`, `C02`, `C03aC07eC10`, `C08eC09`, `C11`, `C12`, `C14`, `C15`<br>[T/reserva/ReservaServiceIntegracaoTest.java](../src/test/java/br/senac/alocasenac/reserva/ReservaServiceIntegracaoTest.java#L176) :: `RN_T2_C13` (garantia no banco) | `0ce1526 → 18af400` | ✅ |
| **RN-T3** simultâneas → uma aceita | ✱ | R-01, R1-02 | ADR-003, ADR-004 · `ReservaService`, `RegistroDeRecusas` | [T/reserva/ReservaServiceIntegracaoTest.java](../src/test/java/br/senac/alocasenac/reserva/ReservaServiceIntegracaoTest.java#L75) :: `RN_T3_C01`…`RN_T3_C05`<br>[T/reserva/ReservaAlteracaoConcorrenteTest.java](../src/test/java/br/senac/alocasenac/reserva/ReservaAlteracaoConcorrenteTest.java#L45) :: `RN_T3_C06` (alteração) | `0ce1526 → 18af400`; C06 `gap 96cd4d1` | ✅ |
| **RN-T4** manutenção não reservável | ✱ | R-02, R1-06 | ADR-003 · ocupação de origem BLOQUEIO | **B3, ciclo BDD**: cenários em `docs/testes/bdd-aprovacao-bloqueio.md` (a criar) | — | ⏳ |

### 2.2 Estados e auditoria
| Requisito | Crítico | Risco | Decisão / componente | Teste | Commit | Status |
|---|---|---|---|---|---|---|
| **RN-E1** fluxo SOLICITADA → APROVADA → EM_USO → CONCLUÍDA | ✱ | R-19 | `Reserva.nova` | [T/reserva/ReservaTest.java](../src/test/java/br/senac/alocasenac/reserva/ReservaTest.java#L19) :: `RN_E1_C01` (estado inicial)<br>Transições: **B4, ciclo BDD** | `0ce1526 → 18af400` | 🟡 |
| **RN-E2** rejeitada, cancelada, não compareceu | ✱ | — | — | **B4, ciclo BDD** | — | ⏳ |
| **RN-E3** só responsáveis aprovam restritos | ✱ | R-03 | ADR-015 · Serviço de Aprovação (a criar) | **B1 e B2, ciclo BDD** | — | ⏳ |
| **RN-E4** iniciadas não são apagadas | ✱ | — | D-04 · `Reserva.verificarPodeSerApagada` | [T/reserva/ReservaTest.java](../src/test/java/br/senac/alocasenac/reserva/ReservaTest.java#L35) :: `RN_E4_C01eC02`, `RN_E4_C03`<br>[T/reserva/ReservaApiTest.java](../src/test/java/br/senac/alocasenac/reserva/ReservaApiTest.java#L131) :: `RN_E4_C04` (API, 409) | `0ce1526 → 18af400` | ✅ |
| **RN-E5** toda mudança gera auditoria | ✱ | R-04, R-20 | ADR-004 · `AuditoriaService` | [T/reserva/ReservaServiceIntegracaoTest.java](../src/test/java/br/senac/alocasenac/reserva/ReservaServiceIntegracaoTest.java#L189) :: `RN_E5_C01`, `RN_E5_C02`<br>[T/recurso/RecursoIntegracaoTest.java](../src/test/java/br/senac/alocasenac/recurso/RecursoIntegracaoTest.java#L49) :: `RN_E5_C03`<br>[T/reserva/ReservaAuditoriaFalhaTest.java](../src/test/java/br/senac/alocasenac/reserva/ReservaAuditoriaFalhaTest.java#L36) :: `RN_E5_C04`<br>[T/auditoria/RegistroAuditoriaTest.java](../src/test/java/br/senac/alocasenac/auditoria/RegistroAuditoriaTest.java#L17) :: `RN_E5_C05`<br>Auditoria das transições: **B4** | `0ce1526 → 18af400` | 🟡 |

### 2.3 Requisitos funcionais
| Requisito | Crítico | Risco | Decisão / componente | Teste | Commit | Status |
|---|---|---|---|---|---|---|
| **RF1** autenticação e autorização por perfil | ✱ (LIM-3) | R-03, R1-01 (tratado), R1-04 | ADR-014, ADR-015, D-03, G-01 · `SegurancaConfig`, `PoliticaAcessoReserva`, controllers | [T/reserva/ReservaApiTest.java](../src/test/java/br/senac/alocasenac/reserva/ReservaApiTest.java#L54) :: `RF1_C01`, `C02`, `C05`, `C06`<br>[T/recurso/RecursoApiTest.java](../src/test/java/br/senac/alocasenac/recurso/RecursoApiTest.java#L29) :: `RF1_C03`, `RF1_C04`<br>[T/reserva/PoliticaAcessoReservaTest.java](../src/test/java/br/senac/alocasenac/reserva/PoliticaAcessoReservaTest.java#L38) :: `RF1_C07eC08`, `RF1_C05`<br>[T/config/SegredoJwtTest.java](../src/test/java/br/senac/alocasenac/config/SegredoJwtTest.java#L25) :: `RF1_C09`<br>[T/config/AutenticacaoJwtApiTest.java](../src/test/java/br/senac/alocasenac/config/AutenticacaoJwtApiTest.java#L52) :: `RF1_C10`…`RF1_C13` (token real) | C01–C06 `0ce1526 → 18af400`; C07–C13 `gap 96cd4d1` | ✅ |
| **RF2** cadastro e consulta | — | — | ADR-001 · `RecursoService`, `Recurso.novo` | [T/recurso/RecursoIntegracaoTest.java](../src/test/java/br/senac/alocasenac/recurso/RecursoIntegracaoTest.java#L33) :: `RF2_C01`<br>[T/recurso/RecursoTest.java](../src/test/java/br/senac/alocasenac/recurso/RecursoTest.java#L13) :: `RF2_C02` | `0ce1526 → 18af400` | ✅ |
| **RF3** pesquisa | — | R1-07 | `CatalogoService` | [T/recurso/RecursoIntegracaoTest.java](../src/test/java/br/senac/alocasenac/recurso/RecursoIntegracaoTest.java#L68) :: `RF3_C01`…`RF3_C04`<br>[T/recurso/PesquisaCapacidadeTest.java](../src/test/java/br/senac/alocasenac/recurso/PesquisaCapacidadeTest.java#L24) :: `RF3_C06` | `0ce1526 → 18af400`; C06 `gap 96cd4d1` | 🟡 (competência aguarda Q-16) |
| **RF4** criar, alterar, cancelar | — | R1-10 | `ReservaService` | Criar e alterar: `RF1_C02`, `RF1_C06`, `RN_E5_C01`, `RN_E5_C02`, `RN_T3_C06` (acima)<br>Cancelar: **B4, ciclo BDD** | `0ce1526 → 18af400` | 🟡 |
| **RF5** sobreposição, inclusive do professor | ✱ (LIM-4) | R-02 | ADR-003 · `VerificadorDisponibilidade` | [T/reserva/VerificadorDisponibilidadeTest.java](../src/test/java/br/senac/alocasenac/reserva/VerificadorDisponibilidadeTest.java#L164) :: `RF5_C01`, `RF5_C02`, `RF5_C03`<br>[T/reserva/ReservaServiceIntegracaoTest.java](../src/test/java/br/senac/alocasenac/reserva/ReservaServiceIntegracaoTest.java#L160) :: `RF5_C04` | `0ce1526 → 18af400` | ✅ |
| **RF6** sem dupla reserva simultânea | ✱ (LIM-4) | R-01 | ADR-000, ADR-003 | Mesmos testes de **RN-T3** (2.1) | `0ce1526 → 18af400` | ✅ |
| **RF7** aprovação de restritos | ✱ (RN-E3) | R-03 | Serviço de Aprovação (a criar) | **B1, ciclo BDD** | — | ⏳ |
| **RF8** bloqueio por manutenção | ✱ (RN-T4) | R-02, R1-06 | ADR-003 | **B3, ciclo BDD** | — | ⏳ |
| **RF9** retirada e devolução | — | — | `Emprestimo` | [T/emprestimo/EmprestimoTest.java](../src/test/java/br/senac/alocasenac/emprestimo/EmprestimoTest.java#L23) :: `RF9_C01`…`RF9_C04`<br>[T/emprestimo/EmprestimoDevolucaoRepetidaTest.java](../src/test/java/br/senac/alocasenac/emprestimo/EmprestimoDevolucaoRepetidaTest.java#L23) :: `RF9_C08` | `0ce1526 → 18af400`; C08 `gap 96cd4d1` | 🟡 (C05–C07 aguardam Q-10–Q-12; sem API) |
| **RF10** histórico auditável | ✱ (RN-E5) | R-04 | ADR-004 · `AuditoriaService.historico` | [T/auditoria/HistoricoAuditoriaTest.java](../src/test/java/br/senac/alocasenac/auditoria/HistoricoAuditoriaTest.java#L22) :: `RF10_C01` + testes de RN-E5 | `0ce1526 → 18af400` | ✅ |
| **RF11** notificação simulada | — | R-23, R1-03 | ADR-005 · `DespachanteNotificacoes`, outbox | [T/notificacao/NotificacaoIntegracaoTest.java](../src/test/java/br/senac/alocasenac/notificacao/NotificacaoIntegracaoTest.java#L66) :: `RF11_C01`, `C02`, `C03` (WireMock) | `0ce1526 → 18af400` | ✅ |
| **RF12** relatório de utilização | — | R-10, R1-02 | `RelatorioService` | [T/relatorio/RelatorioServiceTest.java](../src/test/java/br/senac/alocasenac/relatorio/RelatorioServiceTest.java#L34) :: `RF12_C01`…`RF12_C04`<br>[T/relatorio/RelatorioPeriodoTest.java](../src/test/java/br/senac/alocasenac/relatorio/RelatorioPeriodoTest.java#L37) :: `RF12_C07` | `0ce1526 → 18af400`; C07 `gap 96cd4d1` | 🟡 (Q-13, Q-14; R1-02; sem API) |
| **RF13** interface responsiva e mensagens claras | — | R-21 | `TratamentoDeErros` | [T/reserva/ReservaApiErrosTest.java](../src/test/java/br/senac/alocasenac/reserva/ReservaApiErrosTest.java#L42) :: `RF13_C01`, `C02`, `C03` (mensagens) | `gap 96cd4d1` | 🟡 (sem telas) |
| **RF14** documentação de API | — | — | ADR-014 | Sem teste | — | ⏳ (sem cobertura) |

## 3. Requisitos técnicos, metas e limitadores

| Requisito | Crítico | Risco | Evidência | Status |
|---|---|---|---|---|
| **RQ-1** unitários, integração, API, e2e | — | — | Unitários (`IntervaloTest`, `VerificadorDisponibilidadeTest`…), integração (`*IntegracaoTest`), API (`*ApiTest`); **sem e2e** | 🟡 |
| **RQ-2** Testcontainers e WireMock | — | — | [T/IntegracaoBase.java](../src/test/java/br/senac/alocasenac/IntegracaoBase.java#L21) (PostgreSQL real); [T/notificacao/NotificacaoIntegracaoTest.java](../src/test/java/br/senac/alocasenac/notificacao/NotificacaoIntegracaoTest.java#L66) (WireMock) | ✅ |
| **RQ-3** teste de concorrência contra dupla reserva | ✱ (LIM-4) | R-01 | `RN_T3_C01`…`RN_T3_C06` (2.1) | ✅ |
| **RQ-4** TDD ou BDD | — | — | Fase red `0ce1526` → green `18af400` (`docs/testes/plano-tdd.md` §5–§6); BDD pendente | 🟡 |
| **RQ-5** GitHub Actions, JaCoCo, SonarCloud | ✱ (LIM-2) | R1-11 | [`.github/workflows/ci.yml`](../.github/workflows/ci.yml); run vermelho proposital `44615426256` (README); JaCoCo e Quality Gate no check `verify` | ✅ |
| **RQ-6** JMeter, autorização, validação, erros seguros | — | R-09 | Autorização: RF1. Validação e erros: `RN_T1_C06`, `RF13_C01`–`C03`. **JMeter pendente** (semana 42) | 🟡 |
| **RQ-7** mocks só em unitários | — | — | Integração com banco real; único dublê é a falha injetada em [T/reserva/ReservaAuditoriaFalhaTest.java](../src/test/java/br/senac/alocasenac/reserva/ReservaAuditoriaFalhaTest.java#L36) | ✅ |
| **MT-1** linhas ≥ 80% | — | R1-12 | 95,1% (JaCoCo); gate em `pom.xml` | ✅ |
| **MT-2** branches ≥ 70% | — | R1-12 | 92,4% (JaCoCo) | ✅ |
| **MT-3** 100% dos críticos na RTM | — | — | Este documento, §4 | 🟡 (rastreados 100%; verificados 13/21) |
| **MT-4** 0 bugs/vulnerabilidades críticas | — | — | SonarCloud: 0 bugs, 0 vulnerabilidades | ✅ |
| **LIM-1** aplicação executa | ✱ | R1-04 | `AlocaSenacApplicationTests.contextLoads` no CI | ✅ |
| **LIM-2** pipeline presente | ✱ | R1-11 | Workflow `CI` + proteção da `main` | ✅ |
| **LIM-3** sem falha de autorização | ✱ | R-03, R1-01 | RF1 (2.3); aprovação de restritos pendente (B1, B2) | 🟡 |
| **LIM-4** sem dupla reserva | ✱ | R-01, R-15 | RN-T2, RN-T3, RF5, RF6 | ✅ |
| **CT-06** cron diário de ML (professor) | — | R-05–R-08, R-11–R-14 | Não implementado (`ciclo-01.md` DV1-01) | ⏳ |

## 4. Cobertura da RTM (MT-3)

| Situação | Requisitos críticos | Quantidade |
|---|---|---|
| ✅ Verificados | RN-T1, RN-T2, RN-T3, RN-E4, RF1, RF5, RF6, RF10, RQ-3, RQ-5, LIM-1, LIM-2, LIM-4 | 13 |
| 🟡 Parciais | RN-E1, RN-E5, LIM-3 | 3 |
| ⏳ Pendentes (ciclo BDD) | RN-T4, RN-E2, RN-E3, RF7, RF8 | 5 |
| **Total** | | **21** |

- **Rastreados:** 21 de 21 (100%). Todo requisito crítico tem linha com risco, decisão e teste, ou o motivo de ainda não ter teste.
- **Verificados por teste verde:** 13 de 21 (62%).
- **Para fechar:** o ciclo BDD das regras B1–B4 cobre os 5 pendentes e completa os 3 parciais.

## 5. Regras reservadas para o ciclo BDD

| Regra BDD | Requisitos | Linhas desta RTM que ela fecha |
|---|---|---|
| B1 — recurso restrito exige aprovação de um Responsável | RF7, RN-E3 | RF7, RN-E3, LIM-3 |
| B2 — Solicitante não aprova a própria reserva | RN-E3 | RN-E3, LIM-3 |
| B3 — recurso em manutenção não pode ser reservado | RF8, RN-T4 | RF8, RN-T4 |
| B4 — transições de estado permitidas | RN-E1, RN-E2 | RN-E1, RN-E2, RN-E5 (transições), RF4 (cancelar) |

Ao fim do BDD, cada uma dessas linhas recebe: o cenário em `docs/testes/bdd-aprovacao-bloqueio.md`, o teste JUnit e a feature Cucumber (arquivo e nome) e os commits `test(red)`, `feat(green)` e `refactor`.

## 6. Requisitos que aguardam resposta
| Pergunta | Requisitos afetados |
|---|---|
| Q-03 duração mínima | RN-T1-C03 |
| Q-10, Q-11, Q-12 empréstimo | RF9-C05–C07 |
| Q-13, Q-14 relatório | RF12-C05, C06 e branches de `RelatorioService` |
| Q-16 competência do professor | RF3-C05 |
| P-11 API documentada | RF14 |
| R1-02 o que é "conflito evitado" | RF12 (contagem) |

## 7. Como manter esta matriz
1. Todo teste novo cita o ID do caso no nome do método e entra na linha do requisito, com arquivo, método e commit.
2. Todo defeito corrigido (`docs/defeitos.md`) acrescenta o teste de regressão na linha do requisito afetado.
3. Um link quebrado nesta tabela é um defeito de rastreabilidade: cada linha deve abrir um arquivo que existe.
