package com.hampicare.dao;

import com.hampicare.db.Conexion;
import com.hampicare.model.Medicamento;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MedicamentoDAO implements ICRUD<Medicamento> {

    @Override
    public void guardar(Medicamento m) throws SQLException {
        String sql = "INSERT INTO medicamentos (nombre, categoria, precio, stock, lote, fecha_vencimiento, proveedor_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = Conexion.getInstancia().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, m.getNombre());
            ps.setString(2, m.getCategoria());
            ps.setDouble(3, m.getPrecio());
            ps.setInt(4, m.getStock());
            ps.setString(5, m.getLote());
            ps.setDate(6, m.getFechaVencimiento() != null ? Date.valueOf(m.getFechaVencimiento()) : null);
            ps.setInt(7, m.getProveedorId());
            ps.executeUpdate();
        }
    }

    @Override
    public List<Medicamento> listar() throws SQLException {
        String sql = "SELECT id, nombre, categoria, precio, stock, lote, fecha_vencimiento, proveedor_id " +
                "FROM medicamentos ORDER BY id";
        List<Medicamento> lista = new ArrayList<>();
        try (Connection c = Conexion.getInstancia().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public void actualizar(Medicamento m) throws SQLException {
        String sql = "UPDATE medicamentos SET nombre=?, categoria=?, precio=?, stock=?, lote=?, " +
                "fecha_vencimiento=?, proveedor_id=? WHERE id=?";
        try (Connection c = Conexion.getInstancia().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, m.getNombre());
            ps.setString(2, m.getCategoria());
            ps.setDouble(3, m.getPrecio());
            ps.setInt(4, m.getStock());
            ps.setString(5, m.getLote());
            ps.setDate(6, m.getFechaVencimiento() != null ? Date.valueOf(m.getFechaVencimiento()) : null);
            ps.setInt(7, m.getProveedorId());
            ps.setInt(8, m.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM medicamentos WHERE id=?";
        try (Connection c = Conexion.getInstancia().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Validación obligatoria: no permitir nombres duplicados **/
    public boolean existeNombre(String nombre) throws SQLException {
        String sql = "SELECT 1 FROM medicamentos WHERE nombre=?";
        try (Connection c = Conexion.getInstancia().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int contarStockBajo(int umbral) throws SQLException {
        String sql = "SELECT COUNT(*) FROM medicamentos WHERE stock <= ?";
        try (Connection c = Conexion.getInstancia().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, umbral);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Medicamento mapear(ResultSet rs) throws SQLException {
        Date fv = rs.getDate("fecha_vencimiento");
        return new Medicamento(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("categoria"),
                rs.getDouble("precio"),
                rs.getInt("stock"),
                rs.getString("lote"),
                fv != null ? fv.toLocalDate() : null,
                rs.getInt("proveedor_id")
        );
    }
}
