# AlocaSenac — Cenários BDD: aprovação, manutenção e transições

> **Etapa:** BDD Parte 1. Só cenários, nenhum código alterado.
> **Regras:** B1–B4 de `docs/testes/plano-tdd.md` §2, reservadas para este ciclo.
> **Fontes lidas:** `docs/prd.md` (personas P1–P3), `docs/requisitos.md` (RF7, RF8, RN-T4, RN-E1–RN-E5, perfis), `docs/rtm.md` (linhas pendentes de §4 e §5), `docs/testes/plano-tdd.md` (decisões D-01–D-04) e o código das reservas (`src/main/java/br/senac/alocasenac/reserva/`).
> **Situação do código:** existem os estados (`EstadoReserva`) e a origem BLOQUEIO (`OrigemOcupacao`). **Não existem** operações de aprovar, rejeitar, iniciar, concluir, cancelar ou marcar não compareceu, nem cadastro de bloqueio. Toda reserva nasce SOLICITADA (`Reserva.nova`).
> **Regra de escrita:** cada cenário tem **um único `Quando`** e **um `Então` observável**. O que a fonte não decide vira pergunta (§6), não política inventada.
> **Idioma Gherkin:** `# language: pt`. A Parte 3 (Cucumber) vai reusar as mesmas palavras.

## 0. Personagens e dados sintéticos

| Personagem | Perfil | Origem |
|---|---|---|
| Mariana (`mariana.teste@example.invalid`) | Solicitante | `docs/prd.md` P1 |
| Carla (`carla.teste@example.invalid`) | Solicitante | Sintética, para "outra pessoa" |
| Juliana (`juliana.teste@example.invalid`) | Responsável | `docs/prd.md` P2 |
| Paulo (`paulo.teste@example.invalid`) | Responsável | Sintético, para "outro Responsável" |
| Roberto (`roberto.teste@example.invalid`) | Administrador | `docs/prd.md` P3 |

| Recurso | Restrito? |
|---|---|
| Laboratório L-01 | sim |
| Sala S-101 | não |
| Projetor P-01 (material) | não |

Dia de referência: 2026-11-23, horários em UTC (o fuso continua sendo a pergunta Q-02).

---

## 1. B1 — Recurso restrito exige aprovação de um Responsável (RF7, RN-E3)

```gherkin
# language: pt
Funcionalidade: Aprovação de reserva de recurso restrito

  Cenário: B1-01 Reserva de recurso restrito aguarda aprovação
    Dado que o "Laboratório L-01" é um recurso restrito
    Quando a Mariana reserva o "Laboratório L-01" das 09:00 às 10:00
    Então a reserva fica no estado "SOLICITADA"

  Cenário: B1-02 Responsável aprova reserva de recurso restrito
    Dado que a Mariana tem uma reserva "SOLICITADA" do "Laboratório L-01"
    Quando a Juliana, Responsável, aprova a reserva
    Então a reserva fica no estado "APROVADA"

  Cenário: B1-03 Solicitante não aprova reserva de recurso restrito
    Dado que a Carla tem uma reserva "SOLICITADA" do "Laboratório L-01"
    Quando a Mariana, Solicitante, tenta aprovar a reserva
    Então a aprovação é negada e a reserva continua "SOLICITADA"

  Cenário: B1-04 Administrador não aprova reserva de recurso restrito
    Dado que a Mariana tem uma reserva "SOLICITADA" do "Laboratório L-01"
    Quando o Roberto, Administrador, tenta aprovar a reserva
    Então a aprovação é negada e a reserva continua "SOLICITADA"

  Cenário: B1-05 Responsável rejeita reserva de recurso restrito
    Dado que a Mariana tem uma reserva "SOLICITADA" do "Laboratório L-01"
    Quando a Juliana, Responsável, rejeita a reserva
    Então a reserva fica no estado "REJEITADA"

  Cenário: B1-06 Reserva rejeitada libera o horário
    Dado que a reserva da Mariana do "Laboratório L-01" das 09:00 às 10:00 foi rejeitada
    Quando a Carla reserva o "Laboratório L-01" das 09:00 às 10:00
    Então a reserva da Carla é aceita

  Cenário: B1-07 Reserva com um recurso restrito entre outros aguarda aprovação
    Dado que o "Laboratório L-01" é restrito e o "Projetor P-01" não é
    Quando a Mariana reserva o "Laboratório L-01" e o "Projetor P-01" das 09:00 às 10:00
    Então a reserva fica no estado "SOLICITADA"

  Cenário: B1-08 Reserva só de recursos não restritos é aprovada pelo sistema
    Dado que a "Sala S-101" não é um recurso restrito
    Quando a Mariana reserva a "Sala S-101" das 09:00 às 10:00
    Então a reserva fica no estado "APROVADA"

  Cenário: B1-09 Aprovação automática fica na auditoria com o sistema como ator
    Dado que a "Sala S-101" não é um recurso restrito
    Quando a Mariana reserva a "Sala S-101" das 09:00 às 10:00
    Então a auditoria registra a mudança de "SOLICITADA" para "APROVADA" com o sistema como ator
```

