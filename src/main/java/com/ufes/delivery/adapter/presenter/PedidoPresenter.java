package com.ufes.delivery.adapter.presenter;

import com.ufes.delivery.application.dto.PedidoResumoDTO;
import com.ufes.delivery.domain.entity.CupomDescontoEntrega;
import com.ufes.delivery.domain.entity.CupomDescontoPedido;
import com.ufes.delivery.domain.entity.Item;
import com.ufes.delivery.domain.entity.Pedido;
import com.ufes.delivery.application.port.out.PresenterOutputPort;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PedidoPresenter implements PresenterOutputPort {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    public PedidoResumoDTO toResumoDTO(Pedido pedido) {
        List<String> itensDescricao = pedido.getItens().stream()
                .map(item -> item.getNome() + " (" + item.getQuantidade() + "x R$" +
                        String.format("%.2f", item.getValorUnitario()) + ") = R$" +
                        String.format("%.2f", item.valorTotal()))
                .collect(Collectors.toList());

        List<String> cuponsEntrega = pedido.getCupomDescontoEntrega().stream()
                .map(c -> c.getNomeMetodo() + ": R$" + String.format("%.2f", c.getValorDesconto()))
                .collect(Collectors.toList());

        Optional<CupomDescontoPedido> cupomPedido = pedido.getCupomAplicado();
        String cupomPedidoStr = cupomPedido.map(c -> c.getCodigo() + " (" + c.getPercentual() + "%)").orElse("Nenhum");

        return new PedidoResumoDTO(
                pedido.getId(),
                pedido.getData(),
                pedido.getCliente().getNome(),
                pedido.getCliente().getTipo(),
                pedido.getCliente().getBairro(),
                itensDescricao,
                pedido.getValorPedido(),
                pedido.getTaxaEntrega(),
                pedido.getTotalDescontosTaxaEntrega(),
                pedido.getTaxaEntregaComDesconto(),
                cuponsEntrega,
                cupomPedidoStr,
                pedido.calcularValorTotal(),
                pedido.getStatus().name()
        );
    }

    @Override
    public String formatarPedido(Pedido pedido) {
        PedidoResumoDTO resumo = toResumoDTO(pedido);
        StringBuilder sb = new StringBuilder();

        sb.append("\n============================================================\n");
        sb.append("                    RESUMO DO PEDIDO                        \n");
        sb.append("============================================================\n");
        sb.append(String.format("  Data/Hora:   %s%n", resumo.getData().format(FORMATTER)));
        sb.append(String.format("  Cliente:     %s (%s)%n", resumo.getNomeCliente(), resumo.getTipoCliente()));
        sb.append(String.format("  Bairro:      %s%n", resumo.getBairroCliente()));
        sb.append("------------------------------------------------------------\n");
        sb.append("  ITENS:\n");

        for (String item : resumo.getItensDescricao()) {
            sb.append(String.format("    - %s%n", item));
        }

        sb.append("------------------------------------------------------------\n");
        sb.append(String.format("  Valor dos Itens:            R$ %8.2f%n", resumo.getValorPedido()));
        sb.append(String.format("  Taxa de Entrega:            R$ %8.2f%n", resumo.getTaxaEntrega()));

        if (!resumo.getCuponsDescontoEntregaDescricao().isEmpty()) {
            sb.append("  Descontos na Entrega:\n");
            for (String cupom : resumo.getCuponsDescontoEntregaDescricao()) {
                sb.append(String.format("    - %s%n", cupom));
            }
            sb.append(String.format("  Total Descontos Entrega:   -R$ %8.2f%n", resumo.getTotalDescontosEntrega()));
            sb.append(String.format("  Taxa Entrega c/ Desconto:   R$ %8.2f%n", resumo.getTaxaEntregaComDesconto()));
        }

        sb.append(String.format("  Cupom do Pedido:            %s%n", resumo.getCupomPedidoAplicado()));
        sb.append("------------------------------------------------------------\n");
        sb.append(String.format("  VALOR TOTAL:                R$ %8.2f%n", resumo.getValorTotal()));
        sb.append("============================================================\n");

        return sb.toString();
    }
}
