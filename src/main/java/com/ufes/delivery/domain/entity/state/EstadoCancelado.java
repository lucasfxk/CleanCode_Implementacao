package com.ufes.delivery.domain.entity.state;

import com.ufes.delivery.domain.entity.StatusPedido;

public class EstadoCancelado implements EstadoPedido {
    @Override
    public EstadoPedido atualizarStatus(StatusPedido novoStatus) {
        throw new IllegalStateException("Nao e possivel alterar o status de um pedido CANCELADO.");
    }

    @Override
    public StatusPedido getStatus() {
        return StatusPedido.CANCELADO;
    }
}
