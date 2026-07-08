package com.ufes.delivery.adapter.controller;

import com.ufes.delivery.application.dto.ItemDTO;
import com.ufes.delivery.application.port.in.CadastrarItemInputPort;
import com.ufes.delivery.application.port.in.ListarItensInputPort;
import com.ufes.delivery.domain.entity.Item;

import java.util.List;
import java.util.stream.Collectors;

public class ItemController {

    private final CadastrarItemInputPort cadastrarItem;
    private final ListarItensInputPort listarItens;

    public ItemController(CadastrarItemInputPort cadastrarItem, ListarItensInputPort listarItens) {
        this.cadastrarItem = cadastrarItem;
        this.listarItens = listarItens;
    }

    public void cadastrarItem(String nome, double valorUnitario, String tipo) {
        cadastrarItem.executar(nome, valorUnitario, tipo);
    }

    public List<ItemDTO> listarTodos() {
        return listarItens.listarTodos().stream()
                .map(item -> new ItemDTO(item.getId(), item.getNome(), item.getQuantidade(), item.getValorUnitario(), item.getTipo()))
                .collect(Collectors.toList());
    }
}