| Cenário | Base na fonte | Pendência |
|---|---|---|
| B1-01 | RF7 ("aprovação obrigatória para recursos restritos"); RN-E1 (fluxo começa em SOLICITADA) | — |
| B1-02 | RN-E3; P2 "aprova ou rejeita solicitações restritas" | — |
| B1-03 | RN-E3 ("somente responsáveis") | — |
| B1-04 | RN-E3 ("somente responsáveis"): o Administrador não está incluído | Q-08 do plano de TDD fica respondida pela própria fonte |
| B1-05 | P2 "aprova ou **rejeita**"; RN-E2 | — |
| B1-06 | D-02 (REJEITADA libera o horário) | — |
| B1-07 | RF7 aplicado a uma reserva com vários recursos | Decidido: **D-09** (a reserva inteira aguarda) |
| B1-08, B1-09 | RF7 (aprovação obrigatória **só** para restritos); RN-E1 (passa por SOLICITADA); RN-E5 (auditoria) | Decidido: **D-05** (aprovada pelo sistema) |

---

## 2. B2 — Solicitante não aprova a própria reserva (RN-E3)

```gherkin
# language: pt
Funcionalidade: Ninguém aprova a própria reserva

  Cenário: B2-01 Solicitante tenta aprovar a própria reserva
    Dado que a Mariana tem uma reserva "SOLICITADA" do "Laboratório L-01"
    Quando a Mariana tenta aprovar a própria reserva
    Então a aprovação é negada e a reserva continua "SOLICITADA"

  Cenário: B2-02 Usuário que também é Responsável não aprova a própria reserva
    Dado que a Juliana tem os perfis Solicitante e Responsável
    E que a Juliana tem uma reserva "SOLICITADA" do "Laboratório L-01"
    Quando a Juliana tenta aprovar a própria reserva
    Então a aprovação é negada e a reserva continua "SOLICITADA"

  Cenário: B2-03 Outro Responsável aprova a reserva de quem também é Responsável
    Dado que a Juliana tem os perfis Solicitante e Responsável
    E que a Juliana tem uma reserva "SOLICITADA" do "Laboratório L-01"
    Quando o Paulo, Responsável, aprova a reserva
    Então a reserva fica no estado "APROVADA"
```

| Cenário | Base na fonte | Pendência |
|---|---|---|
| B2-01 | Regra B2 (enunciado do roteiro); RN-E3 | — |
| B2-02 | Regra B2: o caso em que ela se distingue de RN-E3 | Decidido: **D-10** (pode ter dois perfis) |
| B2-03 | RN-E3 + regra B2 | Decidido: **D-10** |

---

## 3. B3 — Recurso em manutenção não pode ser reservado (RF8, RN-T4)

