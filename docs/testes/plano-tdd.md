# AlocaSenac — Plano de TDD

> **Etapa:** Aula 05, Prompt 1 — inventário das regras de negócio (nenhum teste nem código escrito nesta etapa).
> **Fonte de verdade:** somente os artefatos do repositório. Nenhuma regra foi inventada; o que a fonte não decide está em §4 (ambiguidades e perguntas).

## 1. Artefatos consultados

| Caminho | O que foi usado | Situação |
|---|---|---|
| `docs/requisitos.md` | RF1–RF14, RN-T1–RN-T4, RN-E1–RN-E5, perfis PF-SOL/PF-RES/PF-ADM | **Fonte primária das regras** |
| `docs/prd.md` | Personas P1–P3, escopos E1–E4, fora de escopo | Contexto dos casos |
| `docs/arquitetura.md` | ASR-01–ASR-13, suposições S-01–S-11, perguntas P-01–P-22, ADR-000–ADR-015 | Componentes e decisões; **suposições não são tratadas como regra** |
| `docs/defeitos.md` | Fluxo de registro | Destino de divergências encontradas na fase red |
| `docs/plano-de-teste.md` | — | Só esqueleto (`TODO`); nada aproveitável |
| `src/main/java/**` | — | **Não há código de domínio.** Procurei por `reserva` e `recurso` em todos os `.java`; só existe `AlocaSenacApplication.java` |
| `src/test/java/**` | `AlocaSenacApplicationTests`, `TestcontainersConfiguration`, `TestAlocaSenacApplication` | Só o teste de contexto do marco zero; nenhum teste de regra |
| `docs/rtm.md` | — | **Não existe** (planejado para a etapa 4). A rastreabilidade atual está na §27 de `docs/arquitetura.md` |

## 2. Regras reservadas para o ciclo BDD

Por decisão do usuário (prioridade dos prompts de BDD), quatro regras **entram neste inventário com seus casos, mas não recebem teste nem código nas Aulas 05 e 06**. Elas nascem uma a uma no ciclo BDD (`docs/testes/bdd-aprovacao-bloqueio.md`), com vermelho real, verde e refatoração.

| Regra BDD | Requisitos | Motivo |
|---|---|---|
| B1 — recurso restrito exige aprovação de um Responsável | RF7, RN-E3 | Prompt BDD 1 e 2 |
| B2 — Solicitante não aprova a própria reserva | RN-E3 | Prompt BDD 1 e 2 |
| B3 — recurso em manutenção não pode ser reservado | RF8, RN-T4 | Prompt BDD 1 e 2 |
| B4 — transições de estado permitidas | RN-E1, RN-E2 | Prompt BDD 1 e 2 |

> **Desvio consciente:** a Aula 05 pede "todas as regras cobertas por testes que falham". Para B1–B4 isso fica adiado, porque o prompt BDD 2 proíbe escrever vários testes de uma vez e pular o vermelho. Escrever esses testes agora também tiraria a fase vermelha do BDD.

## 3. Inventário de regras e casos de teste

**Legenda de tipo:** FEL = caminho feliz · LIM = valor-limite · INV = entrada inválida · CON = conflito · PRO = estado proibido.
**Camada sugerida:** U = unidade (JUnit 5, sem Spring) · I = integração (Testcontainers/PostgreSQL) · A = API (MockMvc).

### 3.1 RN-T1 — O término deve ser posterior ao início
*Origem:* `docs/requisitos.md` §4.1.
| Caso | Tipo | Entrada | Resultado esperado | Camada |
|---|---|---|---|---|
| RN-T1-C01 | FEL | início 09:00, término 10:00 | Intervalo aceito | U |
| RN-T1-C02 | LIM | término = início (09:00–09:00) | Recusado: "posterior" exclui igualdade | U |
| RN-T1-C03 | LIM | término = início + 1 unidade mínima de tempo | Aceito (unidade mínima é pergunta **Q-03**) | U |
| RN-T1-C04 | INV | término antes do início (10:00–09:00) | Recusado | U |
| RN-T1-C05 | INV | início ou término ausente | Recusado | U |
| RN-T1-C06 | INV | criação de reserva com intervalo inválido pela API | Recusada com mensagem compreensível, nada gravado | A |

