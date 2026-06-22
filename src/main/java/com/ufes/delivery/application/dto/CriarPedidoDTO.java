package com.ufes.delivery.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CriarPedidoDTO {
    private String nomeCliente;
    private String tipoCliente;
    private double fidelidadeCliente;
    private String logradouroCliente;
    private String bairroCliente;
    private String cidadeCliente;
    private LocalDateTime dataPedido;
    private List<ItemDTO> itens;

    public CriarPedidoDTO(String nomeCliente, String tipoCliente, double fidelidadeCliente,
                          String logradouroCliente, String bairroCliente, String cidadeCliente,
                          LocalDateTime dataPedido, List<ItemDTO> itens) {
        this.nomeCliente = nomeCliente;
        this.tipoCliente = tipoCliente;
        this.fidelidadeCliente = fidelidadeCliente;
        this.logradouroCliente = logradouroCliente;
        this.bairroCliente = bairroCliente;
        this.cidadeCliente = cidadeCliente;
        this.dataPedido = dataPedido;
        this.itens = itens;
    }

    public String getNomeCliente() { return nomeCliente; }
    public String getTipoCliente() { return tipoCliente; }
    public double getFidelidadeCliente() { return fidelidadeCliente; }
    public String getLogradouroCliente() { return logradouroCliente; }
    public String getBairroCliente() { return bairroCliente; }
    public String getCidadeCliente() { return cidadeCliente; }
    public LocalDateTime getDataPedido() { return dataPedido; }
    public List<ItemDTO> getItens() { return itens; }
}
