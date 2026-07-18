package com.hampicare.dao;

import com.hampicare.db.Conexion;
import com.hampicare.model.DetalleVenta;
import com.hampicare.model.Venta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VentaDAO {

    public Venta registrarVenta(int usuarioId, List<DetalleVenta> items) throws SQLException {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un producto.");
        }

        Connection c = Conexion.getInstancia().getConnection();
        boolean autoCommitOriginal = c.getAutoCommit();
        c.setAutoCommit(false);
        try {
            int nuevoId;
            try (PreparedStatement psSeq = c.prepareStatement("SELECT nextval('ventas_id_seq')");
                 ResultSet rs = psSeq.executeQuery()) {
                rs.next();
                nuevoId = rs.getInt(1);
            }

            String numeroFactura = "FAC-" + String.format("%04d", nuevoId);
            double total = 0;
            for (DetalleVenta d : items) total += d.getSubtotal();

            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO ventas (id, usuario_id, numero_factura, total) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, nuevoId);
                ps.setInt(2, usuarioId);
                ps.setString(3, numeroFactura);
                ps.setDouble(4, total);
                ps.executeUpdate();
            }

            try (PreparedStatement psDet = c.prepareStatement(
                    "INSERT INTO detalle_ventas (venta_id, medicamento_id, cantidad, precio_unitario, subtotal) " +
                            "VALUES (?, ?, ?, ?, ?)");
                 PreparedStatement psStock = c.prepareStatement(
                         "UPDATE medicamentos SET stock = stock - ? WHERE id = ? AND stock >= ?");
                 PreparedStatement psMov = c.prepareStatement(
                         "INSERT INTO movimientos_inventario (medicamento_id, tipo, cantidad, usuario_id) " +
                                 "VALUES (?, 'SALIDA', ?, ?)")) {

                for (DetalleVenta d : items) {
                    psStock.setInt(1, d.getCantidad());
                    psStock.setInt(2, d.getMedicamentoId());
                    psStock.setInt(3, d.getCantidad());
                    int filas = psStock.executeUpdate();
                    if (filas == 0) {
                        throw new SQLException("Stock insuficiente para \"" + d.getNombreMedicamento() + "\".");
                    }

                    psDet.setInt(1, nuevoId);
                    psDet.setInt(2, d.getMedicamentoId());
                    psDet.setInt(3, d.getCantidad());
                    psDet.setDouble(4, d.getPrecioUnitario());
                    psDet.setDouble(5, d.getSubtotal());
                    psDet.executeUpdate();

                    psMov.setInt(1, d.getMedicamentoId());
                    psMov.setInt(2, d.getCantidad());
                    psMov.setInt(3, usuarioId);
                    psMov.executeUpdate();
                }
            }

            c.commit();
            return new Venta(nuevoId, usuarioId, numeroFactura, LocalDateTime.now(), total);

        } catch (SQLException e) {
            c.rollback();
            throw e;
        } finally {
            c.setAutoCommit(autoCommitOriginal);
        }
    }

    /** Historial de ventas de un cajero **/
    public List<Venta> listarPorUsuario(int usuarioId) throws SQLException {
        String sql = "SELECT id, usuario_id, numero_factura, fecha, total FROM ventas " +
                "WHERE usuario_id = ? ORDER BY fecha DESC";
        List<Venta> lista = new ArrayList<>();
        try (Connection c = Conexion.getInstancia().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** Detalle de productos de una venta */
    public List<DetalleVenta> obtenerDetalle(int ventaId) throws SQLException {
        String sql = "SELECT dv.medicamento_id, m.nombre, dv.cantidad, dv.precio_unitario, dv.subtotal " +
                "FROM detalle_ventas dv JOIN medicamentos m ON m.id = dv.medicamento_id " +
                "WHERE dv.venta_id = ?";
        List<DetalleVenta> lista = new ArrayList<>();
        try (Connection c = Conexion.getInstancia().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, ventaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleVenta d = new DetalleVenta(
                            rs.getInt("medicamento_id"),
                            rs.getString("nombre"),
                            rs.getInt("cantidad"),
                            rs.getDouble("precio_unitario"));
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    public void anularVenta(int ventaId, int usuarioId) throws SQLException {
        Connection c = Conexion.getInstancia().getConnection();
        boolean autoCommitOriginal = c.getAutoCommit();
        c.setAutoCommit(false);
        try {
            List<DetalleVenta> detalles = obtenerDetalle(ventaId);
            if (detalles.isEmpty()) {
                throw new SQLException("La venta no existe o ya fue anulada.");
            }

            try (PreparedStatement psStock = c.prepareStatement(
                    "UPDATE medicamentos SET stock = stock + ? WHERE id = ?");
                 PreparedStatement psMov = c.prepareStatement(
                         "INSERT INTO movimientos_inventario (medicamento_id, tipo, cantidad, usuario_id) " +
                                 "VALUES (?, 'ENTRADA', ?, ?)")) {
                for (DetalleVenta d : detalles) {
                    psStock.setInt(1, d.getCantidad());
                    psStock.setInt(2, d.getMedicamentoId());
                    psStock.executeUpdate();

                    psMov.setInt(1, d.getMedicamentoId());
                    psMov.setInt(2, d.getCantidad());
                    psMov.setInt(3, usuarioId);
                    psMov.executeUpdate();
                }
            }

            try (PreparedStatement psDel = c.prepareStatement(
                    "DELETE FROM ventas WHERE id = ? AND usuario_id = ?")) {
                psDel.setInt(1, ventaId);
                psDel.setInt(2, usuarioId);
                int filas = psDel.executeUpdate();
                if (filas == 0) {
                    throw new SQLException("No puedes anular una venta que no te pertenece.");
                }
            }

            c.commit();
        } catch (SQLException e) {
            c.rollback();
            throw e;
        } finally {
            c.setAutoCommit(autoCommitOriginal);
        }
    }

    /** Total vendido y número de transacciones. */
    public double[] resumenHoy(int usuarioId) throws SQLException {
        String sql = "SELECT COUNT(*), COALESCE(SUM(total), 0) FROM ventas " +
                "WHERE usuario_id = ? AND fecha::date = CURRENT_DATE";
        try (Connection c = Conexion.getInstancia().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new double[]{rs.getInt(1), rs.getDouble(2)};
                }
            }
        }
        return new double[]{0, 0};
    }

    /** Últimas N ventas de un usuario. */
    public List<Venta> ultimasVentas(int usuarioId, int limite) throws SQLException {
        String sql = "SELECT id, usuario_id, numero_factura, fecha, total FROM ventas " +
                "WHERE usuario_id = ? ORDER BY fecha DESC LIMIT ?";
        List<Venta> lista = new ArrayList<>();
        try (Connection c = Conexion.getInstancia().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setInt(2, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private Venta mapear(ResultSet rs) throws SQLException {
        Timestamp fecha = rs.getTimestamp("fecha");
        return new Venta(
                rs.getInt("id"),
                rs.getInt("usuario_id"),
                rs.getString("numero_factura"),
                fecha != null ? fecha.toLocalDateTime() : null,
                rs.getDouble("total")
        );
    }
}