```gherkin
# language: pt
Funcionalidade: Bloqueio por manutenção

  Cenário: B3-01 Administrador bloqueia um recurso para manutenção
    Quando o Roberto, Administrador, bloqueia a "Sala S-101" para manutenção das 08:00 às 10:00
    Então a "Sala S-101" passa a ter um bloqueio de manutenção das 08:00 às 10:00

  Cenário: B3-02 Solicitante não bloqueia recurso para manutenção
    Quando a Mariana, Solicitante, tenta bloquear a "Sala S-101" para manutenção das 08:00 às 10:00
    Então o bloqueio é negado e a "Sala S-101" continua sem bloqueio

  Cenário: B3-03 Bloqueio com término antes do início é recusado
    Quando o Roberto, Administrador, tenta bloquear a "Sala S-101" das 10:00 às 08:00
    Então o bloqueio é recusado por período inválido

  Esquema do Cenário: B3-04 Reserva diante de um período de manutenção (fronteiras)
    Dado que a "Sala S-101" está em manutenção das 08:00 às 10:00
    Quando a Mariana reserva a "Sala S-101" das <inicio> às <termino>
    Então a reserva é <resultado>

    Exemplos:
      | caso                                  | inicio   | termino  | resultado |
      | termina 1 segundo antes do início     | 07:00    | 07:59:59 | aceita    |
      | termina exatamente no início          | 07:00    | 08:00    | aceita    |
      | cruza o início por 1 segundo          | 07:00    | 08:00:01 | recusada  |
      | fica dentro do período                | 08:30    | 09:30    | recusada  |
      | cobre o período inteiro               | 07:00    | 11:00    | recusada  |
      | cruza o fim por 1 segundo             | 09:59:59 | 11:00    | recusada  |
      | começa exatamente no fim              | 10:00    | 11:00    | aceita    |
      | começa 1 segundo depois do fim        | 10:00:01 | 11:00    | aceita    |

  Cenário: B3-05 Recusa por manutenção conta como conflito evitado
    Dado que a "Sala S-101" está em manutenção das 08:00 às 10:00
    Quando a Mariana reserva a "Sala S-101" das 08:30 às 09:30
    Então fica registrada uma tentativa recusada com motivo "BLOQUEIO"

  Cenário: B3-06 Reserva com vários recursos e um em manutenção é recusada por inteiro
    Dado que o "Projetor P-01" está em manutenção das 08:00 às 10:00
    Quando a Mariana reserva a "Sala S-101" e o "Projetor P-01" das 09:00 às 10:00
    Então a reserva é recusada e nenhuma ocupação é gravada

  Cenário: B3-07 Alterar uma reserva para dentro da manutenção é recusado
    Dado que a "Sala S-101" está em manutenção das 08:00 às 10:00
    E que a Mariana tem uma reserva da "Sala S-101" das 11:00 às 12:00
    Quando a Mariana altera a reserva para das 09:00 às 10:00
    Então a alteração é recusada e a reserva continua das 11:00 às 12:00

  Cenário: B3-08 Outro recurso no horário da manutenção continua disponível
    Dado que a "Sala S-101" está em manutenção das 08:00 às 10:00
    Quando a Mariana reserva o "Projetor P-01" das 08:00 às 10:00
    Então a reserva é aceita

  Cenário: B3-09 O banco recusa reserva sobreposta a um bloqueio mesmo sem passar pela aplicação
    Dado que a "Sala S-101" tem um bloqueio de manutenção ativo das 08:00 às 10:00
    Quando uma ocupação de reserva da "Sala S-101" das 09:00 às 09:30 é gravada direto no banco
    Então o banco recusa a gravação

  Cenário: B3-10 Bloqueio sobre período com reserva ativa é recusado
    Dado que a Mariana tem uma reserva "APROVADA" da "Sala S-101" das 09:00 às 10:00
    Quando o Roberto, Administrador, tenta bloquear a "Sala S-101" para manutenção das 08:00 às 10:00
    Então o bloqueio é recusado e a reserva da Mariana continua "APROVADA"

  Cenário: B3-11 Bloqueio sobre período de reserva cancelada é aceito
    Dado que a reserva da Mariana da "Sala S-101" das 09:00 às 10:00 foi cancelada
    Quando o Roberto, Administrador, bloqueia a "Sala S-101" para manutenção das 08:00 às 10:00
    Então a "Sala S-101" passa a ter um bloqueio de manutenção das 08:00 às 10:00

  Esquema do Cenário: B3-12 Bloqueio não começa no passado
    Dado que agora são 08:00
    Quando o Roberto, Administrador, bloqueia a "Sala S-101" para manutenção das <inicio> às 10:00
    Então o bloqueio é <resultado>

    Exemplos:
      | caso                        | inicio   | resultado |
      | começa 1 segundo antes      | 07:59:59 | recusado  |
      | começa exatamente agora     | 08:00    | aceito    |
      | começa 1 segundo depois     | 08:00:01 | aceito    |

  Esquema do Cenário: B3-13 Reserva não começa no passado
    Dado que agora são 08:00
    Quando a Mariana reserva a "Sala S-101" das <inicio> às 10:00
    Então a reserva é <resultado>

    Exemplos:
      | caso                        | inicio   | resultado |
      | começa 1 segundo antes      | 07:59:59 | recusada  |
      | começa exatamente agora     | 08:00    | aceita    |
      | começa 1 segundo depois     | 08:00:01 | aceita    |
```

