package com.ufes.delivery.infrastructure.repository;

import com.ufes.delivery.application.port.out.ItemRepositoryOutputPort;
import com.ufes.delivery.domain.entity.Item;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ItemRepositoryEmSQLite implements ItemRepositoryOutputPort {
    private final String url;

    public ItemRepositoryEmSQLite() {
        this.url = "jdbc:sqlite:delivery.db";

        String sqlTbItem = "CREATE TABLE IF NOT EXISTS tbItem ("
                + "id TEXT PRIMARY KEY,"
                + "nome TEXT NOT NULL,"
                + "quantidade INTEGER NOT NULL,"
                + "valorUnitario REAL NOT NULL,"
                + "tipo TEXT NOT NULL"
                + ");";

        try (var conn = DriverManager.getConnection(this.url); var stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute(sqlTbItem);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inicializar ItemRepositoryEmSQLite: " + e.getMessage(), e);
        }
    }

    @Override
    public void salvar(Item item) {
        String sql = "INSERT INTO tbItem (id, nome, quantidade, valorUnitario, tipo) VALUES (?, ?, ?, ?, ?) "
                + "ON CONFLICT(id) DO UPDATE SET "
                + "nome = excluded.nome, "
                + "quantidade = excluded.quantidade, "
                + "valorUnitario = excluded.valorUnitario, "
                + "tipo = excluded.tipo";

        try (var conn = DriverManager.getConnection(this.url);
             var stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getId());
            stmt.setString(2, item.getNome());
            stmt.setInt(3, item.getQuantidade());
            stmt.setDouble(4, item.getValorUnitario());
            stmt.setString(5, item.getTipo());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar item: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Item> listarTodos() {
        List<Item> itens = new ArrayList<>();
        String sql = "SELECT id, nome, quantidade, valorUnitario, tipo FROM tbItem";

        try (var conn = DriverManager.getConnection(this.url);
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Item item = Item.reconstruir(
                        rs.getString("id"),
                        rs.getString("nome"),
                        rs.getInt("quantidade"),
                        rs.getDouble("valorUnitario"),
                        rs.getString("tipo")
                );
                itens.add(item);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar itens: " + e.getMessage(), e);
        }
        return itens;
    }
}
