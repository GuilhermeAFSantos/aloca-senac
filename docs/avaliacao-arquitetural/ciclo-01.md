# AlocaSenac — Avaliação Arquitetural · Ciclo 01 (ATAM)

> **Método:** ATAM (Len Bass, Paul Clements e Rick Kazman), primeiro ciclo, **sobre o que já foi implementado**.
> **Data:** 2026-11-19. **Código avaliado:** branch `pratica/ci-github-actions`, commit `36a5fa5` (PR #1).
> **Convenção:** **[F]** fato com origem em artefato · **[H]** hipótese, com validação pendente · **[S]** suposição. Nenhuma [H] ou [S] é tratada como fato.
> **Restrições desta avaliação:** nenhum código alterado, nenhum requisito inventado.
> **Atualização (Aula 07):** a §10 acrescenta as medidas do JaCoCo e do SonarCloud como evidência, revisa riscos, não-riscos e sensibilidades e marca as ações atendidas. As §1–§9 ficam como foram escritas no ciclo 01.

## 0. Fontes consultadas

| Fonte | Uso |
|---|---|
| `docs/requisitos.md` | Requisitos, regras, metas e limitadores (fonte primária) |
| `docs/prd.md` | Personas P1–P3, escopos e fora de escopo |
| `docs/arquitetura.md` | Drivers (§6), invariantes (§10), decisões DP e ADR (§11, §26), C4 (§12–§14), dados (§15), ATAM de projeto (§22) |
| `docs/testes/plano-tdd.md` | Decisões D-01 a D-04, matriz regra → teste, divergências G-01 a G-06 |
| `src/main/**`, `src/test/**` | Abordagens e táticas implementadas, com arquivo e linha |
| GitHub Actions, PR #1 | Runs do workflow `CI` (§6.1) |
| SonarCloud, PR #1 | Quality Gate: S2119 corrigido em `36a5fa5`; S4502 aceito com justificativa |

---

## 1. Direcionadores de negócio

| ID | Direcionador | Origem | Situação no código |
|---|---|---|---|
| D1 | Nenhuma dupla reserva, inclusive na agenda do professor | `docs/requisitos.md` RF5, RF6, RN-T2, RN-T3, LIM-4; `docs/prd.md` P1 | **Implementado** para reservas; bloqueio de manutenção pendente (B3, ciclo BDD) |
| D2 | Autorização correta por perfil | RF1, RN-E3, RQ-6, LIM-3 | **Implementado** na API; aprovação de restritos pendente (B1, B2) |
| D3 | Aplicação executável e pipeline presente | LIM-1, LIM-2, RQ-5 | **Implementado**: CI no PR, `main` protegida |
| D4 | Testabilidade com ambiente realista e metas de cobertura | RQ-1–RQ-7, MT-1, MT-2 | Testcontainers e WireMock em uso; **cobertura ainda não medida** (Aula 07) |
| D5 | Auditoria de toda mudança | RF10, RN-E5; P3 | **Implementado** para criação e alteração; transições pendentes (B4) |
| D6 | Controle de restritos e empréstimos | RF7, RF9; P2 | Empréstimo só no domínio; aprovação pendente (B1) |
| D7 | Uso pelo celular com mensagens compreensíveis | RF13; P1 | Mensagens da API em linguagem do usuário; **nenhuma tela** |
| D8 | Relatórios de utilização e conflitos evitados | RF12; P3 | Cálculo implementado, sem API; comportamento provisório (Q-13, Q-14) |
| D9 | Cron diário de treino só com dados novos | **restrição fornecida pelo professor** (CT-06) | **Não implementado** |

---

## 2. Abordagens arquiteturais encontradas no código

| ID | Abordagem | Evidência (arquivo:linha) | Decisão |
|---|---|---|---|
| AQ-1 | Exclusão mútua garantida pelo banco (restrição `EXCLUDE` sobre intervalo semiaberto) | [V1__esquema_inicial.sql:43-44](../../src/main/resources/db/migration/V1__esquema_inicial.sql#L43-L44) | ADR-000, ADR-003, D-01 |
| AQ-2 | Checagem prévia na aplicação para dar mensagem clara | [VerificadorDisponibilidade.java:26-30](../../src/main/java/br/senac/alocasenac/reserva/VerificadorDisponibilidade.java#L26-L30), [Intervalo.java:24](../../src/main/java/br/senac/alocasenac/reserva/Intervalo.java#L24) | DP-04 |
| AQ-3 | Uma transação por mudança: reserva, ocupações, auditoria e outbox juntas | [ReservaService.java:53](../../src/main/java/br/senac/alocasenac/reserva/ReservaService.java#L53), [:84-92](../../src/main/java/br/senac/alocasenac/reserva/ReservaService.java#L84-L92) | ADR-001, ADR-004, ADR-005 |
| AQ-4 | Recusa registrada depois da transação desfeita, em transação própria | [ReservaService.java:54-60](../../src/main/java/br/senac/alocasenac/reserva/ReservaService.java#L54-L60), [RegistroDeRecusas.java:23](../../src/main/java/br/senac/alocasenac/reserva/RegistroDeRecusas.java#L23) | ADR-004 (DP-06) |
| AQ-5 | Outbox com despacho assíncrono e reenvio | [DespachanteNotificacoes.java:26-35](../../src/main/java/br/senac/alocasenac/notificacao/DespachanteNotificacoes.java#L26-L35), [NotificacaoClient.java:20-21](../../src/main/java/br/senac/alocasenac/notificacao/NotificacaoClient.java#L20-L21) | ADR-005 |
| AQ-6 | API stateless com JWT, autorização na rota e no controller, negação por padrão fora da API | [SegurancaConfig.java:44-53](../../src/main/java/br/senac/alocasenac/config/SegurancaConfig.java#L44-L53), [:61](../../src/main/java/br/senac/alocasenac/config/SegurancaConfig.java#L61), [ReservaController.java:41](../../src/main/java/br/senac/alocasenac/reserva/ReservaController.java#L41), [PoliticaAcessoReserva.java:12-16](../../src/main/java/br/senac/alocasenac/reserva/PoliticaAcessoReserva.java#L12-L16) | ADR-014 (rev.), ADR-015, G-01, D-03 |
| AQ-7 | Erros traduzidos em `ProblemDetail` sem detalhes internos | [TratamentoDeErros.java:24-47](../../src/main/java/br/senac/alocasenac/config/TratamentoDeErros.java#L24-L47) | RF13, RQ-6 |
| AQ-8 | Tempo por `Clock` injetado | [ClockConfig.java](../../src/main/java/br/senac/alocasenac/config/ClockConfig.java), [RegistroAuditoria.java:54](../../src/main/java/br/senac/alocasenac/auditoria/RegistroAuditoria.java#L54); busca por `Instant.now`/`Thread.sleep` em `src/` sem resultado | RQ-1 |
| AQ-9 | Esquema versionado (Flyway) e validado pelo Hibernate | [application.properties:4](../../src/main/resources/application.properties#L4) | ADR-003 |
| AQ-10 | Ambiente de teste realista: PostgreSQL em container e serviço externo simulado | [IntegracaoBase.java:21-23](../../src/test/java/br/senac/alocasenac/IntegracaoBase.java#L21-L23), [NotificacaoIntegracaoTest.java](../../src/test/java/br/senac/alocasenac/notificacao/NotificacaoIntegracaoTest.java) | RQ-2, RQ-7 |
| AQ-11 | Pipeline que roda o mesmo `verify` local, com `main` protegida pelo check | [`.github/workflows/ci.yml`](../../.github/workflows/ci.yml); proteção da `main` (check `verify`, strict) | RQ-5, LIM-2 |

---

## 3. Utility tree

> Prioridade = (importância, dificuldade) em A/M/B. Os cenários marcados **n/a** não podem ser avaliados neste ciclo, porque o código não existe.

- **Integridade**
  - Dupla reserva sob concorrência → **INT-01** (A, A)
  - Agenda do professor → **INT-02** (A, M)
  - Recurso em manutenção → **INT-03** (A, M) — **n/a** (B3, ciclo BDD)
- **Segurança**
  - Autorização por perfil na API → **SEG-01** (A, M)
  - Token JWT real resolve o perfil certo → **SEG-02** (A, M)
  - Erro sem detalhes internos → **SEG-03** (M, B)
- **Desempenho**
  - Reservas simultâneas sob carga → **DES-01** (A, A)
  - Pesquisa de disponibilidade com crescimento de dados → **DES-02** (M, M)
- **Implantabilidade**
  - Regressão barrada antes da `main` → **CI-01** (A, B)
- **Auditabilidade**
  - Mudança sem auditoria → **AUD-01** (A, B)
- **Testabilidade**
  - Ambiente realista e tempo determinístico → **TST-01** (A, B)
- **Integrabilidade**
  - Serviço de notificação fora do ar → **INTG-01** (M, B)
  - Queda no meio do despacho → **INTG-02** (M, M)
- **Correção de indicadores**
  - Conflitos evitados contados corretamente → **REL-01** (M, M)
- **Usabilidade**
  - Uso pelo celular → **USA-01** (M, M) — **n/a** (sem telas)
- **Batch diário (CT-06)**
  - Isolamento, checkpoint, gate, rollback → **DISP-01, CONF-01–03, ML-01–04** de `docs/arquitetura.md` §22 — **n/a** (não implementado)

**Justificativa das prioridades**
| Cenário | Importância | Dificuldade |
|---|---|---|
| INT-01 | A — LIM-4 | A — corrida entre transações |
| DES-01 | A — RQ-6, semana 42 | A — sem meta (L-04) e sem medição |
| INT-02 | A — RF5 cita o professor | M — depende do professor ser recurso da reserva |
| SEG-01 | A — LIM-3 | M — regra em rota, controller e política de dono |
| SEG-02 | A — LIM-3; o BDD Parte 3 usará "tokens de cada perfil" | M — mapeamento de claim configurado, mas não testado |
| CI-01 | A — LIM-2 | B — workflow e proteção já existem |
| AUD-01 | A — RN-E5 | B — transação única |
| TST-01 | A — peso 25% + 15% na avaliação | B — infraestrutura pronta |
| SEG-03 | M — RQ-6 | B — tratamento central |
| DES-02 | M — sem volume definido (L-03) | M — consulta em memória |
| INTG-01 | M — RF11 | B — outbox |
| INTG-02 | M — RF11 aceita notificação simulada | M — envio e gravação não são atômicos |
| REL-01 | M — RF12, D8 | M — depende da definição de "conflito evitado" (S-08) |

---

## 4. Cenários de atributo de qualidade (seis partes)

> Ordem: (A, A) → (A, M) → (A, B) → (M, M) → (M, B).

| ID | Fonte | Estímulo | Artefato | Ambiente | Resposta | Medida de resposta |
|---|---|---|---|---|---|---|
| **INT-01** | 2 a 20 Solicitantes | Pedem o mesmo recurso em intervalos sobrepostos ao mesmo tempo | `ReservaService` + restrição `ocupacao_sem_sobreposicao` | Operação normal, PostgreSQL real | Uma reserva aceita; as outras recebem 409 e são registradas como tentativa recusada | **[F]** 0 duplas reservas (RN-T3, LIM-4). Observado: 1 aceita em 20 simultâneas (`RN_T3_C02`), verde em todos os runs de CI desde `68015df` |
| **DES-01** | Ferramenta de carga | Muitos usuários reservando ao mesmo tempo | Aplicação + banco | Pico simulado | Sem erro técnico; recusas de negócio com mensagem | **[H] validação pendente:** p95 ≤ 1 s e erro técnico < 1% com 50 usuários (P-03). **Sem medição** |
| **INT-02** | Solicitante | Reserva sala livre com professor já alocado em outra sala no horário | `VerificadorDisponibilidade` | Operação normal | Recusa por conflito do professor | **[F]** 0 sobreposições na agenda do professor (RF5). Observado: `RF5_C01` verde |
| **SEG-01** | Solicitante autenticado | Cadastra sala ou altera reserva de outra pessoa pela API | `SegurancaConfig`, controllers, `PoliticaAcessoReserva` | Operação normal | 403; nada muda | **[F]** 100% das tentativas negadas (RF1, D-03). Observado: `RF1_C03`, `RF1_C05` verdes |
| **SEG-02** | Cliente da API | Envia token JWT assinado com claim `perfis: [RESPONSAVEL]` | `JwtDecoder`, `JwtAuthenticationConverter` | Operação normal, segredo configurado | Usuário recebe exatamente as permissões do perfil do token | **[H] validação pendente:** 100% dos perfis mapeados corretamente. **Sem evidência**: nenhum teste usa token real (§7, R1-01) |
| **CI-01** | Desenvolvedor | Envia PR com uma asserção quebrada | Workflow `CI` + proteção da `main` | PR aberto | Check `verify` vermelho; merge bloqueado | **[F]** pipeline presente (LIM-2). Observado: run vermelho `44615426256`; job `verify` verde em cerca de 49 s (`44618051808`) |
| **AUD-01** | `ReservaService` | Gravação da auditoria falha | `AuditoriaService` dentro da transação | Operação normal | A reserva também é desfeita | **[F]** 100% das mudanças com auditoria (RN-E5). Observado: `RN_E5_C04` verde |
| **TST-01** | Equipe | Roda a suíte | Testes de integração | CI e local | Banco real, serviço simulado, tempo fixo | **[F]** 0 mocks de persistência em teste de integração (RQ-7); **[F]** cobertura ≥ 80% / 70% (MT-1, MT-2): **não medida** |
| **SEG-03** | Usuário | Requisição inválida ou proibida | `TratamentoDeErros` | Operação normal | Mensagem compreensível, sem pilha nem SQL | **[F]** RQ-6. Observado: 400/409 com `ProblemDetail` (`RN_T1_C06`, `RN_E4_C04`) |
| **DES-02** | Crescimento de uso | Pesquisa de disponibilidade com muitas ocupações | `CatalogoService` | Base com histórico acumulado | Resultado no tempo aceitável | **[H] validação pendente:** p95 ≤ 500 ms com 10 mil ocupações. **Sem medição** |
| **INTG-02** | Ambiente de execução | Processo cai depois de enviar a notificação e antes de gravar ENVIADA | `DespachanteNotificacoes` | Execução do agendador | Notificação não é perdida nem duplicada | **[H] validação pendente:** 0 notificações duplicadas. O código entrega "pelo menos uma vez" (§7, R1-03) |
| **REL-01** | Solicitantes | Recusas por concorrência em reserva com vários recursos, e recusas em alteração | `ReservaService` + `RegistroDeRecusas` | Operação normal | "Conflitos evitados" igual ao número de pedidos recusados | **[F]** RF12. **[S]** definição de S-08 ("criar ou alterar"). O código diverge nos dois casos (§7, R1-02) |
| **INTG-01** | Serviço de Notificação | Fica fora do ar (503) | `DespachanteNotificacoes` | Operação normal | Reserva mantida; notificação pendente com tentativa contada | **[F]** RF11. Observado: `RF11_C02` verde |

---

## 5. Análise dos cenários de maior prioridade

### INT-01 — dupla reserva sob concorrência (A, A)
- **Abordagens:** AQ-1 (garantia no banco) + AQ-2 (checagem prévia) + AQ-4 (recusa em transação própria).
- **Táticas de Bass:** *barreira / interlock* (safety); *transações* (disponibilidade, prevenir falhas); *tratamento de exceção* (disponibilidade, recuperar), que traduz a violação do banco em 409.
- **Evidência:** [V1__esquema_inicial.sql:43-44](../../src/main/resources/db/migration/V1__esquema_inicial.sql#L43-L44); [ReservaService.java:57-60](../../src/main/java/br/senac/alocasenac/reserva/ReservaService.java#L57-L60); testes [ReservaServiceIntegracaoTest.java:75](../../src/test/java/br/senac/alocasenac/reserva/ReservaServiceIntegracaoTest.java#L75), [:91](../../src/test/java/br/senac/alocasenac/reserva/ReservaServiceIntegracaoTest.java#L91), [:176](../../src/test/java/br/senac/alocasenac/reserva/ReservaServiceIntegracaoTest.java#L176); runs de CI verdes (§6.1); 3 rodadas locais seguidas sem instabilidade (`plano-tdd.md` §6.1).
- **Descobertas:** a garantia vale **mesmo com bug na aplicação** (`RN_T2_C13` grava direto no banco e é recusado). Sensível ao filtro `WHERE (ativa AND origem = 'RESERVA')` (SP1-01).

### DES-01 — carga (A, A)
- **Abordagens:** nenhuma específica de desempenho no código.
- **Táticas:** *limitar tempo de execução* só no cliente de notificação ([NotificacaoClient.java:20-21](../../src/main/java/br/senac/alocasenac/notificacao/NotificacaoClient.java#L20-L21)). Não há tempo limite de transação nem de consulta no fluxo online.
- **Evidência:** **nenhuma medição** (JMeter na semana 42). Sem meta (L-04).
- **Descobertas:** o cenário não pode ser julgado neste ciclo. É o maior vazio de evidência entre os (A, A).

### SEG-01 / SEG-02 — autorização e token (A, M)
- **Abordagens:** AQ-6.
- **Táticas de Bass:** *autenticar atores*, *autorizar atores*, *limitar acesso*, *limitar exposição* (negação por padrão fora de `/api/**`), *verificar integridade da mensagem* (assinatura HMAC do token).
- **Evidência:** [SegurancaConfig.java:46-48](../../src/main/java/br/senac/alocasenac/config/SegurancaConfig.java#L46-L48), [:61](../../src/main/java/br/senac/alocasenac/config/SegurancaConfig.java#L61), [:68](../../src/main/java/br/senac/alocasenac/config/SegurancaConfig.java#L68), [:84](../../src/main/java/br/senac/alocasenac/config/SegurancaConfig.java#L84); [ReservaController.java:41-57](../../src/main/java/br/senac/alocasenac/reserva/ReservaController.java#L41-L57); [RecursoController.java:34](../../src/main/java/br/senac/alocasenac/recurso/RecursoController.java#L34); testes [ReservaApiTest.java:54](../../src/test/java/br/senac/alocasenac/reserva/ReservaApiTest.java#L54), [:82](../../src/test/java/br/senac/alocasenac/reserva/ReservaApiTest.java#L82), [RecursoApiTest.java:29](../../src/test/java/br/senac/alocasenac/recurso/RecursoApiTest.java#L29).
- **Descobertas:** a autorização **depois** da autenticação está testada. A **conversão do token em perfil** (claim `perfis` → `ROLE_*`) não está: os testes simulam o usuário com `user(...)`, sem passar pelo decodificador nem pelo conversor (R1-01).

### CI-01 — regressão barrada (A, B)
- **Abordagens:** AQ-11.
- **Táticas de Bass:** *scriptar comandos de implantação* (implantabilidade); *asserções executáveis* (testabilidade) como gate.
- **Evidência:** [ci.yml](../../.github/workflows/ci.yml); run vermelho `44615426256` (asserção quebrada em `IntervaloTest.java:29`) e verde depois do revert, `44615652155`; proteção da `main` com check `verify` obrigatório e `strict`.

### AUD-01 — auditoria (A, B)
- **Abordagens:** AQ-3.
- **Táticas de Bass:** *transações* (disponibilidade); *manter trilha de auditoria* (segurança).
- **Evidência:** [ReservaService.java:91](../../src/main/java/br/senac/alocasenac/reserva/ReservaService.java#L91), [:106](../../src/main/java/br/senac/alocasenac/reserva/ReservaService.java#L106); [AuditoriaService.java:25-28](../../src/main/java/br/senac/alocasenac/auditoria/AuditoriaService.java#L25-L28); teste [ReservaAuditoriaFalhaTest.java:36](../../src/test/java/br/senac/alocasenac/reserva/ReservaAuditoriaFalhaTest.java#L36).

### TST-01 — testabilidade (A, B)
- **Abordagens:** AQ-8, AQ-10.
- **Táticas de Bass:** *sandbox* (relógio virtualizado com `Clock` fixo); *abstrair fontes de dados* (repositórios Spring Data sobre banco real em container); *interfaces especializadas* (cliente de notificação substituível por WireMock).
- **Evidência:** [IntegracaoBase.java:21-23](../../src/test/java/br/senac/alocasenac/IntegracaoBase.java#L21-L23); [NotificacaoIntegracaoTest.java:66-97](../../src/test/java/br/senac/alocasenac/notificacao/NotificacaoIntegracaoTest.java#L66-L97).
- **Descobertas:** a medida de cobertura (MT-1, MT-2) não existe ainda (R1-06).

---

## 6. Evidências dos runs

### 6.1 GitHub Actions (workflow `CI`, PR #1)
| Run | Commit | Resultado |
|---|---|---|
| 44614700125 | `68015df` | ✅ primeiro run |
| 44614975542 | `c79f46f` | ✅ actions v5 |
| 44615229324 | `eb61b79` | ✅ `upload-artifact@v6`, sem avisos |
| 44615426256 | `1e81ffc` | ❌ quebra proposital |
| 44615652155 | `ca89893` | ✅ revert |
| 44615944489 | `673354e` | ✅ README |
| 44618051808 | `36a5fa5` | ✅ correção Sonar S2119 |

### 6.2 SonarCloud (análise automática, PR #1)
- Primeira análise: Quality Gate **falhou** (Confiabilidade D, Segurança D) por 2 issues em `SegurancaConfig.java`:
  - `java:S2119` (reuso do `SecureRandom`): **corrigido** em `36a5fa5`.
  - `java:S4502` (CSRF desligado): **aceito com justificativa** pela equipe, porque a API é stateless, com token no cabeçalho e sem cookie (ADR-014).
- A análise ainda é a automática do SonarCloud, que não lê cobertura. A análise pelo CI com JaCoCo é da Aula 07.

---

## 7. Pontos de sensibilidade, trade-offs, riscos, não-riscos e temas de risco

### 7.1 Pontos de sensibilidade
| ID | Ponto | Parâmetro | Cenários | Evidência |
|---|---|---|---|---|
| SP1-01 | O que conta como "ocupado" | Cláusula `WHERE (ativa AND origem = 'RESERVA')` e filtros do Verificador | INT-01, INT-03 | [V1:44](../../src/main/resources/db/migration/V1__esquema_inicial.sql#L44), [VerificadorDisponibilidade.java:26-27](../../src/main/java/br/senac/alocasenac/reserva/VerificadorDisponibilidade.java#L26-L27) |
| SP1-02 | Disputa sob concorrência sem esgotar conexões | Recusa gravada **fora** da transação desfeita × tamanho do pool | INT-01, DES-01 | [ReservaService.java:53-60](../../src/main/java/br/senac/alocasenac/reserva/ReservaService.java#L53-L60) |
| SP1-03 | Perfil efetivo do usuário | Nome da claim `perfis` e prefixo `ROLE_` | SEG-01, SEG-02 | [SegurancaConfig.java:84](../../src/main/java/br/senac/alocasenac/config/SegurancaConfig.java#L84) |
| SP1-04 | Atraso da notificação | Tempo limite de 2 s / 5 s × despacho sequencial × intervalo de 30 s | INTG-01, INTG-02 | [NotificacaoClient.java:20-21](../../src/main/java/br/senac/alocasenac/notificacao/NotificacaoClient.java#L20-L21), [application.properties:10](../../src/main/resources/application.properties#L10) |
| SP1-05 | Tempo da pesquisa | Número de recursos e de ocupações ativas, lidos inteiros | DES-02 | [CatalogoService.java:28-32](../../src/main/java/br/senac/alocasenac/recurso/CatalogoService.java#L28-L32) |

### 7.2 Pontos de trade-off
| ID | Trade-off | Melhora | Piora | Decisão |
|---|---|---|---|---|
| TO1-01 | Garantia no banco | Integridade (LIM-4) | Portabilidade; a recusa por concorrência volta sem detalhe do conflito ([ReservaService.java:60](../../src/main/java/br/senac/alocasenac/reserva/ReservaService.java#L60)) | ADR-003 |
| TO1-02 | Autorização no controller | Testes de serviço simples | Serviço sem proteção se chamado por outra entrada (ex.: futuras telas) | G-01, ADR-015 |
| TO1-03 | Outbox "pelo menos uma vez" | Reserva independe do serviço externo | Possível notificação duplicada | ADR-005 |
| TO1-04 | Chave JWT aleatória sem segredo | Aplicação sobe sem configuração (LIM-1) | Falha de configuração fica silenciosa: a API recusa todo token | ADR-014 |
| TO1-05 | Recusa gravada fora da transação | Evita esgotar o pool sob disputa | Se a gravação da recusa falhar, a reserva já foi recusada e a recusa some do RF12 | ADR-004 (DP-06) |

### 7.3 Riscos
> **Confirmado** = a análise do código mostra a falha ou a lacuna. **Dúvida** = não investigado ou sem medida.

| ID | Risco | Cenário | Decisão / evidência | Direcionador | Tipo |
|---|---|---|---|---|---|
| R1-01 | O caminho token → perfil não é testado: um erro na claim ou no prefixo passaria pela suíte inteira | SEG-02 | [SegurancaConfig.java:84](../../src/main/java/br/senac/alocasenac/config/SegurancaConfig.java#L84); `grep jwt()` nos testes sem resultado | D2, LIM-3 | **Confirmado** |
| R1-02 | "Conflitos evitados" fica impreciso: na concorrência registra uma recusa **por recurso pedido**, e recusas em **alteração** não são registradas | REL-01 | [ReservaService.java:59](../../src/main/java/br/senac/alocasenac/reserva/ReservaService.java#L59), [:66-69](../../src/main/java/br/senac/alocasenac/reserva/ReservaService.java#L66-L69); [RegistroDeRecusas.java](../../src/main/java/br/senac/alocasenac/reserva/RegistroDeRecusas.java) | D8 | **Confirmado** |
| R1-03 | Notificação pode ser enviada de novo se o processo cair entre o envio e a gravação de ENVIADA | INTG-02 | [DespachanteNotificacoes.java:28-35](../../src/main/java/br/senac/alocasenac/notificacao/DespachanteNotificacoes.java#L28-L35) | D4 (RF11) | **Confirmado** (consequência baixa: notificação simulada) |
| R1-04 | Não existe como emitir token: sem emissor e com fallback aleatório, a API só é usável com token gerado fora do sistema | SEG-02, LIM-1 | [SegurancaConfig.java:68-75](../../src/main/java/br/senac/alocasenac/config/SegurancaConfig.java#L68-L75) | D3 | **Confirmado** (afeta a demonstração) |
| R1-05 | Cobertura não medida: MT-1 e MT-2 sem evidência | TST-01 | Sem JaCoCo no `pom.xml` | D4 | **Confirmado** (Aula 07) |
| R1-06 | Reserva sobre recurso em manutenção é aceita hoje | INT-03 | [VerificadorDisponibilidade.java:27](../../src/main/java/br/senac/alocasenac/reserva/VerificadorDisponibilidade.java#L27), [V1:44](../../src/main/resources/db/migration/V1__esquema_inicial.sql#L44) | D1 (RN-T4) | **Confirmado e intencional** (B3, ciclo BDD) |
| R1-07 | Pesquisa de disponibilidade degrada com o volume | DES-02 | [CatalogoService.java:28-32](../../src/main/java/br/senac/alocasenac/recurso/CatalogoService.java#L28-L32) | D7, D1 | Dúvida (sem volume, L-03) |
| R1-08 | Sem tempo limite de transação no online, a margem de dado tardio do batch (§17.2 item 6) não tem garantia | DES-01 | Nenhum tempo limite configurado | D9 | Dúvida (batch não existe) |
| R1-09 | Sem id de correlação nem métricas, não é possível medir DES-01 nem DISP-01 | DES-01 | Nenhum Actuator, métrica ou correlação em `src/main` | D4 | **Confirmado** (lacuna) |
| R1-10 | Alteração de reserva não verifica o estado (reagenda EM_USO ou CONCLUÍDA) | — | [ReservaService.java:97-107](../../src/main/java/br/senac/alocasenac/reserva/ReservaService.java#L97-L107) | D1, D5 | Dúvida (regra de estado é B4) |

### 7.4 Não-riscos
| ID | Não-risco | Evidência |
|---|---|---|
| NR1-01 | Dupla reserva por concorrência: o banco recusa mesmo com bug na aplicação | `RN_T2_C13`, `RN_T3_C01–C05` verdes; [V1:43-44](../../src/main/resources/db/migration/V1__esquema_inicial.sql#L43-L44) |
| NR1-02 | Mudança sem auditoria | `RN_E5_C04` verde |
| NR1-03 | Serviço de notificação fora do ar derrubar reserva | `RF11_C02` verde |
| NR1-04 | Rota fora da API acessível sem regra | [SegurancaConfig.java:61](../../src/main/java/br/senac/alocasenac/config/SegurancaConfig.java#L61) (`denyAll`) |
| NR1-05 | Teste dependente do relógio | Nenhum `Instant.now`/`Thread.sleep` em `src/`; `Clock` fixo nos testes |
| NR1-06 | Regressão chegar à `main` sem passar pelo CI | Proteção com `verify` obrigatório; run vermelho `44615426256` |
| NR1-07 | Segredo versionado | Segredo só por variável de ambiente ([application.properties:14](../../src/main/resources/application.properties#L14)); `.env` no `.gitignore` |
| NR1-08 | Intervalos encostados tratados como sobrepostos | `RN_T2_C08eC09` verde; [Intervalo.java:24](../../src/main/java/br/senac/alocasenac/reserva/Intervalo.java#L24) |

### 7.5 Temas de risco
| ID | Tema | Riscos | Direcionadores |
|---|---|---|---|
| TR1-1 | **Evidência de segurança incompleta**: a autorização está provada, mas a autenticação real e a emissão de token não | R1-01, R1-04 | D2, D3 (LIM-3, LIM-1) |
| TR1-2 | **Planejado × implementado**: batch inteiro e B1–B4 ainda ausentes | R1-06, R1-08, R1-10 | D1, D5, D6, D9 |
| TR1-3 | **Atributos sem medida**: cobertura, desempenho e observabilidade | R1-05, R1-07, R1-09 | D4 |
| TR1-4 | **Precisão dos indicadores de negócio** | R1-02 | D8 |

---

## 8. `docs/arquitetura.md` promete × código faz

| ID | Promessa (onde) | O código | Situação |
|---|---|---|---|
| DV1-01 | Job de Treino Diário, Agendador, Registro de Modelos, PREVISAO e Leitor de Previsões (§13–§17, ADR-002, 006–011) | Nenhum desses componentes existe em `src/main` | **Não implementado** (D9, CT-06) |
| DV1-02 | Bloqueio de manutenção na mesma checagem das reservas (ADR-003, §15 BLOQUEIO_MANUTENCAO) | Só o valor `BLOQUEIO` no enum; restrição e Verificador consideram apenas `RESERVA` | **Intencional** (B3, G-02) |
| DV1-03 | Serviço de Aprovação (§14.1) | Não existe | **Pendente** (B1, B2) |
| DV1-04 | Serviço de Empréstimos (§14.1) | Só a entidade `Emprestimo` com as regras; sem repositório, serviço ou API | **Parcial** |
| DV1-05 | Serviço de Relatórios (§14.1) | Cálculo puro, sem API e sem consulta ao banco; Q-13 e Q-14 provisórios | **Parcial** (G-03) |
| DV1-06 | AUDITORIA com id de correlação, estado anterior e novo (§15, §18) | `valorAnterior`/`valorNovo` em texto; sem correlação | **Divergente** |
| DV1-07 | Observabilidade: logs correlacionados, métricas, alertas (§18) | Nada além de logs simples | **Não implementado** |
| DV1-08 | Autorização em rota e em método de serviço (ADR-015) | Rota e controller | **Divergência aceita** (G-01) |
| DV1-09 | Três papéis de banco: online, batch, administração (§19) | Um único usuário de banco | **Não implementado** (sem batch ainda) |
| DV1-10 | RECURSO do tipo PROFESSOR com usuário vinculado (§15, IC-03) | Sem vínculo: a agenda do professor como solicitante não é cruzada com a do professor como recurso | **Não implementado** |
| DV1-11 | Entidade USUARIO (§15) | Não existe; a identidade vem do `sub` do token | **Divergente** |
| DV1-12 | Interface Thymeleaf responsiva (DA-03, RF13) | Nenhuma tela; rotas fora da API negadas | **Não implementado** |
| DV1-13 | Reserva de recurso não restrito "SOLICITADA (ou APROVADA se não restrito)" (§16, S-09) | Toda reserva nasce SOLICITADA | **Divergente**: o código segue RN-E1-C01 e deixa P-18 em aberto |
| DV1-14 | Documentação da API (RF14, ADR-014) | Não existe | **Pendente** |
| — | ADR-000, ADR-001, ADR-003 (exclusão), ADR-004, ADR-005, ADR-014 (JWT) | Implementados como descritos | **Conforme** |

---

## 9. Ações priorizadas para o próximo ciclo

| Prioridade | Ação | Riscos / divergências | Quando |
|---|---|---|---|
| **1** | Teste de API com token JWT real (claim `perfis`) para cada perfil, passando pelo decodificador | R1-01, TR1-1 | Antes do BDD Parte 3, que usa "tokens de cada perfil" |
| **2** | JaCoCo com gate de 80% / 70% e SonarCloud pelo CI | R1-05, TR1-3 | Aula 07 |
| **3** | Ciclo BDD de B1–B4 (aprovação, dono, manutenção, transições), incluindo a revisão da cláusula da restrição (SP1-01) | R1-06, R1-10, DV1-02, DV1-03 | Prática de BDD |
| **4** | Definir com o grupo o que é um "conflito evitado" (por pedido ou por recurso; inclui alteração?) e ajustar o registro de recusas | R1-02, TR1-4 | Antes da semana 43–44 (relatórios) |
| **5** | Decidir como obter tokens para demonstração (emissor de desenvolvimento ou script local) sem versionar segredo | R1-04 | Antes da demonstração |
| **6** | Observabilidade mínima: id de correlação na auditoria e nos logs, mais `health` | R1-09, DV1-06, DV1-07 | Antes do JMeter (semana 42) |
| **7** | Despacho de notificação com transação por mensagem ou chave de idempotência no envio | R1-03 | Quando P-10 (eventos de notificação) for respondida |
| **8** | Medir DES-01 e DES-02 com JMeter; mover a pesquisa de disponibilidade para consulta no banco se o p95 passar da meta | R1-07, DES-01 | Semana 42 |
| **9** | Atualizar `docs/arquitetura.md` com as divergências aceitas (DV1-08, DV1-11, DV1-13) | §8 | Junto com a ação 3 |

**Perguntas a levar ao professor neste ciclo:** P-03 (meta de carga), P-18 (não restrito nasce aprovado?), Q-13 e Q-14 (regras do relatório), P-01 (alvo do modelo do cron).

---

## 10. Atualização com as medidas da Aula 07 (JaCoCo e SonarCloud)

> **Fontes:** relatório do JaCoCo do `./mvnw -B clean verify` (artifact `relatorio-jacoco` do CI); análise do SonarCloud pelo CI no PR #3 (run `44622229915`, plugin Maven, cobertura importada do XML do JaCoCo); detalhes em `docs/testes/plano-tdd.md` §7.

### 10.1 Medidas do projeto
| Medida | Valor | Fonte | Meta |
|---|---|---|---|
| Cobertura de linhas | **95,1%** (385/405) | JaCoCo; o SonarCloud importou o mesmo valor | MT-1 ≥ 80% ✅ |
| Cobertura de branches | **92,4%** (85/92) | JaCoCo; idem no SonarCloud | MT-2 ≥ 70% ✅ |
| Cobertura geral (Sonar) | 94,6% | SonarCloud, PR #3 | — |
| Bugs / vulnerabilidades | **0 / 0** | SonarCloud, PR #3 | MT-4 = 0 críticos ✅ |
| Code smells / hotspots | 0 / 0 (100% revisados) | SonarCloud, PR #3 | — |
| Duplicação | 0,0% | SonarCloud, PR #3 | — |
| Quality Gate do código novo | **PASSED** (confiabilidade, segurança e manutenibilidade com nota A) | SonarCloud, PR #3 | Gate padrão do SonarCloud |
| Issues tratados até aqui | `java:S2119` **corrigido** (`36a5fa5`); `java:S4502` **aceito com justificativa** (API stateless com JWT, ADR-014) | SonarCloud, PR #1 | — |

**Gates no PR:** o check `verify` agora barra o merge em três casos: teste falhando, cobertura abaixo de 80%/70% (`jacoco:check`) e Quality Gate reprovado (`sonar.qualitygate.wait=true`). O token do SonarCloud vem só do secret do repositório (`SONAR_TOKEN_ALCOA_SENAC`) e aparece mascarado (`***`) no log.

### 10.2 Evidência por cenário
| Cenário | Classes do artefato | Linhas | Branches | Testes de evidência | Leitura |
|---|---|---|---|---|---|
| INT-01 | `ReservaService` · `VerificadorDisponibilidade` · `RegistroDeRecusas` | 96,1% · 100% · 100% | 100% · 75% · 100% | RN-T3-C01–C06, RN-T2-C13 | O branch que falta no Verificador é o de origem BLOQUEIO, que é B3 |
| INT-02 | `VerificadorDisponibilidade` · `Intervalo` | 100% · 100% | 75% · 100% | RF5-C01–C04, RN-T2-C03–C15 | Fronteiras semiabertas cobertas (D-01) |
| SEG-01 | `PoliticaAcessoReserva` · `SegurancaConfig` | 100% · 100% | 100% · 75% | RF1-C01–C08 | O branch que falta é o segredo `null`, cosmético |
| SEG-02 | `SegurancaConfig` + caminho do token | 100% | 75% | **RF1-C09–C13 (novos)**: token real, trinca 31/32/33 | **Agora com evidência** (§10.3, R1-01) |
| AUD-01 | `AuditoriaService` | 100% | sem branches | RN-E5-C01–C05, RF10-C01 | — |
| SEG-03 | `TratamentoDeErros` | 100% | sem branches | **RF13-C01–C03 (novos)** | 400, 404 e 409 sem detalhes internos |
| TST-01 | Projeto inteiro | 95,1% | 92,4% | 93 testes | **Medida existe** (antes "não medida") |
| CI-01 | Workflow `CI` | — | — | Runs `44615426256` (vermelho) e `44622229915` (com Sonar) | O gate agora também cobre cobertura e Quality Gate |
| INTG-01 | `DespachanteNotificacoes` | 100% | 100% | RF11-C01–C03 | — |
| REL-01 | `RelatorioService` | 94,7% | 75% | RF12-C01–C07 | Os branches que faltam são Q-13 e Q-14. R1-02 continua: a cobertura alta **não** mostra que a contagem está certa |
| DES-01, DES-02 | — | — | — | — | **Continua sem medida**. Cobertura não mede desempenho |

### 10.3 Riscos revisados
| Risco | Antes | Agora | Evidência |
|---|---|---|---|
| R1-01 caminho token → perfil sem teste | Confirmado | **Tratado** | `AutenticacaoJwtApiTest` (RF1-C10–C13) passa um token assinado real pelo decodificador e pelo conversor: Administrador 201, Solicitante 403, sem perfil 403, assinatura errada 401; o `sub` vira o ator da auditoria |
| R1-05 cobertura não medida | Confirmado | **Tratado** | JaCoCo 95,1% / 92,4%, gate no `verify` e build falhando abaixo da meta (`plano-tdd.md` §7.1) |
| R1-02 contagem de conflitos evitados | Confirmado | **Confirmado, sem mudança** | A cobertura de `ReservaService` é de 96,1%, mas os testes verificam o comportamento atual, que é exatamente o que está em dúvida. Depende da decisão do grupo (ação 4) |
| R1-03 notificação duplicada | Confirmado | Confirmado, sem mudança | `DespachanteNotificacoes` tem 100% de cobertura, mas nenhum teste simula queda entre envio e gravação |
| R1-04 sem emissor de token | Confirmado | Confirmado, sem mudança | Os testes geram o token com `TokensDeTeste`; a aplicação continua sem emissor |
| R1-06 manutenção aceita (B3) | Confirmado e intencional | Sem mudança | Branch de BLOQUEIO não coberto no Verificador (75%) |
| R1-07, R1-08, R1-09, R1-10 | Dúvida ou confirmado | Sem mudança | A análise estática não aponta nada sobre eles |
| **R1-11 (novo)** | — | **Dúvida** | O check `verify` passa a depender da disponibilidade do SonarCloud (`qualitygate.wait`). Se o serviço cair, nenhum PR é mesclado |
| **R1-12 (novo)** | — | **Confirmado, mitigado** | Localmente, o JaCoCo acumula `target/jacoco.exec` entre execuções e infla a cobertura. No CI não ocorre (checkout limpo); localmente, usar `clean verify` |
| **R1-13 (novo)** | — | **Confirmado, baixo** | Mutação manual no mínimo do segredo **sobreviveu**: a biblioteca Nimbus repete a mesma regra. O comportamento está garantido, mas a checagem própria não é isolada por teste |

### 10.4 Não-riscos acrescentados
| ID | Não-risco | Evidência |
|---|---|---|
| NR1-09 | Cobertura cair sem ninguém ver | `jacoco:check` derruba o build abaixo de 80%/70% |
| NR1-10 | Issue novo de confiabilidade ou segurança entrar na `main` | Quality Gate do código novo no check `verify` (`sonar.qualitygate.wait=true`) |
| NR1-11 | Token do SonarCloud vazar no repositório ou no log | Só o secret do repositório; mascarado no log; nenhuma ocorrência em arquivo versionado |
| NR1-12 | Teste de lacuna passando por acaso | Mutação no relatório (RF12-C07) derrubou os casos −1 s e +1 s |

### 10.5 Pontos de sensibilidade revisados
| ID | Ponto | Revisão |
|---|---|---|
| SP1-01 | Cláusula da restrição de exclusão | Confirmado pela cobertura: o único branch não coberto do Verificador é a origem BLOQUEIO. É o ponto que o BDD B3 vai mexer |
| SP1-03 | Claim `perfis` e prefixo `ROLE_` | **Agora protegido por teste** (RF1-C10–C13); trocar o nome da claim derruba a suíte |
| **SP1-06 (novo)** | Disponibilidade do merge | Depende de dois serviços externos no mesmo check: GitHub Actions e SonarCloud (R1-11) |
| **SP1-07 (novo)** | Confiabilidade da medida local de cobertura | Depende de limpar o `target/` antes de medir (R1-12) |

### 10.6 Ações priorizadas: situação
| Ação (§9) | Situação | Evidência |
|---|---|---|
| 1. Teste de API com token JWT real | ✅ **Atendida** | `AutenticacaoJwtApiTest`, `SegredoJwtTest` (PR #2) |
| 2. JaCoCo com gate e SonarCloud pelo CI | ✅ **Atendida** | PR #2 (JaCoCo) e PR #3 (SonarCloud, Quality Gate PASSED) |
| 3. Ciclo BDD de B1–B4 | ⏳ Pendente | Próxima prática |
| 4. Definir "conflito evitado" | ⏳ Pendente | Depende do grupo (R1-02) |
| 5. Tokens para demonstração | ⏳ Pendente | R1-04 |
| 6. Observabilidade mínima | ⏳ Pendente | R1-09 |
| 7. Despacho idempotente | ⏳ Pendente | R1-03; aguarda P-10 |
| 8. JMeter (DES-01, DES-02) | ⏳ Pendente | Semana 42 |
| 9. Atualizar `docs/arquitetura.md` com divergências aceitas | ⏳ Pendente | Junto com a ação 3 |
| **10 (nova)** | Decidir se o Quality Gate fica no `verify` ou num check separado e obrigatório, para o CI não travar quando o SonarCloud estiver fora (R1-11) | — |
