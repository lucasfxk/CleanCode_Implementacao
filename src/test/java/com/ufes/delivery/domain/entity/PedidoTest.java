package com.ufes.delivery.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pedido — Domain Entity")
class PedidoTest {

    private Pedido pedido;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente("Maria", "Ouro", 1, "Rua A", "Centro", "Castelo");
        pedido = new Pedido(LocalDateTime.now(), cliente, 10.0);
    }

    @Test
    @DisplayName("Pedido criado com status CRIADO")
    void deveCriarComStatusCriado() {
        assertEquals(StatusPedido.CRIADO, pedido.getStatus());
    }

    @Test
    @DisplayName("Transicao valida: CRIADO -> CONFIRMADO")
    void devePermitirTransicaoValidaParaConfirmado() {
        pedido.atualizarStatus(StatusPedido.CONFIRMADO);
        assertEquals(StatusPedido.CONFIRMADO, pedido.getStatus());
    }

    @Test
    @DisplayName("Transicao invalida: CRIADO -> ENTREGUE lanca excecao")
    void deveLancarExcecaoTransicaoInvalida() {
        assertThrows(IllegalStateException.class,
                () -> pedido.atualizarStatus(StatusPedido.ENTREGUE));
    }

    @Test
    @DisplayName("Taxa de entrega nao pode ser negativa")
    void deveRejeitarTaxaNegativa() {
        assertThrows(IllegalArgumentException.class,
                () -> new Pedido(LocalDateTime.now(), cliente, -1.0));
    }

    @Test
    @DisplayName("Valor total sem desconto = itens + taxa entrega")
    void deveCalcularValorTotalSemDesconto() {
        pedido.adicionarItem(new Item("Produto", 2, 10.0, "Alimentacao"));
        // 2 * 10 + 10 (taxa) = 30
        assertEquals(30.0, pedido.calcularValorTotal(), 0.001);
    }

    @Test
    @DisplayName("Desconto na taxa de entrega nao pode ultrapassar a taxa")
    void deveRejeitarDescontoMaiorQueTaxa() {
        CupomDescontoEntrega cupom = new CupomDescontoEntrega("Teste", 15.0);
        assertThrows(IllegalStateException.class,
                () -> pedido.adicionarCupomDescontoEntrega(cupom));
    }

    @Test
    @DisplayName("Ciclo completo: CRIADO -> CONFIRMADO -> EM_PREPARO -> SAIU -> ENTREGUE")
    void deveCumprirCicloCompleto() {
        pedido.atualizarStatus(StatusPedido.CONFIRMADO);
        pedido.atualizarStatus(StatusPedido.EM_PREPARO);
        pedido.atualizarStatus(StatusPedido.SAIU_PARA_ENTREGA);
        pedido.atualizarStatus(StatusPedido.ENTREGUE);
        assertEquals(StatusPedido.ENTREGUE, pedido.getStatus());
    }

    @Test
    @DisplayName("Pedido ENTREGUE nao pode ser cancelado")
    void deveRejeitarCancelamentoAposEntrega() {
        pedido.atualizarStatus(StatusPedido.CONFIRMADO);
        pedido.atualizarStatus(StatusPedido.EM_PREPARO);
        pedido.atualizarStatus(StatusPedido.SAIU_PARA_ENTREGA);
        pedido.atualizarStatus(StatusPedido.ENTREGUE);
        assertThrows(IllegalStateException.class,
                () -> pedido.atualizarStatus(StatusPedido.CANCELADO));
    }
}
