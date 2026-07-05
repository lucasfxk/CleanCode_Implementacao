package com.ufes.delivery.domain.entity;

import java.util.UUID;

public class Cliente {
    private final String id;
    private String nome;
    private String tipo;
    private double fidelidade;
    private String logradouro;
    private String bairro;
    private String cidade;

    public Cliente(String nome, String tipo, double fidelidade, String logradouro, String bairro, String cidade) {
        validarTextoObrigatorio(nome, "Nome do cliente nao pode ser vazio");
        validarTextoObrigatorio(tipo, "Tipo do cliente nao pode ser vazio");
        validarTextoObrigatorio(logradouro, "Logradouro do cliente nao pode ser vazio");
        validarTextoObrigatorio(bairro, "Bairro do cliente nao pode ser vazio");
        validarTextoObrigatorio(cidade, "Cidade do cliente nao pode ser vazia");

        if (fidelidade < 0) {
            throw new IllegalArgumentException("Fidelidade do cliente nao pode ser negativa");
        }

        this.id = UUID.randomUUID().toString();
        this.nome = nome;
        this.tipo = tipo;
        this.fidelidade = fidelidade;
        this.logradouro = logradouro;
        this.bairro = bairro;
        this.cidade = cidade;
    }

    private Cliente(String id, String nome, String tipo, double fidelidade, String logradouro, String bairro,
            String cidade) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.fidelidade = fidelidade;
        this.logradouro = logradouro;
        this.bairro = bairro;
        this.cidade = cidade;
    }

    public static Cliente reconstruir(String id, String nome, String tipo, double fidelidade, String logradouro,
            String bairro, String cidade) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id do cliente deve ser informado para reconstrucao");
        }
        validarTextoObrigatorioEstatico(nome, "Nome do cliente nao pode ser vazio");
        validarTextoObrigatorioEstatico(tipo, "Tipo do cliente nao pode ser vazio");
        validarTextoObrigatorioEstatico(logradouro, "Logradouro do cliente nao pode ser vazio");
        validarTextoObrigatorioEstatico(bairro, "Bairro do cliente nao pode ser vazio");
        validarTextoObrigatorioEstatico(cidade, "Cidade do cliente nao pode ser vazia");

        if (fidelidade < 0) {
            throw new IllegalArgumentException("Fidelidade do cliente nao pode ser negativa");
        }

        return new Cliente(id, nome, tipo, fidelidade, logradouro, bairro, cidade);
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getTipo() {
        return tipo;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public String getBairro() {
        return bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public double getFidelidade() {
        return fidelidade;
    }

    public void setFidelidade(double fidelidade) {
        if (fidelidade < 0) {
            throw new IllegalArgumentException("Fidelidade do cliente nao pode ser negativa");
        }

        this.fidelidade = fidelidade;
    }

    private void validarTextoObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    private static void validarTextoObrigatorioEstatico(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    @Override
    public String toString() {
        return "Cliente{"
                + "id='" + id + '\''
                + ", nome='" + nome + '\''
                + ", tipo='" + tipo + '\''
                + ", fidelidade=" + fidelidade
                + ", logradouro='" + logradouro + '\''
                + ", bairro='" + bairro + '\''
                + ", cidade='" + cidade + '\''
                + "}";
    }
}