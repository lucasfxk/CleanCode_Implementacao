package com.ufes.delivery.application.port.in;

import com.ufes.delivery.domain.entity.Item;
import java.util.List;

public interface ListarItensInputPort {
    List<Item> listarTodos();
}
