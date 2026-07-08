package com.ufes.delivery.domain.entity.state;

import com.ufes.delivery.domain.entity.StatusPedido;

public class EstadoConfirmado implements EstadoPedido {
    @Override
    public EstadoPedido atualizarStatus(StatusPedido novoStatus) {
        if (novoStatus == StatusPedido.EM_PREPARO) return new EstadoEmPreparo();
        if (novoStatus == StatusPedido.CANCELADO) return new EstadoCancelado();
        
        throw new IllegalStateException("Transicao invalida: " + getStatus().name() + " -> " + novoStatus.name());
    }

    @Override
    public StatusPedido getStatus() {
        return StatusPedido.CONFIRMADO;
    }
}
