package com.ufes.delivery.application.port.out;

import com.ufes.delivery.domain.entity.Item;
import java.util.List;

public interface ItemRepositoryOutputPort {
    void salvar(Item item);
    List<Item> listarTodos();
}
