package com.ufes.delivery.adapter.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.ufes.delivery.application.dto.PedidoResumoDTO;
import com.ufes.delivery.application.port.in.BuscarPedidoInputPort;
import com.ufes.delivery.application.port.out.PresenterOutputPort;
import com.ufes.delivery.domain.entity.Pedido;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PedidoWebController implements HttpHandler {

    private final BuscarPedidoInputPort buscarPedidoUseCase;
    private final PresenterOutputPort presenter;

    public PedidoWebController(BuscarPedidoInputPort buscarPedidoUseCase, PresenterOutputPort presenter) {
        this.buscarPedidoUseCase = buscarPedidoUseCase;
        this.presenter = presenter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            List<Pedido> pedidos = buscarPedidoUseCase.listarTodos();
            List<PedidoResumoDTO> dtos = pedidos.stream().map(presenter::toResumoDTO).toList();
            
            String json = toJson(dtos);
            
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }

    // Conversor manual super leve para nao depender de bibliotecas externas (Jackson/Gson) na apresentacao.
    private String toJson(List<PedidoResumoDTO> dtos) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < dtos.size(); i++) {
            PedidoResumoDTO dto = dtos.get(i);
            sb.append("  {\n");
            sb.append("    \"id\": \"").append(dto.getId()).append("\",\n");
            sb.append("    \"status\": \"").append(dto.getStatus()).append("\",\n");
            sb.append("    \"cliente\": \"").append(dto.getNomeCliente()).append("\",\n");
            sb.append("    \"bairro\": \"").append(dto.getBairroCliente()).append("\",\n");
            sb.append("    \"valorPedido\": ").append(dto.getValorPedido()).append(",\n");
            sb.append("    \"taxaEntrega\": ").append(dto.getTaxaEntregaComDesconto()).append(",\n");
            sb.append("    \"valorTotal\": ").append(dto.getValorTotal()).append("\n");
            sb.append("  }");
            if (i < dtos.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }
}