### 3.2 RN-T2 / RF5 — Reservas do mesmo recurso não podem se sobrepor, inclusive na agenda do professor
*Origem:* `docs/requisitos.md` RN-T2, RF5. *Decisão:* ADR-003 (OCUPACAO).
| Caso | Tipo | Entrada (recurso já ocupado 09:00–10:00) | Resultado esperado | Camada |
|---|---|---|---|---|
| RN-T2-C01 | FEL | mesmo recurso, 11:00–12:00 | Aceita | U |
| RN-T2-C02 | FEL | outro recurso, 09:00–10:00 | Aceita (a regra é por recurso) | U |
| RN-T2-C03 | CON | mesmo recurso, 09:00–10:00 (idêntico) | Recusada por sobreposição | U |
| RN-T2-C04 | CON | mesmo recurso, 09:30–09:45 (contido) | Recusada | U |
| RN-T2-C05 | CON | mesmo recurso, 08:00–11:00 (contém) | Recusada | U |
| RN-T2-C06 | CON | mesmo recurso, 08:30–09:30 (cruza o início) | Recusada | U |
| RN-T2-C07 | CON | mesmo recurso, 09:30–10:30 (cruza o fim) | Recusada | U |
| RN-T2-C08 | LIM | mesmo recurso, 10:00–11:00 (começa exatamente no fim) | Aceita — intervalo semiaberto (**D-01**) | U |
| RN-T2-C09 | LIM | mesmo recurso, 08:00–09:00 (termina exatamente no início) | Aceita — intervalo semiaberto (**D-01**) | U |
| RN-T2-C10 | LIM | mesmo recurso, 09:59–10:30 (cruza por 1 minuto) | Recusada | U |
| RF5-C01 | CON | sala livre, professor já alocado em outra sala 09:00–10:00, pedido 09:30–10:30 | Recusada por conflito na agenda do professor | U |
| RF5-C02 | FEL | sala livre, professor livre | Aceita | U |
| RF5-C03 | CON | material já ocupado no mesmo horário, sala e professor livres | Recusada | U |
| RF5-C04 | CON | reserva com vários recursos, só um em conflito | Recusada por inteiro, nada gravado | I |
| RN-T2-C11 | CON | alteração de uma reserva para um horário ocupado por outra | Recusada | U |
| RN-T2-C12 | FEL | alteração de uma reserva dentro do próprio horário (ex.: 09:00–10:00 para 09:15–10:00) | Aceita, sem conflito consigo mesma | U |
| RN-T2-C13 | CON | sobreposição gravada diretamente no banco, sem passar pela aplicação | Rejeitada pela restrição de exclusão (ADR-003) | I |

### 3.3 RN-T3 / RF6 — Duas solicitações simultâneas produzem uma única reserva aceita
*Origem:* `docs/requisitos.md` RN-T3, RF6, RQ-3, LIM-4.
| Caso | Tipo | Entrada | Resultado esperado | Camada |
|---|---|---|---|---|
| RN-T3-C01 | CON | 2 requisições simultâneas, mesmo recurso e intervalo | Exatamente 1 aceita, 1 recusada | I |
| RN-T3-C02 | CON | N requisições simultâneas, mesmo recurso e intervalo | Exatamente 1 aceita, N−1 recusadas | I |
| RN-T3-C03 | CON | 2 simultâneas, intervalos parcialmente sobrepostos | Exatamente 1 aceita | I |
| RN-T3-C04 | FEL | 2 simultâneas, recursos diferentes | As 2 aceitas | I |
| RN-T3-C05 | CON | recusa por concorrência | Registrada como tentativa recusada, motivo CONCORRÊNCIA (RF12) | I |

