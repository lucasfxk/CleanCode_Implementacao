package com.ufes.delivery.application.usecase;

import com.ufes.delivery.application.port.out.NotificacaoOutputPort;
import com.ufes.delivery.domain.entity.*;
import com.ufes.delivery.infrastructure.notification.NotificacaoConsole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AtualizarStatusPedidoUseCase — Application")
class AtualizarStatusPedidoUseCaseTest {

    private AtualizarStatusPedidoUseCase useCase;
    private Pedido pedido;

    // Captura o status notificado para asserção
    private StatusPedido statusAnteriorCapturado;
    private StatusPedido novoStatusCapturado;

    @BeforeEach
    void setUp() {
        NotificacaoOutputPort notificacaoSpy = (p, anterior, novo) -> {
            statusAnteriorCapturado = anterior;
            novoStatusCapturado = novo;
        };
        useCase = new AtualizarStatusPedidoUseCase(notificacaoSpy);

        Cliente cliente = new Cliente("Jose", "Prata", 2, "Rua X", "Norte", "Castelo");
        pedido = new Pedido(LocalDateTime.now(), cliente, 8.0);
    }

    @Test
    @DisplayName("Deve atualizar status e notificar corretamente")
    void deveAtualizarStatusENotificar() {
        useCase.executar(pedido, StatusPedido.CONFIRMADO);

        assertEquals(StatusPedido.CONFIRMADO, pedido.getStatus());
        assertEquals(StatusPedido.CRIADO, statusAnteriorCapturado);
        assertEquals(StatusPedido.CONFIRMADO, novoStatusCapturado);
    }

    @Test
    @DisplayName("Transicao invalida deve lancar excecao sem alterar status")
    void deveRejeitarTransicaoInvalidaSemAlterar() {
        assertThrows(IllegalStateException.class,
                () -> useCase.executar(pedido, StatusPedido.SAIU_PARA_ENTREGA));

        // Status nao foi alterado
        assertEquals(StatusPedido.CRIADO, pedido.getStatus());
    }

    @Test
    @DisplayName("Notificacao nula no construtor deve lancar NullPointerException")
    void deveRejeitarNotificacaoNula() {
        assertThrows(NullPointerException.class,
                () -> new AtualizarStatusPedidoUseCase(null));
    }
}
