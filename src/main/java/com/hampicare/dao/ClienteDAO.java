package com.hampicare.dao;

import com.hampicare.db.Conexion;
import com.hampicare.model.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO implements ICRUD<Cliente> {

    @Override
    public void guardar(Cliente c) throws Exception {
        String sql = "INSERT INTO clientes (nombres, apellidos, correo, cedula, telefono, sector) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexion.getInstancia().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, c.getNombre());
            stmt.setString(2, c.getApellidos());
            stmt.setString(3, c.getCorreo());
            stmt.setString(4, c.getCedula());
            stmt.setString(5, c.getTelefono());
            stmt.setString(6, c.getSector());

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

    public Cliente leer(int id) throws Exception {
        String sql = "SELECT * FROM clientes WHERE id = ?";
        try (Connection conn = Conexion.getInstancia().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCliente(rs);
                }
            }
                } catch (SQLException e) {
            throw new Exception("DB Error", e);
        }
        return null;
    }

    @Override
    public void actualizar(Cliente c) throws Exception {
        String sql = "UPDATE clientes SET nombres=?, apellidos=?, correo=?, cedula=?, telefono=?, sector=? WHERE id=?";
        try (Connection conn = Conexion.getInstancia().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, c.getNombre());
            stmt.setString(2, c.getApellidos());
            stmt.setString(3, c.getCorreo());
            stmt.setString(4, c.getCedula());
            stmt.setString(5, c.getTelefono());
            stmt.setString(6, c.getSector());
            stmt.setInt(7, c.getId());

            stmt.executeUpdate();
                } catch (SQLException e) {
            throw new Exception("DB Error", e);
        }
        
    }

    @Override
    public void eliminar(int id) throws Exception {
        String sql = "DELETE FROM clientes WHERE id=?";
        try (Connection conn = Conexion.getInstancia().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new Exception("No se puede eliminar el cliente porque tiene facturas/ventas asociadas.");
            }
            throw new Exception("DB Error: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Cliente> listar() throws Exception {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes ORDER BY apellidos, nombres";
        try (Connection conn = Conexion.getInstancia().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapResultSetToCliente(rs));
            }
                } catch (SQLException e) {
            throw new Exception("DB Error", e);
        }
        return lista;
    }

    private Cliente mapResultSetToCliente(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getInt("id"),
                rs.getString("nombres"),
                rs.getString("apellidos"),
                rs.getString("correo"),
                rs.getString("cedula"),
                rs.getString("telefono"),
                rs.getString("sector")
        );
    }
}
