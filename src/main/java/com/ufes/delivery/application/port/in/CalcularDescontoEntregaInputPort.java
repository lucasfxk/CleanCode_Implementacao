package com.ufes.delivery.application.port.in;

import com.ufes.delivery.domain.entity.Pedido;

public interface CalcularDescontoEntregaInputPort {
    void executar(String pedidoId);
}