**Tabela de fronteiras (B3-04):** a fonte (`docs/prd.md`, `docs/requisitos.md`) **não decide** se o término de um período ocupa o instante final. A pergunta foi levada ao grupo e respondida na **decisão D-01** (`docs/testes/plano-tdd.md` §4.1): intervalo **semiaberto [início, término)**, válido para reserva × reserva **e reserva × manutenção**. Por isso "termina exatamente no início" e "começa exatamente no fim" são **aceitas**, e "cruza um segundo" é **recusada**. Se o professor decidir diferente, só a coluna `resultado` dessas duas linhas muda.

| Cenário | Base na fonte | Pendência |
|---|---|---|
| B3-01 | PF-ADM "gerencia… bloqueios e períodos de manutenção"; P3 "cria bloqueios de manutenção"; RF8 | — |
| B3-02 | Perfis (§2 de `docs/requisitos.md`): bloqueio é do Administrador | — |
| B3-03 | RN-T1 ("o término deve ser posterior ao início") aplicada ao período do bloqueio | — |
| B3-04 | RN-T4 + D-01 | Fronteiras decididas em D-01 |
| B3-05 | RF12 (conflitos evitados) + motivo `BLOQUEIO` já existente em `MotivoRecusa` | Contagem por pedido ou por recurso: R1-02 |
| B3-06 | RN-T4 + RF5-C04 (recusa por inteiro) | — |
| B3-07 | RN-T4 aplicada à alteração (RF4) | — |
| B3-08 | RN-T4 vale por recurso | — |
| B3-09 | I-4 / ADR-003 (garantia no banco para bloqueio também) | — |
| B3-10, B3-11 | RN-T4 + D-02 (só ocupação ativa conta) | Decidido: **D-08** (recusar o bloqueio) |
| B3-12, B3-13 | Regra de tempo com `Clock` injetado; trinca −1 s / agora / +1 s | Decidido: **D-12** (nada começa no passado) |

---

## 4. B4 — Só as transições de estado permitidas (RN-E1, RN-E2, RN-E5)

