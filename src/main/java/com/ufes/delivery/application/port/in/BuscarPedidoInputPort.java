package com.ufes.delivery.application.port.in;

import com.ufes.delivery.domain.entity.Pedido;
import java.util.List;

/**
 * Input Port para busca de pedidos.
 */
public interface BuscarPedidoInputPort {
    List<Pedido> listarTodos();
    Pedido buscarPorId(String id);
}
