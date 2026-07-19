package com.hampicare.dao;

import com.hampicare.db.Conexion;
import com.hampicare.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO implements ICRUD<Usuario> {

    @Override
    public void guardar(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre, correo, contrasena, rol, activo) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = Conexion.getInstancia().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getCorreo());
            ps.setString(3, u.getContrasena());
            ps.setString(4, u.getRol());
            ps.setBoolean(5, u.isActivo());
            ps.executeUpdate();
        }
    }

    @Override
    public List<Usuario> listar() throws SQLException {
        String sql = "SELECT id, nombre, correo, contrasena, rol, activo FROM usuarios ORDER BY id";
        List<Usuario> lista = new ArrayList<>();
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
    public void actualizar(Usuario u) throws SQLException {
        String sql = "UPDATE usuarios SET nombre=?, correo=?, contrasena=?, rol=?, activo=? WHERE id=?";
        try (Connection c = Conexion.getInstancia().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getCorreo());
            ps.setString(3, u.getContrasena());
            ps.setString(4, u.getRol());
            ps.setBoolean(5, u.isActivo());
            ps.setInt(6, u.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id=?";
        try (Connection c = Conexion.getInstancia().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Valida credenciales de login */
    public Usuario autenticar(String correo, String contrasena) throws SQLException {
        String sql = "SELECT id, nombre, correo, contrasena, rol, activo FROM usuarios " +
                "WHERE correo=? AND contrasena=? AND activo=true";
        System.out.println("[UsuarioDAO] autenticar: " + sql);
        try (Connection c = Conexion.getInstancia().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, correo);
            ps.setString(2, contrasena);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("[UsuarioDAO] autenticar: usuario encontrado id=" + rs.getInt("id"));
                    return mapear(rs);
                }
            }
        }
        System.out.println("[UsuarioDAO] autenticar: usuario NO encontrado");
        return null;
    }

    /** Validación obligatoria: no permitir correos duplicados al registrar. */
    public boolean existeCorreo(String correo) throws SQLException {
        String sql = "SELECT 1 FROM usuarios WHERE correo=?";
        try (Connection c = Conexion.getInstancia().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        return Usuario.crearPorRol(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("correo"),
                rs.getString("contrasena"),
                rs.getString("rol"),
                rs.getBoolean("activo")
        );
    }
}
