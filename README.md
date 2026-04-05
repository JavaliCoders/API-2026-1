<div align="center">
  <img src="src/docs/img/Logo.jpeg" alt="Logo JavaliCoders" width="500px">
  <h2>JavaliCoders</h2>

  | [Desafio](#desafio) | [Solução](#solucao) | [Backlog do Produto](#backlog) | [DoR e DoD](#dor-dod) | [Evolução do Projeto](#evolucao) | [Cronograma de Sprints](#sprint) | [Tecnologias](#tecnologias) | [Estrutura do Projeto](#estrutura) | [Como Executar](#execucao) | [Documentação](#documentacao) | [Equipe](#equipe) |
</div>

Status do Projeto: 🚧 Em andamento

---

## 🏅 Desafio <a name="desafio"></a>

O desafio consiste em desenvolver uma plataforma integrada de Gestão de Compras para uma organização. O objetivo é criar uma ferramenta digital que otimize o processo de compras, desde a solicitação até o recebimento, onde os usuários possam criar pedidos, solicitar cotações comparativas, registrar recebimentos com conferência de produtos e analisar dados de compras. A plataforma visa aprimorar a eficiência operacional, reduzir custos através de análise comparativa de fornecedores e garantir rastreabilidade completa de todas as operações de compra.

---

## 🏅 Solução <a name="solucao"></a>

A plataforma desenvolvida pela equipe JavaliCoders centraliza todo o ciclo de compras em um único sistema web. Funcionários podem abrir pedidos de compra e acompanhar cada etapa do processo em tempo real. O setor financeiro gerencia cotações com múltiplos fornecedores, podendo anexar documentos e notas fiscais diretamente no sistema. Cada ação — aprovação, rejeição, alteração de status, registro de recebimento — é registrada no histórico do pedido, garantindo rastreabilidade completa do processo. Gestores e diretores têm acesso a um dashboard com indicadores de desempenho, permitindo análise comparativa de fornecedores, acompanhamento de prazos e visibilidade sobre o estoque, facilitando a tomada de decisão com base em dados.

---

## 📊 Backlog do Produto <a name="backlog"></a>

### SPRINT 1 - Fundação Operacional

| Rank | Prioridade | User Story | Story Points | Sprint | Status |
| --- | --- | --- | --- | --- | --- |
| 1 | Alta | Como diretor, quero cadastrar novos usuários no sistema, para permitir que eles acessem a plataforma conforme seu perfil de acesso. | 3 | 1 | ⏳ |
| 2 | Alta | Como funcionário, quero fazer login no sistema, para acessar as funcionalidades conforme meu perfil. | 5 | 1 | ⏳ |
| 3 | Alta | Como financeiro, quero cadastrar produtos, para utilizá-los em pedidos e cotações. | 3 | 1 | ⏳ |
| 4 | Alta | Como usuário, quero visualizar o estoque, para acompanhar a disponibilidade de produtos. | 3 | 1 | ⏳ |
| 11 | Alta | Como financeiro, quero cadastrar fornecedores, para realizar cotações e analisar opções mais viáveis. | 3 | 1 | ⏳ |
| 12 | Alta | Como financeiro, quero vincular produtos aos fornecedores, para registrar quais fornecedores vendem cada produto. | 3 | 1 | ⏳ |
| 5 | Alta | Como funcionário, quero criar um pedido de compra, para solicitar materiais ou produtos necessários para o setor. | 5 | 1 | ⏳ |
| 6 | Alta | Como funcionário, quero visualizar pedidos feitos, para consultar informações, histórico e alterações de status. | 1 | 1 | ⏳ |

---

### SPRINT 2 - Ciclo Completo de Logística

| Rank | Prioridade | User Story | Story Points | Sprint | Status |
| --- | --- | --- | --- | --- | --- |
| 7 | Alta | Como diretor/administrador, quero aprovar pedidos de compra, para autorizar cotação. | 3 | 2 | ⏳ |
| 8 | Alta | Como diretor/administrador, quero negar pedidos, para bloquear compras desnecessárias. | 1 | 2 | ⏳ |
| 9 | Média | Como solicitante, quero editar pedido antes da aprovação, para corrigir informações. | 1 | 2 | ⏳ |
| 10 | Média | Como solicitante, quero excluir um pedido feito por mim, para cancelar solicitações. | 1 | 2 | ⏳ |
| 14 | Média | Como financeiro, quero atualizar o status dos pedidos aprovados, para registrar quando os pedidos estão em processo de cotação com os fornecedores. | 1 | 2 | ⏳ |
| 13 | Alta | Como financeiro, quero visualizar os fornecedores de um produto, para analisar opções de compra. | 2 | 2 | ⏳ |
| 15 | Alta | Como financeiro, quero registrar uma cotação realizada para um pedido de compra, para solicitar aprovação da diretoria antes de realizar a compra. | 3 | 2 | ⏳ |
| 16 | Alta | Como diretor, quero aprovar ou rejeitar uma cotação, para autorizar ou negar a compra. | 2 | 2 | ⏳ |
| 17 | Média | Como financeiro, quero registrar que a compra de um pedido foi realizada, para atualizar o status do pedido e iniciar o acompanhamento da entrega. | 1 | 2 | ⏳ |
| 18 | Alta | Como funcionário, quero filtrar os pedidos, para encontrar mais facilmente o pedido desejado. | 2 | 2 | ⏳ |
| 19 | Alta | Como operacional, quero registrar a nota fiscal, para iniciar o processo de recebimento. | 1 | 2 | ⏳ |
| 20 | Média | Como operador de conferência, quero conferir os produtos recebidos, para validar a entrega. | 3 | 2 | ⏳ |
| 21 | Alta | Como operador de conferência, quero registrar divergências na entrega, para informar problemas no recebimento. | 2 | 2 | ⏳ |
| 22 | Alta | Como operacional, quero registrar a solução de uma divergência, para dar continuidade ao pedido. | 2 | 2 | ⏳ |
| 23 | Alta | Como operador de conferência, quero confirmar o recebimento correto, para liberar os produtos no estoque. | 1 | 2 | ⏳ |
| 24 | Alta | Como operador de estoque, quero registrar a retirada do material, para entregar ao solicitante. | 3 | 2 | ⏳ |
| 25 | Alta | Como operacional, quero finalizar o pedido, para indicar que o processo foi concluído. | 1 | 2 | ⏳ |

---

### SPRINT 3 - Inteligência e Valor Agregado

| Rank | Prioridade | User Story | Story Points | Sprint | Status |
| --- | --- | --- | --- | --- | --- |
| 26 | Média | Como funcionário, quero filtrar os produtos do estoque de acordo com a quantidade, para facilitar a visualização de produtos que precisam ser comprados. | 5 | 3 | ⏳ |
| 27 | Média | Como financeiro, quero filtrar fornecedores, para facilitar a análise. | 5 | 3 | ⏳ |
| 28 | Média | Como operador de estoque, quero registrar manualmente a entrada de material no estoque, para corrigir ou ajustar o saldo quando houver reposição ou erro de contagem. | 2 | 3 | ⏳ |
| 29 | Média | Como operador de estoque, quero registrar manualmente a saída de um produto do estoque, para manter o controle correto do saldo de estoque. | 2 | 3 | ⏳ |
| 30 | Alta | Como funcionário, quero visualizar o histórico de movimentações do estoque, para acompanhar entradas, saídas e ajustes realizados. | 5 | 3 | ⏳ |
| 31 | Alta | Como diretor/administrador, quero visualizar um dashboard visual com indicadores, para acompanhar o desempenho das compras e do funcionamento dos processos. | 8 | 3 | ⏳ |
| 32 | Média | Como diretor, quero alterar os dados de um usuário, para manter as informações e permissões atualizadas. | 2 | 3 | ⏳ |

---

## 📝 DoR e DoD <a name="dor-dod"></a>

### DoR - Definition of Ready

- User Stories escritas no formato "Como [persona], quero [ação] para que [objetivo]"
- As US contêm critérios de aceitação definidos
- Subtarefas divididas a partir das US
- Priorização atribuída (Alta, Média, Baixa)
- Story Points estimados

### DoD - Definition of Done

- Funcionalidade implementada e testada
- Código revisado via Pull Request
- Documentação atualizada
- Vídeo demonstrativo do incremento entregue

> <!-- TODO: Ajustar o DoR e DoD conforme os critérios definidos pela equipe e pelo professor -->

---

## 📈 Evolução do Projeto <a name="evolucao"></a>

#### 🏁 Sprint 1 - Fundação Operacional

- **Status**: ⏳ Em andamento
- **Período**: 16/03 a 05/04
- **Objetivo Principal**: Estruturar autenticação, cadastros base (usuários, produtos, fornecedores) e criação de pedidos
- **Principais Entregas**:
  - ⏳ Cadastro e login de usuários
  - ⏳ Cadastro de produtos e fornecedores
  - ⏳ Visualização de estoque
  - ⏳ Criação e listagem de pedidos de compra
- **Documentação Detalhada**: <!-- TODO: [Sprint 1 Docs](link) -->

---

#### 🎯 Sprint 2 - Ciclo Completo de Logística

- **Status**: ⏳ Não iniciada
- **Período**: 13/04 a 03/05
- **Objetivo Principal**: Implementar fluxo completo de aprovação, cotação, recebimento e entrega
- **Principais Entregas**:
  - ⏳ Aprovação/negação de pedidos
  - ⏳ Registro de cotações com aprovação da diretoria
  - ⏳ Conferência de recebimento e registro de divergências
  - ⏳ Finalização do pedido
- **Documentação Detalhada**: <!-- TODO: [Sprint 2 Docs](link) -->

---

#### 🎯 Sprint 3 - Inteligência e Valor Agregado

- **Status**: ⏳ Não iniciada
- **Período**: 11/05 a 31/05
- **Objetivo Principal**: Adicionar filtros avançados, movimentações manuais de estoque e dashboard gerencial
- **Principais Entregas**:
  - ⏳ Filtros de estoque e fornecedores
  - ⏳ Movimentações manuais (entrada/saída)
  - ⏳ Histórico de movimentações
  - ⏳ Dashboard com indicadores
  - ⏳ Gerenciamento de usuários pelo diretor
- **Documentação Detalhada**: <!-- TODO: [Sprint 3 Docs](link) -->

---

## 📅 Cronograma de Sprints <a name="sprint"></a>

| Sprint | Período | Documentação | Vídeo do Incremento |
| --- | --- | --- | --- |
| 🔖 **SPRINT 1** | 16/03 a 05/04 | <!-- TODO: [Sprint 1 Docs](link) --> | <!-- TODO: [Incremento 1](link YouTube) --> |
| 🔖 **SPRINT 2** | 13/04 a 03/05 | <!-- TODO: [Sprint 2 Docs](link) --> | <!-- TODO: [Incremento 2](link YouTube) --> |
| 🔖 **SPRINT 3** | 11/05 a 31/05 | <!-- TODO: [Sprint 3 Docs](link) --> | <!-- TODO: [Incremento 3](link YouTube) --> |

---

## 💻 Tecnologias <a name="tecnologias"></a>

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)
![Bootstrap](https://img.shields.io/badge/Bootstrap-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white)
<!-- TODO: adicionar banco de dados utilizado: MySQL, PostgreSQL, H2, etc. -->
<!-- TODO: adicionar outras ferramentas: Maven/Gradle, Thymeleaf, etc. -->

---

## 📁 Estrutura do Projeto <a name="estrutura"></a>

<!-- TODO: Atualizar o diagrama abaixo com a estrutura real do projeto Java/Spring Boot após o desenvolvimento -->

```
├── 📁 src
│   └── 📁 main
│       ├── 📁 java
│       │   └── 📁 com/javalicoders
│       │       └── ... (controllers, services, models, repositories)
│       └── 📁 resources
│           ├── 📁 static
│           ├── 📁 templates
│           └── ⚙️ application.properties
├── 📁 documents
│   ├── 📁 cliente
│   └── 📁 processo
│       ├── 📁 sprints
│       ├── 📝 dor_e_dod.md
│       ├── 📝 estrategia-de-branches.md
│       └── 📝 padrao-de-commit.md
├── ⚙️ .gitignore
├── 📄 pom.xml
└── 📝 README.md
```

---

## ⚡ Como Executar <a name="execucao"></a>

### Pré-requisitos

<!-- TODO: Preencher com as versões e ferramentas reais do projeto -->
- [Java JDK](https://www.oracle.com/java/technologies/downloads/) (versão <!-- TODO: ex: 17 ou 21 -->)
- [Maven](https://maven.apache.org/download.cgi) ou [Gradle](https://gradle.org/install/) <!-- TODO: indicar qual o projeto usa -->
- [Git](https://git-scm.com/downloads)
- [IntelliJ IDEA](https://www.jetbrains.com/idea/download/) ou [VS Code](https://code.visualstudio.com/download) (ou editor de sua preferência)
- <!-- TODO: banco de dados necessário, ex: MySQL 8+ -->

### Passo a passo

<!-- TODO: Preencher com as instruções reais de execução do projeto -->

**1.** Clone o repositório:

```bash
git clone https://github.com/JavaliCoders/API-2026-1.git
cd API-2026-1
```

**2.** Configure o banco de dados:

```
# TODO: instruções de configuração do banco (ex: criar schema, ajustar application.properties)
```

**3.** Execute o projeto:

```bash
# Com Maven
./mvnw spring-boot:run

# Com Maven no Windows
mvnw.cmd spring-boot:run
```

**4.** Acesse no navegador: `http://localhost:8080`

---

## 📄 Documentação <a name="documentacao"></a>

<!-- TODO: Atualizar os links conforme os arquivos forem criados -->

> Pasta de documentação: [documents/](documents/)
>
> Checklist de DoR e DoD: <!-- TODO: [Checklist](documents/processo/dor_e_dod.md) -->
>
> DoR e DoD por Sprint: <!-- TODO: [DoR e DoD](documents/processo/sprints) -->
>
> Estratégia de Branch: <!-- TODO: [Branch](documents/processo/estrategia-de-branches.md) -->
>
> Padrão de Commits: <!-- TODO: [Commits](documents/processo/padrao-de-commit.md) -->
>
> Manual de Usuário: <!-- TODO: [Manual](documents/cliente/manual_de_usuario.pdf) -->
>
> Manual de Instalação: <!-- TODO: [Manual](documents/cliente/manual_de_instalacao.pdf) -->

---

## 🎓 Equipe <a name="equipe"></a>

<div align="center">
  <table>
    <tr>
      <th>Membro</th>
      <th>Função</th>
      <th>GitHub</th>
      <th>LinkedIn</th>
    </tr>
    <tr>
      <td>Kamille Fernandes</td>
      <td>Product Owner</td>
      <td><a href="https://github.com/KamilleFernandes"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white"></a></td>
      <td><a href="https://www.linkedin.com/in/kamille-f-da-silva-122a10284/"><img src="https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white"></a></td>
    </tr>
    <tr>
      <td>Nicolas Pacheco</td>
      <td>Scrum Master</td>
      <td><a href="https://github.com/Nocholas0"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white"></a></td>
      <td><a href="https://www.linkedin.com/in/nicolas-pacheco-591216287/"><img src="https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white"></a></td>
    </tr>
    <tr>
      <td>Daniel Nathan</td>
      <td>Developer</td>
      <td><a href="https://github.com/Danithan"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white"></a></td>
      <td><a href="https://www.linkedin.com/in/daniel-nathan-621b623aa/"><img src="https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white"></a></td>
    </tr>
    <tr>
      <td>Alex Gabriel</td>
      <td>Developer</td>
      <td><a href="https://github.com/AlexGabrielll"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white"></a></td>
      <td><a href="https://www.linkedin.com/in/alex-gabriel-leonel-0b3339302/"><img src="https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white"></a></td>
    </tr>
    <tr>
      <td>Vinícius Henrique</td>
      <td>Developer</td>
      <td><a href="https://github.com/ViniciusAmante"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white"></a></td>
      <td><a href="https://www.linkedin.com/in/vinicius-oliveira-amante/"><img src="https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white"></a></td>
    </tr>
    <tr>
      <td>Caio Moreira</td>
      <td>Developer</td>
      <td><a href="https://github.com/CaioMoreiraujo"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white"></a></td>
      <td><a href="https://www.linkedin.com/in/caiomoreiradearaujo/"><img src="https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white"></a></td>
    </tr>
    <tr>
      <td>Mateus Borges</td>
      <td>Developer</td>
      <td><a href="https://github.com/MGBorgess"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white"></a></td>
      <td><a href="https://www.linkedin.com/in/matheus-de-oliveira-b68bbb383/"><img src="https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white"></a></td>
    </tr>
  </table>
</div>
