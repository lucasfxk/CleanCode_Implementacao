package com.ufes.delivery.application.port.out;

import com.ufes.delivery.application.dto.PedidoResumoDTO;
import com.ufes.delivery.domain.entity.Pedido;

public interface PresenterOutputPort {
    PedidoResumoDTO toResumoDTO(Pedido pedido);
    String formatarPedido(Pedido pedido);
}
