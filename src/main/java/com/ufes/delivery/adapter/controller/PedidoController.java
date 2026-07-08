package com.ufes.delivery.adapter.controller;

import com.ufes.delivery.adapter.presenter.PedidoPresenter;
import com.ufes.delivery.application.dto.CriarPedidoDTO;
import com.ufes.delivery.application.dto.PedidoResumoDTO;
import com.ufes.delivery.application.port.in.AplicarCupomInputPort;
import com.ufes.delivery.application.port.in.CalcularDescontoEntregaInputPort;
import com.ufes.delivery.application.port.in.AtualizarStatusPedidoInputPort;
import com.ufes.delivery.application.port.in.BuscarPedidoInputPort;
import com.ufes.delivery.application.port.in.CriarPedidoInputPort;
import com.ufes.delivery.application.port.out.PresenterOutputPort;
import com.ufes.delivery.domain.entity.Pedido;
import com.ufes.delivery.domain.entity.StatusPedido;
import java.util.List;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.util.Objects;

public class PedidoController {
    private final CriarPedidoInputPort criarPedidoUseCase;
    private final CalcularDescontoEntregaInputPort calcularDescontoEntregaUseCase;
    private final AplicarCupomInputPort aplicarCupomUseCase;
    private final AtualizarStatusPedidoInputPort atualizarStatusUseCase;
    private final BuscarPedidoInputPort buscarPedidoUseCase;
    private final PresenterOutputPort presenter;

    public PedidoController(CriarPedidoInputPort criarPedidoUseCase,
                            CalcularDescontoEntregaInputPort calcularDescontoEntregaUseCase,
                            AplicarCupomInputPort aplicarCupomUseCase,
                            AtualizarStatusPedidoInputPort atualizarStatusUseCase,
                            BuscarPedidoInputPort buscarPedidoUseCase,
                            PresenterOutputPort presenter) {
        this.criarPedidoUseCase = Objects.requireNonNull(criarPedidoUseCase);
        this.calcularDescontoEntregaUseCase = Objects.requireNonNull(calcularDescontoEntregaUseCase);
        this.aplicarCupomUseCase = Objects.requireNonNull(aplicarCupomUseCase);
        this.atualizarStatusUseCase = Objects.requireNonNull(atualizarStatusUseCase);
        this.buscarPedidoUseCase = Objects.requireNonNull(buscarPedidoUseCase);
        this.presenter = Objects.requireNonNull(presenter);
    }

    public PedidoResumoDTO criarPedido(CriarPedidoDTO dto) {
        Pedido pedido = criarPedidoUseCase.executar(dto);
        return presenter.toResumoDTO(pedido);
    }

    public void calcularDescontosEntrega(String pedidoId) {
        calcularDescontoEntregaUseCase.executar(pedidoId);
    }

    public void aplicarCupom(String pedidoId, String codigoCupom, LocalDateTime dataHoraAplicacao) {
        aplicarCupomUseCase.executar(pedidoId, codigoCupom, dataHoraAplicacao);
    }

    public PedidoResumoDTO obterResumo(String pedidoId) {
        Pedido pedido = buscarPedidoUseCase.buscarPorId(pedidoId);
        return presenter.toResumoDTO(pedido);
    }

    public String apresentarPedido(String pedidoId) {
        Pedido pedido = buscarPedidoUseCase.buscarPorId(pedidoId);
        return presenter.formatarPedido(pedido);
    }

    public void atualizarStatus(String pedidoId, String novoStatusStr) {
        StatusPedido novoStatus = StatusPedido.valueOf(novoStatusStr);
        atualizarStatusUseCase.executar(pedidoId, novoStatus);
    }

    public List<PedidoResumoDTO> listarPedidos() {
        return buscarPedidoUseCase.listarTodos().stream()
                .map(presenter::toResumoDTO)
                .collect(Collectors.toList());
    }

}
