package com.ufes.delivery.application.usecase;

import com.ufes.delivery.application.port.in.CalcularDescontoEntregaInputPort;
import com.ufes.delivery.application.strategy.IFormaDescontoTaxaEntrega;
import com.ufes.delivery.domain.entity.CupomDescontoEntrega;
import com.ufes.delivery.domain.entity.Pedido;

import java.util.List;
import java.util.Objects;

public class CalcularDescontoEntregaUseCase implements CalcularDescontoEntregaInputPort {
    private final List<IFormaDescontoTaxaEntrega> metodosDeDesconto;

    public CalcularDescontoEntregaUseCase(List<IFormaDescontoTaxaEntrega> metodosDeDesconto) {
        this.metodosDeDesconto = Objects.requireNonNull(metodosDeDesconto,
                "Lista de metodos de desconto nao pode ser nula");
    }

    @Override
    public void executar(Pedido pedido) {
        Objects.requireNonNull(pedido, "Pedido nao pode ser nulo");

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
    }
}
