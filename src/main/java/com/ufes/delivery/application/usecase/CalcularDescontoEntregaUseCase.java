package com.ufes.delivery.application.usecase;

import com.ufes.delivery.application.port.in.CalcularDescontoEntregaInputPort;
import com.ufes.delivery.application.port.out.PedidoRepositoryOutputPort;
import com.ufes.delivery.application.strategy.IFormaDescontoTaxaEntrega;
import com.ufes.delivery.domain.entity.CupomDescontoEntrega;
import com.ufes.delivery.domain.entity.Pedido;

import java.util.List;
import java.util.Objects;

public class CalcularDescontoEntregaUseCase implements CalcularDescontoEntregaInputPort {
    private final List<IFormaDescontoTaxaEntrega> metodosDeDesconto;
    private final PedidoRepositoryOutputPort pedidoRepository;

    public CalcularDescontoEntregaUseCase(List<IFormaDescontoTaxaEntrega> metodosDeDesconto, PedidoRepositoryOutputPort pedidoRepository) {
        this.metodosDeDesconto = Objects.requireNonNull(metodosDeDesconto,
                "Lista de metodos de desconto nao pode ser nula");
        this.pedidoRepository = Objects.requireNonNull(pedidoRepository, "Repositorio de pedidos nao pode ser nulo");
    }

    @Override
    public void executar(String pedidoId) {
        Objects.requireNonNull(pedidoId, "ID do pedido nao pode ser nulo");

        Pedido pedido = pedidoRepository.buscarPorId(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido nao encontrado: " + pedidoId));

        pedido.limparCuponsDescontoEntrega();

        double limiteAplicavel = pedido.getTaxaEntrega();

        for (IFormaDescontoTaxaEntrega formaDescontoTaxaEntrega : metodosDeDesconto) {
            double totalDescontos = pedido.getTotalDescontosTaxaEntrega();

            if (formaDescontoTaxaEntrega.seAplica(pedido) && totalDescontos < limiteAplicavel) {
                CupomDescontoEntrega cupom = formaDescontoTaxaEntrega.calcularDesconto(pedido);
                double limiteRestante = limiteAplicavel - totalDescontos;
                double valorAplicado = Math.min(cupom.getValorDesconto(), limiteRestante);
                cupom.aplicar(valorAplicado);

                if (cupom.getValorDesconto() > 0) {
                    pedido.adicionarCupomDescontoEntrega(cupom);
                }
            }
        }

        pedidoRepository.salvar(pedido);
    }
}
