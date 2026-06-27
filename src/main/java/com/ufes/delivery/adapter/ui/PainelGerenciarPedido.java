package com.ufes.delivery.adapter.ui;

import com.ufes.delivery.adapter.controller.PedidoController;
import com.ufes.delivery.application.dto.PedidoResumoDTO;
import com.ufes.delivery.domain.entity.Pedido;
import com.ufes.delivery.domain.entity.StatusPedido;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Painel de gerenciamento do pedido selecionado.
 *
 * Responsabilidades (SRP):
 *   - Exibir lista de pedidos criados
 *   - Permitir aplicar descontos e cupom
 *   - Avançar o status do pedido selecionado
 *   - Mostrar o resumo formatado
 *
 * Clean Architecture — Adapter Layer:
 *   Usa apenas PedidoController e DTOs. Nao acessa Use Cases diretamente.
 */
public class PainelGerenciarPedido extends JPanel {

    private final PedidoController controller;

    // --- Lista de pedidos ---
    private final DefaultListModel<String> modeloLista = new DefaultListModel<>();
    private final JList<String> listaPedidos = new JList<>(modeloLista);
    private List<Pedido> pedidosCache = List.of();

    // --- Ações ---
    private final JTextField campoCupom = new JTextField("VALIDOHOJE", 12);
    private final JTextArea  areaResumo = new JTextArea();
    private final JTextArea  areaLog    = new JTextArea();

    // --- Status ---
    private final JLabel labelStatusAtual = new JLabel("—");
    private final JButton btnConfirmar    = new JButton("CONFIRMADO");
    private final JButton btnEmPreparo    = new JButton("EM_PREPARO");
    private final JButton btnSaiu         = new JButton("SAIU_PARA_ENTREGA");
    private final JButton btnEntregue     = new JButton("ENTREGUE");
    private final JButton btnCancelar     = new JButton("CANCELADO");

    public PainelGerenciarPedido(PedidoController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(construirPainelEsquerdo(), BorderLayout.WEST);
        add(construirPainelCentro(),   BorderLayout.CENTER);
        add(construirPainelLog(),      BorderLayout.SOUTH);
    }

    // -------------------------------------------------------------------------
    // Construção dos sub-painéis
    // -------------------------------------------------------------------------

    private JPanel construirPainelEsquerdo() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setPreferredSize(new Dimension(220, 0));
        p.setBorder(titulado("Pedidos"));