### 3.4 RN-T4 / RF8 — Recursos em manutenção não podem ser reservados ⟶ **reservada B3**
*Origem:* `docs/requisitos.md` RN-T4, RF8.
| Caso | Tipo | Entrada (bloqueio 08:00–10:00) | Resultado esperado | Camada |
|---|---|---|---|---|
| RN-T4-C01 | CON | reserva 08:30–09:30 | Recusada | U (B3) |
| RN-T4-C02 | LIM | reserva que termina exatamente no início do bloqueio (07:00–08:00) | Aceita — intervalo semiaberto (**D-01**) | U (B3) |
| RN-T4-C03 | LIM | reserva que começa exatamente no fim do bloqueio (10:00–11:00) | Aceita — intervalo semiaberto (**D-01**) | U (B3) |
| RN-T4-C04 | LIM | reserva que cruza o bloqueio em 1 segundo | Recusada | U (B3) |
| RN-T4-C05 | FEL | reserva fora do bloqueio | Aceita | U (B3) |
| RN-T4-C06 | CON | bloqueio criado sobre reserva já existente | **Pergunta Q-05** | U (B3) |
| RN-T4-C07 | CON | criação de reserva pela API sobre recurso em manutenção | Recusada com mensagem compreensível | A (B3) |

### 3.5 RN-E1 / RN-E2 — Fluxo de estados ⟶ **reservada B4**
*Origem:* `docs/requisitos.md` RN-E1, RN-E2.
| Caso | Tipo | Entrada | Resultado esperado | Camada |
|---|---|---|---|---|
| RN-E1-C01 | FEL | nova reserva | Nasce SOLICITADA | U (**fora de B4**: estado inicial, sem transição) |
| RN-E1-C02 | FEL | SOLICITADA → APROVADA | Permitida | U (B4) |
| RN-E1-C03 | FEL | APROVADA → EM_USO | Permitida | U (B4) |
| RN-E1-C04 | FEL | EM_USO → CONCLUÍDA | Permitida | U (B4) |
| RN-E1-C05 | PRO | SOLICITADA → EM_USO (pula a aprovação) | Recusada | U (B4) |
| RN-E1-C06 | PRO | SOLICITADA → CONCLUÍDA | Recusada | U (B4) |
| RN-E1-C07 | PRO | CONCLUÍDA → qualquer estado | Recusada | U (B4) |
| RN-E2-C01 | FEL | SOLICITADA → REJEITADA | Permitida | U (B4) |
| RN-E2-C02 | FEL | SOLICITADA ou APROVADA → CANCELADA | Permitida (quais estados de origem é pergunta **Q-06**) | U (B4) |
| RN-E2-C03 | FEL | APROVADA → NÃO_COMPARECEU | Permitida (quem e quando é pergunta **Q-07**) | U (B4) |
| RN-E2-C04 | PRO | REJEITADA, CANCELADA ou NÃO_COMPARECEU → qualquer estado | Recusada | U (B4) |

### 3.6 RN-E3 / RF7 — Somente responsáveis aprovam recursos restritos ⟶ **reservada B1 e B2**
*Origem:* `docs/requisitos.md` RN-E3, RF7.
| Caso | Tipo | Entrada | Resultado esperado | Camada |
|---|---|---|---|---|
| RN-E3-C01 | FEL | Responsável aprova reserva SOLICITADA de recurso restrito | APROVADA | U / A (B1) |
| RN-E3-C02 | PRO | Solicitante tenta aprovar reserva de recurso restrito | Negado, estado inalterado | A (B1) |
| RN-E3-C03 | PRO | Administrador tenta aprovar reserva de recurso restrito | **Pergunta Q-08** (a fonte diz "somente responsáveis") | A (B1) |
| RN-E3-C04 | PRO | Solicitante tenta aprovar a própria reserva | Negado | U / A (B2) |
| RN-E3-C05 | FEL | reserva de recurso não restrito | **Pergunta Q-04** (P-18: nasce aprovada?) | U (B1) |
| RN-E3-C06 | FEL | Responsável rejeita reserva de recurso restrito | REJEITADA | U (B1/B4) |

### 3.7 RN-E4 — Reservas iniciadas não podem ser apagadas
*Origem:* `docs/requisitos.md` RN-E4.
| Caso | Tipo | Entrada | Resultado esperado | Camada |
|---|---|---|---|---|
| RN-E4-C01 | PRO | apagar reserva EM_USO | Recusado; reserva continua existindo | U |
| RN-E4-C02 | PRO | apagar reserva CONCLUÍDA | Recusado (CONCLUÍDA também foi iniciada) | U |
| RN-E4-C03 | PRO | apagar reserva SOLICITADA ou APROVADA (não iniciada) | Recusado — nenhuma reserva é apagada; desistir é cancelar (**D-04**) | U |
| RN-E4-C04 | PRO | apagar reserva iniciada pela API | Recusado com mensagem; nada removido | A |