```gherkin
# language: pt
Funcionalidade: Transições de estado da reserva

  Esquema do Cenário: B4-01 Transições do fluxo principal são permitidas
    Dado que existe uma reserva no estado "<origem>"
    Quando a reserva recebe a ação "<acao>"
    Então a reserva fica no estado "<destino>"

    Exemplos:
      | origem     | acao      | destino    |
      | SOLICITADA | aprovar   | APROVADA   |
      | APROVADA   | iniciar   | EM_USO     |
      | EM_USO     | concluir  | CONCLUIDA  |

  Esquema do Cenário: B4-02 Transições alternativas são permitidas
    Dado que existe uma reserva no estado "<origem>"
    Quando a reserva recebe a ação "<acao>"
    Então a reserva fica no estado "<destino>"

    Exemplos:
      | origem     | acao                   | destino        |
      | SOLICITADA | rejeitar               | REJEITADA      |
      | SOLICITADA | cancelar               | CANCELADA      |
      | APROVADA   | cancelar               | CANCELADA      |
      | APROVADA   | marcar não compareceu  | NAO_COMPARECEU |

  Esquema do Cenário: B4-03 Transições fora do fluxo são recusadas
    Dado que existe uma reserva no estado "<origem>"
    Quando a reserva recebe a ação "<acao>"
    Então a transição é recusada e a reserva continua "<origem>"

    Exemplos:
      | origem         | acao                   |
      | SOLICITADA     | iniciar                |
      | SOLICITADA     | concluir               |
      | SOLICITADA     | marcar não compareceu  |
      | APROVADA       | aprovar                |
      | APROVADA       | rejeitar               |
      | APROVADA       | concluir               |
      | EM_USO         | aprovar                |
      | EM_USO         | iniciar                |
      | EM_USO         | cancelar               |
      | EM_USO         | marcar não compareceu  |
      | CONCLUIDA      | cancelar               |
      | CONCLUIDA      | iniciar                |
      | REJEITADA      | aprovar                |
      | CANCELADA      | iniciar                |
      | NAO_COMPARECEU | iniciar                |

  Cenário: B4-04 Toda transição gera auditoria
    Dado que a Mariana tem uma reserva "SOLICITADA" do "Laboratório L-01"
    Quando a Juliana, Responsável, aprova a reserva
    Então a auditoria registra a mudança de "SOLICITADA" para "APROVADA" com a Juliana como ator

  Cenário: B4-05 Reserva cancelada libera o horário
    Dado que a reserva da Mariana da "Sala S-101" das 09:00 às 10:00 foi cancelada
    Quando a Carla reserva a "Sala S-101" das 09:00 às 10:00
    Então a reserva da Carla é aceita

  Cenário: B4-06 Solicitante cancela a própria reserva
    Dado que a Mariana tem uma reserva "SOLICITADA" da "Sala S-101"
    Quando a Mariana cancela a reserva
    Então a reserva fica no estado "CANCELADA"

  Cenário: B4-07 Solicitante não cancela reserva de outra pessoa
    Dado que a Carla tem uma reserva "SOLICITADA" da "Sala S-101"
    Quando a Mariana tenta cancelar a reserva
    Então o cancelamento é negado e a reserva continua "SOLICITADA"

  Cenário: B4-08 Solicitante não marca não compareceu
    Dado que a Mariana tem uma reserva "APROVADA" da "Sala S-101"
    Quando a Mariana tenta marcar a reserva como não compareceu
    Então a marcação é negada e a reserva continua "APROVADA"

  Esquema do Cenário: B4-09 Uso só começa a partir do horário de início
    Dado que a Mariana tem uma reserva "APROVADA" da "Sala S-101" das 09:00 às 10:00
    E que agora são <agora>
    Quando a Juliana, Responsável, inicia o uso da reserva
    Então o início do uso é <resultado>

    Exemplos:
      | caso                     | agora    | resultado |
      | 1 segundo antes          | 08:59:59 | recusado  |
      | exatamente no início     | 09:00    | aceito    |
      | 1 segundo depois         | 09:00:01 | aceito    |

  Esquema do Cenário: B4-10 Não compareceu só a partir do horário de início
    Dado que a Mariana tem uma reserva "APROVADA" da "Sala S-101" das 09:00 às 10:00
    E que agora são <agora>
    Quando a Juliana, Responsável, marca a reserva como não compareceu
    Então a marcação é <resultado>

    Exemplos:
      | caso                     | agora    | resultado |
      | 1 segundo antes          | 08:59:59 | recusada  |
      | exatamente no início     | 09:00    | aceita    |
      | 1 segundo depois         | 09:00:01 | aceita    |

  Cenário: B4-11 Solicitante não inicia o uso
    Dado que a Mariana tem uma reserva "APROVADA" da "Sala S-101" das 09:00 às 10:00
    E que agora são 09:00
    Quando a Mariana tenta iniciar o uso da reserva
    Então o início é negado e a reserva continua "APROVADA"

  Cenário: B4-12 Não compareceu libera o restante do horário
    Dado que a Mariana tem uma reserva "APROVADA" da "Sala S-101" das 09:00 às 11:00
    E que a Juliana marcou a reserva como não compareceu às 09:15
    E que agora são 09:20
    Quando a Carla reserva a "Sala S-101" das 09:30 às 11:00
    Então a reserva da Carla é aceita
```

