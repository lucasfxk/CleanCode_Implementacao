package com.ufes.delivery.application.port.out;

import com.ufes.delivery.domain.entity.CupomDescontoPedido;
import java.util.Optional;

public interface CupomRepositoryOutputPort {
    Optional<CupomDescontoPedido> buscarCupom(String codigo);
    void adicionarCupom(CupomDescontoPedido cupom);
}
