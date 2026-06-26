package com.ufes.delivery.adapter.ui;

import com.ufes.delivery.application.port.out.NotificacaoOutputPort;
import com.ufes.delivery.domain.entity.Pedido;
import com.ufes.delivery.domain.entity.StatusPedido;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * Implementacao Swing do Output Port de notificacao.
 *
 * Clean Architecture — Infrastructure/Adapter Layer:
 *   Implementa a interface definida em Application sem que nenhum
 *   Use Case saiba que existe uma UI.
 *
 * Padrao Observer:
 *   Esta classe e o "Observer" concreto. O Use Case (Subject) a chama
 *   via porta de saida sempre que o status muda.
 *
 * SRP: responsabilidade unica — entregar notificacoes de status para a UI.
 */
public class NotificacaoSwing implements NotificacaoOutputPort {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    /** Callback fornecido pela UI para receber a mensagem formatada. */
    private final Consumer<String> destinoMensagem;

    public NotificacaoSwing(Consumer<String> destinoMensagem) {
        if (destinoMensagem == null) {
            throw new IllegalArgumentException("Destino de mensagem nao pode ser nulo");
        }
        this.destinoMensagem = destinoMensagem;
    }

    @Override
    public void notificarMudancaStatus(Pedido pedido, StatusPedido anterior, StatusPedido novo) {
        String msg = String.format(
                "[%s] %s: %s → %s%n",
                LocalDateTime.now().format(FMT),
                pedido.getCliente().getNome(),
                anterior.name(),
                novo.name()
        );
        // Garante execucao na Event Dispatch Thread do Swing
        SwingUtilities.invokeLater(() -> destinoMensagem.accept(msg));
    }
}