| Cenário | Base na fonte | Pendência |
|---|---|---|
| B4-01 | RN-E1 | Quem inicia e quem conclui: Responsável (**D-07**) |
| B4-02 SOLICITADA → REJEITADA | RN-E2; P2 "aprova ou rejeita" (decisão sobre a solicitação) | — |
| B4-02 SOLICITADA → CANCELADA | RN-E2; PF-SOL "cancela reservas" | — |
| B4-02 APROVADA → CANCELADA | RN-E2 | Decidido: **D-06** (cancela em SOLICITADA e APROVADA) |
| B4-02 APROVADA → NAO_COMPARECEU | RN-E2; P2 "marca não compareceu" | Decidido: **D-07** (Responsável, a partir do início) |
| B4-03 | RN-E1 e RN-E2; `cancelar` em EM_USO recusado | Decidido: **D-06** |
| B4-04 | RN-E5 | — |
| B4-05 | D-02 (CANCELADA libera o horário) | — |
| B4-06, B4-07 | PF-SOL "cancela reservas"; D-03 (só as próprias) | — |
| B4-08, B4-11 | P2: quem registra o uso e marca não compareceu é o Responsável | Decidido: **D-07** |
| B4-09, B4-10 | Regra de tempo com `Clock`; trinca −1 s / início / +1 s | Decidido: **D-07** |
| B4-12 | D-02 estendida | Decidido: **D-11** (não compareceu libera) |

> **Quem faz cada transição (D-07, D-03, RN-E3):** aprovar e rejeitar = Responsável (para restritos; o sistema aprova os não restritos, D-05) · iniciar, concluir e marcar não compareceu = Responsável · cancelar = o dono (Solicitante), Responsável ou Administrador (D-03).

---

## 5. Unidade × API

| Cenário | Vira | Por quê |
|---|---|---|
| B1-01, B1-07 | Teste de unidade (domínio: estado inicial conforme o recurso) **e** API | A regra é de domínio; a API prova a resposta ao Solicitante |
| B1-02, B1-05 | Teste de unidade (transição) **e** API | A transição é de domínio; a API prova quem pode |
| B1-03, B1-04 | **API** | É autorização por perfil (Filtro de Segurança e controller) |
| B1-06, B4-05 | Integração (banco real) | Depende da ocupação ficar inativa e da restrição do banco |
| B2-01, B2-02, B2-03 | **API** | Depende do usuário autenticado e dos perfis do token |
| B3-01, B3-02, B3-03 | **API** | Cadastro de bloqueio pelo Administrador |
| B3-04 (8 exemplos) | **Teste de unidade** parametrizado (Verificador) | Regra pura de intervalo, com as fronteiras D-01 |
| B3-05, B3-06, B3-07, B3-08 | Integração (serviço + banco) | Registro de recusa, atomicidade e alteração |
| B3-09 | Integração (banco real) | Garantia da restrição de exclusão |
| B4-01, B4-02, B4-03 | **Teste de unidade** parametrizado (máquina de estados de `Reserva`) | Regra pura de estado |
| B4-04, B1-09 | Integração | Auditoria na mesma transação |
| B4-06, B4-07, B4-08, B4-11 | **API** | Autorização por dono e por perfil |
| B1-08 | Teste de unidade (serviço decide pela restrição) **e** API | Regra de domínio e resposta ao Solicitante |
| B3-10, B3-11 | Integração | Depende das ocupações ativas no banco |
| B3-12, B3-13, B4-09, B4-10 | **Teste de unidade** parametrizado com `Clock` fixo | Regra de tempo; o "agora" vem do `Clock` injetado, nunca de `Instant.now()` |
| B4-12 | Integração com `Clock` fixo | Ocupação inativa + nova reserva |

**Vão para a feature Cucumber (Parte 3), por serem cenários de API:** B1-01, B1-02, B1-03, B1-04, B1-05, B1-07, B1-08, B2-01, B2-02, B2-03, B3-01, B3-02, B3-03, B3-04 (pela API, com os mesmos 8 exemplos), B3-10, B4-06, B4-07, B4-08 e B4-11.

