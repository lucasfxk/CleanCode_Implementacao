# Diagramas UML — Sistema de Delivery (Clean Architecture)

---

## 1. Diagrama de Caso de Uso

```mermaid
flowchart LR
    subgraph Sistema de Delivery
        UC1["Criar Pedido"]
        UC2["Calcular Desconto\nna Entrega"]
        UC3["Aplicar Cupom\nde Desconto"]
        UC4["Atualizar Status\ndo Pedido"]
        UC5["Cancelar Pedido"]
        UC6["Listar Pedidos"]
        UC7["Visualizar Resumo\ndo Pedido"]
    end

    Usuario(("👤 Usuário"))

    Usuario --> UC1
    Usuario --> UC2
    Usuario --> UC3
    Usuario --> UC4
    Usuario --> UC5
    Usuario --> UC6
    Usuario --> UC7

    UC2 -.->|"«include»"| UC7
    UC3 -.->|"«include»"| UC7
    UC4 -.->|"«include»"| UC7

    UC4 -.->|"«extend»\nnotificação"| N["Notificar Mudança\nde Status"]
    UC2 -.->|"«extend»\n4 estratégias"| S["Aplicar Estratégia\nde Desconto"]
```

### Descrição dos Casos de Uso

| # | Caso de Uso | Descrição | Use Case correspondente |
|---|------------|-----------|------------------------|
| UC1 | **Criar Pedido** | Usuário informa dados do cliente e itens; sistema cria o pedido com status CRIADO | `CriarPedidoUseCase` |
| UC2 | **Calcular Desconto na Entrega** | Aplica automaticamente as 4 estratégias de desconto na taxa de entrega (Strategy) | `CalcularDescontoEntregaUseCase` |
| UC3 | **Aplicar Cupom de Desconto** | Usuário informa código do cupom; sistema valida existência, validade e aplica percentual | `AplicarCupomUseCase` |
| UC4 | **Atualizar Status do Pedido** | Avança o pedido no ciclo: CRIADO → CONFIRMADO → EM_PREPARO → SAIU_PARA_ENTREGA → ENTREGUE | `AtualizarStatusPedidoUseCase` |
| UC5 | **Cancelar Pedido** | Cancela o pedido (permitido a partir de CRIADO, CONFIRMADO ou EM_PREPARO) | `AtualizarStatusPedidoUseCase` |
| UC6 | **Listar Pedidos** | Lista todos os pedidos cadastrados no repositório | `BuscarPedidoUseCase` |
| UC7 | **Visualizar Resumo** | Exibe resumo formatado com valores, descontos e status | `PedidoPresenter` |

---

## 2. Diagrama de Classes

### 2.1 Camada Domain (Entities)

```mermaid
classDiagram
    class Pedido {
        -String id
        -double taxaEntrega
        -List~Item~ itens
        -Cliente cliente
        -List~CupomDescontoEntrega~ cuponsDescontoEntrega
        -LocalDateTime data
        -CupomDescontoPedido cupomPedidoAplicado
        -StatusPedido status
        +getId() String
        +adicionarItem(Item) void
        +getValorPedido() double
        +getTotalDescontosTaxaEntrega() double
        +getTaxaEntregaComDesconto() double
        +calcularValorTotal() double
        +atualizarStatus(StatusPedido) void
        +adicionarCupomDescontoEntrega(CupomDescontoEntrega) void
        +limparCuponsDescontoEntrega() void
        +setCupomAplicado(CupomDescontoPedido) void
        +getCupomAplicado() Optional~CupomDescontoPedido~
        +getCliente() Cliente
        +getItens() List~Item~
        +getStatus() StatusPedido
        +getData() LocalDateTime
    }

    class Cliente {
        -String nome
        -String tipo
        -double fidelidade
        -String logradouro
        -String bairro
        -String cidade
        +getNome() String
        +getTipo() String
        +getFidelidade() double
        +getBairro() String
        +getCidade() String
        +getLogradouro() String
        +setFidelidade(double) void
    }

    class Item {
        -String nome
        -int quantidade
        -double valorUnitario
        -String tipo
        +valorTotal() double
        +getNome() String
        +getQuantidade() int
        +getValorUnitario() double
        +getTipo() String
    }

    class StatusPedido {
        <<enumeration>>
        CRIADO
        CONFIRMADO
        EM_PREPARO
        SAIU_PARA_ENTREGA
        ENTREGUE
        CANCELADO
        -String descricao
        +podeTransicionarPara(StatusPedido) boolean
        +getDescricao() String
    }

    class CupomDescontoEntrega {
        -String nomeMetodo
        -double valorDesconto
        -boolean aplicado
        +getValorDesconto() double
        +getNomeMetodo() String
        +isAplicado() boolean
        +aplicar(double) void
    }

    class CupomDescontoPedido {
        -String codigo
        -double percentual
        -LocalDateTime dataHoraInicio
        -LocalDateTime dataHoraFim
        +getCodigo() String
        +getPercentual() double
        +getDataHoraInicio() LocalDateTime
        +getDataHoraFim() LocalDateTime
    }

    Pedido "1" --> "1" Cliente : cliente
    Pedido "1" --> "*" Item : itens
    Pedido "1" --> "1" StatusPedido : status
    Pedido "1" --> "*" CupomDescontoEntrega : cuponsDescontoEntrega
    Pedido "1" --> "0..1" CupomDescontoPedido : cupomPedidoAplicado
```

