package com.ufes.delivery.adapter.controller;

import com.ufes.delivery.adapter.presenter.PedidoPresenter;
import com.ufes.delivery.application.dto.CriarPedidoDTO;
import com.ufes.delivery.application.dto.PedidoResumoDTO;
import com.ufes.delivery.application.port.in.AplicarCupomInputPort;
import com.ufes.delivery.application.port.in.CalcularDescontoEntregaInputPort;
import com.ufes.delivery.application.port.in.AtualizarStatusPedidoInputPort;
import com.ufes.delivery.application.port.in.BuscarPedidoInputPort;
import com.ufes.delivery.application.port.in.CriarPedidoInputPort;
import com.ufes.delivery.domain.entity.Pedido;
import com.ufes.delivery.domain.entity.StatusPedido;
import java.util.List;

import java.time.LocalDateTime;
import java.util.Objects;

public class PedidoController {
    private final CriarPedidoInputPort criarPedidoUseCase;
    private final CalcularDescontoEntregaInputPort calcularDescontoEntregaUseCase;
    private final AplicarCupomInputPort aplicarCupomUseCase;
    private final AtualizarStatusPedidoInputPort atualizarStatusUseCase;
    private final BuscarPedidoInputPort buscarPedidoUseCase;
    private final PedidoPresenter presenter;

    public PedidoController(CriarPedidoInputPort criarPedidoUseCase,
                            CalcularDescontoEntregaInputPort calcularDescontoEntregaUseCase,
                            AplicarCupomInputPort aplicarCupomUseCase,
                            AtualizarStatusPedidoInputPort atualizarStatusUseCase,
                            BuscarPedidoInputPort buscarPedidoUseCase,
                            PedidoPresenter presenter) {
        this.criarPedidoUseCase = Objects.requireNonNull(criarPedidoUseCase);
        this.calcularDescontoEntregaUseCase = Objects.requireNonNull(calcularDescontoEntregaUseCase);
        this.aplicarCupomUseCase = Objects.requireNonNull(aplicarCupomUseCase);
        this.atualizarStatusUseCase = Objects.requireNonNull(atualizarStatusUseCase);
        this.buscarPedidoUseCase = Objects.requireNonNull(buscarPedidoUseCase);
        this.presenter = Objects.requireNonNull(presenter);
    }

    public Pedido criarPedido(CriarPedidoDTO dto) {
        return criarPedidoUseCase.executar(dto);
    }

    public void calcularDescontosEntrega(Pedido pedido) {
        calcularDescontoEntregaUseCase.executar(pedido);
    }

    public void aplicarCupom(Pedido pedido, String codigoCupom, LocalDateTime dataHoraAplicacao) {
        aplicarCupomUseCase.executar(pedido, codigoCupom, dataHoraAplicacao);
    }

    public PedidoResumoDTO obterResumo(Pedido pedido) {
        return presenter.toResumoDTO(pedido);
    }

    public String apresentarPedido(Pedido pedido) {
        return presenter.formatarPedido(pedido);
    }

    public void atualizarStatus(Pedido pedido, StatusPedido novoStatus) {
        atualizarStatusUseCase.executar(pedido, novoStatus);
    }

    public List<Pedido> listarPedidos() {
        return buscarPedidoUseCase.listarTodos();
    }

}