---

## 6. Perguntas levadas ao grupo e decisões (2026-11-19)

> A fonte não decidia estes pontos. As perguntas foram levadas ao grupo com propostas, e as respostas viraram decisões da equipe, com origem "decisão da equipe" e não "especificação". Se o professor decidir diferente, mudam só os cenários indicados.

| Pergunta | Decisão | Alternativas descartadas | Cenários |
|---|---|---|---|
| QB-1 Reserva só de recursos não restritos precisa de aprovação? | **D-05**: nasce SOLICITADA e o **sistema a aprova na hora**, com auditoria (ator "sistema") | Todas esperam um Responsável | B1-08, B1-09 |
| QB-2 De quais estados se cancela? | **D-06**: **SOLICITADA e APROVADA**; EM_USO em diante, não | Só SOLICITADA; também EM_USO | B4-02, B4-03 |
| QB-3 Quem marca EM_USO, CONCLUÍDA e NÃO COMPARECEU, e quando? | **D-07**: o **Responsável**; EM_USO e NÃO COMPARECEU só **a partir do horário de início** (`Clock`); CONCLUÍDA a qualquer momento depois de EM_USO | Responsável sem horário; Solicitante inicia e conclui | B4-01, B4-02, B4-08–B4-11 |
| QB-4 Bloqueio sobre período com reserva ativa? | **D-08**: o **bloqueio é recusado**; o Administrador resolve antes | Aceitar e cancelar as reservas; aceitar e manter as duas | B3-10, B3-11 |
| QB-5 Reserva com vários recursos e só um restrito? | **D-09**: a **reserva inteira** aguarda aprovação | Dividir a reserva | B1-07 |
| QB-6 Um usuário pode ter dois perfis? | **D-10**: **pode**; mesmo assim não aprova a própria reserva | Um perfil por usuário | B2-02, B2-03 |
| QB-7 NÃO COMPARECEU libera o horário? | **D-11**: **libera** (ocupação inativa, como D-02) | Não libera | B4-12 |
| QB-8 Reserva ou bloqueio no passado? | **D-12**: **nenhum dos dois** começa antes de agora (`Clock`) | Bloqueio retroativo permitido; sem regra | B3-12, B3-13 |

## 7. Efeitos das decisões sobre testes existentes (para a Parte 2)

Duas decisões mudam o comportamento que **testes já existentes** esperam. Na Parte 2 não se altera teste para ele passar, então os dois casos ficam registrados aqui para decisão explícita **antes** do ciclo red/green:

| Decisão | Teste afetado | O que acontece | Tratamento proposto |
|---|---|---|---|
| **D-05** (não restrito é aprovado pelo sistema) | `ReservaServiceIntegracaoTest :: RN_E5_C01_criarReservaGeraAuditoria` | O teste cria reserva de uma **sala não restrita** e espera **1** registro de auditoria com `SOLICITADA`. Com D-05 passam a ser 2 registros (criação + aprovação automática) | **Mudança de requisito, não de teste para passar:** o caso RN-E5-C01 passa a usar um recurso **restrito**, mantendo a asserção, e B1-09 cobre a aprovação automática. Exige aprovação do grupo |
| **D-12** (nada começa no passado) | Todos os testes de integração e de API que criam reservas em 2026-11-23 | Hoje passam porque o relógio real (`Clock.systemUTC`) está antes dessa data. **Depois de 23/11/2026, quebrariam todos** | A `IntegracaoBase` passa a fornecer um `Clock` fixo (ex.: 2026-11-20 08:00 UTC). É infraestrutura de teste, sem mexer em asserção. Tira a dependência do relógio real, que hoje é uma bomba-relógio |

**Aprovado pelo grupo (2026-11-19):** os dois tratamentos propostos. Na Parte 2 eles entram **antes** do primeiro `test(red)`, cada um no próprio commit justificado:
1. `test: ajusta RN-E5-C01 à decisão D-05`: o recurso do caso passa a ser restrito; a asserção não muda.
2. `test: Clock fixo na IntegracaoBase (D-12)`: só infraestrutura de teste.
