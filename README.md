# Sistema de Delivery — Clean Architecture

## Projeto de Sistemas de Software — UFES 2026/1

### Descricao

Este projeto implementa um **Sistema de Delivery** utilizando os principios da **Clean Architecture** (Robert C. Martin). O sistema gerencia pedidos com calculo de descontos na taxa de entrega e aplicacao de cupons de desconto.

### Arquitetura

O projeto esta organizado em 4 camadas concentricas, seguindo a **Dependency Rule** (dependencias sempre apontam para dentro):

```
Infrastructure -> Adapter -> Application (Use Cases) -> Domain (Entities)
```

#### Camadas

| Camada | Pacote | Responsabilidade |
|--------|--------|------------------|
| **Domain** | `domain.entity` | Entidades de negocio puras (Cliente, Item, Pedido, Cupons) |
| **Application** | `application.usecase`, `application.port`, `application.dto`, `application.strategy` | Casos de uso, ports (interfaces de entrada/saida), DTOs, estrategias de desconto |
| **Adapter** | `adapter.controller`, `adapter.presenter` | Controladores e formatadores de saida |
| **Infrastructure** | `infrastructure.repository`, `infrastructure.config` | Repositorios em memoria e configuracao |

### Principios SOLID Aplicados

- **S** (Single Responsibility): Cada classe tem uma unica responsabilidade
- **O** (Open/Closed): Novas estrategias de desconto sao adicionadas sem alterar codigo existente
- **L** (Liskov Substitution): Implementacoes de `IFormaDescontoTaxaEntrega` sao intercambiaveis
- **I** (Interface Segregation): Input/Output ports segregados por caso de uso
- **D** (Dependency Inversion): Use Cases dependem de abstracoes (ports), nao de implementacoes

### Padroes de Projeto

- **Strategy**: Diferentes formas de desconto na taxa de entrega
- **Repository**: Abstracao da persistencia de dados
- **DTO**: Transferencia de dados entre camadas
- **Dependency Injection**: Composicao de dependencias no Main (Composition Root)
- **Facade**: Controller simplifica o acesso aos use cases

### Pre-requisitos

- **Java 21** (JDK 21)
- **Apache Maven 3.8+**

### Como Executar

1. Clone o repositorio:
```bash
git clone <URL_DO_REPOSITORIO>
cd Delivery+CleanArchitecture
```

2. Compile o projeto:
```bash
mvn clean compile
```

3. Execute:
```bash
mvn exec:java
```

Ou alternativamente:
```bash
mvn clean package
java -jar target/delivery-clean-architecture-1.0-SNAPSHOT.jar
```

### Estrutura do Projeto

```
src/main/java/com/ufes/delivery/
|-- Main.java                          (Composition Root)
|-- domain/
|   |-- entity/
|       |-- Cliente.java
|       |-- Item.java
|       |-- Pedido.java
|       |-- CupomDescontoPedido.java
|       |-- CupomDescontoEntrega.java
|-- application/
|   |-- dto/
|   |   |-- CriarPedidoDTO.java
|   |   |-- ItemDTO.java
|   |   |-- PedidoResumoDTO.java
|   |-- port/
|   |   |-- in/
|   |   |   |-- CriarPedidoInputPort.java
|   |   |   |-- AplicarCupomInputPort.java
|   |   |   |-- CalcularDescontoEntregaInputPort.java
|   |   |-- out/
|   |       |-- CupomRepositoryOutputPort.java
|   |       |-- PedidoRepositoryOutputPort.java
|   |-- strategy/
|   |   |-- IFormaDescontoTaxaEntrega.java
|   |   |-- FormaDescontoTaxaPorBairro.java
|   |   |-- FormaDescontoTaxaPorTipoCliente.java
|   |   |-- FormaDescontoTipoItem.java
|   |   |-- FormaDescontoValorPedido.java
|   |-- usecase/
|       |-- CriarPedidoUseCase.java
|       |-- AplicarCupomUseCase.java
|       |-- CalcularDescontoEntregaUseCase.java
|-- adapter/
|   |-- controller/
|   |   |-- PedidoController.java
|   |-- presenter/
|       |-- PedidoPresenter.java
|-- infrastructure/
    |-- config/
    |   |-- ConfiguracaoService.java
    |-- repository/
        |-- CupomRepositoryEmMemoria.java
        |-- PedidoRepositoryEmMemoria.java
```

### Tecnologias

- Java 21
- Apache Maven
# trabalho-final-pss
