package com.hampicare.dao;

import com.hampicare.db.Conexion;
import com.hampicare.model.Compra;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CompraDAO implements ICRUD<Compra> {

    @Override
    public void guardar(Compra c) throws Exception {
        String sql = "INSERT INTO compras (proveedor_id, medicamento_id, cantidad, precio_compra, fecha) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Conexion.getInstancia().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, c.getProveedorId());
            stmt.setInt(2, c.getMedicamentoId());
            stmt.setInt(3, c.getCantidad());
            stmt.setDouble(4, c.getPrecioCompra());
            stmt.setTimestamp(5, Timestamp.valueOf(c.getFecha()));

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        c.setId(rs.getInt(1));
                    }
                }
                
            }
                } catch (SQLException e) {
            throw new Exception("DB Error", e);
        }
        
    }

    public Compra leer(int id) throws Exception {
        String sql = "SELECT c.*, p.nombre AS proveedor_nombre, m.nombre AS medicamento_nombre " +
                     "FROM compras c " +
                     "JOIN proveedores p ON c.proveedor_id = p.id " +
                     "JOIN medicamentos m ON c.medicamento_id = m.id " +
                     "WHERE c.id = ?";
        try (Connection conn = Conexion.getInstancia().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCompra(rs);
                }
            }
                } catch (SQLException e) {
            throw new Exception("DB Error", e);
        }
        return null;
    }

    @Override
    public void actualizar(Compra c) throws Exception {
        // Normally, purchases are not updated to maintain accounting consistency.
        // But for completeness of ICRUD:
        String sql = "UPDATE compras SET proveedor_id=?, medicamento_id=?, cantidad=?, precio_compra=? WHERE id=?";
        try (Connection conn = Conexion.getInstancia().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, c.getProveedorId());
            stmt.setInt(2, c.getMedicamentoId());
            stmt.setInt(3, c.getCantidad());
            stmt.setDouble(4, c.getPrecioCompra());
            stmt.setInt(5, c.getId());

            stmt.executeUpdate();
                } catch (SQLException e) {
            throw new Exception("DB Error", e);
        }
        
    }

    @Override
    public void eliminar(int id) throws Exception {
        String sql = "DELETE FROM compras WHERE id=?";
        try (Connection conn = Conexion.getInstancia().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
                } catch (SQLException e) {
            throw new Exception("DB Error", e);
        }
        
    }

    @Override
    public List<Compra> listar() throws Exception {
        List<Compra> lista = new ArrayList<>();
        String sql = "SELECT c.*, p.nombre AS proveedor_nombre, m.nombre AS medicamento_nombre " +
                     "FROM compras c " +
                     "JOIN proveedores p ON c.proveedor_id = p.id " +
                     "JOIN medicamentos m ON c.medicamento_id = m.id " +
                     "ORDER BY c.fecha DESC";
        try (Connection conn = Conexion.getInstancia().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapResultSetToCompra(rs));
            }
                } catch (SQLException e) {
            throw new Exception("DB Error", e);
        }
        return lista;
    }

    private Compra mapResultSetToCompra(ResultSet rs) throws SQLException {
        Compra c = new Compra(
                rs.getInt("id"),
                rs.getInt("proveedor_id"),
                rs.getInt("medicamento_id"),
                rs.getInt("cantidad"),
                rs.getDouble("precio_compra"),
                rs.getTimestamp("fecha").toLocalDateTime()
        );
        c.setProveedorNombre(rs.getString("proveedor_nombre"));
        c.setMedicamentoNombre(rs.getString("medicamento_nombre"));
        return c;
    }
}
