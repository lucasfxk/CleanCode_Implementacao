package com.ufes.delivery.domain.entity;

/**
 * Ciclo de vida de um pedido de delivery.
 *
 * Clean Architecture — Domain Layer:
 *   Enum puro de negocio, sem dependencia de nenhuma camada externa.
 */
public enum StatusPedido {
    CRIADO("Pedido criado e aguardando confirmacao"),
    CONFIRMADO("Pedido confirmado pelo restaurante"),
    EM_PREPARO("Pedido em preparo"),
    SAIU_PARA_ENTREGA("Pedido saiu para entrega"),
    ENTREGUE("Pedido entregue ao cliente"),
    CANCELADO("Pedido cancelado");

    private final String descricao;

    StatusPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return name() + " — " + descricao;
    }
}
