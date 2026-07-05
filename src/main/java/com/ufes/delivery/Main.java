package com.ufes.delivery;

import com.ufes.delivery.adapter.controller.PedidoController;
import com.ufes.delivery.adapter.presenter.PedidoPresenter;
import com.ufes.delivery.application.dto.CriarPedidoDTO;
import com.ufes.delivery.application.dto.ItemDTO;
import com.ufes.delivery.application.port.in.*;
import com.ufes.delivery.application.port.out.*;
import com.ufes.delivery.application.strategy.*;
import com.ufes.delivery.application.usecase.*;
import com.ufes.delivery.domain.entity.CupomDescontoPedido;
import com.ufes.delivery.domain.entity.Pedido;
import com.ufes.delivery.domain.entity.StatusPedido;
import com.ufes.delivery.application.dto.PedidoResumoDTO;
import com.ufes.delivery.infrastructure.config.ConfiguracaoService;
import com.ufes.delivery.infrastructure.notification.NotificacaoConsole;
import com.ufes.delivery.infrastructure.repository.CupomRepositoryEmMemoria;
import com.ufes.delivery.infrastructure.repository.PedidoRepositoryEmMemoria;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Ponto de entrada da aplicacao — Composition Root.
 *
 * Aqui ocorre a montagem de toda a arvore de dependencias (Poor Man's DI).
 * Nenhuma camada interna conhece esta classe; ela apenas conecta as
 * implementacoes concretas as interfaces (ports) da camada Application.
 *
 * Clean Architecture — Dependency Rule:
 *   Main -> Infrastructure -> Adapter -> Application (Use Cases/Ports) -> Domain
 *   As dependencias sempre apontam para dentro (em direcao ao dominio).
 */
public class Main {

    public static void main(String[] args) {
        cabecalho("CLEAN ARCHITECTURE — Sistema de Delivery", '=');

        // =====================================================================
        // 1. INFRASTRUCTURE — repositorios e servicos externos
        // =====================================================================
        CupomRepositoryEmMemoria cupomRepositoryConcrete = new CupomRepositoryEmMemoria();
        PedidoRepositoryOutputPort pedidoRepository = new PedidoRepositoryEmMemoria();
        NotificacaoOutputPort notificacao = new NotificacaoConsole(); // <-- Observer

        LocalDateTime agora = LocalDateTime.now();
        cupomRepositoryConcrete.adicionarCupom(
                new CupomDescontoPedido("VALIDOHOJE", 15.0, agora.minusDays(1), agora.plusDays(1)));

        CupomRepositoryOutputPort cupomRepository = cupomRepositoryConcrete;

        // =====================================================================
        // 2. APPLICATION — estrategias de desconto (Strategy Pattern)
        // =====================================================================
        List<IFormaDescontoTaxaEntrega> estrategiasDesconto = Arrays.asList(
                new FormaDescontoTaxaPorBairro(),
                new FormaDescontoTaxaPorTipoCliente(),
                new FormaDescontoTipoItem(),
                new FormaDescontoValorPedido()
        );

        // =====================================================================
        // 3. APPLICATION — Use Cases (injecao via construtor — DIP)
        // =====================================================================
        double taxaEntregaPadrao = ConfiguracaoService.getTaxaEntregaPadrao();

        CriarPedidoInputPort criarPedidoUseCase =
                new CriarPedidoUseCase(pedidoRepository, taxaEntregaPadrao);

        CalcularDescontoEntregaInputPort calcularDescontoUseCase =
                new CalcularDescontoEntregaUseCase(estrategiasDesconto);

        AplicarCupomInputPort aplicarCupomUseCase =
                new AplicarCupomUseCase(cupomRepository);

        AtualizarStatusPedidoInputPort atualizarStatusUseCase =
                new AtualizarStatusPedidoUseCase(notificacao);

        BuscarPedidoInputPort buscarPedidoUseCase =
                new BuscarPedidoUseCase(pedidoRepository);

        // =====================================================================
        // 4. ADAPTER — Presenter e Controller
        // =====================================================================
        PedidoPresenter presenter = new PedidoPresenter();
        PedidoController controller = new PedidoController(
                criarPedidoUseCase,
                calcularDescontoUseCase,
                aplicarCupomUseCase,
                atualizarStatusUseCase,
                buscarPedidoUseCase,
                presenter
        );

        // =====================================================================
        // 5. FLUXO COMPLETO — Pedido 1 (cliente Ouro, bairro com desconto)
        // =====================================================================
        cabecalho("PEDIDO 1 — Criacao e Descontos", '-');

        List<ItemDTO> itens1 = Arrays.asList(
                new ItemDTO("Caderno",   2,  10.50, "Educacao"),
                new ItemDTO("Borracha",  5,   4.25, "Educacao"),
                new ItemDTO("Biscoito",  4,   5.80, "Alimentacao"),
                new ItemDTO("Pao",       2,   1.50, "Alimentacao"),
                new ItemDTO("Livro",     2,  40.20, "Lazer"),
                new ItemDTO("Jogo",      1,  45.90, "Lazer")
        );

        CriarPedidoDTO dto1 = new CriarPedidoDTO(
                "Maria", "Ouro", 1,
                "Limoeiro", "Cidade Maravilhosa", "Castelo",
                agora, itens1);

        PedidoResumoDTO pedido1 = controller.criarPedido(dto1);
        System.out.println("Pedido criado! Status: " + pedido1.getStatus());

        System.out.println("\n>>> Calculando descontos na taxa de entrega (Strategy)...");
        controller.calcularDescontosEntrega(pedido1.getId());

        System.out.println("\n>>> Aplicando cupom VALIDOHOJE...");
        controller.aplicarCupom(pedido1.getId(), "VALIDOHOJE", agora);

        System.out.println("\n>>> Tentando aplicar cupom inexistente...");
        try {
            controller.aplicarCupom(pedido1.getId(), "CUPOMINEXISTENTE", agora);
        } catch (RuntimeException ex) {
            System.out.println("Erro esperado: " + ex.getMessage());
        }

        System.out.println(controller.apresentarPedido(pedido1.getId()));

        // =====================================================================
        // 6. CICLO DE VIDA — Atualizacao de Status com Notificacoes (Observer)
        // =====================================================================
        cabecalho("CICLO DE VIDA DO PEDIDO — Notificacoes em Tempo Real", '-');

        avancarStatus(controller, pedido1.getId(), "CONFIRMADO");
        avancarStatus(controller, pedido1.getId(), "EM_PREPARO");
        avancarStatus(controller, pedido1.getId(), "SAIU_PARA_ENTREGA");
        avancarStatus(controller, pedido1.getId(), "ENTREGUE");

        System.out.println("\n>>> Tentando transicao invalida (ENTREGUE -> CANCELADO)...");
        try {
            controller.atualizarStatus(pedido1.getId(), "CANCELADO");
        } catch (IllegalStateException ex) {
            System.out.println("Erro esperado: " + ex.getMessage());
        }

        // =====================================================================
        // 7. PEDIDO 2 — Cancelamento
        // =====================================================================
        cabecalho("PEDIDO 2 — Cancelamento", '-');

        List<ItemDTO> itens2 = Arrays.asList(
                new ItemDTO("Pizza",  1, 55.00, "Alimentacao"),
                new ItemDTO("Refri",  2,  8.00, "Alimentacao")
        );

        CriarPedidoDTO dto2 = new CriarPedidoDTO(
                "Joao", "Prata", 3,
                "Centro", "Castelo", "Castelo",
                agora.plusMinutes(5), itens2);

        PedidoResumoDTO pedido2 = controller.criarPedido(dto2);
        avancarStatus(controller, pedido2.getId(), "CONFIRMADO");
        avancarStatus(controller, pedido2.getId(), "CANCELADO");

        // =====================================================================
        // 8. LISTAGEM — todos os pedidos no repositorio
        // =====================================================================
        cabecalho("LISTAGEM DE PEDIDOS", '-');
        List<PedidoResumoDTO> todos = controller.listarPedidos();
        System.out.printf("Total de pedidos registrados: %d%n", todos.size());
        todos.forEach(p -> System.out.printf("  - Cliente: %-10s | Status: %s%n",
                p.getNomeCliente(), p.getStatus()));

        cabecalho("Execucao finalizada com sucesso!", '=');
    }

    // -------------------------------------------------------------------------
    // Helpers de apresentacao
    // -------------------------------------------------------------------------
    private static void avancarStatus(PedidoController controller, String pedidoId, String novoStatus) {
        System.out.printf("%n>>> Atualizando status para: %s%n", novoStatus);
        controller.atualizarStatus(pedidoId, novoStatus);
    }

    private static void cabecalho(String titulo, char separador) {
        String linha = String.valueOf(separador).repeat(63);
        System.out.println("\n" + linha);
        System.out.printf("  %s%n", titulo);
        System.out.println(linha);
    }
}
