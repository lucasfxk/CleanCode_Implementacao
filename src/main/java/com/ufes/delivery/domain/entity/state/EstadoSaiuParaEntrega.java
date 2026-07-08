package com.ufes.delivery.domain.entity.state;

import com.ufes.delivery.domain.entity.StatusPedido;

public class EstadoSaiuParaEntrega implements EstadoPedido {
    @Override
    public EstadoPedido atualizarStatus(StatusPedido novoStatus) {
        if (novoStatus == StatusPedido.ENTREGUE) return new EstadoEntregue();
        
        throw new IllegalStateException("Transicao invalida: " + getStatus().name() + " -> " + novoStatus.name());
    }

    @Override
    public StatusPedido getStatus() {
        return StatusPedido.SAIU_PARA_ENTREGA;
    }
}
