package com.ufes.delivery.application.port.in;

import com.ufes.delivery.domain.entity.Item;

public interface CadastrarItemInputPort {
    Item executar(String nome, double valorUnitario, String tipo);
}
