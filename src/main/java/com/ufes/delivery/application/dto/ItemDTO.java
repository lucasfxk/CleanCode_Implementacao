package com.ufes.delivery.application.dto;

public class ItemDTO {
    private String id;
    private String nome;
    private int quantidade;
    private double valorUnitario;
    private String tipo;

    public ItemDTO(String id, String nome, int quantidade, double valorUnitario, String tipo) {
        this.id = id;
        this.nome = nome;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.tipo = tipo;
    }

    public ItemDTO(String nome, int quantidade, double valorUnitario, String tipo) {
        this.id = null;
        this.nome = nome;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.tipo = tipo;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public double getValorUnitario() {
        return valorUnitario;
    }

    public String getTipo() {
        return tipo;
    }
}