### 2.2 Camada Application (Use Cases, Ports, DTOs, Strategy)

```mermaid
classDiagram
    class CriarPedidoInputPort {
        <<interface>>
        +executar(CriarPedidoDTO) Pedido
    }

    class CalcularDescontoEntregaInputPort {
        <<interface>>
        +executar(Pedido) void
    }

    class AplicarCupomInputPort {
        <<interface>>
        +executar(Pedido, String, LocalDateTime) void
    }

    class AtualizarStatusPedidoInputPort {
        <<interface>>
        +executar(Pedido, StatusPedido) void
    }

    class BuscarPedidoInputPort {
        <<interface>>
        +buscarPorId(String) Pedido
        +listarTodos() List~Pedido~
    }

    class PedidoRepositoryOutputPort {
        <<interface>>
        +salvar(Pedido) void
        +buscarPorId(String) Optional~Pedido~
        +buscarPorData(LocalDateTime) Optional~Pedido~
        +listarTodos() List~Pedido~
    }

    class CupomRepositoryOutputPort {
        <<interface>>
        +buscarCupom(String) Optional~CupomDescontoPedido~
        +adicionarCupom(CupomDescontoPedido) void
    }

    class NotificacaoOutputPort {
        <<interface>>
        +notificarMudancaStatus(Pedido, StatusPedido, StatusPedido) void
    }

    class PresenterOutputPort {
        <<interface>>
        +toResumoDTO(Pedido) PedidoResumoDTO
        +formatarPedido(Pedido) String
    }

    class IFormaDescontoTaxaEntrega {
        <<interface>>
        +calcularDesconto(Pedido) CupomDescontoEntrega
        +seAplica(Pedido) boolean
    }

    class CriarPedidoUseCase {
        -PedidoRepositoryOutputPort pedidoRepository
        -double taxaEntregaPadrao
        +executar(CriarPedidoDTO) Pedido
    }

    class CalcularDescontoEntregaUseCase {
        -List~IFormaDescontoTaxaEntrega~ metodosDeDesconto
        +executar(Pedido) void
    }

    class AplicarCupomUseCase {
        -CupomRepositoryOutputPort cupomRepository
        +executar(Pedido, String, LocalDateTime) void
    }

    class AtualizarStatusPedidoUseCase {
        -NotificacaoOutputPort notificacao
        +executar(Pedido, StatusPedido) void
    }

    class BuscarPedidoUseCase {
        -PedidoRepositoryOutputPort pedidoRepository
        +buscarPorId(String) Pedido
        +listarTodos() List~Pedido~
    }

    class FormaDescontoTaxaPorBairro {
        -Map~String,Double~ descontoBairro
        +calcularDesconto(Pedido) CupomDescontoEntrega
        +seAplica(Pedido) boolean
    }

    class FormaDescontoTaxaPorTipoCliente {
        -Map~String,Double~ descontosPorTipoCliente
        +calcularDesconto(Pedido) CupomDescontoEntrega
        +seAplica(Pedido) boolean
    }

    class FormaDescontoTipoItem {
        -Map~String,Double~ descontosPorTipoItem
        +calcularDesconto(Pedido) CupomDescontoEntrega
        +seAplica(Pedido) boolean
    }

    class FormaDescontoValorPedido {
        +calcularDesconto(Pedido) CupomDescontoEntrega
        +seAplica(Pedido) boolean
    }

    CriarPedidoUseCase ..|> CriarPedidoInputPort
    CalcularDescontoEntregaUseCase ..|> CalcularDescontoEntregaInputPort
    AplicarCupomUseCase ..|> AplicarCupomInputPort
    AtualizarStatusPedidoUseCase ..|> AtualizarStatusPedidoInputPort
    BuscarPedidoUseCase ..|> BuscarPedidoInputPort

    CriarPedidoUseCase --> PedidoRepositoryOutputPort
    AplicarCupomUseCase --> CupomRepositoryOutputPort
    AtualizarStatusPedidoUseCase --> NotificacaoOutputPort
    BuscarPedidoUseCase --> PedidoRepositoryOutputPort
    CalcularDescontoEntregaUseCase --> IFormaDescontoTaxaEntrega

    FormaDescontoTaxaPorBairro ..|> IFormaDescontoTaxaEntrega
    FormaDescontoTaxaPorTipoCliente ..|> IFormaDescontoTaxaEntrega
    FormaDescontoTipoItem ..|> IFormaDescontoTaxaEntrega
    FormaDescontoValorPedido ..|> IFormaDescontoTaxaEntrega
```

