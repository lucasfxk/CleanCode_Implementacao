/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ufes.delivery.infrastructure.repository;

import com.ufes.delivery.application.port.out.ClienteRepositoryOutputPort;
import com.ufes.delivery.application.port.out.CupomRepositoryOutputPort;
import com.ufes.delivery.application.port.out.PedidoRepositoryOutputPort;
import com.ufes.delivery.domain.entity.Cliente;
import com.ufes.delivery.domain.entity.CupomDescontoEntrega;
import com.ufes.delivery.domain.entity.CupomDescontoPedido;
import com.ufes.delivery.domain.entity.Item;
import com.ufes.delivery.domain.entity.Pedido;
import com.ufes.delivery.domain.entity.StatusPedido;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author raphael
 */
public class PedidoRepositoryEmSQLite implements PedidoRepositoryOutputPort {

    private final String url;
    private final ClienteRepositoryOutputPort clienteRepository;
    private final CupomRepositoryOutputPort cupomPedidoRepository;

    public PedidoRepositoryEmSQLite(ClienteRepositoryOutputPort clienteRepository,
                                     CupomRepositoryOutputPort cupomPedidoRepository) {
        this.url = "jdbc:sqlite:delivery.db";
        this.clienteRepository = clienteRepository;
        this.cupomPedidoRepository = cupomPedidoRepository;

        String sqlPedidos = "CREATE TABLE IF NOT EXISTS Pedidos ("
                + "id TEXT PRIMARY KEY,"
                + "data TEXT NOT NULL,"
                + "taxaEntrega REAL NOT NULL,"
                + "status TEXT NOT NULL,"
                + "clienteId TEXT NOT NULL,"
                + "cupomPedidoCodigo TEXT,"
                + "FOREIGN KEY(clienteId) REFERENCES tbCliente(id)"
                + ");";

        String sqlItens = "CREATE TABLE IF NOT EXISTS PedidoItens ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "pedidoId TEXT NOT NULL,"
                + "nome TEXT NOT NULL,"
                + "quantidade INTEGER NOT NULL,"
                + "valorUnitario REAL NOT NULL,"
                + "tipo TEXT NOT NULL,"
                + "FOREIGN KEY(pedidoId) REFERENCES Pedidos(id)"
                + ");";

        String sqlCuponsEntrega = "CREATE TABLE IF NOT EXISTS PedidoCupomEntrega ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "pedidoId TEXT NOT NULL,"
                + "nomeMetodo TEXT NOT NULL,"
                + "valorDesconto REAL NOT NULL,"
                + "aplicado INTEGER NOT NULL,"
                + "FOREIGN KEY(pedidoId) REFERENCES Pedidos(id)"
                + ");";

        try (var conn = DriverManager.getConnection(this.url); var stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute(sqlPedidos);
            stmt.execute(sqlItens);
            stmt.execute(sqlCuponsEntrega);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inicializar tabelas de Pedidos: " + e.getMessage(), e);
        }
    }

    @Override
    public void salvar(Pedido pedido) {
        validarPedido(pedido);

        clienteRepository.salvar(pedido.getCliente());

        String sqlUpsertPedido = "INSERT INTO Pedidos (id, data, taxaEntrega, status, clienteId, cupomPedidoCodigo) "
                + "VALUES (?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT(id) DO UPDATE SET "
                + "data = excluded.data, "
                + "taxaEntrega = excluded.taxaEntrega, "
                + "status = excluded.status, "
                + "clienteId = excluded.clienteId, "
                + "cupomPedidoCodigo = excluded.cupomPedidoCodigo";

        try (var conn = DriverManager.getConnection(this.url)) {
            conn.setAutoCommit(false);
            try {
                try (var stmt = conn.prepareStatement(sqlUpsertPedido)) {
                    stmt.setString(1, pedido.getId());
                    stmt.setString(2, pedido.getData().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    stmt.setDouble(3, pedido.getTaxaEntrega());
                    stmt.setString(4, pedido.getStatus().name());
                    stmt.setString(5, pedido.getCliente().getId());

                    Optional<CupomDescontoPedido> cupomAplicado = pedido.getCupomAplicado();
                    if (cupomAplicado.isPresent()) {
                        stmt.setString(6, cupomAplicado.get().getCodigo());
                    } else {
                        stmt.setNull(6, java.sql.Types.VARCHAR);
                    }

                    stmt.executeUpdate();
                }

                salvarItens(conn, pedido);
                salvarCuponsDescontoEntrega(conn, pedido);

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Erro ao salvar pedido " + pedido.getId() + ": " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar ao banco para salvar pedido: " + e.getMessage(), e);
        }
    }

    private void salvarItens(Connection conn, Pedido pedido) throws SQLException {
        try (var del = conn.prepareStatement("DELETE FROM PedidoItens WHERE pedidoId = ?")) {
            del.setString(1, pedido.getId());
            del.executeUpdate();
        }

        String sqlInsertItem = "INSERT INTO PedidoItens (pedidoId, nome, quantidade, valorUnitario, tipo) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (var ins = conn.prepareStatement(sqlInsertItem)) {
            for (Item item : pedido.getItens()) {
                ins.setString(1, pedido.getId());
                ins.setString(2, item.getNome());
                ins.setInt(3, item.getQuantidade());
                ins.setDouble(4, item.getValorUnitario());
                ins.setString(5, item.getTipo());
                ins.addBatch();
            }
            ins.executeBatch();
        }
    }

