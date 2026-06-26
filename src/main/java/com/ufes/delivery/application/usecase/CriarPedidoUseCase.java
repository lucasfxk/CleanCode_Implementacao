package com.ufes.delivery.application.usecase;

import com.ufes.delivery.application.dto.CriarPedidoDTO;
import com.ufes.delivery.application.dto.ItemDTO;
import com.ufes.delivery.application.port.in.CriarPedidoInputPort;
import com.ufes.delivery.application.port.out.PedidoRepositoryOutputPort;
import com.ufes.delivery.domain.entity.Cliente;
import com.ufes.delivery.domain.entity.Item;
import com.ufes.delivery.domain.entity.Pedido;

import java.util.Objects;

public class CriarPedidoUseCase implements CriarPedidoInputPort {
    private final PedidoRepositoryOutputPort pedidoRepository;
    private final double taxaEntregaPadrao;

    public CriarPedidoUseCase(PedidoRepositoryOutputPort pedidoRepository, double taxaEntregaPadrao) {
        this.pedidoRepository = Objects.requireNonNull(pedidoRepository, "Repositorio de pedidos nao pode ser nulo");
        this.taxaEntregaPadrao = taxaEntregaPadrao;
    }

    @Override
    public Pedido executar(CriarPedidoDTO dto) {
        Objects.requireNonNull(dto, "Dados do pedido nao podem ser nulos");

        Cliente cliente = new Cliente(
                dto.getNomeCliente(),
                dto.getTipoCliente(),
                dto.getFidelidadeCliente(),
                dto.getLogradouroCliente(),
                dto.getBairroCliente(),
                dto.getCidadeCliente()
        );

        Pedido pedido = new Pedido(dto.getDataPedido(), cliente, taxaEntregaPadrao);

        for (ItemDTO itemDTO : dto.getItens()) {
            Item item = new Item(
                    itemDTO.getNome(),
                    itemDTO.getQuantidade(),
                    itemDTO.getValorUnitario(),
                    itemDTO.getTipo()
            );
            pedido.adicionarItem(item);
        }

        pedidoRepository.salvar(pedido);

        return pedido;
    }
}
