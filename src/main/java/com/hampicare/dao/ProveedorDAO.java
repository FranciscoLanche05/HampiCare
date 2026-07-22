package com.hampicare.dao;

import com.hampicare.db.Conexion;
import com.hampicare.model.Proveedor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO implements ICRUD<Proveedor> {

    @Override
    public void guardar(Proveedor p) throws Exception {
        String sql = "INSERT INTO proveedores (nombre, correo, telefono) VALUES (?, ?, ?)";
        try (Connection conn = Conexion.getInstancia().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, p.getNombre());
            stmt.setString(2, p.getCorreo());
            stmt.setString(3, p.getTelefono());

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        p.setId(rs.getInt(1));
                    }
                }
                
            }
                } catch (SQLException e) {
            throw new Exception("DB Error", e);
        }
        
    }

    public Proveedor leer(int id) throws Exception {
        String sql = "SELECT * FROM proveedores WHERE id = ?";
        try (Connection conn = Conexion.getInstancia().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProveedor(rs);
                }
            }
                } catch (SQLException e) {
            throw new Exception("DB Error", e);
        }
        return null;
    }

    @Override
    public void actualizar(Proveedor p) throws Exception {
        String sql = "UPDATE proveedores SET nombre=?, correo=?, telefono=? WHERE id=?";
        try (Connection conn = Conexion.getInstancia().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getNombre());
            stmt.setString(2, p.getCorreo());
            stmt.setString(3, p.getTelefono());
            stmt.setInt(4, p.getId());

            stmt.executeUpdate();
                } catch (SQLException e) {
            throw new Exception("DB Error", e);
        }
        
    }

    @Override
    public void eliminar(int id) throws Exception {
        String sql = "DELETE FROM proveedores WHERE id=?";
        try (Connection conn = Conexion.getInstancia().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new Exception("No se puede eliminar el proveedor porque tiene historial de compras asociado.");
            }
            throw new Exception("DB Error: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Proveedor> listar() throws Exception {
        List<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT * FROM proveedores ORDER BY nombre";
        try (Connection conn = Conexion.getInstancia().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapResultSetToProveedor(rs));
            }
                } catch (SQLException e) {
            throw new Exception("DB Error", e);
        }
        return lista;
    }

    private Proveedor mapResultSetToProveedor(ResultSet rs) throws SQLException {
        return new Proveedor(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("correo"),
                rs.getString("telefono")
        );
    }
}