    private void salvarCuponsDescontoEntrega(Connection conn, Pedido pedido) throws SQLException {
        try (var del = conn.prepareStatement("DELETE FROM PedidoCupomEntrega WHERE pedidoId = ?")) {
            del.setString(1, pedido.getId());
            del.executeUpdate();
        }

        String sqlInsertCupom = "INSERT INTO PedidoCupomEntrega (pedidoId, nomeMetodo, valorDesconto, aplicado) "
                + "VALUES (?, ?, ?, ?)";
        try (var ins = conn.prepareStatement(sqlInsertCupom)) {
            for (CupomDescontoEntrega cupom : pedido.getCupomDescontoEntrega()) {
                ins.setString(1, pedido.getId());
                ins.setString(2, cupom.getNomeMetodo());
                ins.setDouble(3, cupom.getValorDesconto());
                ins.setInt(4, cupom.isAplicado() ? 1 : 0);
                ins.addBatch();
            }
            ins.executeBatch();
        }
    }

    @Override
    public Optional<Pedido> buscarPorId(String id) {
        validarId(id);

        String sql = "SELECT id, data, taxaEntrega, status, clienteId, cupomPedidoCodigo "
                + "FROM Pedidos WHERE id = ?";

        try (var conn = DriverManager.getConnection(this.url); var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(montarPedido(conn, rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pedido " + id + ": " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Pedido> buscarPorData(LocalDateTime data) {
        if (data == null) {
            throw new IllegalArgumentException("A data deve ser informada.");
        }

        String sql = "SELECT id, data, taxaEntrega, status, clienteId, cupomPedidoCodigo "
                + "FROM Pedidos WHERE data = ?";

        try (var conn = DriverManager.getConnection(this.url); var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, data.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(montarPedido(conn, rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pedido pela data " + data + ": " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    @Override
    public List<Pedido> listarTodos() {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT id, data, taxaEntrega, status, clienteId, cupomPedidoCodigo FROM Pedidos";

        try (var conn = DriverManager.getConnection(this.url); var stmt = conn.createStatement(); var rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                pedidos.add(montarPedido(conn, rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar pedidos: " + e.getMessage(), e);
        }

        return Collections.unmodifiableList(pedidos);
    }

    private Pedido montarPedido(Connection conn, ResultSet rs) throws SQLException {
        String pedidoId = rs.getString("id");
        LocalDateTime data = LocalDateTime.parse(rs.getString("data"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        double taxaEntrega = rs.getDouble("taxaEntrega");
        StatusPedido status = StatusPedido.valueOf(rs.getString("status"));
        String clienteId = rs.getString("clienteId");
        String cupomCodigo = rs.getString("cupomPedidoCodigo");

        Cliente cliente = clienteRepository.buscarPorId(clienteId)
                .orElseThrow(() -> new IllegalStateException(
                        "Cliente " + clienteId + " referenciado pelo pedido " + pedidoId + " nao foi encontrado"));

        CupomDescontoPedido cupomAplicado = null;
        if (cupomCodigo != null) {
            cupomAplicado = cupomPedidoRepository.buscarCupom(cupomCodigo).orElse(null);
        }

        List<Item> itens = carregarItens(conn, pedidoId);
        List<CupomDescontoEntrega> cuponsDescontoEntrega = carregarCuponsDescontoEntrega(conn, pedidoId);

        return Pedido.reconstruir(pedidoId, data, cliente, taxaEntrega, status,
                itens, cuponsDescontoEntrega, cupomAplicado);
    }

    private List<Item> carregarItens(Connection conn, String pedidoId) throws SQLException {
        List<Item> itens = new ArrayList<>();
        String sql = "SELECT nome, quantidade, valorUnitario, tipo FROM PedidoItens WHERE pedidoId = ?";

        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pedidoId);
            try (var rs = stmt.executeQuery()) {
                while (rs.next()) {
                    itens.add(new Item(
                            rs.getString("nome"),
                            rs.getInt("quantidade"),
                            rs.getDouble("valorUnitario"),
                            rs.getString("tipo")));
                }
            }
        }

        return itens;
    }

    private List<CupomDescontoEntrega> carregarCuponsDescontoEntrega(Connection conn, String pedidoId) throws SQLException {
        List<CupomDescontoEntrega> cupons = new ArrayList<>();
        String sql = "SELECT nomeMetodo, valorDesconto, aplicado FROM PedidoCupomEntrega WHERE pedidoId = ?";

        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pedidoId);
            try (var rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cupons.add(CupomDescontoEntrega.reconstruir(
                            rs.getString("nomeMetodo"),
                            rs.getDouble("valorDesconto"),
                            rs.getInt("aplicado") == 1));
                }
            }
        }

        return cupons;
    }

    private void validarPedido(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("O pedido deve ser informado.");
        }
    }

    private void validarId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("O id do pedido deve ser informado.");
        }
    }
}