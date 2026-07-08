package com.ufes.delivery.application.usecase;

import com.ufes.delivery.application.port.in.CadastrarItemInputPort;
import com.ufes.delivery.application.port.out.ItemRepositoryOutputPort;
import com.ufes.delivery.domain.entity.Item;

import java.util.Objects;

public class CadastrarItemUseCase implements CadastrarItemInputPort {
    private final ItemRepositoryOutputPort itemRepository;

    public CadastrarItemUseCase(ItemRepositoryOutputPort itemRepository) {
        this.itemRepository = Objects.requireNonNull(itemRepository, "Repositorio de itens nao pode ser nulo");
    }

    @Override
    public Item executar(String nome, double valorUnitario, String tipo) {
        // A quantidade no catalogo por padrao é 1, servindo apenas de template.
        Item item = new Item(nome, 1, valorUnitario, tipo);
        itemRepository.salvar(item);
        return item;
    }
}
