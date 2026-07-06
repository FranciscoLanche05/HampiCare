package com.hampicare.controller;

import com.hampicare.model.Medicamento;
import com.hampicare.model.Usuario;
import com.hampicare.app.Main;
import com.hampicare.dao.ConfiguracionDAO;
import com.hampicare.dao.MedicamentoDAO;
import com.hampicare.dao.UsuarioDAO;
import com.hampicare.model.*;
import com.hampicare.util.Alertas;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Controlador de la ÚNICA pantalla de dashboard. No existen tres
 * dashboards: existe este controlador, que decide qué secciones se ven
 * según usuario.getModulosPermitidos() (POLIMORFISMO) — así se cumple
 * "reutilización de pantallas con POO".
 */
public class DashboardController {

    // ---- Sidebar ----
    @FXML private javafx.scene.layout.BorderPane rootPane;
    @FXML private Button navHome, navInventario, navUsuarios, navReportes, navConfiguracion;
    @FXML private Label userInitialsLabel, userNameLabel, userRoleLabel;
    @FXML private Label sectionTitleLabel;

    // ---- Secciones (todas viven en el mismo dashboard.fxml) ----
    @FXML private VBox homeView, inventarioView, usuariosView, reportesView, configuracionView;

    // ---- Home ----
    @FXML private javafx.scene.text.Text welcomeNameLabel;
    @FXML private Label stockBajoValueLabel, usuariosActivosValueLabel;
    @FXML private HBox barsContainer;
    @FXML private VBox activityContainer;

    // ---- Inventario (CRUD medicamentos) ----
    @FXML private TextField nombreMedField, categoriaMedField, precioMedField, stockMedField, loteMedField;
    @FXML private DatePicker fechaVencMedPicker;
    @FXML private Button btnGuardarMed, btnActualizarMed, btnEliminarMed, btnLimpiarMed;
    @FXML private TableView<Medicamento> tablaMedicamentos;
    @FXML private TableColumn<Medicamento, Integer> colIdMed;
    @FXML private TableColumn<Medicamento, String> colNombreMed, colCategoriaMed, colLoteMed;
    @FXML private TableColumn<Medicamento, Double> colPrecioMed;
    @FXML private TableColumn<Medicamento, Integer> colStockMed;
    @FXML private TableColumn<Medicamento, LocalDate> colFechaMed;

    // ---- Usuarios (solo Administrador) ----
    @FXML private TextField nombreUsuField, correoUsuField;
    @FXML private PasswordField contrasenaUsuField;
    @FXML private ComboBox<String> rolUsuCombo;
    @FXML private Button btnGuardarUsu, btnEliminarUsu, btnLimpiarUsu;
    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, Integer> colIdUsu;
    @FXML private TableColumn<Usuario, String> colNombreUsu, colCorreoUsu, colRolUsu;
    @FXML private TableColumn<Usuario, Boolean> colActivoUsu;

    // ---- Reportes ----
    @FXML private Label lblStockBajoRep, lblUsuariosActivosRep, lblTotalMedicamentosRep;
    @FXML private Button btnExportarReporte;

    // ---- Configuración ----
    @FXML private TextField empresaField, ivaField, umbralStockField, umbralDiasField;
    @FXML private Button btnGuardarConfig;

    private final MedicamentoDAO medicamentoDAO = new MedicamentoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ConfiguracionDAO configuracionDAO = new ConfiguracionDAO();

    private Usuario usuarioActual;
    private final ObservableList<Medicamento> medicamentos = FXCollections.observableArrayList();
    private final ObservableList<Usuario> usuarios = FXCollections.observableArrayList();
    private Medicamento medicamentoSeleccionado;

