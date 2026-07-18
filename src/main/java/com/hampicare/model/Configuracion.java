package com.hampicare.model;

/** Parámetros generales del sistema. */
public class Configuracion {

    private int id = 1;
    private String nombreEmpresa;
    private double iva;
    private int umbralStockBajo;
    private int umbralDiasVencimiento;

    public Configuracion() {
    }

    public Configuracion(int id, String nombreEmpresa, double iva, int umbralStockBajo, int umbralDiasVencimiento) {
        this.id = id;
        this.nombreEmpresa = nombreEmpresa;
        this.iva = iva;
        this.umbralStockBajo = umbralStockBajo;
        this.umbralDiasVencimiento = umbralDiasVencimiento;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public double getIva() {
        return iva;
    }

    public void setIva(double iva) {
        this.iva = iva;
    }

    public int getUmbralStockBajo() {
        return umbralStockBajo;
    }

    public void setUmbralStockBajo(int umbralStockBajo) {
        this.umbralStockBajo = umbralStockBajo;
    }

    public int getUmbralDiasVencimiento() {
        return umbralDiasVencimiento;
    }

    public void setUmbralDiasVencimiento(int umbralDiasVencimiento) {
        this.umbralDiasVencimiento = umbralDiasVencimiento;
    }
}
