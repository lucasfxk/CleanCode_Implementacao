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
| **Domain** | `domain.entity` | Entidades puras de negócio: `Pedido` (com UUID), `Cliente`, `Item`, `Cupom*`, `StatusPedido` |
| **Application** | `application.usecase`, `application.port`, `application.dto`, `application.strategy` | Use Cases, Input/Output Ports, DTOs (`PedidoResumoDTO` com UUID), estratégias de desconto |
| **Adapter** | `adapter.controller`, `adapter.presenter`, `adapter.ui` | Controladores (`PedidoController` via IDs), `PedidoPresenter` implementando `PresenterOutputPort`, Interface Gráfica isolada do Domain |
| **Infrastructure** | `infrastructure.repository`, `infrastructure.notification`, `infrastructure.config` | Repositórios em memória com busca por ID, notificações e configuração |

### Princípios SOLID Aplicados

| Princípio | Aplicação |
|-----------|-----------|
| **S** — Single Responsibility | Cada classe tem uma única responsabilidade (ex: `PedidoPresenter` só formata, UI só exibe DTOs) |
| **O** — Open/Closed | Novas estratégias de desconto (`IFormaDescontoTaxaEntrega`) adicionadas sem alterar código existente |
| **L** — Liskov Substitution | Implementações de `IFormaDescontoTaxaEntrega` e `NotificacaoOutputPort` são intercambiáveis |
| **I** — Interface Segregation | Input Ports e Output Ports segregados (ex: `PresenterOutputPort`, `PedidoRepositoryOutputPort`) |
| **D** — Dependency Inversion | Controllers e Use Cases dependem de abstrações (ports), a UI não conhece Entidades |

### Padrões de Projeto

| Padrão | Onde | Por quê |
|--------|------|---------|
| **Strategy** | `IFormaDescontoTaxaEntrega` e suas 4 implementações | Permite variar algoritmos de desconto sem alterar o Use Case |
| **Observer** | `NotificacaoOutputPort` / `NotificacaoConsole` / `NotificacaoSwing` | Desacopla a notificação do Use Case |
| **Repository** | `PedidoRepositoryOutputPort`, `CupomRepositoryOutputPort` | Abstrai a persistência (agora com `buscarPorId`) |
| **DTO** | `CriarPedidoDTO`, `ItemDTO`, `PedidoResumoDTO` | Transferência de dados, isolando a UI (Adapter) do Domain |
| **Dependency Injection** | `Main.java` e `MainSwing.java` (Composition Root) | Monta toda a árvore de dependências |
| **Facade** | `PedidoController` | Simplifica o acesso aos use cases, recebendo apenas IDs e DTOs |

### Fluxo da Aplicação

```
1. Criação do Pedido     → UI envia DTO → CriarPedidoUseCase → UI recebe DTO
2. Cálculo de Descontos  → UI envia UUID → BuscarPedidoUseCase → CalcularDescontoEntregaUseCase
3. Aplicação de Cupom    → UI envia UUID → BuscarPedidoUseCase → AplicarCupomUseCase
4. Ciclo de Status       → UI envia UUID → BuscarPedidoUseCase → AtualizarStatusPedidoUseCase
   CRIADO → CONFIRMADO → EM_PREPARO → SAIU_PARA_ENTREGA → ENTREGUE
                       ↘ CANCELADO (a partir de CRIADO ou CONFIRMADO)
5. Listagem              → BuscarPedidoUseCase → UI recebe List<PedidoResumoDTO>
```

### Interface Gráfica (MVP Swing)

O projeto inclui uma interface gráfica Swing (`MainSwing.java`) que demonstra a arquitetura visualmente. 
A UI fica **exclusivamente** na camada Adapter e **não importa nenhuma entidade do Domain**.

**Como executar a interface gráfica:**

```bash
mvn exec:java -Dexec.mainClass="com.ufes.delivery.MainSwing"
```

**Para executar o modo console (Main.java):**
```bash
mvn exec:java -Dexec.mainClass="com.ufes.delivery.Main"
```

### Pré-requisitos

- **Java 21** (JDK 21)
- **Apache Maven 3.8+**

### Como Executar os Testes

São incluídos testes automatizados validando o agregado raiz `Pedido`, os Use Cases principais e as estratégias (Strategy) de desconto.
```bash
mvn test
```
