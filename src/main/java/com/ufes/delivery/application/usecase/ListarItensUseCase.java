package com.ufes.delivery.application.usecase;

import com.ufes.delivery.application.port.in.ListarItensInputPort;
import com.ufes.delivery.application.port.out.ItemRepositoryOutputPort;
import com.ufes.delivery.domain.entity.Item;

import java.util.List;
import java.util.Objects;

public class ListarItensUseCase implements ListarItensInputPort {
    private final ItemRepositoryOutputPort itemRepository;

    public ListarItensUseCase(ItemRepositoryOutputPort itemRepository) {
        this.itemRepository = Objects.requireNonNull(itemRepository, "Repositorio de itens nao pode ser nulo");
    }

    @Override
    public List<Item> listarTodos() {
        return itemRepository.listarTodos();
    }
}