### 2.3 Camada Application — DTOs

```mermaid
classDiagram
    class CriarPedidoDTO {
        -String nomeCliente
        -String tipoCliente
        -double fidelidadeCliente
        -String logradouroCliente
        -String bairroCliente
        -String cidadeCliente
        -LocalDateTime dataPedido
        -List~ItemDTO~ itens
        +getNomeCliente() String
        +getTipoCliente() String
        +getFidelidadeCliente() double
        +getLogradouroCliente() String
        +getBairroCliente() String
        +getCidadeCliente() String
        +getDataPedido() LocalDateTime
        +getItens() List~ItemDTO~
    }

    class ItemDTO {
        -String nome
        -int quantidade
        -double valorUnitario
        -String tipo
        +getNome() String
        +getQuantidade() int
        +getValorUnitario() double
        +getTipo() String
    }

    class PedidoResumoDTO {
        -String id
        -String status
        -LocalDateTime data
        -String nomeCliente
        -String tipoCliente
        -String bairroCliente
        -List~String~ itensDescricao
        -double valorPedido
        -double taxaEntrega
        -double totalDescontosEntrega
        -double taxaEntregaComDesconto
        -List~String~ cuponsDescontoEntregaDescricao
        -String cupomPedidoAplicado
        -double valorTotal
    }

    CriarPedidoDTO "1" --> "*" ItemDTO : itens
```

### 2.4 Camadas Adapter e Infrastructure

```mermaid
classDiagram
    class PedidoController {
        -CriarPedidoInputPort criarPedidoUseCase
        -CalcularDescontoEntregaInputPort calcularDescontoEntregaUseCase
        -AplicarCupomInputPort aplicarCupomUseCase
        -AtualizarStatusPedidoInputPort atualizarStatusUseCase
        -BuscarPedidoInputPort buscarPedidoUseCase
        -PresenterOutputPort presenter
        +criarPedido(CriarPedidoDTO) PedidoResumoDTO
        +calcularDescontosEntrega(String) void
        +aplicarCupom(String, String, LocalDateTime) void
        +atualizarStatus(String, String) void
        +listarPedidos() List~PedidoResumoDTO~
        +obterResumo(String) PedidoResumoDTO
        +apresentarPedido(String) String
    }

    class PedidoPresenter {
        +toResumoDTO(Pedido) PedidoResumoDTO
        +formatarPedido(Pedido) String
    }

    class DeliveryApp {
        -PainelGerenciarPedido painelGerenciar
        +receberNotificacao(String) void
    }

    class PainelNovoPedido {
        -PedidoController controller
        -Consumer~PedidoResumoDTO~ aocriarPedido
        -DefaultTableModel modeloItens
        -criarPedido() void
        -coletarItens() List~ItemDTO~
    }

    class PainelGerenciarPedido {
        -PedidoController controller
        -List~PedidoResumoDTO~ pedidosCache
        +atualizarListaPedidos() void
        +adicionarLogNotificacao(String) void
        -calcularDescontos() void
        -aplicarCupom() void
        -avancarStatus(StatusPedido) void
    }

    class NotificacaoSwing {
        -Consumer~String~ destinoMensagem
        +notificarMudancaStatus(Pedido, StatusPedido, StatusPedido) void
    }

    class NotificacaoConsole {
        +notificarMudancaStatus(Pedido, StatusPedido, StatusPedido) void
    }

    class PedidoRepositoryEmMemoria {
        -List~Pedido~ pedidos
        +salvar(Pedido) void
        +buscarPorId(String) Optional~Pedido~
        +buscarPorData(LocalDateTime) Optional~Pedido~
        +listarTodos() List~Pedido~
    }

    class CupomRepositoryEmMemoria {
        -Map~String,CupomDescontoPedido~ cuponsDisponiveis
        +buscarCupom(String) Optional~CupomDescontoPedido~
        +adicionarCupom(CupomDescontoPedido) void
        +removerCuponsExpirados() void
    }

    class ConfiguracaoService {
        +getTaxaEntregaPadrao()$ double
    }

    DeliveryApp --> PainelNovoPedido
    DeliveryApp --> PainelGerenciarPedido
    PainelNovoPedido --> PedidoController
    PainelGerenciarPedido --> PedidoController
    PedidoController --> PresenterOutputPort
    PedidoPresenter ..|> PresenterOutputPort

    NotificacaoSwing ..|> NotificacaoOutputPort
    NotificacaoConsole ..|> NotificacaoOutputPort
    PedidoRepositoryEmMemoria ..|> PedidoRepositoryOutputPort
    CupomRepositoryEmMemoria ..|> CupomRepositoryOutputPort

    class NotificacaoOutputPort {
        <<interface>>
    }
    class PedidoRepositoryOutputPort {
        <<interface>>
    }
    class CupomRepositoryOutputPort {
        <<interface>>
    }
```

