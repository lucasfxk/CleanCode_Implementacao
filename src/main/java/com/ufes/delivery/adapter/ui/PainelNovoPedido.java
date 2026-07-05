package com.ufes.delivery.adapter.ui;

import com.ufes.delivery.adapter.controller.PedidoController;
import com.ufes.delivery.application.dto.CriarPedidoDTO;
import com.ufes.delivery.application.dto.ItemDTO;
import com.ufes.delivery.application.dto.PedidoResumoDTO;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Painel de criacao de novos pedidos.
 *
 * Clean Architecture — Adapter Layer:
 *   Conhece o PedidoController (Adapter) e os DTOs (Application),
 *   mas nao conhece Use Cases nem entidades de dominio diretamente.
 *
 * SRP: responsabilidade unica — coletar dados do usuario e acionar o controller.
 */
public class PainelNovoPedido extends JPanel {

    // --- Campos do cliente ---
    private final JTextField campoNome       = new JTextField(15);
    private final JComboBox<String> comboTipo = new JComboBox<>(new String[]{"Bronze", "Prata", "Ouro", "Diamante"});
    private final JSpinner spinnerFidelidade  = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    private final JTextField campoLogradouro  = new JTextField(15);
    private final JComboBox<String> comboBairro = new JComboBox<>(
            new String[]{"Centro", "Bela Vista", "Cidade Maravilhosa", "Jardim", "Norte", "Sul"});
    private final JTextField campoCidade      = new JTextField(10);

    // --- Tabela de itens ---
    private final DefaultTableModel modeloItens = new DefaultTableModel(
            new String[]{"Nome", "Qtd", "Valor Unit. (R$)", "Tipo"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabelaItens = new JTable(modeloItens);

    // --- Campos de novo item ---
    private final JTextField campoItemNome  = new JTextField(10);
    private final JSpinner spinnerQtd       = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JSpinner spinnerValor      = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 9999.99, 0.5));
    private final JComboBox<String> comboTipoItem = new JComboBox<>(
            new String[]{"Alimentacao", "Educacao", "Lazer", "Higiene", "Eletronico"});

    private final PedidoController controller;
    private final Consumer<PedidoResumoDTO> aocriarPedido;

    /**
     * @param controller    controlador existente (Adapter)
     * @param aocriarPedido callback invocado quando um pedido e criado com sucesso
     */
    public PainelNovoPedido(PedidoController controller, Consumer<PedidoResumoDTO> aocriarPedido) {
        this.controller   = controller;
        this.aocriarPedido = aocriarPedido;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(construirPainelCliente(), BorderLayout.NORTH);
        add(construirPainelItens(),   BorderLayout.CENTER);
        add(construirBotaoCriar(),    BorderLayout.SOUTH);
        preencherDadosExemplo();
    }

    // -------------------------------------------------------------------------
    // Construcao dos sub-paineis (SRP por metodo)
    // -------------------------------------------------------------------------

    private JPanel construirPainelCliente() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(titulado("Dados do Cliente"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;

        adicionarLinha(p, g, 0, "Nome:",       campoNome);
        adicionarLinha(p, g, 1, "Tipo:",        comboTipo);
        adicionarLinha(p, g, 2, "Fidelidade:", spinnerFidelidade);
        adicionarLinha(p, g, 3, "Logradouro:", campoLogradouro);
        adicionarLinha(p, g, 4, "Bairro:",     comboBairro);
        adicionarLinha(p, g, 5, "Cidade:",     campoCidade);
        return p;
    }

    private JPanel construirPainelItens() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(titulado("Itens do Pedido"));

        // Tabela
        tabelaItens.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaItens.getTableHeader().setReorderingAllowed(false);
        p.add(new JScrollPane(tabelaItens), BorderLayout.CENTER);

        // Formulario de adicao de item
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        form.add(new JLabel("Nome:")); form.add(campoItemNome);
        form.add(new JLabel("Qtd:"));  form.add(spinnerQtd);
        form.add(new JLabel("R$:"));   form.add(spinnerValor);
        form.add(new JLabel("Tipo:")); form.add(comboTipoItem);

        JButton btnAdd = new JButton("+ Adicionar Item");
        btnAdd.addActionListener(e -> adicionarItemNaTabela());
        form.add(btnAdd);

        JButton btnRem = new JButton("Remover");
        btnRem.addActionListener(e -> removerItemSelecionado());
        form.add(btnRem);

        p.add(form, BorderLayout.SOUTH);
        return p;
    }

