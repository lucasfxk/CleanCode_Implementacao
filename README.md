# Sistema de Delivery — Clean Architecture

## Projeto de Sistemas de Software — UFES 2026/1

### Descrição

Implementação de um **Sistema de Delivery** seguindo os princípios da **Clean Architecture** (Robert C. Martin). O sistema gerencia o ciclo de vida completo de pedidos: criação, cálculo de descontos, aplicação de cupons, acompanhamento de status com notificações em tempo real.

### Arquitetura

O projeto segue a **Dependency Rule**: dependências sempre apontam para dentro (em direção ao domínio).

```
Infrastructure → Adapter → Application (Use Cases / Ports) → Domain (Entities)
```

#### Camadas

| Camada | Pacote | Responsabilidade |
|--------|--------|-----------------|
| **Domain** | `domain.entity` | Entidades puras de negócio: `Pedido`, `Cliente`, `Item`, `Cupom*`, `StatusPedido` |
| **Application** | `application.usecase`, `application.port`, `application.dto`, `application.strategy` | Use Cases, Input/Output Ports, DTOs, estratégias de desconto |
| **Adapter** | `adapter.controller`, `adapter.presenter` | Controladores e formatadores de saída |
| **Infrastructure** | `infrastructure.repository`, `infrastructure.notification`, `infrastructure.config` | Repositórios em memória, notificações e configuração |

### Princípios SOLID Aplicados

| Princípio | Aplicação |
|-----------|-----------|
| **S** — Single Responsibility | Cada classe tem uma única responsabilidade (ex: `PedidoPresenter` só formata, `CriarPedidoUseCase` só cria) |
| **O** — Open/Closed | Novas estratégias de desconto (`IFormaDescontoTaxaEntrega`) adicionadas sem alterar código existente |
| **L** — Liskov Substitution | Implementações de `IFormaDescontoTaxaEntrega` e `NotificacaoOutputPort` são intercambiáveis |
| **I** — Interface Segregation | Input Ports e Output Ports segregados por caso de uso |
| **D** — Dependency Inversion | Use Cases dependem de abstrações (ports), não de implementações concretas |

### Padrões de Projeto

| Padrão | Onde | Por quê |
|--------|------|---------|
| **Strategy** | `IFormaDescontoTaxaEntrega` e suas 4 implementações | Permite variar algoritmos de desconto sem alterar o Use Case |
| **Observer** | `NotificacaoOutputPort` / `NotificacaoConsole` | Desacopla a notificação de mudança de status do Use Case que a dispara |
| **Repository** | `PedidoRepositoryOutputPort`, `CupomRepositoryOutputPort` | Abstrai a persistência; troca de banco de dados não afeta a lógica |
| **DTO** | `CriarPedidoDTO`, `ItemDTO`, `PedidoResumoDTO` | Transferência de dados entre camadas sem expor entidades de domínio |
| **Dependency Injection** | `Main.java` (Composition Root) | Monta toda a árvore de dependências em um único ponto |
| **Facade** | `PedidoController` | Simplifica o acesso aos múltiplos use cases para a camada de apresentação |

### Fluxo da Aplicação

```
1. Criação do Pedido     → CriarPedidoUseCase
2. Cálculo de Descontos  → CalcularDescontoEntregaUseCase (Strategy)
3. Aplicação de Cupom    → AplicarCupomUseCase
4. Ciclo de Status       → AtualizarStatusPedidoUseCase (Observer)
   CRIADO → CONFIRMADO → EM_PREPARO → SAIU_PARA_ENTREGA → ENTREGUE
                       ↘ CANCELADO (a partir de CRIADO ou CONFIRMADO)
5. Listagem              → BuscarPedidoUseCase
```


### Interface Gráfica (MVP Swing)

O projeto inclui uma interface gráfica Swing que demonstra toda a arquitetura visualmente,
mantendo o padrão Clean Architecture — o Swing fica **exclusivamente** na camada Adapter.

**Como executar a interface gráfica:**