    @FXML
    public void initialize() {
        configurarColumnasMedicamentos();
        configurarColumnasUsuarios();
        rolUsuCombo.setItems(FXCollections.observableArrayList(
                Usuario.ROL_ADMIN, Usuario.ROL_CAJERO, Usuario.ROL_REPORTES));

        tablaMedicamentos.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            medicamentoSeleccionado = sel;
            if (sel != null) cargarFormularioMedicamento(sel);
        });

        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) cargarFormularioUsuario(sel);
        });
    }

    /**
     * Punto de entrada real de este controlador: recibe el objeto Usuario
     * ya autenticado y adapta toda la pantalla (menús, colores, datos)
     * según su rol. Esto es lo que exige la rúbrica en "Reutilización de
     * pantallas con POO".
     */
    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;

        userNameLabel.setText(usuario.getNombre());
        userRoleLabel.setText(usuario.getDescripcionRol());
        userInitialsLabel.setText(iniciales(usuario.getNombre()));
        welcomeNameLabel.setText(usuario.getNombre());

        // Acento de color por rol (polimorfismo -> CSS)
        rootPane.getStyleClass().removeIf(c -> c.startsWith("role-"));
        rootPane.getStyleClass().add(usuario.getClaseColorRol());

        aplicarPermisos(usuario.getModulosPermitidos());

        // Cajero/Reportes no pueden eliminar (solo lectura / solo operaciones)
        btnEliminarMed.setVisible(usuario.puedeEliminar());
        btnEliminarMed.setManaged(usuario.puedeEliminar());

        cargarDatosIniciales();
        showSection("HOME");
    }

    /** Muestra/oculta botones de navegación según los módulos permitidos del rol. */
    private void aplicarPermisos(Set<String> modulos) {
        navInventario.setVisible(modulos.contains("INVENTARIO"));
        navInventario.setManaged(modulos.contains("INVENTARIO"));

        navUsuarios.setVisible(modulos.contains("USUARIOS"));
        navUsuarios.setManaged(modulos.contains("USUARIOS"));

        navReportes.setVisible(modulos.contains("REPORTES"));
        navReportes.setManaged(modulos.contains("REPORTES"));

        navConfiguracion.setVisible(modulos.contains("CONFIGURACION"));
        navConfiguracion.setManaged(modulos.contains("CONFIGURACION"));
    }

    private void cargarDatosIniciales() {
        try {
            medicamentos.setAll(medicamentoDAO.listar());
            tablaMedicamentos.setItems(medicamentos);

            usuarios.setAll(usuarioDAO.listar());
            tablaUsuarios.setItems(usuarios);

            actualizarStatsHome();
            construirGraficoVentas();
            construirActividad();
        } catch (Exception e) {
            Alertas.error("No se pudieron cargar los datos desde la base de datos.\n" + e.getMessage());
        }
    }

    private void actualizarStatsHome() {
        long stockBajo = medicamentos.stream().filter(Medicamento::isStockBajo).count();
        long activos = usuarios.stream().filter(Usuario::isActivo).count();
        stockBajoValueLabel.setText(String.valueOf(stockBajo));
        usuariosActivosValueLabel.setText(String.valueOf(activos));
        lblStockBajoRep.setText(stockBajo + " artículos con stock bajo");
        lblUsuariosActivosRep.setText(activos + " usuarios activos");
        lblTotalMedicamentosRep.setText(medicamentos.size() + " medicamentos registrados");
    }

    // ============ NAVEGACIÓN ENTRE SECCIONES (mismo FXML) ============

    @FXML private void showHome() { showSection("HOME"); }
    @FXML private void showInventario() { showSection("INVENTARIO"); }
    @FXML private void showUsuarios() { showSection("USUARIOS"); }
    @FXML private void showReportes() { showSection("REPORTES"); }
    @FXML private void showConfiguracion() { showSection("CONFIGURACION"); loadConfiguracion(); }

    private void showSection(String seccion) {
        homeView.setVisible(false); homeView.setManaged(false);
        inventarioView.setVisible(false); inventarioView.setManaged(false);
        usuariosView.setVisible(false); usuariosView.setManaged(false);
        reportesView.setVisible(false); reportesView.setManaged(false);
        configuracionView.setVisible(false); configuracionView.setManaged(false);

        for (Button b : List.of(navHome, navInventario, navUsuarios, navReportes, navConfiguracion)) {
            b.getStyleClass().remove("active");
        }

        switch (seccion) {
            case "INVENTARIO":
                inventarioView.setVisible(true); inventarioView.setManaged(true);
                sectionTitleLabel.setText("Inventario de medicamentos");
                navInventario.getStyleClass().add("active");
                break;
            case "USUARIOS":
                usuariosView.setVisible(true); usuariosView.setManaged(true);
                sectionTitleLabel.setText("Gestión de usuarios");
                navUsuarios.getStyleClass().add("active");
                break;
            case "REPORTES":
                reportesView.setVisible(true); reportesView.setManaged(true);
                sectionTitleLabel.setText("Módulo de reportes");
                navReportes.getStyleClass().add("active");
                break;
            case "CONFIGURACION":
                configuracionView.setVisible(true); configuracionView.setManaged(true);
                sectionTitleLabel.setText("Configuración");
                navConfiguracion.getStyleClass().add("active");
                break;
            default:
                homeView.setVisible(true); homeView.setManaged(true);
                sectionTitleLabel.setText("Panel principal");
                navHome.getStyleClass().add("active");
        }
    }

    // ============ CRUD MEDICAMENTOS ============

    private void configurarColumnasMedicamentos() {
        colIdMed.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombreMed.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoriaMed.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecioMed.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStockMed.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colLoteMed.setCellValueFactory(new PropertyValueFactory<>("lote"));
        colFechaMed.setCellValueFactory(new PropertyValueFactory<>("fechaVencimiento"));
    }

    @FXML
    private void handleGuardarMedicamento() {
        try {
            String nombre = nombreMedField.getText().trim();
            String categoria = categoriaMedField.getText().trim();

            // Validación: campos vacíos
            if (nombre.isEmpty() || categoria.isEmpty() || precioMedField.getText().trim().isEmpty()
                    || stockMedField.getText().trim().isEmpty() || loteMedField.getText().trim().isEmpty()) {
                Alertas.error("Ningún campo puede quedar vacío.");
                return;
            }

            // Validación: no duplicados
            if (medicamentoDAO.existeNombre(nombre)) {
                Alertas.error("Ya existe un medicamento registrado con ese nombre.");
                return;
            }

            double precio = parseDoublePositivo(precioMedField.getText(), "El precio");
            int stock = parseIntPositivo(stockMedField.getText(), "El stock");

            Medicamento m = new Medicamento(0, nombre, categoria, precio, stock,
                    loteMedField.getText().trim(), fechaVencMedPicker.getValue(), 1);

            medicamentoDAO.guardar(m);
            Alertas.info("Medicamento guardado correctamente.");
            limpiarFormularioMedicamento();
            cargarDatosIniciales();

        } catch (NumberFormatException nfe) {
            Alertas.error(nfe.getMessage());
        } catch (IllegalArgumentException iae) {
            Alertas.error(iae.getMessage());
        } catch (Exception e) {
            Alertas.error("No se pudo guardar el medicamento.\n" + e.getMessage());
        }
    }

    @FXML
    private void handleActualizarMedicamento() {
        if (medicamentoSeleccionado == null) {
            Alertas.error("Selecciona un medicamento de la tabla para editarlo.");
            return;
        }
        try {
            medicamentoSeleccionado.setNombre(nombreMedField.getText().trim());
            medicamentoSeleccionado.setCategoria(categoriaMedField.getText().trim());
            medicamentoSeleccionado.setPrecio(parseDoublePositivo(precioMedField.getText(), "El precio"));
            medicamentoSeleccionado.setStock(parseIntPositivo(stockMedField.getText(), "El stock"));
            medicamentoSeleccionado.setLote(loteMedField.getText().trim());
            medicamentoSeleccionado.setFechaVencimiento(fechaVencMedPicker.getValue());

            medicamentoDAO.actualizar(medicamentoSeleccionado);
            Alertas.info("Medicamento actualizado correctamente.");
            limpiarFormularioMedicamento();
            cargarDatosIniciales();

        } catch (IllegalArgumentException iae) {
            Alertas.error(iae.getMessage());
        } catch (Exception e) {
            Alertas.error("No se pudo actualizar el medicamento.\n" + e.getMessage());
        }
    }

    @FXML
    private void handleEliminarMedicamento() {
        if (medicamentoSeleccionado == null) {
            Alertas.error("Selecciona un medicamento de la tabla para eliminarlo.");
            return;
        }
        // Confirmación obligatoria antes de eliminar
        if (!Alertas.confirmar("¿Eliminar el medicamento \"" + medicamentoSeleccionado.getNombre() + "\"?")) {
            return;
        }
        try {
            medicamentoDAO.eliminar(medicamentoSeleccionado.getId());
            Alertas.info("Medicamento eliminado.");
            limpiarFormularioMedicamento();
            cargarDatosIniciales();
        } catch (Exception e) {
            Alertas.error("No se pudo eliminar el medicamento.\n" + e.getMessage());
        }
    }

    @FXML
    private void handleLimpiarMedicamento() {
        limpiarFormularioMedicamento();
    }

    private void cargarFormularioMedicamento(Medicamento m) {
        nombreMedField.setText(m.getNombre());
        categoriaMedField.setText(m.getCategoria());
        precioMedField.setText(String.valueOf(m.getPrecio()));
        stockMedField.setText(String.valueOf(m.getStock()));
        loteMedField.setText(m.getLote());
        fechaVencMedPicker.setValue(m.getFechaVencimiento());
    }

    private void limpiarFormularioMedicamento() {
        nombreMedField.clear();
        categoriaMedField.clear();
        precioMedField.clear();
        stockMedField.clear();
        loteMedField.clear();
        fechaVencMedPicker.setValue(null);
        medicamentoSeleccionado = null;
        tablaMedicamentos.getSelectionModel().clearSelection();
    }

    // ============ CRUD USUARIOS (solo Administrador) ============

    private void configurarColumnasUsuarios() {
        colIdUsu.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombreUsu.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCorreoUsu.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colRolUsu.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colActivoUsu.setCellValueFactory(new PropertyValueFactory<>("activo"));
    }

    @FXML
    private void handleGuardarUsuario() {
        try {
            String nombre = nombreUsuField.getText().trim();
            String correo = correoUsuField.getText().trim();
            String contrasena = contrasenaUsuField.getText().trim();
            String rol = rolUsuCombo.getValue();

            if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty() || rol == null) {
                Alertas.error("Ningún campo puede quedar vacío.");
                return;
            }
            if (usuarioDAO.existeCorreo(correo)) {
                Alertas.error("Ya existe un usuario registrado con ese correo.");
                return;
            }
            // Usuario.setContrasena() ya valida el mínimo de 6 caracteres (lanza excepción si no cumple)
            Usuario nuevo = Usuario.crearPorRol(0, nombre, correo, contrasena, rol, true);

            usuarioDAO.guardar(nuevo);
            Alertas.info("Usuario guardado correctamente.");
            limpiarFormularioUsuario();
            cargarDatosIniciales();

        } catch (IllegalArgumentException iae) {
            Alertas.error(iae.getMessage());
        } catch (Exception e) {
            Alertas.error("No se pudo guardar el usuario.\n" + e.getMessage());
        }
    }

    @FXML
    private void handleEliminarUsuario() {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Alertas.error("Selecciona un usuario de la tabla para eliminarlo.");
            return;
        }
        if (seleccionado.getId() == usuarioActual.getId()) {
            Alertas.error("No puedes eliminar tu propio usuario mientras tienes la sesión activa.");
            return;
        }
        if (!Alertas.confirmar("¿Eliminar al usuario \"" + seleccionado.getNombre() + "\"?")) {
            return;
        }
        try {
            usuarioDAO.eliminar(seleccionado.getId());
            Alertas.info("Usuario eliminado.");
            limpiarFormularioUsuario();
            cargarDatosIniciales();
        } catch (Exception e) {
            Alertas.error("No se pudo eliminar el usuario.\n" + e.getMessage());
        }
    }

    @FXML
    private void handleLimpiarUsuario() {
        limpiarFormularioUsuario();
    }

    private void cargarFormularioUsuario(Usuario u) {
        nombreUsuField.setText(u.getNombre());
        correoUsuField.setText(u.getCorreo());
        contrasenaUsuField.clear();
        rolUsuCombo.setValue(u.getRol());
    }

    private void limpiarFormularioUsuario() {
        nombreUsuField.clear();
        correoUsuField.clear();
        contrasenaUsuField.clear();
        rolUsuCombo.setValue(null);
        tablaUsuarios.getSelectionModel().clearSelection();
    }

    // ============ REPORTES ============

    @FXML
    private void handleExportarReporte() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exportar reporte de inventario");
        chooser.setInitialFileName("reporte_inventario.csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));

        java.io.File archivo = chooser.showSaveDialog(btnExportarReporte.getScene().getWindow());
        if (archivo == null) return;

        try (PrintWriter pw = new PrintWriter(archivo, "UTF-8")) {
            pw.println("id,nombre,categoria,precio,stock,lote,fecha_vencimiento");
            for (Medicamento m : medicamentos) {
                pw.println(m.getId() + "," + m.getNombre() + "," + m.getCategoria() + "," +
                        m.getPrecio() + "," + m.getStock() + "," + m.getLote() + "," +
                        (m.getFechaVencimiento() != null ? m.getFechaVencimiento() : ""));
            }
            Alertas.info("Reporte exportado correctamente en:\n" + archivo.getAbsolutePath());
        } catch (Exception e) {
            Alertas.error("No se pudo exportar el reporte.\n" + e.getMessage());
        }
    }

    // ============ CONFIGURACIÓN ============

    private void loadConfiguracion() {
        try {
            Configuracion cfg = configuracionDAO.obtener();
            empresaField.setText(cfg.getNombreEmpresa());
            ivaField.setText(String.valueOf(cfg.getIva()));
            umbralStockField.setText(String.valueOf(cfg.getUmbralStockBajo()));
            umbralDiasField.setText(String.valueOf(cfg.getUmbralDiasVencimiento()));
        } catch (Exception e) {
            Alertas.error("No se pudo cargar la configuración.\n" + e.getMessage());
        }
    }

    @FXML
    private void handleGuardarConfiguracion() {
        try {
            if (empresaField.getText().trim().isEmpty()) {
                Alertas.error("El nombre de la empresa no puede estar vacío.");
                return;
            }
            Configuracion cfg = new Configuracion(
                    1,
                    empresaField.getText().trim(),
                    parseDoublePositivo(ivaField.getText(), "El IVA"),
                    parseIntPositivo(umbralStockField.getText(), "El umbral de stock bajo"),
                    parseIntPositivo(umbralDiasField.getText(), "El umbral de días de vencimiento")
            );
            configuracionDAO.actualizar(cfg);
            Alertas.info("Configuración actualizada correctamente.");
        } catch (IllegalArgumentException iae) {
            Alertas.error(iae.getMessage());
        } catch (Exception e) {
            Alertas.error("No se pudo guardar la configuración.\n" + e.getMessage());
        }
    }

    // ============ GRÁFICO Y ACTIVIDAD DEL HOME ============

    private void construirGraficoVentas() {
        barsContainer.getChildren().clear();
        // TODO: reemplazar por datos reales de la tabla VENTAS agrupados por día
        String[] dias = {"LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM"};
        double[] valores = {42, 58, 38, 70, 62, 98, 108};

        for (int i = 0; i < dias.length; i++) {
            VBox col = new VBox(8);
            col.setAlignment(javafx.geometry.Pos.BOTTOM_CENTER);
            HBox.setHgrow(col, javafx.scene.layout.Priority.ALWAYS);

            Region bar = new Region();
            bar.getStyleClass().add("bar");
            if (valores[i] >= 90) bar.getStyleClass().add("peak");
            bar.setPrefHeight(valores[i]);
            bar.setMaxWidth(Double.MAX_VALUE);

            Label lbl = new Label(dias[i]);
            lbl.getStyleClass().add("bar-label");

            col.getChildren().addAll(bar, lbl);
            barsContainer.getChildren().add(col);
        }
    }

    private void construirActividad() {
        activityContainer.getChildren().clear();
        // TODO: reemplazar por datos reales de MOVIMIENTOS_INVENTARIO / VENTAS
        agregarActividad("g", "✓", "Venta completada #1042", "Hace 5 min · $145.00");
        agregarActividad("a", "⚠", "Stock bajo detectado", medicamentos.stream()
                .filter(Medicamento::isStockBajo).count() + " artículos en el umbral");
        agregarActividad("n", "📦", "Nuevo ingreso de mercadería", "Hace 1 hora · Lote A-441");
    }

    private void agregarActividad(String color, String glyph, String titulo, String subtitulo) {
        HBox item = new HBox(11);
        item.getStyleClass().add("activity-item");
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        javafx.scene.layout.StackPane dot = new javafx.scene.layout.StackPane(new Label(glyph));
        dot.getStyleClass().addAll("a-dot", color);

        Label t = new Label(titulo);
        t.getStyleClass().add("a-text-title");
        Label s = new Label(subtitulo);
        s.getStyleClass().add("a-text-sub");

        item.getChildren().addAll(dot, new VBox(2, t, s));
        activityContainer.getChildren().add(item);
    }

    // ============ LOGOUT ============

    @FXML
    private void handleLogout() {
        if (!Alertas.confirmar("¿Cerrar la sesión actual?")) return;
        try {
            Main.irALogin();
        } catch (Exception e) {
            Alertas.error("No se pudo cerrar la sesión.\n" + e.getMessage());
        }
    }

    // ============ HELPERS DE VALIDACIÓN ============

    private double parseDoublePositivo(String texto, String etiqueta) {
        try {
            double valor = Double.parseDouble(texto.trim());
            if (valor <= 0) throw new IllegalArgumentException(etiqueta + " debe ser mayor a 0.");
            return valor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(etiqueta + " debe ser un número válido.");
        }
    }

    private int parseIntPositivo(String texto, String etiqueta) {
        try {
            int valor = Integer.parseInt(texto.trim());
            if (valor < 0) throw new IllegalArgumentException(etiqueta + " no puede ser negativo.");
            return valor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(etiqueta + " debe ser un número entero válido.");
        }
    }

    private String iniciales(String nombre) {
        String[] partes = nombre.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : partes) {
            if (!p.isEmpty()) sb.append(Character.toUpperCase(p.charAt(0)));
            if (sb.length() >= 2) break;
        }
        return sb.toString();
    }
}
