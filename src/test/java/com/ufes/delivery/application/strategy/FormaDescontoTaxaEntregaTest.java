package com.ufes.delivery.application.strategy;

import com.ufes.delivery.domain.entity.Cliente;
import com.ufes.delivery.domain.entity.CupomDescontoEntrega;
import com.ufes.delivery.domain.entity.Item;
import com.ufes.delivery.domain.entity.Pedido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class FormaDescontoTaxaEntregaTest {

    private Pedido criarPedidoComBairroETipoCliente(String bairro, String tipoCliente) {
        Cliente cliente = new Cliente("Teste", tipoCliente, 1.0, "Rua A", bairro, "Vitoria");
        return new Pedido(LocalDateTime.now(), cliente, 10.0);
    }

    private Pedido criarPedidoComItemDeTipo(String tipoItem) {
        Cliente cliente = new Cliente("Teste", "Bronze", 1.0, "Rua A", "Bairro X", "Vitoria");
        Pedido pedido = new Pedido(LocalDateTime.now(), cliente, 10.0);
        pedido.adicionarItem(new Item("Produto", 1, 10.0, tipoItem));
        return pedido;
    }

    private Pedido criarPedidoComValorDe(double valorTotalItens) {
        Cliente cliente = new Cliente("Teste", "Bronze", 1.0, "Rua A", "Bairro X", "Vitoria");
        Pedido pedido = new Pedido(LocalDateTime.now(), cliente, 10.0);
        pedido.adicionarItem(new Item("Produto Caro", 1, valorTotalItens, "Outro"));
        return pedido;
    }

    // --- FormaDescontoTaxaPorBairro ---

    @Test
    @DisplayName("Desconto Por Bairro - Deve aplicar desconto de R$2,00 para o Centro")
    void testeDescontoBairroAplica() {
        IFormaDescontoTaxaEntrega strategy = new FormaDescontoTaxaPorBairro();
        Pedido pedido = criarPedidoComBairroETipoCliente("Centro", "Bronze");

        assertTrue(strategy.seAplica(pedido));
        CupomDescontoEntrega cupom = strategy.calcularDesconto(pedido);
        assertEquals(2.0, cupom.getValorDesconto());
        assertEquals("Desconto Taxa Bairro", cupom.getNomeMetodo());
    }

    @Test
    @DisplayName("Desconto Por Bairro - Nao deve aplicar desconto para bairro desconhecido")
    void testeDescontoBairroNaoAplica() {
        IFormaDescontoTaxaEntrega strategy = new FormaDescontoTaxaPorBairro();
        Pedido pedido = criarPedidoComBairroETipoCliente("Jardim Camburi", "Bronze");

        assertFalse(strategy.seAplica(pedido));
        CupomDescontoEntrega cupom = strategy.calcularDesconto(pedido);
        assertEquals(0.0, cupom.getValorDesconto());
    }

    // --- FormaDescontoTaxaPorTipoCliente ---

    @Test
    @DisplayName("Desconto Tipo Cliente - Deve aplicar desconto de R$3,00 para cliente Ouro")
    void testeDescontoTipoClienteAplica() {
        IFormaDescontoTaxaEntrega strategy = new FormaDescontoTaxaPorTipoCliente();
        Pedido pedido = criarPedidoComBairroETipoCliente("Centro", "Ouro");

        assertTrue(strategy.seAplica(pedido));
        CupomDescontoEntrega cupom = strategy.calcularDesconto(pedido);
        assertEquals(3.0, cupom.getValorDesconto());
        assertEquals("Desconto Tipo Cliente", cupom.getNomeMetodo());
    }

    @Test
    @DisplayName("Desconto Tipo Cliente - Nao deve aplicar desconto para tipo desconhecido")
    void testeDescontoTipoClienteNaoAplica() {
        IFormaDescontoTaxaEntrega strategy = new FormaDescontoTaxaPorTipoCliente();
        Pedido pedido = criarPedidoComBairroETipoCliente("Centro", "Desconhecido");

        assertFalse(strategy.seAplica(pedido));
        CupomDescontoEntrega cupom = strategy.calcularDesconto(pedido);
        assertEquals(0.0, cupom.getValorDesconto());
    }

    // --- FormaDescontoTipoItem ---

    @Test
    @DisplayName("Desconto Tipo Item - Deve aplicar desconto de R$5,00 para item Alimentacao")
    void testeDescontoTipoItemAplica() {
        IFormaDescontoTaxaEntrega strategy = new FormaDescontoTipoItem();
        Pedido pedido = criarPedidoComItemDeTipo("Alimentacao");

        assertTrue(strategy.seAplica(pedido));
        CupomDescontoEntrega cupom = strategy.calcularDesconto(pedido);
        assertEquals(5.0, cupom.getValorDesconto());
        assertEquals("Desconto Tipo de Item", cupom.getNomeMetodo());
    }

    @Test
    @DisplayName("Desconto Tipo Item - Deve acumular descontos para tipos diferentes e somar R$7,00")
    void testeDescontoTipoItemMultiplos() {
        IFormaDescontoTaxaEntrega strategy = new FormaDescontoTipoItem();
        Pedido pedido = criarPedidoComItemDeTipo("Alimentacao"); // R$ 5,00
        pedido.adicionarItem(new Item("Lapis", 1, 2.0, "Educacao")); // R$ 2,00
        pedido.adicionarItem(new Item("Salgado", 1, 5.0, "Alimentacao")); // repetido, não conta de novo

        assertTrue(strategy.seAplica(pedido));
        CupomDescontoEntrega cupom = strategy.calcularDesconto(pedido);
        assertEquals(7.0, cupom.getValorDesconto());
    }

    @Test
    @DisplayName("Desconto Tipo Item - Nao deve aplicar desconto se nao houver tipo valido")
    void testeDescontoTipoItemNaoAplica() {
        IFormaDescontoTaxaEntrega strategy = new FormaDescontoTipoItem();
        Pedido pedido = criarPedidoComItemDeTipo("Outro");

        assertFalse(strategy.seAplica(pedido));
        CupomDescontoEntrega cupom = strategy.calcularDesconto(pedido);
        assertEquals(0.0, cupom.getValorDesconto());
    }

    // --- FormaDescontoValorPedido ---

    @Test
    @DisplayName("Desconto Valor Pedido - Deve aplicar desconto de R$5,00 para pedido acima de 200")
    void testeDescontoValorPedidoAplica() {
        IFormaDescontoTaxaEntrega strategy = new FormaDescontoValorPedido();
        Pedido pedido = criarPedidoComValorDe(201.0);

        assertTrue(strategy.seAplica(pedido));
        CupomDescontoEntrega cupom = strategy.calcularDesconto(pedido);
        assertEquals(5.0, cupom.getValorDesconto());
        assertEquals("Desconto Valor Pedido", cupom.getNomeMetodo());
    }

    @Test
    @DisplayName("Desconto Valor Pedido - Nao deve aplicar desconto para pedido de exatos 200")
    void testeDescontoValorPedidoExatoNaoAplica() {
        IFormaDescontoTaxaEntrega strategy = new FormaDescontoValorPedido();
        Pedido pedido = criarPedidoComValorDe(200.0);

        assertFalse(strategy.seAplica(pedido));
        CupomDescontoEntrega cupom = strategy.calcularDesconto(pedido);
        assertEquals(0.0, cupom.getValorDesconto());
    }
}
