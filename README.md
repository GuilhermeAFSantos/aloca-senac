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

## Como rodar os testes
```bash
./mvnw test
```

## Documentação
- [PRD](docs/prd.md)
- [Arquitetura](docs/arquitetura.md)
- [Plano de Teste](docs/plano-de-teste.md)
