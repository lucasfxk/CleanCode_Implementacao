package com.ufes.delivery.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PedidoResumoDTO {
    private String id;
    private LocalDateTime data;
    private String nomeCliente;
    private String tipoCliente;
    private String bairroCliente;
    private List<String> itensDescricao;
    private double valorPedido;
    private double taxaEntrega;
    private double totalDescontosEntrega;
    private double taxaEntregaComDesconto;
    private List<String> cuponsDescontoEntregaDescricao;
    private String cupomPedidoAplicado;
    private double valorTotal;
    private String status;

    public PedidoResumoDTO(String id, LocalDateTime data, String nomeCliente, String tipoCliente, String bairroCliente,
                           List<String> itensDescricao, double valorPedido, double taxaEntrega,
                           double totalDescontosEntrega, double taxaEntregaComDesconto,
                           List<String> cuponsDescontoEntregaDescricao, String cupomPedidoAplicado,
                           double valorTotal, String status) {
        this.id = id;
        this.data = data;
        this.nomeCliente = nomeCliente;
        this.tipoCliente = tipoCliente;
        this.bairroCliente = bairroCliente;
        this.itensDescricao = itensDescricao;
        this.valorPedido = valorPedido;
        this.taxaEntrega = taxaEntrega;
        this.totalDescontosEntrega = totalDescontosEntrega;
        this.taxaEntregaComDesconto = taxaEntregaComDesconto;
        this.cuponsDescontoEntregaDescricao = cuponsDescontoEntregaDescricao;
        this.cupomPedidoAplicado = cupomPedidoAplicado;
        this.valorTotal = valorTotal;
        this.status = status;
    }

    public String getId() { return id; }
    public LocalDateTime getData() { return data; }
    public String getNomeCliente() { return nomeCliente; }
    public String getTipoCliente() { return tipoCliente; }
    public String getBairroCliente() { return bairroCliente; }
    public List<String> getItensDescricao() { return itensDescricao; }
    public double getValorPedido() { return valorPedido; }
    public double getTaxaEntrega() { return taxaEntrega; }
    public double getTotalDescontosEntrega() { return totalDescontosEntrega; }
    public double getTaxaEntregaComDesconto() { return taxaEntregaComDesconto; }
    public List<String> getCuponsDescontoEntregaDescricao() { return cuponsDescontoEntregaDescricao; }
    public String getCupomPedidoAplicado() { return cupomPedidoAplicado; }
    public double getValorTotal() { return valorTotal; }
    public String getStatus() { return status; }
}
