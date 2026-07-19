package com.hampicare.model;

import java.time.LocalDateTime;

/** Cabecera de una venta.*/
public class Venta {

    private int id;
    private int usuarioId;
    private int clienteId;
    private String numeroFactura;
    private LocalDateTime fecha;
    private double total;
    private String clienteNombre;

    public Venta() {
    }

    public Venta(int id, int usuarioId, int clienteId, String numeroFactura, LocalDateTime fecha, double total) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.clienteId = clienteId;
        this.numeroFactura = numeroFactura;
        this.fecha = fecha;
        this.total = total;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }
    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
}
