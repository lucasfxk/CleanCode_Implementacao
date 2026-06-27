package com.ufes.delivery.infrastructure.notification;

import com.ufes.delivery.application.port.out.NotificacaoOutputPort;
import com.ufes.delivery.domain.entity.Pedido;
import com.ufes.delivery.domain.entity.StatusPedido;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementacao de notificacao via console.
 *
 * Clean Architecture — Infrastructure Layer:
 *   Implementa o Output Port definido na camada Application.
 *   Pode ser substituida por NotificacaoEmail, NotificacaoPush etc.
 *   sem alterar nenhum Use Case.
 *
 * Padrao Observer:
 *   Esta classe e o "Observer" concreto acionado pelo use case (Subject)
 *   quando o estado do pedido muda.
 */
public class NotificacaoConsole implements NotificacaoOutputPort {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    public void notificarMudancaStatus(Pedido pedido, StatusPedido statusAnterior, StatusPedido novoStatus) {
        String agora = LocalDateTime.now().format(FORMATTER);
        System.out.printf("%n  [NOTIFICACAO %s]%n", agora);
        System.out.printf("  Cliente : %s%n", pedido.getCliente().getNome());
        System.out.printf("  Status  : %s --> %s%n", statusAnterior.name(), novoStatus.name());
        System.out.printf("  Info    : %s%n", novoStatus.getDescricao());
    }
}
