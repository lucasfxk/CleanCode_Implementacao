package com.ufes.delivery.domain.entity.state;

import com.ufes.delivery.domain.entity.StatusPedido;

public class EstadoPedidoFactory {
    public static EstadoPedido criar(StatusPedido status) {
        if (status == null) {
            throw new IllegalArgumentException("Status nao pode ser nulo");
        }
        
        return switch (status) {
            case CRIADO -> new EstadoCriado();
            case CONFIRMADO -> new EstadoConfirmado();
            case EM_PREPARO -> new EstadoEmPreparo();
            case SAIU_PARA_ENTREGA -> new EstadoSaiuParaEntrega();
            case ENTREGUE -> new EstadoEntregue();
            case CANCELADO -> new EstadoCancelado();
        };
    }
}
