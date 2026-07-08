package com.ufes.delivery;

import com.sun.net.httpserver.HttpServer;
import com.ufes.delivery.adapter.controller.ItemController;
import com.ufes.delivery.adapter.controller.PedidoController;
import com.ufes.delivery.adapter.controller.PedidoWebController;
import com.ufes.delivery.adapter.presenter.PedidoPresenter;
import com.ufes.delivery.adapter.ui.DeliveryApp;
import com.ufes.delivery.adapter.ui.NotificacaoSwing;
import com.ufes.delivery.application.port.in.*;
import com.ufes.delivery.application.port.out.*;
import com.ufes.delivery.application.strategy.*;
import com.ufes.delivery.application.usecase.*;
import com.ufes.delivery.domain.entity.CupomDescontoPedido;
import com.ufes.delivery.infrastructure.config.ConfiguracaoService;
import com.ufes.delivery.infrastructure.repository.ItemRepositoryEmSQLite;
import com.ufes.delivery.infrastructure.repository.CupomRepositoryEmMemoria;
import com.ufes.delivery.infrastructure.repository.PedidoRepositoryEmMemoria;
import com.ufes.delivery.infrastructure.repository.PedidoRepositoryEmSQLite;

import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Ponto de entrada da interface grafica — Composition Root (Swing).
 *
 * Responsabilidade: montar toda a arvore de dependencias e abrir a janela.
 * Nenhuma outra classe da UI conhece este arquivo.
 *
 * Clean Architecture — Dependency Rule:
 *   MainSwing → Infrastructure → Adapter (UI) → Application → Domain
 *
 * A unica diferenca para o Main.java original e a troca de:
 *   NotificacaoConsole  →  NotificacaoSwing
 * Isso demonstra na pratica o beneficio do Output Port (OCP + DIP).
 */
public class MainSwing {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainSwing::iniciar);
    }

    private static void iniciar() {
        configurarLookAndFeel();

        // =================================================================
        // 1. INFRASTRUCTURE
        // =================================================================
        CupomRepositoryEmMemoria cupomRepositoryConcrete = new CupomRepositoryEmMemoria();
        PedidoRepositoryOutputPort pedidoRepository      = new PedidoRepositoryEmMemoria();
        ItemRepositoryOutputPort itemRepository          = new ItemRepositoryEmSQLite();
        

        LocalDateTime agora = LocalDateTime.now();
        cupomRepositoryConcrete.adicionarCupom(
                new CupomDescontoPedido("VALIDOHOJE", 15.0,
                        agora.minusDays(1), agora.plusDays(1)));

        CupomRepositoryOutputPort cupomRepository = cupomRepositoryConcrete;

        // =================================================================
        // 2. APPLICATION — estrategias (Strategy Pattern)
        // =================================================================
        List<IFormaDescontoTaxaEntrega> estrategias = Arrays.asList(
                new FormaDescontoTaxaPorBairro(),
                new FormaDescontoTaxaPorTipoCliente(),
                new FormaDescontoTipoItem(),
                new FormaDescontoValorPedido()
        );

        // =================================================================
        // 3. APPLICATION — Use Cases
        // =================================================================
        double taxa = ConfiguracaoService.getTaxaEntregaPadrao();

        CriarPedidoInputPort criarPedido =
                new CriarPedidoUseCase(pedidoRepository, taxa);

        CalcularDescontoEntregaInputPort calcularDesconto =
                new CalcularDescontoEntregaUseCase(estrategias, pedidoRepository);

        AplicarCupomInputPort aplicarCupom =
                new AplicarCupomUseCase(cupomRepository, pedidoRepository);

        BuscarPedidoInputPort buscarPedido =
                new BuscarPedidoUseCase(pedidoRepository);

        // =================================================================
        // 4. ADAPTER — janela criada antes do use case de status
        //    para que o callback de notificacao aponte para ela
        // =================================================================
        PedidoPresenter presenter = new PedidoPresenter();

        // Placeholder do controller — sera substituido apos criar a janela
        // (precisamos da janela para montar o NotificacaoSwing)
        //
        // Solucao limpa: criamos o controller em duas etapas usando um
        // array de referencia (evita variavel nao-final no lambda).
        DeliveryApp[] appRef = new DeliveryApp[1];

        NotificacaoSwing notificacao = new NotificacaoSwing(
                msg -> { if (appRef[0] != null) appRef[0].receberNotificacao(msg); }
        );

        AtualizarStatusPedidoInputPort atualizarStatus =
                new AtualizarStatusPedidoUseCase(notificacao, pedidoRepository);

        PedidoController controller = new PedidoController(
                criarPedido,
                calcularDesconto,
                aplicarCupom,
                atualizarStatus,
                buscarPedido,
                presenter
        );

        ItemController itemController = new ItemController(
                new CadastrarItemUseCase(itemRepository),
                new ListarItensUseCase(itemRepository)
        );

        // =================================================================
        // 4. UI (ADAPTER)
        // =================================================================
        appRef[0] = new DeliveryApp(controller, itemController);

        // =================================================================
        // 5. WEB API (ADAPTER SIMULTANEO)
        // =================================================================
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/pedidos", new PedidoWebController(buscarPedido, presenter));
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            System.out.println("Servidor Web (API REST) rodando simultaneamente em http://localhost:8080/pedidos");
        } catch (IOException e) {
            System.err.println("Nao foi possivel iniciar a API Web: " + e.getMessage());
        }
    }

    private static void configurarLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // fallback para o Look and Feel padrao do Swing
        }
    }
}
