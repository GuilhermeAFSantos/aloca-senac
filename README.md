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

## Documentação
- [PRD](docs/prd.md)
- [Arquitetura](docs/arquitetura.md)
- [Plano de Teste](docs/plano-de-teste.md)
- [Requisitos](docs/requisitos.md)
- [Gestão de Defeitos](docs/defeitos.md) — relate falhas pelo template de issue "Relato de defeito"
