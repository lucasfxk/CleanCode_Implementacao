package com.ufes.delivery.application.port.in;

import com.ufes.delivery.domain.entity.Pedido;
import java.time.LocalDateTime;

public interface AplicarCupomInputPort {
    void executar(String pedidoId, String codigoCupom, LocalDateTime dataHoraAplicacao);
}
