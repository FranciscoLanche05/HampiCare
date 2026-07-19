package com.hampicare.controller;

import com.hampicare.app.Main;
import com.hampicare.dao.UsuarioDAO;
import com.hampicare.model.Usuario;
import com.hampicare.util.Alertas;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

import java.sql.SQLException;

public class LoginController {

    @FXML private TextField usuarioField;
    @FXML private PasswordField contrasenaField;
    @FXML private ToggleGroup rolToggleGroup;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    private void handleLogin() {
        String correo = usuarioField.getText() == null ? "" : usuarioField.getText().trim();
        String contrasena = contrasenaField.getText() == null ? "" : contrasenaField.getText().trim();

        Toggle seleccionado = rolToggleGroup.getSelectedToggle();

        // Validación obligatoria: rol y campos vacíos
        if (seleccionado == null) {
            Alertas.error("Selecciona tu rol antes de ingresar.");
            return;
        }
        if (correo.isEmpty() || contrasena.isEmpty()) {
            Alertas.error("Ingresa tu usuario y contraseña.");
            return;
        }

        String rolSeleccionado = (String) seleccionado.getUserData();

        try {
            System.out.println("[LOGIN] Intentando autenticar: " + correo);
            Usuario usuario = usuarioDAO.autenticar(correo, contrasena);
            System.out.println("[LOGIN] Resultado autenticacion: " + (usuario != null ? usuario.getNombre() + " (" + usuario.getRol() + ")" : "null"));

            if (usuario == null) {
                Alertas.error("Usuario o contraseña incorrectos.");
                return;
            }

            // Validación extra: el rol elegido en el login debe coincidir con el rol real que tiene el usuario guardado en la base de datos.
            if (!usuario.getRol().equalsIgnoreCase(rolSeleccionado)) {
                Alertas.error("Este usuario no tiene el rol \"" + rolSeleccionado +
                        "\". Verifica el rol seleccionado e intenta de nuevo.");
                return;
            }

            System.out.println("[LOGIN] Navegando al dashboard...");
            // Redirección al dashboard único por rol
            Main.irADashboard(usuario);
            System.out.println("[LOGIN] Dashboard cargado correctamente.");

        } catch (SQLException e) {
            System.err.println("[LOGIN] SQLException: " + e.getMessage());
            e.printStackTrace();
            Alertas.error("No se pudo conectar a la base de datos.\n" +
                    "Revisa la configuración en db.Conexion.\n\nDetalle: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[LOGIN] Exception: " + e.getMessage());
            e.printStackTrace();
            Alertas.error("Ocurrió un error inesperado: " + e.getMessage());
        }
    }

    @FXML
    private void handleForgotPassword() {
        Alertas.info("Contacta al administrador del sistema para restablecer tu contraseña.");
    }
}
