/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ufes.delivery.infrastructure.repository;

import com.ufes.delivery.application.port.out.ClienteRepositoryOutputPort;
import com.ufes.delivery.domain.entity.Cliente;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author raphael
 */
public class ClienteRepositoryEmSQLite implements ClienteRepositoryOutputPort {

    private final String url;

    public ClienteRepositoryEmSQLite() {
        this.url = "jdbc:sqlite:delivery.db";

        String sql = "CREATE TABLE IF NOT EXISTS tbCliente ("
                + "id TEXT PRIMARY KEY,"
                + "nome TEXT NOT NULL,"
                + "tipo TEXT NOT NULL,"
                + "fidelidade REAL NOT NULL,"
                + "logradouro TEXT NOT NULL,"
                + "bairro TEXT NOT NULL,"
                + "cidade TEXT NOT NULL"
                + ");";

        try (var conn = DriverManager.getConnection(this.url); var stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inicializar tabela de Cliente: " + e.getMessage(), e);
        }
    }

    @Override
    public void salvar(Cliente cliente) {
        validarCliente(cliente);

        String sql = "INSERT INTO tbCliente (id, nome, tipo, fidelidade, logradouro, bairro, cidade) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT(id) DO UPDATE SET "
                + "nome = excluded.nome, "
                + "tipo = excluded.tipo, "
                + "fidelidade = excluded.fidelidade, "
                + "logradouro = excluded.logradouro, "
                + "bairro = excluded.bairro, "
                + "cidade = excluded.cidade";

        try (var conn = DriverManager.getConnection(this.url); var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cliente.getId());
            stmt.setString(2, cliente.getNome());
            stmt.setString(3, cliente.getTipo());
            stmt.setDouble(4, cliente.getFidelidade());
            stmt.setString(5, cliente.getLogradouro());
            stmt.setString(6, cliente.getBairro());
            stmt.setString(7, cliente.getCidade());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar cliente " + cliente.getId() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Cliente> buscarPorId(String id) {
        validarId(id);

        String sql = "SELECT id, nome, tipo, fidelidade, logradouro, bairro, cidade FROM tbCliente WHERE id = ?";

        try (var conn = DriverManager.getConnection(this.url); var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(montarCliente(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar cliente " + id + ": " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    @Override
    public List<Cliente> listarTodos() {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT id, nome, tipo, fidelidade, logradouro, bairro, cidade FROM tbCliente";

        try (var conn = DriverManager.getConnection(this.url); var stmt = conn.createStatement(); var rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                clientes.add(montarCliente(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar clientes: " + e.getMessage(), e);
        }

        return Collections.unmodifiableList(clientes);
    }

    private Cliente montarCliente(java.sql.ResultSet rs) throws SQLException {
        return Cliente.reconstruir(
                rs.getString("id"),
                rs.getString("nome"),
                rs.getString("tipo"),
                rs.getDouble("fidelidade"),
                rs.getString("logradouro"),
                rs.getString("bairro"),
                rs.getString("cidade"));
    }

    private void validarCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("O cliente deve ser informado.");
        }
    }

    private void validarId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("O id do cliente deve ser informado.");
        }
    }
}