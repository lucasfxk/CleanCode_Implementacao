package com.ufes.delivery.application.strategy;

import com.ufes.delivery.domain.entity.CupomDescontoEntrega;
import com.ufes.delivery.domain.entity.Pedido;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

public class FormaDescontoTaxaPorTipoCliente implements IFormaDescontoTaxaEntrega {
    private Map<String, Double> descontosPorTipoCliente;

    public FormaDescontoTaxaPorTipoCliente() {
        descontosPorTipoCliente = new HashMap<>();

        descontosPorTipoCliente.put("Ouro", 3.00);
        descontosPorTipoCliente.put("Prata", 2.00);
        descontosPorTipoCliente.put("Bronze", 1.00);
    }

    @Override
    public CupomDescontoEntrega calcularDesconto(Pedido pedido) {
        double valorDesconto = buscarDesconto(pedido).orElse(0.0);
        return new CupomDescontoEntrega("Desconto Tipo Cliente", valorDesconto);
    }

    @Override
    public boolean seAplica(Pedido pedido) {
        return buscarDesconto(pedido).isPresent();
    }

    private Optional<Double> buscarDesconto(Pedido pedido) {
        return Optional.ofNullable(descontosPorTipoCliente.get(pedido.getCliente().getTipo()));
    }
}