    private JButton construirBotaoCriar() {
        JButton btn = new JButton("Criar Pedido");
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 14f));
        btn.setBackground(new Color(34, 139, 34));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> criarPedido());
        return btn;
    }

    // -------------------------------------------------------------------------
    // Acoes
    // -------------------------------------------------------------------------

    private void adicionarItemNaTabela() {
        String nome = campoItemNome.getText().trim();
        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o nome do item.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        modeloItens.addRow(new Object[]{
                nome,
                spinnerQtd.getValue(),
                String.format("%.2f", spinnerValor.getValue()),
                comboTipoItem.getSelectedItem()
        });
        campoItemNome.setText("");
    }

    private void removerItemSelecionado() {
        int linha = tabelaItens.getSelectedRow();
        if (linha >= 0) modeloItens.removeRow(linha);
    }

    private void criarPedido() {
        if (campoNome.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o nome do cliente.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (campoLogradouro.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o logradouro.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (campoCidade.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe a cidade.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (modeloItens.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Adicione ao menos um item.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<ItemDTO> itens = coletarItens();
            CriarPedidoDTO dto = new CriarPedidoDTO(
                    campoNome.getText().trim(),
                    (String) comboTipo.getSelectedItem(),
                    ((Number) spinnerFidelidade.getValue()).doubleValue(),
                    campoLogradouro.getText().trim(),
                    (String) comboBairro.getSelectedItem(),
                    campoCidade.getText().trim(),
                    LocalDateTime.now(),
                    itens
            );

            PedidoResumoDTO pedido = controller.criarPedido(dto);
            JOptionPane.showMessageDialog(this,
                    "Pedido criado com sucesso para " + pedido.getNomeCliente() + "!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            aocriarPedido.accept(pedido);
            modeloItens.setRowCount(0);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<ItemDTO> coletarItens() {
        List<ItemDTO> itens = new ArrayList<>();
        for (int i = 0; i < modeloItens.getRowCount(); i++) {
            String nome  = (String) modeloItens.getValueAt(i, 0);
            int qtd      = (int)    modeloItens.getValueAt(i, 1);
            double valor = Double.parseDouble(((String) modeloItens.getValueAt(i, 2)).replace(",", "."));
            String tipo  = (String) modeloItens.getValueAt(i, 3);
            itens.add(new ItemDTO(nome, qtd, valor, tipo));
        }
        return itens;
    }

    // -------------------------------------------------------------------------
    // Helpers visuais
    // -------------------------------------------------------------------------

    private void adicionarLinha(JPanel p, GridBagConstraints g, int linha, String label, JComponent campo) {
        g.gridx = 0; g.gridy = linha; g.fill = GridBagConstraints.NONE;
        p.add(new JLabel(label), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1.0;
        p.add(campo, g);
        g.weightx = 0;
    }

    private TitledBorder titulado(String titulo) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), titulo,
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12));
    }

    private void preencherDadosExemplo() {
        campoNome.setText("Maria");
        campoCidade.setText("Castelo");
        campoLogradouro.setText("Rua das Flores, 10");
        comboTipo.setSelectedItem("Ouro");
        spinnerFidelidade.setValue(3);
        comboBairro.setSelectedItem("Cidade Maravilhosa");
        modeloItens.addRow(new Object[]{"Biscoito", 4, "5.80", "Alimentacao"});
        modeloItens.addRow(new Object[]{"Livro",    1, "40.20", "Lazer"});
        modeloItens.addRow(new Object[]{"Caderno",  2, "10.50", "Educacao"});
    }
}
