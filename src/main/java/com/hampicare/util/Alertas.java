package com.hampicare.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public final class Alertas {

    private Alertas() {
    }

    public static void error(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static void info(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /** Devuelve true si el usuario confirma con "Sí". */
    public static boolean confirmar(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        ButtonType si = new ButtonType("Sí");
        ButtonType no = new ButtonType("No", ButtonType.CANCEL.getButtonData());
        alert.getButtonTypes().setAll(si, no);

        Optional<ButtonType> resultado = alert.showAndWait();
        return resultado.isPresent() && resultado.get() == si;
    }
}
