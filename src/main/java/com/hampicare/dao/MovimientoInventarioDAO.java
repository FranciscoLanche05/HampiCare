package com.hampicare.dao;

import com.hampicare.db.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MovimientoInventarioDAO {

    /** Aumenta el stock del medicamento y registro **/
    public void registrarEntrada(int medicamentoId, int cantidad, int usuarioId) throws SQLException {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0.");
        }

        Connection c = Conexion.getInstancia().getConnection();
        boolean autoCommitOriginal = c.getAutoCommit();
        c.setAutoCommit(false);
        try {
            try (PreparedStatement psStock = c.prepareStatement(
                    "UPDATE medicamentos SET stock = stock + ? WHERE id = ?")) {
                psStock.setInt(1, cantidad);
                psStock.setInt(2, medicamentoId);
                int filas = psStock.executeUpdate();
                if (filas == 0) {
                    throw new SQLException("El medicamento seleccionado ya no existe.");
                }
            }

            try (PreparedStatement psMov = c.prepareStatement(
                    "INSERT INTO movimientos_inventario (medicamento_id, tipo, cantidad, usuario_id) " +
                            "VALUES (?, 'ENTRADA', ?, ?)")) {
                psMov.setInt(1, medicamentoId);
                psMov.setInt(2, cantidad);
                psMov.setInt(3, usuarioId);
                psMov.executeUpdate();
            }

            c.commit();
        } catch (SQLException e) {
            c.rollback();
            throw e;
        } finally {
            c.setAutoCommit(autoCommitOriginal);
        }
    }
}
