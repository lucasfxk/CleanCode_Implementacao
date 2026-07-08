package com.ufes.delivery.domain.entity.state;

import com.ufes.delivery.domain.entity.StatusPedido;

public interface EstadoPedido {
    /**
     * Tenta avancar para o proximo status.
     *
     * @param novoStatus o status desejado
     * @return a nova instancia de EstadoPedido correspondente ao novo status
     * @throws IllegalStateException se a transicao for invalida
     */
    EstadoPedido atualizarStatus(StatusPedido novoStatus);

    /**
     * @return o identificador (Enum) correspondente a este estado
     */
    StatusPedido getStatus();
}
