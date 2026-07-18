package com.hampicare.dao;
import java.util.List;

public interface ICRUD<T> {

    void guardar(T entidad) throws Exception;

    List<T> listar() throws Exception;

    void actualizar(T entidad) throws Exception;

    void eliminar(int id) throws Exception;
}