> Para RN-E4, a reserva é montada **diretamente** no estado EM_USO ou CONCLUÍDA (fixture), sem passar pela transição, que pertence a B4.

### 3.8 RN-E5 / RF10 — Toda mudança gera auditoria; histórico auditável
*Origem:* `docs/requisitos.md` RN-E5, RF10. *Decisão:* ADR-004.
| Caso | Tipo | Entrada | Resultado esperado | Camada |
|---|---|---|---|---|
| RN-E5-C01 | FEL | criação de reserva (→ SOLICITADA) | 1 registro de auditoria: entidade, estado novo, ator, instante | I |
| RN-E5-C02 | FEL | alteração de reserva | Registro de auditoria com antes e depois | I |
| RN-E5-C03 | FEL | cadastro ou alteração de recurso pelo Administrador | Registro de auditoria (RF10, "mudanças") | I |
| RN-E5-C04 | CON | falha na gravação da auditoria | A mudança também é desfeita; nada gravado | I |
| RN-E5-C05 | LIM | instante da auditoria | Igual ao do `Clock` injetado (determinístico) | U |
| RN-E5-C06 | FEL | transições de estado (aprovar, iniciar, concluir, rejeitar, cancelar, não compareceu) | Registro por transição | I (**B4**) |
| RF10-C01 | FEL | consulta do histórico de uma reserva | Lista em ordem cronológica com ator e mudança | I / A |

### 3.9 RF9 — Registro de retirada e devolução de materiais e equipamentos
*Origem:* `docs/requisitos.md` RF9.
| Caso | Tipo | Entrada | Resultado esperado | Camada |
|---|---|---|---|---|
| RF9-C01 | FEL | registrar retirada de material de uma reserva | Retirada gravada com instante e quem registrou | U |
| RF9-C02 | FEL | registrar devolução de material retirado | Devolução gravada | U |
| RF9-C03 | PRO | devolver material que não foi retirado | Recusado | U |
| RF9-C04 | PRO | registrar retirada duas vezes do mesmo item | Recusado | U |
| RF9-C05 | LIM | devolução no mesmo instante da retirada | **Pergunta Q-10** | U |
| RF9-C06 | INV | retirada de recurso que não é material (ex.: sala) | **Pergunta Q-11** | U |
| RF9-C07 | PRO | retirada em reserva que não está em estado permitido | **Pergunta Q-12** (dependente de B4) | U |

### 3.10 RF12 — Relatório de utilização, carga horária e conflitos evitados
*Origem:* `docs/requisitos.md` RF12.
| Caso | Tipo | Entrada | Resultado esperado | Camada |
|---|---|---|---|---|
| RF12-C01 | FEL | 2 reservas de 1 h na sala S-101 no período | Utilização da S-101 = 2 h | U |
| RF12-C02 | FEL | professor alocado em 3 reservas de 1 h | Carga horária = 3 h | U |
| RF12-C03 | FEL | 2 tentativas recusadas por sobreposição | Conflitos evitados = 2 | U |
| RF12-C04 | LIM | período sem reservas | Utilização 0, carga 0, conflitos 0 | U |
| RF12-C05 | LIM | reserva que cruza o limite do período do relatório | **Pergunta Q-13** (conta inteira ou só a parte dentro?) | U |
| RF12-C06 | CON | reservas CANCELADAS ou REJEITADAS | **Pergunta Q-14** (entram na utilização?) | U |

### 3.11 RF1 — Autenticação e autorização por perfil (fora de B1–B2)
*Origem:* `docs/requisitos.md` RF1, §2; ADR-015.
| Caso | Tipo | Entrada | Resultado esperado | Camada |
|---|---|---|---|---|
| RF1-C01 | PRO | usuário anônimo acessa operação de reserva | Negado (não autenticado) | A |
| RF1-C02 | FEL | Solicitante consulta disponibilidade e cria reserva | Permitido | A |
| RF1-C03 | PRO | Solicitante tenta cadastrar sala | Negado (cadastro é do Administrador, §2) | A |
| RF1-C04 | FEL | Administrador cadastra sala | Permitido | A |
| RF1-C05 | PRO | Solicitante tenta alterar reserva de outro solicitante | Negado, reserva inalterada (**D-03**) | A |
| RF1-C06 | FEL | Responsável ou Administrador altera reserva de um solicitante | Permitido (**D-03**) | A |

