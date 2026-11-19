# AlocaSenac — Requisitos (fonte de verdade)

> **Origem:** especificação do projeto do professor — <https://afonsolelis.github.io/aulas_senac/pages/qualidade2/especificacao-projeto.html> (lida em 2026-11-19).
> **Regra de uso:** este arquivo reproduz a especificação. Não alterar o conteúdo dos itens; mudanças de requisito exigem nova versão da especificação.
> **Sobre os identificadores:** a especificação numera os requisitos funcionais de 1 a 14 e não dá código às regras. Os IDs abaixo (`RF1`…`RF14`, `RN-T*`, `RN-E*`, `RQ-*`, `MT-*`, `LIM-*`) foram **atribuídos por nós, a partir da numeração e da ordem da página**, apenas para rastreabilidade.

## 1. Objetivo
Desenvolver uma aplicação que organize a alocação de salas, professores e materiais sem conflitos de horário. A avaliação foca em evidências objetivas de qualidade, segurança, rastreabilidade e desempenho, não apenas em o sistema funcionar.

- **Tema:** Organização de Recursos — "Um sistema confiável para alocar salas, professores e materiais sem conflitos."
- **Stack exigida:** Java 21, Spring Boot 3.x.
- **Prazo:** entrega na semana 46.

## 2. Perfis
| ID | Perfil | Descrição |
|---|---|---|
| PF-SOL | Solicitante | Professor ou coordenador que consulta disponibilidade e cria, altera ou cancela reservas. |
| PF-RES | Responsável | Aprova solicitações especiais, valida a alocação de docentes e acompanha retirada e devolução de material. |
| PF-ADM | Administrador | Gerencia salas, professores, materiais, usuários, bloqueios e períodos de manutenção. |

## 3. Requisitos funcionais obrigatórios
| ID | Requisito |
|---|---|
| RF1 | Autenticação e autorização por perfil. |
| RF2 | Cadastro e consulta de salas, professores e materiais. |
| RF3 | Pesquisa por tipo, capacidade, localização, competência e disponibilidade. |
| RF4 | Criação, alteração e cancelamento de reservas. |
| RF5 | Detecção de sobreposição de horários, inclusive na agenda do professor. |
| RF6 | Proteção contra dupla reserva em solicitações simultâneas. |
| RF7 | Aprovação obrigatória para recursos restritos. |
| RF8 | Bloqueio de recursos em manutenção. |
| RF9 | Registro de retirada e devolução de materiais e equipamentos. |
| RF10 | Histórico auditável de mudanças. |
| RF11 | Notificação simulada ou integração com API externa. |
| RF12 | Relatório de utilização por recurso, carga horária alocada e conflitos evitados. |
| RF13 | Interface responsiva e mensagens de erro compreensíveis. |
| RF14 | Documentação de API ou dos fluxos públicos. |

## 4. Regras críticas
### 4.1 Tempo e concorrência
| ID | Regra |
|---|---|
| RN-T1 | O término deve ser posterior ao início. |
| RN-T2 | Reservas do mesmo recurso (sala, material ou professor) não podem se sobrepor. |
| RN-T3 | Duas solicitações simultâneas devem produzir uma única reserva aceita. |
| RN-T4 | Recursos em manutenção não podem ser reservados. |

### 4.2 Estados e auditoria
| ID | Regra |
|---|---|
| RN-E1 | Fluxo principal: SOLICITADA → APROVADA → EM_USO → CONCLUÍDA. |
| RN-E2 | Estados alternativos: rejeitada, cancelada e não compareceu. |
| RN-E3 | Somente responsáveis aprovam recursos restritos. |
| RN-E4 | Reservas iniciadas não podem ser apagadas. |
| RN-E5 | Toda mudança de estado deve gerar auditoria. |

