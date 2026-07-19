package com.hampicare.dao;

import com.hampicare.db.Conexion;
import com.hampicare.model.Configuracion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfiguracionDAO {

    public Configuracion obtener() throws SQLException {
        String sql = "SELECT id, nombre_empresa, iva, umbral_stock_bajo, umbral_dias_vencimiento " +
                "FROM configuracion WHERE id = 1";
        try (Connection c = Conexion.getInstancia().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new Configuracion(
                        rs.getInt("id"),
                        rs.getString("nombre_empresa"),
                        rs.getDouble("iva"),
                        rs.getInt("umbral_stock_bajo"),
                        rs.getInt("umbral_dias_vencimiento")
                );
            }
        }
        return new Configuracion(1, "HampiCare", 0.15, 15, 30);
    }

    public void actualizar(Configuracion cfg) throws SQLException {
        String sql = "UPDATE configuracion SET nombre_empresa=?, iva=?, umbral_stock_bajo=?, " +
                "umbral_dias_vencimiento=? WHERE id=1";
        try (Connection c = Conexion.getInstancia().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, cfg.getNombreEmpresa());
            ps.setDouble(2, cfg.getIva());
            ps.setInt(3, cfg.getUmbralStockBajo());
            ps.setInt(4, cfg.getUmbralDiasVencimiento());
            ps.executeUpdate();
        }
    }
}