> O cancelamento por outra pessoa também segue a D-03, mas cancelar é transição de estado (B4) e fica para o ciclo BDD.

### 3.12 RF2 / RF3 — Cadastro, consulta e pesquisa
*Origem:* `docs/requisitos.md` RF2, RF3.
| Caso | Tipo | Entrada | Resultado esperado | Camada |
|---|---|---|---|---|
| RF2-C01 | FEL | cadastrar sala, professor e material | Consultáveis depois | I |
| RF2-C02 | INV | cadastro sem nome ou sem tipo | Recusado | U |
| RF3-C01 | FEL | pesquisa por tipo SALA | Só salas | I |
| RF3-C02 | LIM | pesquisa por capacidade ≥ 30 com salas de 29, 30 e 31 | Retorna 30 e 31 | I |
| RF3-C03 | FEL | pesquisa por localização | Só recursos da localização | I |
| RF3-C04 | FEL | pesquisa por disponibilidade 09:00–10:00 | Exclui recursos ocupados no intervalo | I |
| RF3-C05 | FEL | pesquisa de professor por competência | **Pergunta Q-16** (o que é competência, L-07) | I |

### 3.13 RF11 — Notificação simulada ou API externa
*Origem:* `docs/requisitos.md` RF11; ADR-005.
| Caso | Tipo | Entrada | Resultado esperado | Camada |
|---|---|---|---|---|
| RF11-C01 | FEL | reserva criada | Notificação registrada na outbox | I |
| RF11-C02 | CON | serviço externo fora do ar (WireMock) | Reserva mantida; notificação pendente para reenvio | I |
| RF11-C03 | FEL | serviço externo volta | Notificação enviada uma vez | I |

### 3.14 Fora do inventário de regras de negócio
RF13 (interface responsiva) e RF14 (documentação) são requisitos de qualidade e de entrega, não regras de negócio. Ficam para os testes end-to-end e para a revisão de documentação, fora deste plano. RQ-*, MT-* e LIM-* são critérios do processo e da avaliação.

## 4. Ambiguidades, lacunas e perguntas

| ID | Pergunta | Regras / casos afetados | Ligação |
|---|---|---|---|
| Q-01 | Intervalos que se encostam (fim de um = início de outro) se sobrepõem? Isso vale para reserva × reserva e reserva × manutenção | RN-T2-C08, C09; RN-T4-C02, C03 | — |
| Q-02 | Qual o fuso e a precisão do horário (minuto, segundo)? | RN-T1, RN-T2, RN-T4 | S-07, P-05 |
| Q-03 | Qual a duração mínima de uma reserva? | RN-T1-C03 | — |
| Q-04 | Reserva de recurso não restrito nasce APROVADA ou SOLICITADA? | RN-E3-C05, RN-E1-C01 | P-18, S-09 |
| Q-05 | O que acontece com reservas existentes quando um bloqueio de manutenção é criado por cima? | RN-T4-C06 | — |
| Q-06 | De quais estados se pode cancelar? | RN-E2-C02 | L-08 |
| Q-07 | Quem marca "não compareceu" e a partir de quando? | RN-E2-C03 | L-08, P-08 |
| Q-08 | O Administrador também pode aprovar recurso restrito? | RN-E3-C03 | — |
| Q-09 | Reserva não iniciada pode ser apagada ou só cancelada? | RN-E4-C03 | — |
| Q-10 | Devolução no mesmo instante da retirada é válida? | RF9-C05 | — |
| Q-11 | Só materiais e equipamentos têm retirada e devolução? | RF9-C06 | — |
| Q-12 | Em que estados da reserva a retirada é permitida? | RF9-C07 | B4 |
| Q-13 | No relatório, uma reserva que cruza o período conta inteira ou só a parte de dentro? | RF12-C05 | — |
| Q-14 | Reservas canceladas ou rejeitadas entram na utilização? | RF12-C06 | — |
| Q-15 | Um Solicitante só altera e cancela as próprias reservas? | RF1-C05 | `docs/prd.md` P1 |
| Q-16 | O que é competência de um professor e como é cadastrada? | RF3-C05 | L-07, P-07 |
| Q-17 | Uma reserva SOLICITADA já ocupa o horário? | RN-T2, RN-T3 | P-19, S-10 |

