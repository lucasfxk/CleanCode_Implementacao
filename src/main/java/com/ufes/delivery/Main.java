package com.ufes.delivery;

import com.ufes.delivery.adapter.controller.PedidoController;
import com.ufes.delivery.adapter.presenter.PedidoPresenter;
import com.ufes.delivery.application.dto.CriarPedidoDTO;
import com.ufes.delivery.application.dto.ItemDTO;
import com.ufes.delivery.application.port.in.AplicarCupomInputPort;
import com.ufes.delivery.application.port.in.CalcularDescontoEntregaInputPort;
import com.ufes.delivery.application.port.in.CriarPedidoInputPort;
import com.ufes.delivery.application.port.out.CupomRepositoryOutputPort;
import com.ufes.delivery.application.port.out.PedidoRepositoryOutputPort;
import com.ufes.delivery.application.strategy.FormaDescontoTaxaPorBairro;
import com.ufes.delivery.application.strategy.FormaDescontoTaxaPorTipoCliente;
import com.ufes.delivery.application.strategy.FormaDescontoTipoItem;
import com.ufes.delivery.application.strategy.FormaDescontoValorPedido;
import com.ufes.delivery.application.strategy.IFormaDescontoTaxaEntrega;
import com.ufes.delivery.application.usecase.AplicarCupomUseCase;
import com.ufes.delivery.application.usecase.CalcularDescontoEntregaUseCase;
import com.ufes.delivery.application.usecase.CriarPedidoUseCase;
import com.ufes.delivery.domain.entity.CupomDescontoPedido;
import com.ufes.delivery.domain.entity.Pedido;
import com.ufes.delivery.infrastructure.config.ConfiguracaoService;
import com.ufes.delivery.infrastructure.repository.CupomRepositoryEmMemoria;
import com.ufes.delivery.infrastructure.repository.PedidoRepositoryEmMemoria;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Ponto de entrada da aplicacao — Composition Root.
 *
 * Aqui ocorre a montagem de toda a arvore de dependencias (Poor Man's Dependency Injection).
 * Nenhuma camada interna conhece esta classe; ela apenas conecta as implementacoes concretas
 * as interfaces (ports) definidas na camada de Application.
 *
 * Clean Architecture — Dependency Rule:
 *   Main -> Infrastructure -> Application (Use Cases / Ports) -> Domain (Entities)
 *   As dependencias sempre apontam para dentro (em direcao ao dominio).
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("===========================================================");
        System.out.println("  CLEAN ARCHITECTURE — Sistema de Delivery");
        System.out.println("  Projeto de Sistemas de Software — UFES 2026-1");
        System.out.println("===========================================================");

        // =====================================================================
        // 1. INFRASTRUCTURE — Instanciacao dos repositorios (camada externa)
        // =====================================================================
        CupomRepositoryEmMemoria cupomRepositoryConcrete = new CupomRepositoryEmMemoria();
        PedidoRepositoryOutputPort pedidoRepository = new PedidoRepositoryEmMemoria();

        // Adiciona cupom valido para demonstracao
        LocalDateTime agora = LocalDateTime.now();
        cupomRepositoryConcrete.adicionarCupom(
                new CupomDescontoPedido("VALIDOHOJE", 15.0, agora.minusDays(1), agora.plusDays(1)));

        // Cast para a interface (output port) — demonstra Dependency Inversion
        CupomRepositoryOutputPort cupomRepository = cupomRepositoryConcrete;

        // =====================================================================
        // 2. APPLICATION — Instanciacao das estrategias de desconto (Strategy)
        // =====================================================================
        List<IFormaDescontoTaxaEntrega> estrategiasDesconto = Arrays.asList(
                new FormaDescontoTaxaPorBairro(),
                new FormaDescontoTaxaPorTipoCliente(),
                new FormaDescontoTipoItem(),
                new FormaDescontoValorPedido()
        );

        // =====================================================================
        // 3. APPLICATION — Instanciacao dos Use Cases (injecao via construtor)
        // =====================================================================
        double taxaEntregaPadrao = ConfiguracaoService.getTaxaEntregaPadrao();

        CriarPedidoInputPort criarPedidoUseCase =
                new CriarPedidoUseCase(pedidoRepository, taxaEntregaPadrao);

        CalcularDescontoEntregaInputPort calcularDescontoUseCase =
                new CalcularDescontoEntregaUseCase(estrategiasDesconto);

        AplicarCupomInputPort aplicarCupomUseCase =
                new AplicarCupomUseCase(cupomRepository);

        // =====================================================================
        // 4. ADAPTER — Instanciacao do Presenter e Controller
        // =====================================================================
        PedidoPresenter presenter = new PedidoPresenter();
        PedidoController controller = new PedidoController(
                criarPedidoUseCase,
                calcularDescontoUseCase,
                aplicarCupomUseCase,
                presenter
        );

        // =====================================================================
        // 5. EXECUCAO DO CASO DE USO — Simulacao de um pedido de delivery
        // =====================================================================
        System.out.println("\n>>> Criando pedido...");

        List<ItemDTO> itens = Arrays.asList(
                new ItemDTO("Caderno", 2, 10.50, "Educacao"),
                new ItemDTO("Borracha", 5, 4.25, "Educacao"),
                new ItemDTO("Biscoito", 4, 5.80, "Alimentacao"),
                new ItemDTO("Pao", 2, 1.50, "Alimentacao"),
                new ItemDTO("Livro", 2, 40.20, "Lazer"),
                new ItemDTO("Jogo", 1, 45.90, "Lazer")
        );

        CriarPedidoDTO criarPedidoDTO = new CriarPedidoDTO(
                "Maria", "Ouro", 1,
                "Limoeiro", "Cidade Maravilhosa", "Castelo",
                agora, itens
        );

        Pedido pedido = controller.criarPedido(criarPedidoDTO);
        System.out.println("Pedido criado com sucesso!");

        // =====================================================================
        // 6. CALCULO DE DESCONTOS NA TAXA DE ENTREGA (Strategy Pattern)
        // =====================================================================
        System.out.println("\n>>> Calculando descontos na taxa de entrega...");
        controller.calcularDescontosEntrega(pedido);
        System.out.println("Descontos calculados!");

        // =====================================================================
        // 7. APLICACAO DE CUPOM DE DESCONTO NO PEDIDO
        // =====================================================================
        System.out.println("\n>>> Aplicando cupom VALIDOHOJE...");
        controller.aplicarCupom(pedido, "VALIDOHOJE", LocalDateTime.now());
        System.out.println("Cupom aplicado com sucesso!");

        // Tentativa de aplicar cupom inexistente (tratamento de erro)
        System.out.println("\n>>> Tentando aplicar cupom inexistente...");
        try {
            controller.aplicarCupom(pedido, "CUPOMINEXISTENTE", LocalDateTime.now());
        } catch (RuntimeException ex) {
            System.out.println("Erro esperado: " + ex.getMessage());
        }

        // =====================================================================
        // 8. APRESENTACAO DO RESULTADO (Presenter)
        // =====================================================================
        String resultado = controller.apresentarPedido(pedido);
        System.out.println(resultado);

        System.out.println("===========================================================");
        System.out.println("  Execucao finalizada com sucesso!");
        System.out.println("===========================================================");
    }
}
