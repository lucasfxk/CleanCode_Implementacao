package com.ufes.delivery.application.port.out;

import com.ufes.delivery.domain.entity.Pedido;
import java.util.List;
import java.util.Optional;

public interface PedidoRepositoryOutputPort {
    void salvar(Pedido pedido);
    Optional<Pedido> buscarPorData(java.time.LocalDateTime data);
    List<Pedido> listarTodos();
}
