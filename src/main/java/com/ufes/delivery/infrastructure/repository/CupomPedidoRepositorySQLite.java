/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ufes.delivery.infrastructure.repository;

import com.ufes.delivery.application.port.out.CupomRepositoryOutputPort;
import com.ufes.delivery.domain.entity.CupomDescontoPedido;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author raphael
 */
public class CupomPedidoRepositorySQLite implements CupomRepositoryOutputPort {

    private final String url;

    public CupomPedidoRepositorySQLite() {
        this.url = "jdbc:sqlite:delivery.db";

        String sql = "CREATE TABLE IF NOT EXISTS tbCupomPedido ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT," + "codigo TEXT NOT NULL,"
                + "percentual DOUBLE NOT NULL," + "dataHoraInicio TEXT NOT NULL"
                + "dataHoraFim TEXT NOT NULL" + ");";

        try (var conn = DriverManager.getConnection(this.url); var stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("ERRO!!! " + e.getMessage());
        }
    }

    @Override
    public void adicionarCupom(CupomDescontoPedido cupom) {
        validarCupom(cupom);

        String sql = "SELECT codigo, percentual, dataHoraInicio, dataHoraFim "
                + "FROM tbCupomPedido WHERE = " + cupom.getCodigo();
        try (var conn = DriverManager.getConnection(url); var stmt = conn.createStatement(); var rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                throw new SQLException("O cupom já existe na tabela.");
            }

            sql = "INSERT INTO tbCupomPedido(codigo, percentual, dataHoraInicio, "
                    + "dataHoraFim) VALUES (?, ?, ?, ?)";
            var istmt = conn.prepareStatement(sql);
            istmt.setString(1, cupom.getCodigo());
            istmt.setDouble(2, cupom.getPercentual());

            String dataInicio = cupom.getDataHoraInicio().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String dataFim = cupom.getDataHoraFim().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            istmt.setString(3, dataInicio);
            istmt.setString(4, dataFim);
            istmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("ERRO!!! " + e.getMessage());
        }
    }

    public void atualizarCupom(CupomDescontoPedido cupom) {
        validarCupom(cupom);

        String sql = "SELECT codigo, percentual, dataHoraInicio, dataHoraFim "
                + "FROM tbCupomPedido WHERE = " + cupom.getCodigo();
        try (var conn = DriverManager.getConnection(url); var stmt = conn.createStatement(); var rs = stmt.executeQuery(sql)) {
            if (!rs.next()) {
                throw new SQLException("O cupom ainda não existe na tabela.");
            }

            sql = "UPDATE tbCupomPedidos SET codigo = ?, percentual = ?, "
                    + "dataHoraInicio = ?, dataHoraFim = ? WHERE codigo = "
                    + cupom.getCodigo();

            var ustmt = conn.prepareStatement(sql);
            ustmt.setString(1, cupom.getCodigo());
            ustmt.setDouble(2, cupom.getPercentual());

            String dataInicio = cupom.getDataHoraInicio().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String dataFim = cupom.getDataHoraFim().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            ustmt.setString(3, dataInicio);
            ustmt.setString(4, dataFim);
            ustmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("ERRO!!! " + e.getMessage());
        }
    }

    public void removerCupom(String codigo) {
        validarCodigoCupom(codigo);

        String sql = "DELETE FROM tbCupomPedido WHERE codigo = ?";

        try (var conn = DriverManager.getConnection(this.url); var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("ERRO!!! " + e.getMessage());
        }
    }

    public void removerCuponsExpirados() {
        String sql = "SELECT nome, userName, tipo, situacao, autorizado FROM "
                + "tbUsuario";

        try (var conn = DriverManager.getConnection(this.url); var stmt = conn.createStatement(); var rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String dataFimStr = rs.getString("dataHoraFim");
                LocalDateTime dataFim = LocalDateTime.parse(dataFimStr,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                if (dataFim.isBefore(LocalDateTime.now())) {
                    sql = "DELETE FROM tbUsuario WHERE userName = ?";
                    var dstmt = conn.prepareStatement(sql);
                    dstmt.setString(1, rs.getString("codigo"));
                    dstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.out.println("ERRO!!! " + e.getMessage());
        }
    }

    @Override
    public Optional<CupomDescontoPedido> buscarCupom(String codigo) {
        validarCodigoCupom(codigo);

        String sql = "SELECT nome, userName, tipo, situacao, autorizado FROM "
                + "tbUsuario WHERE userName = ?";

        try (var conn = DriverManager.getConnection(this.url); var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            var rs = stmt.executeQuery();

            if (rs.next()) {
                String dataFimStr = rs.getString("dataHoraFim");
                LocalDateTime dataFim = LocalDateTime.parse(dataFimStr,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                String dataInicioStr = rs.getString("dataHoraInicio");
                LocalDateTime dataInicio = LocalDateTime.parse(dataInicioStr,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                return Optional.of(new CupomDescontoPedido(rs.getString("codigo"),
                         rs.getDouble("percentual"), dataInicio, dataFim));
            }
        } catch (SQLException e) {
            System.out.println("ERRO!!! " + e.getMessage());
        }

        return Optional.empty();
    }

    public Map<String, CupomDescontoPedido> getCuponsDisponiveis() {
        Map<String, CupomDescontoPedido> cuponsDisponiveis = new HashMap<>();
        removerCuponsExpirados();

        String sql = "SELECT nome, userName, tipo, situacao, autorizado FROM "
                + "tbUsuario";

        try (var conn = DriverManager.getConnection(this.url); 
                var stmt = conn.createStatement(); 
                var rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String dataFimStr = rs.getString("dataHoraFim");
                LocalDateTime dataFim = LocalDateTime.parse(dataFimStr,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                String dataInicioStr = rs.getString("dataHoraInicio");
                LocalDateTime dataInicio = LocalDateTime.parse(dataInicioStr,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                cuponsDisponiveis.put(rs.getString("codigo"),
                        new CupomDescontoPedido(rs.getString("codigo"),
                                rs.getDouble("Percentual"), dataInicio, dataFim));
            }
            
            return Map.copyOf(cuponsDisponiveis);
        } catch (SQLException e) {
            System.out.println("ERRO!!! " + e.getMessage());
        }
        
        return null;
    }

    private void validarCupom(CupomDescontoPedido cupom) {
        if (cupom == null) {
            throw new IllegalArgumentException("Cupom nao pode ser nulo");
        }

        validarCodigoCupom(cupom.getCodigo());

        if (cupom.getPercentual() <= 0) {
            throw new IllegalArgumentException("Percentual do cupom deve ser maior que zero");
        }

        if (cupom.getDataHoraInicio() == null || cupom.getDataHoraFim() == null) {
            throw new IllegalArgumentException("Periodo de validade do cupom deve ser informado");
        }

        if (cupom.getDataHoraFim().isBefore(cupom.getDataHoraInicio())) {
            throw new IllegalArgumentException("Data final do cupom nao pode ser anterior a data inicial");
        }
    }

    private void validarCodigoCupom(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Codigo do cupom nao pode ser vazio");
        }
    }
}
