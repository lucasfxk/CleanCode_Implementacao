package com.ufes.delivery.adapter.ui;

import com.ufes.delivery.adapter.controller.PedidoController;
import com.ufes.delivery.application.dto.PedidoResumoDTO;

import javax.swing.*;
import java.awt.*;

/**
 * Janela principal da aplicacao.
 *
 * Clean Architecture — Adapter Layer:
 *   Conhece apenas o PedidoController. Todo acesso ao dominio e mediado
 *   pelo controller ou pelos paineis filhos (que tambem sao Adapters).
 *
 * SRP: responsabilidade unica — montar e exibir a janela principal.
 *      A logica de cada aba vive em seu proprio painel.
 */
public class DeliveryApp extends JFrame {

    private final PainelGerenciarPedido painelGerenciar;

    public DeliveryApp(PedidoController controller) {
        super("Sistema de Delivery — Clean Architecture | UFES 2026-1");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        painelGerenciar = new PainelGerenciarPedido(controller);

        PainelNovoPedido painelNovo = new PainelNovoPedido(controller, this::aoCriarPedido);

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("📋  Novo Pedido",   null, painelNovo,       "Cria um novo pedido de delivery");
        abas.addTab("⚙️  Gerenciar",     null, painelGerenciar,  "Gerencia descontos, cupons e status");

        add(abas, BorderLayout.CENTER);
        add(construirRodape(), BorderLayout.SOUTH);

        setVisible(true);
    }

    // -------------------------------------------------------------------------
    // Callback — chamado por PainelNovoPedido quando um pedido e criado
    // -------------------------------------------------------------------------

    private void aoCriarPedido(PedidoResumoDTO pedido) {
        painelGerenciar.atualizarListaPedidos();
        // Muda para a aba de gerenciamento automaticamente
        for (Component c : getContentPane().getComponents()) {
            if (c instanceof JTabbedPane tp) {
                tp.setSelectedIndex(1);
                break;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Acesso externo para receber notificacoes do Observer
    // -------------------------------------------------------------------------

    /** Encaminha mensagem de notificacao de status para o painel de gerenciamento. */
    public void receberNotificacao(String msg) {
        painelGerenciar.adicionarLogNotificacao(msg);
    }

    // -------------------------------------------------------------------------
    // Helpers visuais
    // -------------------------------------------------------------------------

    private JPanel construirRodape() {
        JPanel rod = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rod.setBackground(new Color(50, 50, 50));
        JLabel label = new JLabel("Clean Architecture  •  SOLID  •  Strategy  •  Observer  •  Repository  •  DTO");
        label.setForeground(Color.LIGHT_GRAY);
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        rod.add(label);
        return rod;
    }
}