        listaPedidos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaPedidos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) exibirPedidoSelecionado();
        });
        p.add(new JScrollPane(listaPedidos), BorderLayout.CENTER);

        JButton btnAtualizar = new JButton("↺ Atualizar lista");
        btnAtualizar.addActionListener(e -> atualizarListaPedidos());
        p.add(btnAtualizar, BorderLayout.SOUTH);
        return p;
    }

    private JPanel construirPainelCentro() {
        JPanel p = new JPanel(new BorderLayout(6, 6));

        // Ações
        JPanel acoes = new JPanel(new GridLayout(1, 3, 8, 0));
        acoes.setBorder(titulado("Ações"));

        acoes.add(construirPainelDescontos());
        acoes.add(construirPainelCupom());
        acoes.add(construirPainelStatus());

        p.add(acoes, BorderLayout.NORTH);

        // Resumo
        areaResumo.setEditable(false);
        areaResumo.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        areaResumo.setText("← Selecione um pedido na lista para ver o resumo.");
        JScrollPane scroll = new JScrollPane(areaResumo);
        scroll.setBorder(titulado("Resumo do Pedido"));
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    private JPanel construirPainelDescontos() {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBorder(titulado("Descontos na Entrega (Strategy)"));

        JTextArea info = new JTextArea(
                "Aplica automaticamente as 4 estratégias:\n" +
                "• Por Bairro\n• Por Tipo de Cliente\n• Por Tipo de Item\n• Por Valor do Pedido");
        info.setEditable(false);
        info.setBackground(UIManager.getColor("Panel.background"));
        info.setFont(new Font("SansSerif", Font.PLAIN, 11));
        p.add(info, BorderLayout.CENTER);

        JButton btn = new JButton("Calcular Descontos");
        btn.setBackground(new Color(70, 130, 180));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.addActionListener(e -> calcularDescontos());
        p.add(btn, BorderLayout.SOUTH);
        return p;
    }

    private JPanel construirPainelCupom() {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBorder(titulado("Cupom de Desconto"));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Código:"));
        form.add(campoCupom);
        p.add(form, BorderLayout.CENTER);

        JTextArea dica = new JTextArea("Cupom válido para teste:\nVALIDOHOJE (15%)");
        dica.setEditable(false);
        dica.setBackground(new Color(255, 255, 200));
        dica.setFont(new Font("SansSerif", Font.ITALIC, 11));
        p.add(dica, BorderLayout.NORTH);

        JButton btn = new JButton("Aplicar Cupom");
        btn.setBackground(new Color(184, 134, 11));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.addActionListener(e -> aplicarCupom());
        p.add(btn, BorderLayout.SOUTH);
        return p;
    }

    private JPanel construirPainelStatus() {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBorder(titulado("Ciclo de Status (Observer)"));

        JPanel atual = new JPanel(new FlowLayout(FlowLayout.LEFT));
        atual.add(new JLabel("Status atual:"));
        labelStatusAtual.setFont(labelStatusAtual.getFont().deriveFont(Font.BOLD));
        atual.add(labelStatusAtual);
        p.add(atual, BorderLayout.NORTH);

        JPanel botoes = new JPanel(new GridLayout(5, 1, 3, 3));
        configurarBotaoStatus(btnConfirmar,  StatusPedido.CONFIRMADO,       new Color(70, 130, 180));
        configurarBotaoStatus(btnEmPreparo,  StatusPedido.EM_PREPARO,       new Color(255, 140, 0));
        configurarBotaoStatus(btnSaiu,       StatusPedido.SAIU_PARA_ENTREGA, new Color(72, 61, 139));
        configurarBotaoStatus(btnEntregue,   StatusPedido.ENTREGUE,         new Color(34, 139, 34));
        configurarBotaoStatus(btnCancelar,   StatusPedido.CANCELADO,        new Color(178, 34, 34));
        botoes.add(btnConfirmar);
        botoes.add(btnEmPreparo);
        botoes.add(btnSaiu);
        botoes.add(btnEntregue);
        botoes.add(btnCancelar);
        p.add(botoes, BorderLayout.CENTER);
        return p;
    }

    private JPanel construirPainelLog() {
        JPanel p = new JPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(0, 110));
        areaLog.setEditable(false);
        areaLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        areaLog.setBackground(new Color(30, 30, 30));
        areaLog.setForeground(new Color(0, 220, 80));
        JScrollPane scroll = new JScrollPane(areaLog);
        scroll.setBorder(titulado("Log de Notificações (Observer)"));
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // -------------------------------------------------------------------------
    // Ações
    // -------------------------------------------------------------------------

    public void atualizarListaPedidos() {
        pedidosCache = controller.listarPedidos();
        modeloLista.clear();
        for (int i = 0; i < pedidosCache.size(); i++) {
            Pedido p = pedidosCache.get(i);
            modeloLista.addElement(String.format("#%d %s [%s]",
                    i + 1, p.getCliente().getNome(), p.getStatus().name()));
        }
    }

    public void adicionarLogNotificacao(String msg) {
        areaLog.append(msg);
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
        // Atualiza a lista para refletir novo status
        atualizarListaPedidos();
        // Atualiza resumo se o pedido afetado estiver selecionado
        exibirPedidoSelecionado();
    }

    private void exibirPedidoSelecionado() {
        Pedido pedido = pedidoSelecionado();
        if (pedido == null) {
            labelStatusAtual.setText("—");
            return;
        }
        labelStatusAtual.setText(pedido.getStatus().name());
        atualizarBotoesStatus(pedido.getStatus());
        areaResumo.setText(controller.apresentarPedido(pedido));
        areaResumo.setCaretPosition(0);
    }

    private void calcularDescontos() {
        Pedido pedido = pedidoSelecionadoComAviso();
        if (pedido == null) return;
        try {
            controller.calcularDescontosEntrega(pedido);
            areaResumo.setText(controller.apresentarPedido(pedido));
            areaLog.append(String.format("[DESCONTOS] Calculados para %s%n",
                    pedido.getCliente().getNome()));
        } catch (Exception ex) {
            exibirErro(ex);
        }
    }

    private void aplicarCupom() {
        Pedido pedido = pedidoSelecionadoComAviso();
        if (pedido == null) return;
        String codigo = campoCupom.getText().trim();
        try {
            controller.aplicarCupom(pedido, codigo, LocalDateTime.now());
            areaResumo.setText(controller.apresentarPedido(pedido));
            areaLog.append(String.format("[CUPOM] %s aplicado em pedido de %s%n",
                    codigo, pedido.getCliente().getNome()));
        } catch (Exception ex) {
            exibirErro(ex);
        }
    }

    private void avancarStatus(StatusPedido novoStatus) {
        Pedido pedido = pedidoSelecionadoComAviso();
        if (pedido == null) return;
        try {
            controller.atualizarStatus(pedido, novoStatus);
            // A notificacao via NotificacaoSwing ja atualiza o log e a lista
            exibirPedidoSelecionado();
        } catch (IllegalStateException ex) {
            exibirErro(ex);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void configurarBotaoStatus(JButton btn, StatusPedido alvo, Color cor) {
        btn.setBackground(cor);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 10f));
        btn.addActionListener(e -> avancarStatus(alvo));
    }

    private void atualizarBotoesStatus(StatusPedido atual) {
        btnConfirmar.setEnabled(atual.podeTransicionarPara(StatusPedido.CONFIRMADO));
        btnEmPreparo.setEnabled(atual.podeTransicionarPara(StatusPedido.EM_PREPARO));
        btnSaiu.setEnabled(atual.podeTransicionarPara(StatusPedido.SAIU_PARA_ENTREGA));
        btnEntregue.setEnabled(atual.podeTransicionarPara(StatusPedido.ENTREGUE));
        btnCancelar.setEnabled(atual.podeTransicionarPara(StatusPedido.CANCELADO));
    }

    private Pedido pedidoSelecionado() {
        int idx = listaPedidos.getSelectedIndex();
        return (idx >= 0 && idx < pedidosCache.size()) ? pedidosCache.get(idx) : null;
    }

    private Pedido pedidoSelecionadoComAviso() {
        Pedido p = pedidoSelecionado();
        if (p == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um pedido na lista.", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
        return p;
    }

    private void exibirErro(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        areaLog.append("[ERRO] " + ex.getMessage() + "\n");
    }

    private TitledBorder titulado(String titulo) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), titulo,
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 11));
    }
}
