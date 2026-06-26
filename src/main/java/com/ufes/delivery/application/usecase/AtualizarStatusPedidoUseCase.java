package com.ufes.delivery.application.usecase;

import com.ufes.delivery.application.port.in.AtualizarStatusPedidoInputPort;
import com.ufes.delivery.application.port.out.NotificacaoOutputPort;
import com.ufes.delivery.domain.entity.Pedido;
import com.ufes.delivery.domain.entity.StatusPedido;

import java.util.Objects;

/**
 * Use Case: Atualizar Status do Pedido.
 *
 * Clean Architecture — Application Layer:
 *   Orquestra a regra de transicao de status (Domain) e dispara
 *   notificacao via Output Port (Infrastructure).
 *
 * Padrao de Projeto — Observer (via porta de saida):
 *   O use case notifica o canal de saida apos cada mudanca de status,
 *   desacoplando a logica de negocio do mecanismo de notificacao.
 */
public class AtualizarStatusPedidoUseCase implements AtualizarStatusPedidoInputPort {

    private final NotificacaoOutputPort notificacao;

    public AtualizarStatusPedidoUseCase(NotificacaoOutputPort notificacao) {
        this.notificacao = Objects.requireNonNull(notificacao, "Servico de notificacao nao pode ser nulo");
    }

    @Override
    public void executar(Pedido pedido, StatusPedido novoStatus) {
        Objects.requireNonNull(pedido, "Pedido nao pode ser nulo");
        Objects.requireNonNull(novoStatus, "Novo status nao pode ser nulo");

        StatusPedido statusAnterior = pedido.getStatus();

        // Regra de negocio encapsulada na entidade de dominio (Domain Layer)
        pedido.atualizarStatus(novoStatus);

        // Notifica via Output Port — sem acoplamento a tecnologia concreta
        notificacao.notificarMudancaStatus(pedido, statusAnterior, novoStatus);
    }
}
