package com.ufes.delivery.application.usecase;

import com.ufes.delivery.application.port.out.NotificacaoOutputPort;
import com.ufes.delivery.application.port.out.PedidoRepositoryOutputPort;
import com.ufes.delivery.domain.entity.*;
import com.ufes.delivery.infrastructure.notification.NotificacaoConsole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
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

        Cliente cliente = new Cliente("Jose", "Prata", 2, "Rua X", "Norte", "Castelo");
        pedido = new Pedido(LocalDateTime.now(), cliente, 8.0);

        PedidoRepositoryOutputPort mockRepo = new PedidoRepositoryOutputPort() {
            @Override public void salvar(Pedido p) {}
            @Override public Optional<Pedido> buscarPorId(String id) {
                return id.equals(pedido.getId()) ? Optional.of(pedido) : Optional.empty();
            }
            @Override public Optional<Pedido> buscarPorData(LocalDateTime data) { return Optional.empty(); }
            @Override public java.util.List<Pedido> listarTodos() { return java.util.Collections.emptyList(); }
        };

        useCase = new AtualizarStatusPedidoUseCase(notificacaoSpy, mockRepo);
    }

    @Test
    @DisplayName("Deve atualizar status e notificar corretamente")
    void deveAtualizarStatusENotificar() {
        useCase.executar(pedido.getId(), StatusPedido.CONFIRMADO);

        assertEquals(StatusPedido.CONFIRMADO, pedido.getStatus());
        assertEquals(StatusPedido.CRIADO, statusAnteriorCapturado);
        assertEquals(StatusPedido.CONFIRMADO, novoStatusCapturado);
    }

    @Test
    @DisplayName("Transicao invalida deve lancar excecao sem alterar status")
    void deveRejeitarTransicaoInvalidaSemAlterar() {
        assertThrows(IllegalStateException.class,
                () -> useCase.executar(pedido.getId(), StatusPedido.SAIU_PARA_ENTREGA));

        // Status nao foi alterado
        assertEquals(StatusPedido.CRIADO, pedido.getStatus());
    }

    @Test
    @DisplayName("Notificacao nula no construtor deve lancar NullPointerException")
    void deveRejeitarNotificacaoNula() {
        assertThrows(NullPointerException.class,
                () -> new AtualizarStatusPedidoUseCase(null, null));
    }
}
