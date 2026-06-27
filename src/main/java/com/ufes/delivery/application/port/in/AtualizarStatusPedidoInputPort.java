package com.ufes.delivery.application.port.in;

import com.ufes.delivery.domain.entity.Pedido;
import com.ufes.delivery.domain.entity.StatusPedido;

/**
 * Input Port para atualizacao de status de pedido.
 */
public interface AtualizarStatusPedidoInputPort {
    void executar(Pedido pedido, StatusPedido novoStatus);
}
