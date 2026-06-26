package com.ufes.delivery.application.port.in;

import com.ufes.delivery.application.dto.CriarPedidoDTO;
import com.ufes.delivery.domain.entity.Pedido;

public interface CriarPedidoInputPort {
    Pedido executar(CriarPedidoDTO dto);
}
