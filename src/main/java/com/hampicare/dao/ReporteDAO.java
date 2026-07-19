package com.hampicare.dao;

import com.hampicare.db.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReporteDAO {

    public static class VentaPeriodo {
        private int id;
        private String numeroFactura;
        private String cliente;
        private LocalDateTime fecha;
        private double total;

        public VentaPeriodo(int id, String numeroFactura, String cliente, LocalDateTime fecha, double total) {
            this.id = id;
            this.numeroFactura = numeroFactura;
            this.cliente = cliente;
            this.fecha = fecha;
            this.total = total;
        }

        public int getId() { return id; }
        public String getNumeroFactura() { return numeroFactura; }
        public String getCliente() { return cliente; }
        public LocalDateTime getFecha() { return fecha; }
        public double getTotal() { return total; }
    }

    public static class TopMedicamento {
        private int id;
        private String nombre;
        private String categoria;
        private int totalVendido;
        private double totalIngresos;

        public TopMedicamento(int id, String nombre, String categoria, int totalVendido, double totalIngresos) {
            this.id = id;
            this.nombre = nombre;
            this.categoria = categoria;
            this.totalVendido = totalVendido;
            this.totalIngresos = totalIngresos;
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public String getCategoria() { return categoria; }
        public int getTotalVendido() { return totalVendido; }
        public double getTotalIngresos() { return totalIngresos; }
    }

    public static class InventarioItem {
        private int id;
        private String nombre;
        private String categoria;
        private int stock;
        private double precio;
        private String lote;
        private String fechaVencimiento;
        private boolean stockBajo;
        private boolean porVencer;

        public InventarioItem(int id, String nombre, String categoria, int stock, double precio,
                              String lote, String fechaVencimiento, boolean stockBajo, boolean porVencer) {
            this.id = id;
            this.nombre = nombre;
            this.categoria = categoria;
            this.stock = stock;
            this.precio = precio;
            this.lote = lote;
            this.fechaVencimiento = fechaVencimiento;
            this.stockBajo = stockBajo;
            this.porVencer = porVencer;
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public String getCategoria() { return categoria; }
        public int getStock() { return stock; }
        public double getPrecio() { return precio; }
        public String getLote() { return lote; }
        public String getFechaVencimiento() { return fechaVencimiento; }
        public boolean isStockBajo() { return stockBajo; }
        public boolean isPorVencer() { return porVencer; }
    }

    public static class CompraPeriodo {
        private int id;
        private String proveedor;
        private String medicamento;
        private int cantidad;
        private double precioCompra;
        private LocalDateTime fecha;

        public CompraPeriodo(int id, String proveedor, String medicamento, int cantidad,
                             double precioCompra, LocalDateTime fecha) {
            this.id = id;
            this.proveedor = proveedor;
            this.medicamento = medicamento;
            this.cantidad = cantidad;
            this.precioCompra = precioCompra;
            this.fecha = fecha;
        }

        public int getId() { return id; }
        public String getProveedor() { return proveedor; }
        public String getMedicamento() { return medicamento; }
        public int getCantidad() { return cantidad; }
        public double getPrecioCompra() { return precioCompra; }
        public LocalDateTime getFecha() { return fecha; }
    }

    public static class ClienteReporte {
        private int id;
        private String nombre;
        private String cedula;
        private int totalCompras;
        private double totalGastado;

        public ClienteReporte(int id, String nombre, String cedula, int totalCompras, double totalGastado) {
            this.id = id;
            this.nombre = nombre;
            this.cedula = cedula;
            this.totalCompras = totalCompras;
            this.totalGastado = totalGastado;
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public String getCedula() { return cedula; }
        public int getTotalCompras() { return totalCompras; }
        public double getTotalGastado() { return totalGastado; }
    }

    public static class ResumenCaja {
        private double totalVentas;
        private double totalCompras;
        private int cantidadVentas;
        private int cantidadCompras;
        private double gananciaEstimada;

        public ResumenCaja(double totalVentas, double totalCompras, int cantidadVentas,
                           int cantidadCompras, double gananciaEstimada) {
            this.totalVentas = totalVentas;
            this.totalCompras = totalCompras;
            this.cantidadVentas = cantidadVentas;
            this.cantidadCompras = cantidadCompras;
            this.gananciaEstimada = gananciaEstimada;
        }

        public double getTotalVentas() { return totalVentas; }
        public double getTotalCompras() { return totalCompras; }
        public int getCantidadVentas() { return cantidadVentas; }
        public int getCantidadCompras() { return cantidadCompras; }
        public double getGananciaEstimada() { return gananciaEstimada; }
    }

    public List<VentaPeriodo> ventasPorPeriodo(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT v.id, v.numero_factura, " +
                "COALESCE(c.nombres || ' ' || c.apellidos, 'Sin cliente') AS cliente, " +
                "v.fecha, v.total " +
                "FROM ventas v LEFT JOIN clientes c ON v.cliente_id = c.id " +
                "WHERE CAST(v.fecha AS DATE) >= ? AND CAST(v.fecha AS DATE) <= ? " +
                "ORDER BY v.fecha DESC";
        List<VentaPeriodo> lista = new ArrayList<>();
        try (Connection conn = Conexion.getInstancia().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(desde));
            ps.setDate(2, java.sql.Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Timestamp ts = rs.getTimestamp("fecha");
                    lista.add(new VentaPeriodo(
                            rs.getInt("id"),
                            rs.getString("numero_factura"),
                            rs.getString("cliente"),
                            ts != null ? ts.toLocalDateTime() : null,
                            rs.getDouble("total")));
                }
            }
        }
        return lista;
    }

    public List<TopMedicamento> topMedicamentos() throws SQLException {
        String sql = "SELECT m.id, m.nombre, m.categoria, " +
                "COALESCE(SUM(dv.cantidad), 0) AS total_vendido, " +
                "COALESCE(SUM(dv.subtotal), 0) AS total_ingresos " +
                "FROM medicamentos m " +
                "LEFT JOIN detalle_ventas dv ON m.id = dv.medicamento_id " +
                "GROUP BY m.id, m.nombre, m.categoria " +
                "ORDER BY total_vendido DESC";
        List<TopMedicamento> lista = new ArrayList<>();
        try (Connection conn = Conexion.getInstancia().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new TopMedicamento(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("categoria"),
                        rs.getInt("total_vendido"),
                        rs.getDouble("total_ingresos")));
            }
        }
        return lista;
    }

    public List<InventarioItem> inventarioActual() throws SQLException {
        String sql = "SELECT m.id, m.nombre, m.categoria, m.stock, m.precio, m.lote, " +
                "m.fecha_vencimiento, " +
                "(SELECT COALESCE(c2.umbral_stock_bajo, 10) FROM configuracion c2 LIMIT 1) AS umbral_stock, " +
                "(SELECT COALESCE(c3.umbral_dias_vencimiento, 30) FROM configuracion c3 LIMIT 1) AS umbral_dias " +
                "FROM medicamentos m, configuracion c1 " +
                "ORDER BY m.nombre";
        List<InventarioItem> lista = new ArrayList<>();
        try (Connection conn = Conexion.getInstancia().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                java.sql.Date fv = rs.getDate("fecha_vencimiento");
                String fvStr = fv != null ? fv.toLocalDate().toString() : "N/A";
                int umbralStock = rs.getInt("umbral_stock");
                int umbralDias = rs.getInt("umbral_dias");
                boolean stockBajo = rs.getInt("stock") <= umbralStock;
                boolean porVencer = fv != null &&
                        fv.toLocalDate().isBefore(LocalDate.now().plusDays(umbralDias));
                lista.add(new InventarioItem(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("categoria"),
                        rs.getInt("stock"),
                        rs.getDouble("precio"),
                        rs.getString("lote"),
                        fvStr,
                        stockBajo,
                        porVencer));
            }
        }
        return lista;
    }

    public List<CompraPeriodo> comprasPorPeriodo(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT co.id, p.nombre AS proveedor, m.nombre AS medicamento, " +
                "co.cantidad, co.precio_compra, co.fecha " +
                "FROM compras co " +
                "JOIN proveedores p ON co.proveedor_id = p.id " +
                "JOIN medicamentos m ON co.medicamento_id = m.id " +
                "WHERE CAST(co.fecha AS DATE) >= ? AND CAST(co.fecha AS DATE) <= ? " +
                "ORDER BY co.fecha DESC";
        List<CompraPeriodo> lista = new ArrayList<>();
        try (Connection conn = Conexion.getInstancia().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(desde));
            ps.setDate(2, java.sql.Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Timestamp ts = rs.getTimestamp("fecha");
                    lista.add(new CompraPeriodo(
                            rs.getInt("id"),
                            rs.getString("proveedor"),
                            rs.getString("medicamento"),
                            rs.getInt("cantidad"),
                            rs.getDouble("precio_compra"),
                            ts != null ? ts.toLocalDateTime() : null));
                }
            }
        }
        return lista;
    }

    public List<ClienteReporte> clientesReporte() throws SQLException {
        String sql = "SELECT c.id, c.nombres || ' ' || c.apellidos AS nombre, c.cedula, " +
                "COALESCE(ven.total_ventas, 0) AS total_ventas, " +
                "COALESCE(ven.total_gastado, 0) AS total_gastado " +
                "FROM clientes c " +
                "LEFT JOIN ( " +
                "  SELECT cliente_id, COUNT(*) AS total_ventas, SUM(total) AS total_gastado " +
                "  FROM ventas GROUP BY cliente_id " +
                ") ven ON c.id = ven.cliente_id " +
                "ORDER BY total_gastado DESC";
        List<ClienteReporte> lista = new ArrayList<>();
        try (Connection conn = Conexion.getInstancia().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new ClienteReporte(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("cedula"),
                        rs.getInt("total_ventas"),
                        rs.getDouble("total_gastado")));
            }
        }
        return lista;
    }

    public ResumenCaja resumenCaja(LocalDate desde, LocalDate hasta) throws SQLException {
        String sqlVentas = "SELECT COUNT(*), COALESCE(SUM(total), 0) FROM ventas " +
                "WHERE CAST(fecha AS DATE) >= ? AND CAST(fecha AS DATE) <= ?";
        String sqlCompras = "SELECT COUNT(*), COALESCE(SUM(cantidad * precio_compra), 0) FROM compras " +
                "WHERE CAST(fecha AS DATE) >= ? AND CAST(fecha AS DATE) <= ?";
        int cv = 0;
        double tv = 0;
        int cc = 0;
        double tc = 0;
        try (Connection conn = Conexion.getInstancia().getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlVentas)) {
                ps.setDate(1, java.sql.Date.valueOf(desde));
                ps.setDate(2, java.sql.Date.valueOf(hasta));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        cv = rs.getInt(1);
                        tv = rs.getDouble(2);
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlCompras)) {
                ps.setDate(1, java.sql.Date.valueOf(desde));
                ps.setDate(2, java.sql.Date.valueOf(hasta));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        cc = rs.getInt(1);
                        tc = rs.getDouble(2);
                    }
                }
            }
        }
        return new ResumenCaja(tv, tc, cv, cc, tv - tc);
    }

    public Map<String, double[]> ventasPorDia(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT CAST(fecha AS DATE) AS dia, COUNT(*) AS cantidad, SUM(total) AS total " +
                "FROM ventas WHERE CAST(fecha AS DATE) >= ? AND CAST(fecha AS DATE) <= ? " +
                "GROUP BY CAST(fecha AS DATE) ORDER BY dia";
        Map<String, double[]> mapa = new LinkedHashMap<>();
        try (Connection conn = Conexion.getInstancia().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(desde));
            ps.setDate(2, java.sql.Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date d = rs.getDate("dia");
                    mapa.put(d.toLocalDate().toString(),
                            new double[]{rs.getInt("cantidad"), rs.getDouble("total")});
                }
            }
        }
        return mapa;
    }
}
