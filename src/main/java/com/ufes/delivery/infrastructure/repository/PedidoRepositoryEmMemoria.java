package com.ufes.delivery.infrastructure.repository;

import com.ufes.delivery.application.port.out.PedidoRepositoryOutputPort;
import com.ufes.delivery.domain.entity.Pedido;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PedidoRepositoryEmMemoria implements PedidoRepositoryOutputPort {
    private List<Pedido> pedidos = new ArrayList<>();

    @Override
    public void salvar(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido nao pode ser nulo");
        }
        pedidos.add(pedido);
    }

    @Override
    public Optional<Pedido> buscarPorId(String id) {
        return pedidos.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Pedido> buscarPorData(LocalDateTime data) {
        return pedidos.stream()
                .filter(p -> p.getData().equals(data))
                .findFirst();
    }

    @Override
    public List<Pedido> listarTodos() {
        return List.copyOf(pedidos);
    }
}
