package com.ufes.delivery.domain.entity.state;

import com.ufes.delivery.domain.entity.StatusPedido;

public class EstadoCriado implements EstadoPedido {
    @Override
    public EstadoPedido atualizarStatus(StatusPedido novoStatus) {
        if (novoStatus == StatusPedido.CONFIRMADO) return new EstadoConfirmado();
        if (novoStatus == StatusPedido.CANCELADO) return new EstadoCancelado();
        
        throw new IllegalStateException("Transicao invalida: " + getStatus().name() + " -> " + novoStatus.name());
    }

    @Override
    public StatusPedido getStatus() {
        return StatusPedido.CRIADO;
    }
}