### 4.1 Decisões da equipe (2026-11-19)

> Respostas da equipe às perguntas que bloqueavam a fase red. Elas **não são** da especificação: a origem é "decisão da equipe", e cada uma pode ser revista se o professor decidir diferente.

| ID | Pergunta | Decisão | Alternativas descartadas | Casos afetados |
|---|---|---|---|---|
| **D-01** | Q-01 — intervalos que se encostam se sobrepõem? | **Não.** Intervalo **semiaberto** [início, término): o término não ocupa. Vale para reserva × reserva e reserva × manutenção | Intervalo fechado (impede aulas seguidas); folga obrigatória entre reservas (regra nova, fora da especificação) | RN-T2-C08, C09; RN-T4-C02, C03 |
| **D-02** | Q-17 — SOLICITADA já ocupa o horário? | **Sim.** SOLICITADA e APROVADA ocupam; REJEITADA e CANCELADA liberam | Só APROVADA ocupa (conflita com RN-T3); ocupa só se não restrito (mistura regras) | RN-T2, RN-T3 (todos os casos com reserva pendente) |
| **D-03** | Q-15 — Solicitante altera ou cancela reserva de outra pessoa? | **Não.** Solicitante age só sobre as próprias; Responsável e Administrador podem agir sobre qualquer uma | Qualquer Solicitante (risco LIM-3); só o dono, sem exceção | RF1-C05, RF1-C06 |
| **D-04** | Q-09 — reserva não iniciada pode ser apagada? | **Não.** Nenhuma reserva é apagada; desistir é cancelar, preservando o histórico | Apagar se não iniciada; só o Administrador apaga | RN-E4-C03 |

**Lacunas:**
- **L-T1** — não há código de domínio nem testes de regra no repositório. Todos os testes da Aula 05 vão exigir assinaturas mínimas.
- **L-T2** — `docs/rtm.md` não existe; a ligação requisito → teste fica na matriz da §5 até a etapa 4.
- **L-T3** — casos marcados com pergunta (**Q-xx**) não têm resultado esperado na fonte. Na fase red, eles recebem teste só depois da resposta; até lá ficam "sem teste — aguardando Q-xx".

## 5. Matriz regra → caso → teste → status

> Preenchida na Aula 05, Prompt 2 (fase red). Status possíveis: **sem teste** · **vermelho** (falha pelo motivo esperado) · **verde** (Aula 06) · **reservada BDD** · **aguardando Q-xx**.

| Regra | Casos | Teste | Status |
|---|---|---|---|
| RN-T1 | C01–C06 (C03 aguarda Q-03) | — | sem teste |
| RN-T2 / RF5 | C01–C13, RF5-C01–C04 | — | sem teste |
| RN-T3 / RF6 | C01–C05 | — | sem teste |
| RN-T4 / RF8 | C01–C07 | — | **reservada BDD (B3)** |
| RN-E1 / RN-E2 | RN-E1-C01 | — | sem teste |
| RN-E1 / RN-E2 | demais casos | — | **reservada BDD (B4)** |
| RN-E3 / RF7 | C01–C06 | — | **reservada BDD (B1, B2)** |
| RN-E4 | C01–C04 | — | sem teste |
| RN-E5 / RF10 | C01–C05, RF10-C01 (C06 é B4) | — | sem teste |
| RF9 | C01–C04 (C05–C07 aguardam Q-10–Q-12) | — | sem teste |
| RF12 | C01–C04 (C05, C06 aguardam Q-13, Q-14) | — | sem teste |
| RF1 | C01–C06 | — | sem teste |
| RF2 / RF3 | RF2-C01, C02, RF3-C01–C04 (RF3-C05 aguarda Q-16) | — | sem teste |
| RF11 | C01–C03 | — | sem teste |
