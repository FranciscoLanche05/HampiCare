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

    public Venta registrarVenta(int usuarioId, int clienteId, List<DetalleVenta> items) throws SQLException {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un producto.");
        }

        Connection c = Conexion.getInstancia().getConnection();
        boolean autoCommitOriginal = c.getAutoCommit();
        c.setAutoCommit(false);
        try {
            double total = 0;
            for (DetalleVenta d : items) total += d.getSubtotal();

            String numeroFactura = generarNumeroFactura(c);

            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO ventas (usuario_id, cliente_id, numero_factura, total) VALUES (?, ?, ?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, usuarioId);
                ps.setInt(2, clienteId);
                ps.setString(3, numeroFactura);
                ps.setDouble(4, total);
                ps.executeUpdate();

                int nuevoId;
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    nuevoId = rs.getInt(1);
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
                return new Venta(nuevoId, usuarioId, clienteId, numeroFactura, LocalDateTime.now(), total);
            }
        } catch (SQLException e) {
            c.rollback();
            throw e;
        } finally {
            c.setAutoCommit(autoCommitOriginal);
        }
    }

    private String generarNumeroFactura(Connection c) throws SQLException {
        String sql = "SELECT COUNT(*) + 1 FROM ventas";
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return "FAC-" + String.format("%04d", rs.getInt(1));
        }
    }

    public List<Venta> listarPorUsuario(int usuarioId) throws SQLException {
        String sql = "SELECT v.*, c.nombres || ' ' || c.apellidos AS cliente_nombre " +
                "FROM ventas v LEFT JOIN clientes c ON v.cliente_id = c.id " +
                "WHERE v.usuario_id = ? ORDER BY v.fecha DESC";
        System.out.println("[VentaDAO] listarPorUsuario: " + sql);
        List<Venta> lista = new ArrayList<>();
        try (Connection conn = Conexion.getInstancia().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public List<Venta> listarTodas() throws SQLException {
        String sql = "SELECT v.*, c.nombres || ' ' || c.apellidos AS cliente_nombre " +
                "FROM ventas v LEFT JOIN clientes c ON v.cliente_id = c.id " +
                "ORDER BY v.fecha DESC";
        System.out.println("[VentaDAO] listarTodas: " + sql);
        List<Venta> lista = new ArrayList<>();
        try (Connection conn = Conexion.getInstancia().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public List<DetalleVenta> obtenerDetalle(int ventaId, Connection c) throws SQLException {
        String sql = "SELECT dv.medicamento_id, m.nombre, dv.cantidad, dv.precio_unitario, dv.subtotal " +
                "FROM detalle_ventas dv JOIN medicamentos m ON m.id = dv.medicamento_id " +
                "WHERE dv.venta_id = ?";
        List<DetalleVenta> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
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

    public List<DetalleVenta> obtenerDetalle(int ventaId) throws SQLException {
        return obtenerDetalle(ventaId, Conexion.getInstancia().getConnection());
    }

    public void anularVenta(int ventaId, int usuarioId) throws SQLException {
        Connection c = Conexion.getInstancia().getConnection();
        boolean autoCommitOriginal = c.getAutoCommit();
        c.setAutoCommit(false);
        try {
            List<DetalleVenta> detalles = obtenerDetalle(ventaId, c);
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

    public double[] resumenHoy(int usuarioId) throws SQLException {
        String sql = "SELECT COUNT(*), COALESCE(SUM(total), 0) FROM ventas " +
                "WHERE usuario_id = ? AND CAST(fecha AS DATE) = CURRENT_DATE";
        System.out.println("[VentaDAO] resumenHoy: " + sql);
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

    public double[] resumenHoyTodas() throws SQLException {
        String sql = "SELECT COUNT(*), COALESCE(SUM(total), 0) FROM ventas " +
                "WHERE CAST(fecha AS DATE) = CURRENT_DATE";
        try (Connection c = Conexion.getInstancia().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new double[]{rs.getInt(1), rs.getDouble(2)};
            }
        }
        return new double[]{0, 0};
    }

    public List<Venta> ultimasVentas(int usuarioId, int limite) throws SQLException {
        String sql = "SELECT v.*, c.nombres || ' ' || c.apellidos AS cliente_nombre " +
                "FROM ventas v LEFT JOIN clientes c ON v.cliente_id = c.id " +
                "WHERE v.usuario_id = ? ORDER BY v.fecha DESC LIMIT ?";
        System.out.println("[VentaDAO] ultimasVentas: " + sql + " params=[" + usuarioId + ", " + limite + "]");
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

    public List<Venta> ultimasVentasTodas(int limite) throws SQLException {
        String sql = "SELECT v.*, c.nombres || ' ' || c.apellidos AS cliente_nombre " +
                "FROM ventas v LEFT JOIN clientes c ON v.cliente_id = c.id " +
                "ORDER BY v.fecha DESC LIMIT ?";
        List<Venta> lista = new ArrayList<>();
        try (Connection c = Conexion.getInstancia().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limite);
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
        Venta v = new Venta(
                rs.getInt("id"),
                rs.getInt("usuario_id"),
                rs.getInt("cliente_id"),
                rs.getString("numero_factura"),
                fecha != null ? fecha.toLocalDateTime() : null,
                rs.getDouble("total")
        );
        try {
            v.setClienteNombre(rs.getString("cliente_nombre"));
        } catch (SQLException ignored) {
        }
        return v;
    }
}
