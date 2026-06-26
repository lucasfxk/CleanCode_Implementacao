package com.ufes.delivery.application.usecase;

import com.ufes.delivery.application.port.in.BuscarPedidoInputPort;
import com.ufes.delivery.application.port.out.PedidoRepositoryOutputPort;
import com.ufes.delivery.domain.entity.Pedido;

import java.util.List;
import java.util.Objects;

/**
 * Use Case: Buscar / Listar Pedidos.
 *
 * Clean Architecture — Application Layer:
 *   Delega ao Output Port a busca, sem conhecer a tecnologia de persistencia.
 */
public class BuscarPedidoUseCase implements BuscarPedidoInputPort {

    private final PedidoRepositoryOutputPort pedidoRepository;

    public BuscarPedidoUseCase(PedidoRepositoryOutputPort pedidoRepository) {
        this.pedidoRepository = Objects.requireNonNull(pedidoRepository, "Repositorio de pedidos nao pode ser nulo");
    }

    @Override
    public List<Pedido> listarTodos() {
        return pedidoRepository.listarTodos();
    }
}
