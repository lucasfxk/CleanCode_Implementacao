# Sistema de Delivery — Clean Architecture

## Projeto de Sistemas de Software — UFES 2026/1
Integrantes do grupo: Suelen Salarolli Bisi, Lucas Borges do Carmo,Fabrício Lopes Lima do Amaral,Raphael Figueiredo Secchin,Marcelo Vieira Gomes,Maria Clara Gueler Feitani e  Henrique Queiroz Teixeira.

### Descrição

Implementação de um **Sistema de Delivery** seguindo os princípios da **Clean Architecture** (Robert C. Martin). O sistema gerencia o ciclo de vida completo de pedidos e agora conta com um **Catálogo de Itens** persistido em banco de dados SQLite. Entre as funcionalidades estão: criação de pedidos, cálculo de descontos, aplicação de cupons, acompanhamento de status com notificações em tempo real e gerenciamento de um catálogo base de produtos.

### Arquitetura

O projeto segue a **Dependency Rule**: dependências sempre apontam para dentro (em direção ao domínio).

```text
Infrastructure → Adapter → Application (Use Cases / Ports) → Domain (Entities)
```

#### Camadas

| Camada | Pacote | Responsabilidade |
|--------|--------|-----------------|
| **Domain** | `domain.entity` | Entidades puras de negócio: `Pedido`, `Cliente`, `Item` (agora com UUID), `Cupom*`, `StatusPedido` |
| **Application** | `application.usecase`, `application.port`, `application.dto`, `application.strategy` | Use Cases (incluindo `CadastrarItemUseCase` e `ListarItensUseCase`), Input/Output Ports, DTOs, estratégias de desconto |
| **Adapter** | `adapter.controller`, `adapter.presenter`, `adapter.ui` | Controladores (`PedidoController` e `ItemController`), `PedidoPresenter`, Interface Gráfica isolada do Domain |
| **Infrastructure** | `infrastructure.repository`, `infrastructure.notification`, `infrastructure.config` | Banco de Dados em **SQLite** (`PedidoRepositoryEmSQLite`, `ItemRepositoryEmSQLite`), Repositórios em Memória (para flexibilidade e testes), notificações e configuração |

### Princípios SOLID Aplicados

| Princípio | Aplicação |
|-----------|-----------|
| **S** — Single Responsibility | Cada classe tem uma única responsabilidade (ex: `PedidoPresenter` só formata, UI só exibe DTOs). |
| **O** — Open/Closed | Novas estratégias de desconto (`IFormaDescontoTaxaEntrega`) adicionadas sem alterar código existente. |
| **L** — Liskov Substitution | Implementações de repositórios (SQLite vs Memória) e notificações são totalmente intercambiáveis. |
| **I** — Interface Segregation | Input Ports e Output Ports segregados para Casos de Uso específicos e Repositórios distintos. |
| **D** — Dependency Inversion | Controllers e Use Cases dependem de abstrações (ports). A Injeção é feita no Composition Root (`MainSwing`). |

### Padrões de Projeto

| Padrão | Onde | Por quê |
|--------|------|---------|
| **State (GoF)** | `EstadoPedido` (e suas filhas `EstadoCriado`, etc.) | Centraliza a lógica de transição de status usando polimorfismo, evitando complexidade ciclomática na Entidade raiz. |
| **Strategy** | `IFormaDescontoTaxaEntrega` | Permite variar algoritmos de desconto sem alterar o Use Case. |
| **Observer** | `NotificacaoOutputPort` | Desacopla as atualizações (UI/Console) da regra de negócio (Use Case). |
| **Repository** | Output Ports de Repositório | Abstrai a persistência, permitindo trocar Memória por Banco de Dados SQLite de forma invisível para o domínio. |
| **DTO** | `CriarPedidoDTO`, `ItemDTO`, `PedidoResumoDTO` | Transferência de dados segura, blindando a UI para que ela não acesse Entidades de Domínio. |
| **Dependency Injection** | `MainSwing.java` e `Main.java` | Monta toda a árvore de dependências no ponto mais externo (Composition Root). |

### Destaques da Implementação

- **Multi-Interface Simultânea (Swing + Web API):** O projeto sobe automaticamente uma janela Desktop (Swing) E um Servidor Web (na porta 8080) simultaneamente. Ambos consomem 100% dos mesmos Use Cases, provando que o Core da aplicação é agnóstico ao canal de entrada/saída. Basta acessar `http://localhost:8080/pedidos` com o sistema rodando.
- **Padrão State Puro:** O ciclo de vida do pedido foi modelado utilizando o Padrão State Clássico do GoF, garantindo que o Domínio permaneça rico e isento de estruturas como `switch/case` sem comprometer os bancos de dados que armazenam os status em formato de texto.

- **Catálogo de Itens (SQLite):** Ao iniciar o sistema, um catálogo de produtos pré-cadastrados é carregado do SQLite e disponibilizado na UI. O usuário pode adicionar os itens do catálogo direto para o carrinho ou cadastrar novos itens permanentemente.
- **Auto-Load de Pedidos:** A interface consulta automaticamente o repositório ao abrir e exibe os pedidos em uma `JTable`.
- **Transparência de Banco de Dados:** É possível trocar facilmente o SQLite (`ItemRepositoryEmSQLite`) por memória RAM (`ItemRepositoryEmMemoria`) alterando uma única linha no `MainSwing.java`, comprovando a eficácia da Clean Architecture.

### Fluxo da Aplicação

```text
1. Criação do Pedido     → UI envia DTO → CriarPedidoUseCase → UI recebe DTO
2. Cálculo de Descontos  → UI envia UUID → BuscarPedidoUseCase → CalcularDescontoEntregaUseCase
3. Aplicação de Cupom    → UI envia UUID → BuscarPedidoUseCase → AplicarCupomUseCase
4. Ciclo de Status       → UI envia UUID → BuscarPedidoUseCase → AtualizarStatusPedidoUseCase
5. Catálogo de Itens     → UI ↔ ItemController ↔ CadastrarItemUseCase / ListarItensUseCase ↔ Repositório
```

### Interface Gráfica (MVP Swing)

O projeto inclui uma interface gráfica Swing completa. O código da UI fica **exclusivamente** na camada Adapter e **não importa nenhuma entidade do Domain**, validando arquiteturalmente o projeto.

**Como executar a interface gráfica:**
```bash
mvn compile exec:java@swing
```

**Para executar o modo console (Main.java original):**
```bash
mvn compile exec:java
```

### Pré-requisitos

- **Java 21** (JDK 21)
- **Apache Maven 3.8+**
- Drivers JDBC do SQLite (já configurado no `pom.xml`).

### Como Executar os Testes

O projeto contém testes automatizados (JUnit 5) validando regras de negócios vitais (Agregado raiz de Pedido, Use Cases e estratégias de desconto).
```bash
mvn clean test
```
