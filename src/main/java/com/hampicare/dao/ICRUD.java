package com.hampicare.dao;
import java.util.List;

/**
 * PILAR APLICADO: Abstracción.
 * Define el contrato mínimo (guardar, listar, actualizar, eliminar) que
 * cualquier DAO del sistema debe cumplir, sin importar la tabla que
 * gestione. Cada DAO concreto decide CÓMO lo hace (SQL específico).
 */
public interface ICRUD<T> {

    void guardar(T entidad) throws Exception;

    List<T> listar() throws Exception;

    void actualizar(T entidad) throws Exception;

    void eliminar(int id) throws Exception;
}
