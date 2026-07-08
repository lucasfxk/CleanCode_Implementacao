package com.ufes.delivery.domain.entity.state;

import com.ufes.delivery.domain.entity.StatusPedido;

public class EstadoEmPreparo implements EstadoPedido {
    @Override
    public EstadoPedido atualizarStatus(StatusPedido novoStatus) {
        if (novoStatus == StatusPedido.SAIU_PARA_ENTREGA) return new EstadoSaiuParaEntrega();
        if (novoStatus == StatusPedido.CANCELADO) return new EstadoCancelado();
        
        throw new IllegalStateException("Transicao invalida: " + getStatus().name() + " -> " + novoStatus.name());
    }

    @Override
    public StatusPedido getStatus() {
        return StatusPedido.EM_PREPARO;
    }
}
