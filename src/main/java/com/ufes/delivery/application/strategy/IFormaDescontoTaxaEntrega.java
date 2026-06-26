package com.ufes.delivery.application.strategy;

import com.ufes.delivery.domain.entity.CupomDescontoEntrega;
import com.ufes.delivery.domain.entity.Pedido;

public interface IFormaDescontoTaxaEntrega {
    CupomDescontoEntrega calcularDesconto(Pedido pedido);

    boolean seAplica(Pedido pedido);
}