## 5. Estratégia obrigatória de qualidade (requisitos técnicos)
| ID | Requisito |
|---|---|
| RQ-1 | Testes automatizados: unitários com JUnit 5 (parametrizados), integração, API em caixa-preta e end-to-end. |
| RQ-2 | Ambiente realista: banco containerizado com Testcontainers e WireMock para integrações externas. |
| RQ-3 | Concorrência: teste automatizado que comprove a proteção contra dupla reserva. |
| RQ-4 | TDD ou BDD: ao menos uma funcionalidade nova desenvolvida com evidências da técnica. |
| RQ-5 | CI e análise estática: GitHub Actions em pull requests, cobertura com JaCoCo e projeto integrado ao SonarCloud. |
| RQ-6 | Não funcionais: JMeter, autorização, validação de entrada e tratamento seguro de erros. |
| RQ-7 | Uso de mocks: permitido apenas em testes unitários isolados. Persistência e integrações críticas devem ser verificadas com ambiente real, Testcontainers ou WireMock. |

## 6. Metas mensuráveis
| ID | Meta | Valor |
|---|---|---|
| MT-1 | Cobertura de linhas | 80% |
| MT-2 | Cobertura de branches | 70% |
| MT-3 | Requisitos críticos rastreados na RTM | 100% |
| MT-4 | Bugs ou vulnerabilidades críticas | 0 |

> Observação da especificação: cobertura alta, sozinha, não garante qualidade; testes triviais feitos só para subir o percentual não garantem pontuação máxima.

## 7. Entregáveis
- Repositório com histórico de trabalho e revisão por pares.
- Aplicação executável e instruções de instalação.
- README.md e documentação dos fluxos ou da API.
- RTM.md ligando requisito, risco, teste e evidência.
- Diagramas de contexto, componentes e sequências críticas.
- Plano, suíte automatizada e relatório de testes.
- Pipeline de integração contínua.
- Relatórios do SonarCloud e de cobertura (JaCoCo).
- Plano e resultado do teste de carga.
- Registro de defeitos encontrados, priorizados e corrigidos.
- Relatório final de qualidade.
- Apresentação e demonstração do sistema.

## 8. Avaliação
| Dimensão | Peso |
|---|---|
| Requisitos e regras de negócio | 15% |
| Estratégia e documentação de testes | 15% |
| Qualidade dos testes automatizados | 25% |
| CI, cobertura e análise estática | 15% |
| Performance, segurança e robustez | 10% |
| Rastreabilidade e gestão de defeitos | 10% |
| Organização, apresentação e autoria | 10% |

### Limitadores de nota
| ID | Limitador |
|---|---|
| LIM-1 | Aplicação que não executa. |
| LIM-2 | Pipeline ausente. |
| LIM-3 | Falha de autorização. |
| LIM-4 | Possibilidade de dupla reserva. |

## 9. Cronograma (resumo)
| Semana | Tema | Entrega da semana |
|---|---|---|
| 34 | Caixa branca vs caixa preta | Repositório, ambiente, escopo e perfis definidos |
| 35 | Arquitetura e análise ATAM com IA agêntica | C4, utility tree, riscos, trade-offs e ADRs |
| 36 | Gestão de erros e bugs | Registro e triagem dos primeiros defeitos |
| 37 | Testes automatizados em CI | Casca Maven com teste mínimo e pipeline a cada PR |
| 38 | JUnit 5 + JaCoCo + SonarCloud | Cobertura de ramo nas regras de sobreposição |
| 39 | WireMock/Testcontainers e chamadas de API seguras | Banco em container e integração externa isolada |
| 40 | Prova escrita | — |
| 41 | TDD e BDD | Regras de aprovação e bloqueio guiadas por teste |
| 42 | Testes de carga com JMeter | Reservas simultâneas do mesmo recurso sob carga |
| 43–44 | Desenvolvimento do projeto final | Relatórios, auditoria e fechamento dos requisitos |
| 45 | Preparação para a prova oral | RTM fechada e relatório de qualidade |
| 46 | Finalização e entrega | Release candidate, pacote de evidências e decisão de qualidade |
| 47 | Prova oral | — |
| 48 | Feedback e correções | Causa raiz, teste de regressão e memória do projeto |
| 49 | Revisão integrada | Requisito, risco, técnica, automação, evidência e decisão |
| 50 | Encerramento | Retrospectiva por métricas, dívida e plano de evolução |