```bash
mvn exec:java
# ou diretamente:
mvn clean package
java -jar target/delivery-clean-architecture-1.0-SNAPSHOT.jar
```

**Para executar o modo console (Main.java original):**
```bash
mvn exec:java -Dexec.mainClass=com.ufes.delivery.Main
```

**Abas da interface:**
- **Novo Pedido** — formulário para criar pedidos com itens, tipo de cliente e bairro
- **Gerenciar** — aplicar descontos (Strategy), cupons, avançar status (Observer) e ver resumo

**O que a UI demonstra na prática:**
- Os botões de status são habilitados/desabilitados conforme as transições válidas do `StatusPedido`
- O log verde (Observer) mostra notificações em tempo real via `NotificacaoSwing`, que implementa `NotificacaoOutputPort` — mesma interface usada pelo `NotificacaoConsole`
- Trocar de console para UI não mudou **nenhuma linha** do Domain ou Application

### Pré-requisitos

- **Java 21** (JDK 21)
- **Apache Maven 3.8+**

### Como Executar

```bash
# 1. Clone o repositório
git clone <URL_DO_REPOSITORIO>
cd delivery-clean-architecture

# 2. Compile
mvn clean compile

# 3. Execute
mvn exec:java

# Alternativa (JAR executável)
mvn clean package
java -jar target/delivery-clean-architecture-1.0-SNAPSHOT.jar
```

### Como Executar os Testes

```bash
mvn test
```

### Estrutura do Projeto

```
src/
├── main/java/com/ufes/delivery/
│   ├── Main.java                              (Composition Root)
│   ├── domain/entity/
│   │   ├── Cliente.java
│   │   ├── Item.java
│   │   ├── Pedido.java
│   │   ├── StatusPedido.java                  (enum — ciclo de vida)
│   │   ├── CupomDescontoPedido.java
│   │   └── CupomDescontoEntrega.java
│   ├── application/
│   │   ├── dto/
│   │   │   ├── CriarPedidoDTO.java
│   │   │   ├── ItemDTO.java
│   │   │   └── PedidoResumoDTO.java
│   │   ├── port/
│   │   │   ├── in/
│   │   │   │   ├── CriarPedidoInputPort.java
│   │   │   │   ├── AplicarCupomInputPort.java
│   │   │   │   ├── CalcularDescontoEntregaInputPort.java
│   │   │   │   ├── AtualizarStatusPedidoInputPort.java
│   │   │   │   └── BuscarPedidoInputPort.java
│   │   │   └── out/
│   │   │       ├── PedidoRepositoryOutputPort.java
│   │   │       ├── CupomRepositoryOutputPort.java
│   │   │       └── NotificacaoOutputPort.java
│   │   ├── strategy/
│   │   │   ├── IFormaDescontoTaxaEntrega.java
│   │   │   ├── FormaDescontoTaxaPorBairro.java
│   │   │   ├── FormaDescontoTaxaPorTipoCliente.java
│   │   │   ├── FormaDescontoTipoItem.java
│   │   │   └── FormaDescontoValorPedido.java
│   │   └── usecase/
│   │       ├── CriarPedidoUseCase.java
│   │       ├── AplicarCupomUseCase.java
│   │       ├── CalcularDescontoEntregaUseCase.java
│   │       ├── AtualizarStatusPedidoUseCase.java
│   │       └── BuscarPedidoUseCase.java
│   ├── adapter/
│   │   ├── controller/PedidoController.java
│   │   └── presenter/PedidoPresenter.java
│   └── infrastructure/
│       ├── config/ConfiguracaoService.java
│       ├── notification/NotificacaoConsole.java
│       └── repository/
│           ├── CupomRepositoryEmMemoria.java
│           └── PedidoRepositoryEmMemoria.java
└── test/java/com/ufes/delivery/
    ├── domain/entity/PedidoTest.java
    └── application/usecase/
        ├── CriarPedidoUseCaseTest.java
        └── AtualizarStatusPedidoUseCaseTest.java
```

### Tecnologias

- Java 21
- Apache Maven
- JUnit 5 (testes unitários)
