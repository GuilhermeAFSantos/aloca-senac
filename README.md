# AlocaSenac

Sistema de organização de recursos: alocação de salas, professores e materiais sem conflito de horário.
Projeto da disciplina Qualidade 2026.2 (Senac).

## Pré-requisitos
- Java 21
- Docker Desktop em execução (o PostgreSQL sobe em container via Testcontainers)

Não é preciso instalar Maven: o projeto usa o wrapper (`mvnw`).

## Como subir o projeto
```bash
./mvnw spring-boot:test-run
```
Esse comando sobe a aplicação junto com um PostgreSQL em container. A aplicação fica em http://localhost:8080.

### API e autenticação
A API REST fica em `/api/**` e exige um token **JWT HS256** no cabeçalho `Authorization: Bearer <token>`:
- claim `sub`: identificador do usuário (use contas sintéticas, ex.: `mariana.teste@example.invalid`);
- claim `perfis`: lista com `SOLICITANTE`, `RESPONSAVEL` ou `ADMINISTRADOR`.

O segredo de assinatura vem da variável de ambiente `ALOCASENAC_JWT_SEGREDO`, com no mínimo 32 caracteres e **nunca versionado** (veja `.env.example`). Sem ela, a aplicação sobe com uma chave aleatória e não aceita token externo.

## Como rodar os testes
```bash
./mvnw test
```

## Integração contínua
O workflow [`.github/workflows/ci.yml`](.github/workflows/ci.yml) roda `./mvnw -B verify` em todo pull request e em todo push na `main`. Os relatórios do Surefire e do Failsafe ficam como artifact por 7 dias. A `main` é protegida: o merge exige o check **`verify`** verde e atualizado com a base, e force-push e exclusão da branch estão bloqueados.

### Evidência da prática (Aula 06, PR #1)
| Run | Commit | Resultado | O que mostra |
|---|---|---|---|
| [44614700125](https://github.com/GuilhermeAFSantos/aloca-senac/actions/runs/44614700125) | `68015df` | ✅ verde | Primeiro run do workflow no PR |
| [44614975542](https://github.com/GuilhermeAFSantos/aloca-senac/actions/runs/44614975542) | — | ✅ verde | Actions atualizadas para v5 |
| [44615229324](https://github.com/GuilhermeAFSantos/aloca-senac/actions/runs/44615229324) | — | ✅ verde | `upload-artifact@v6`, sem avisos de Node 20 |
| [44615426256](https://github.com/GuilhermeAFSantos/aloca-senac/actions/runs/44615426256) | `1e81ffc` | ❌ **vermelho** | Quebra proposital da asserção de `RN_T1_C01`. O CI falha em `IntervaloTest.java:29` |
| [44615652155](https://github.com/GuilhermeAFSantos/aloca-senac/actions/runs/44615652155) | `ca89893` | ✅ verde | Revert da quebra; o teste ficou idêntico ao original |

## Documentação
- [PRD](docs/prd.md)
- [Arquitetura](docs/arquitetura.md)
- [Plano de Teste](docs/plano-de-teste.md)
- [Requisitos](docs/requisitos.md)
- [Gestão de Defeitos](docs/defeitos.md) — relate falhas pelo template de issue "Relato de defeito"
