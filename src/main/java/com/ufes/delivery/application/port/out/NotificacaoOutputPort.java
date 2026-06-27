package com.ufes.delivery.application.port.out;

import com.ufes.delivery.domain.entity.Pedido;
import com.ufes.delivery.domain.entity.StatusPedido;

/**
 * Output Port para notificacao de mudancas de status.
 *
 * Clean Architecture — Application Layer:
 *   Interface definida aqui; implementacao fica na Infrastructure.
 *   Permite trocar o canal de notificacao (console, email, push) sem
 *   alterar nenhuma regra de negocio.
 */
public interface NotificacaoOutputPort {
    void notificarMudancaStatus(Pedido pedido, StatusPedido statusAnterior, StatusPedido novoStatus);
}