---

## 3. Visão Geral das Camadas

```mermaid
graph TB
    subgraph "🟦 Domain — Entidades puras de negócio"
        Pedido
        Cliente
        Item
        StatusPedido
        CupomDescontoEntrega
        CupomDescontoPedido
    end

    subgraph "🟩 Application — Use Cases, Ports, DTOs, Strategy"
        direction TB
        subgraph "Input Ports"
            CriarPedidoIP["CriarPedidoInputPort"]
            CalcDescontoIP["CalcularDescontoEntregaInputPort"]
            AplicarCupomIP["AplicarCupomInputPort"]
            AtualizarStatusIP["AtualizarStatusPedidoInputPort"]
            BuscarPedidoIP["BuscarPedidoInputPort"]
        end
        subgraph "Use Cases"
            CriarPedidoUC["CriarPedidoUseCase"]
            CalcDescontoUC["CalcularDescontoEntregaUseCase"]
            AplicarCupomUC["AplicarCupomUseCase"]
            AtualizarStatusUC["AtualizarStatusPedidoUseCase"]
            BuscarPedidoUC["BuscarPedidoUseCase"]
        end
        subgraph "Output Ports"
            PedidoRepoOP["PedidoRepositoryOutputPort"]
            CupomRepoOP["CupomRepositoryOutputPort"]
            NotificacaoOP["NotificacaoOutputPort"]
            PresenterOP["PresenterOutputPort"]
        end
        subgraph "Strategy"
            IForma["IFormaDescontoTaxaEntrega"]
            PorBairro["FormaDescontoTaxaPorBairro"]
            PorTipo["FormaDescontoTaxaPorTipoCliente"]
            PorItem["FormaDescontoTipoItem"]
            PorValor["FormaDescontoValorPedido"]
        end
    end

    subgraph "🟨 Adapter — Controller, Presenter, UI"
        PedidoCtrl["PedidoController"]
        Presenter["PedidoPresenter"]
        App["DeliveryApp"]
        PainelNovo["PainelNovoPedido"]
        PainelGerenciar["PainelGerenciarPedido"]
        NotifSwing["NotificacaoSwing"]
    end

    subgraph "🟥 Infrastructure — Repositórios, Notificação, Config"
        PedidoRepoMem["PedidoRepositoryEmMemoria"]
        CupomRepoMem["CupomRepositoryEmMemoria"]
        NotifConsole["NotificacaoConsole"]
        Config["ConfiguracaoService"]
    end

    PedidoRepoMem -.-> PedidoRepoOP
    CupomRepoMem -.-> CupomRepoOP
    NotifConsole -.-> NotificacaoOP
    NotifSwing -.-> NotificacaoOP
    Presenter -.-> PresenterOP

    PedidoCtrl --> CriarPedidoIP
    PedidoCtrl --> CalcDescontoIP
    PedidoCtrl --> AplicarCupomIP
    PedidoCtrl --> AtualizarStatusIP
    PedidoCtrl --> BuscarPedidoIP
    PedidoCtrl --> PresenterOP

    CriarPedidoUC -.-> CriarPedidoIP
    CalcDescontoUC -.-> CalcDescontoIP
    AplicarCupomUC -.-> AplicarCupomIP
    AtualizarStatusUC -.-> AtualizarStatusIP
    BuscarPedidoUC -.-> BuscarPedidoIP

    PorBairro -.-> IForma
    PorTipo -.-> IForma
    PorItem -.-> IForma
    PorValor -.-> IForma
```
