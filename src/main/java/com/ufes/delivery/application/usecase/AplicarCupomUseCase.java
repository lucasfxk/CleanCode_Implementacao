package com.ufes.delivery.application.usecase;

import com.ufes.delivery.application.port.in.AplicarCupomInputPort;
import com.ufes.delivery.application.port.out.CupomRepositoryOutputPort;
import com.ufes.delivery.application.port.out.PedidoRepositoryOutputPort;
import com.ufes.delivery.domain.entity.CupomDescontoPedido;
import com.ufes.delivery.domain.entity.Pedido;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class AplicarCupomUseCase implements AplicarCupomInputPort {
    private final CupomRepositoryOutputPort cupomRepository;
    private final PedidoRepositoryOutputPort pedidoRepository;

    public AplicarCupomUseCase(CupomRepositoryOutputPort cupomRepository, PedidoRepositoryOutputPort pedidoRepository) {
        this.cupomRepository = Objects.requireNonNull(cupomRepository, "Repositorio de cupons nao pode ser nulo");
        this.pedidoRepository = Objects.requireNonNull(pedidoRepository, "Repositorio de pedidos nao pode ser nulo");
    }

    @Override
    public void executar(String pedidoId, String codigoCupom, LocalDateTime dataHoraAplicacao) {
        Objects.requireNonNull(pedidoId, "ID do pedido nao pode ser nulo");
        Objects.requireNonNull(dataHoraAplicacao, "Data e hora de aplicacao nao podem ser nulas");

        Pedido pedido = pedidoRepository.buscarPorId(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido nao encontrado: " + pedidoId));

        if (codigoCupom == null || codigoCupom.isBlank()) {
            throw new IllegalArgumentException("Codigo do cupom nao pode ser vazio");
        }

        Optional<CupomDescontoPedido> cupomEncontrado = cupomRepository.buscarCupom(codigoCupom);

        if (cupomEncontrado.isEmpty()) {
            throw new IllegalArgumentException("Cupom inexistente: " + codigoCupom);
        }

        CupomDescontoPedido cupom = cupomEncontrado.get();

        if (dataHoraAplicacao.isBefore(cupom.getDataHoraInicio())
                || dataHoraAplicacao.isAfter(cupom.getDataHoraFim())) {
            throw new IllegalStateException("O pedido nao esta dentro da validade do cupom");
        }

        Optional<CupomDescontoPedido> cupomAtual = pedido.getCupomAplicado();

        if (cupomAtual.isPresent()) {
            if (cupom.getPercentual() <= cupomAtual.get().getPercentual()) {
                throw new IllegalStateException(
                        "O cupom " + codigoCupom + " nao tem um percentual maior que o cupom atual");
            }
        }

        pedido.setCupomAplicado(cupom);

        pedidoRepository.salvar(pedido);
    }
}
