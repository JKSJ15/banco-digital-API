# 🏦 Banco Digital

API REST de sistema bancário desenvolvida em Java com Spring Boot, simulando operações bancárias reais como cadastro de usuários, autenticação JWT, gerenciamento de contas, Pix, transferências, depósitos, saques, extratos e relatórios financeiros.

Projeto criado com foco em estudo de desenvolvimento Back-End, arquitetura em camadas, segurança com Spring Security e boas práticas de desenvolvimento.

---

# 🚀 Tecnologias Utilizadas

* Java 21
* Spring Boot 3
* Spring Security
* JWT (JSON Web Token)
* Refresh Token
* Spring Data JPA
* Hibernate
* MySQL
* Docker
* Docker Compose
* Bean Validation
* Swagger / OpenAPI
* JUnit 5
* MockMvc
* ViaCEP API

---

# 📚 Funcionalidades

## 👤 Usuários

* Cadastro de usuário
* Validação de CPF
* Validação de telefone
* Validação de idade mínima
* Criptografia de senha com BCrypt
* Login com JWT
* Refresh Token
* Logout invalidando Refresh Token

---

## 🏦 Conta Bancária

Cada usuário possui uma conta bancária criada automaticamente.

### Dados da conta

* ID
* Agência
* Número da conta
* Chave Pix automática
* Saldo
* Status
* Data de criação
* CEP cadastrado
* Endereço obtido via ViaCEP

### Operações

* Consultar conta
* Bloquear conta
* Desbloquear conta
* Encerrar conta

### Regras

Conta bloqueada:

* Não pode realizar:

  * Pix
  * Transferências
  * Saques

Conta encerrada:

* Não pode realizar movimentações
* Encerramento somente com saldo igual a R$ 0,00

---

## 💸 Pix

### Funcionalidades

* Pix por chave Pix
* Validação de senha
* Limite por transação
* Limite diário

### Regras

* Não permite Pix para a própria conta
* Conta precisa estar ativa
* Saldo suficiente

---

## 🔁 Transferências

### Funcionalidades

* Transferência entre contas

### Regras

* Validação de senha
* Limite por transação
* Não permite transferência para a própria conta
* Saldo suficiente
* Conta ativa

---

## 💰 Depósitos

### Funcionalidades

* Depósito em conta própria

### Regras

* Limite máximo por operação
* Validação de senha

---

## 💵 Saques

### Funcionalidades

* Saque em conta própria

### Regras

* Validação de senha
* Saldo suficiente
* Limite máximo por operação
* Conta ativa

---

## 📄 Extrato

Consulta de movimentações financeiras.

### Informações retornadas

* Tipo da transação
* Valor
* Descrição
* Data
* Conta origem
* Conta destino
* Saldo anterior
* Saldo posterior

### Recursos

* Paginação
* Ordenação
* Filtro por período

Exemplo:

```http
GET /transacoes/extrato?inicio=2026-06-01&fim=2026-06-30
```

---

## 📊 Relatório Mensal

Geração de indicadores financeiros da conta.

Retorna:

* Saldo atual
* Total recebido no mês
* Total enviado no mês
* Saldo movimentado no mês
* Quantidade de Pix realizados

---

# 🌎 Integração com ViaCEP

A API consulta automaticamente o endereço do usuário a partir do CEP cadastrado.

Exemplo de retorno:

```json
{
  "cep": "55730000",
  "bairro": "",
  "localidade": "Bom Jardim",
  "uf": "PE",
  "estado": "Pernambuco",
  "regiao": "Nordeste"
}
```

Caso o ViaCEP esteja indisponível:

* A consulta da conta continua funcionando
* Apenas os campos de endereço retornam nulos

---

# 🔒 Segurança

A aplicação utiliza:

* Spring Security
* JWT Authentication
* Refresh Token
* BCrypt Password Encoder

Fluxo:

```text
Login
 ↓
Access Token
 ↓
Acesso aos endpoints protegidos
 ↓
Refresh Token
 ↓
Novo Access Token
```

---

# 📖 Documentação Swagger

Após iniciar a aplicação:

```text
http://localhost:8080/swagger-ui.html
```

A documentação inclui:

* Exemplos de requisição
* Exemplos de resposta
* Exemplos de erros
* Regras de negócio

---

# 🐳 Executando com Docker

## Subir containers

```bash
docker compose up -d
```

## Derrubar containers

```bash
docker compose down
```

---

# 🧪 Testes

O projeto possui:


## Testes de Integração

### Autenticação

* Registro
* Login
* Refresh Token
* Logout

### Conta

* Consulta
* Bloqueio
* Desbloqueio
* Encerramento

### Transações

* Pix
* Transferência
* Saque
* Depósito
* Extrato
* Relatório mensal

## Testes Unitários

### Conta

* Consulta
* Bloqueio
* Desbloqueio
* Encerramento

### Transações

* Pix
* Transferência
* Saque
* Depósito
---

# 📂 Arquitetura

```text
src/main/java/com/jks/bank
├── cliente
├── configuracoes
├── controles
├── dto
├── entidades
├── exceptions
├── mapeamento
├── repositorios
└── servicos

```

---

# 🎯 Objetivos do Projeto

Este projeto foi desenvolvido para praticar:

* Programação Orientada a Objetos
* Spring Boot
* Spring Security
* JWT
* Persistência com JPA/Hibernate
* Testes automatizados
* Integrações externas
* Docker
* Documentação de APIs
* Arquitetura em camadas
* Lógica de negócio bancária

---

# 👨‍💻 Autor

Jakson José

Projeto desenvolvido para estudos, evolução profissional e composição de portfólio.
