package com.ufes.delivery.domain.entity.state;

import com.ufes.delivery.domain.entity.StatusPedido;

public class EstadoEntregue implements EstadoPedido {
    @Override
    public EstadoPedido atualizarStatus(StatusPedido novoStatus) {
        throw new IllegalStateException("Nao e possivel alterar o status de um pedido ENTREGUE.");
    }

    @Override
    public StatusPedido getStatus() {
        return StatusPedido.ENTREGUE;
    }
}
