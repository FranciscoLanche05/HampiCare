package com.hampicare.controller;

import com.hampicare.model.Medicamento;
import com.hampicare.model.Usuario;
import com.hampicare.app.Main;
import com.hampicare.dao.ConfiguracionDAO;
import com.hampicare.dao.MedicamentoDAO;
import com.hampicare.dao.MovimientoInventarioDAO;
import com.hampicare.dao.UsuarioDAO;
import com.hampicare.dao.VentaDAO;
import com.hampicare.model.*;
import com.hampicare.service.PDFGeneratorService;
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
import javafx.util.StringConverter;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;


public class DashboardController {

    // ---- Sidebar ----
    @FXML private javafx.scene.layout.BorderPane rootPane;
    @FXML private Button navHome, navInventario, navVentas, navUsuarios, navReportes, navConfiguracion;
    @FXML private Label userInitialsLabel, userNameLabel, userRoleLabel;
    @FXML private Label sectionTitleLabel;

    // ---- Secciones ----
    @FXML private VBox homeView, inventarioView, ventasView, usuariosView, reportesView, configuracionView;

    // ---- Home ----
    @FXML private javafx.scene.text.Text welcomeNameLabel;
    @FXML private Label stockBajoValueLabel, usuariosActivosValueLabel;
    @FXML private HBox barsContainer;
    @FXML private VBox activityContainer;
    @FXML private HBox ventasHoyCard;
    @FXML private Label ventasHoyValueLabel, ventasHoyCountLabel;

    // ---- Inventario (CRUD de los medicamentos) ----
    @FXML private TextField nombreMedField, categoriaMedField, precioMedField, stockMedField, loteMedField;
    @FXML private DatePicker fechaVencMedPicker;
    @FXML private Button btnGuardarMed, btnActualizarMed, btnEliminarMed, btnLimpiarMed;
    @FXML private TableView<Medicamento> tablaMedicamentos;
    @FXML private TableColumn<Medicamento, Integer> colIdMed;
    @FXML private TableColumn<Medicamento, String> colNombreMed, colCategoriaMed, colLoteMed;
    @FXML private TableColumn<Medicamento, Double> colPrecioMed;
    @FXML private TableColumn<Medicamento, Integer> colStockMed;
    @FXML private TableColumn<Medicamento, LocalDate> colFechaMed;

    // ---- Ventas (Cajero) ----
    @FXML private ComboBox<Medicamento> ventaMedicamentoCombo, entradaMedicamentoCombo;
    @FXML private TextField ventaCantidadField, entradaCantidadField;
    @FXML private Button btnAgregarCarrito, btnQuitarDelCarrito, btnVaciarCarrito, btnRegistrarVenta;
    @FXML private Button btnRegistrarEntrada, btnAnularVenta;
    @FXML private Label totalVentaLabel;
    @FXML private TableView<DetalleVenta> tablaCarrito;
    @FXML private TableColumn<DetalleVenta, String> colCarritoNombre;
    @FXML private TableColumn<DetalleVenta, Integer> colCarritoCantidad;
    @FXML private TableColumn<DetalleVenta, Double> colCarritoPrecio, colCarritoSubtotal;
    @FXML private TableView<Venta> tablaHistorialVentas;
    @FXML private TableColumn<Venta, Integer> colVentaId;
    @FXML private TableColumn<Venta, String> colVentaFactura, colVentaFecha;
    @FXML private TableColumn<Venta, Double> colVentaTotal;

    // ---- Usuarios (Administrador) ----
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
    private final VentaDAO ventaDAO = new VentaDAO();
    private final MovimientoInventarioDAO movimientoInventarioDAO = new MovimientoInventarioDAO();
    private final PDFGeneratorService pdfGeneratorService = new PDFGeneratorService();

    private static final DateTimeFormatter FMT_FECHA_VENTA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Usuario usuarioActual;
    private final ObservableList<Medicamento> medicamentos = FXCollections.observableArrayList();
    private final ObservableList<Usuario> usuarios = FXCollections.observableArrayList();
    private final ObservableList<DetalleVenta> carrito = FXCollections.observableArrayList();
    private final ObservableList<Venta> historialVentas = FXCollections.observableArrayList();
    private Medicamento medicamentoSeleccionado;

    @FXML
    public void initialize() {
        configurarColumnasMedicamentos();
        configurarColumnasUsuarios();
        configurarColumnasVentas();
        configurarCombosMedicamento();
        rolUsuCombo.setItems(FXCollections.observableArrayList(
                Usuario.ROL_ADMIN, Usuario.ROL_CAJERO, Usuario.ROL_REPORTES));

        tablaMedicamentos.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            medicamentoSeleccionado = sel;
            if (sel != null) cargarFormularioMedicamento(sel);
        });

        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) cargarFormularioUsuario(sel);
        });

        tablaCarrito.setItems(carrito);
        tablaHistorialVentas.setItems(historialVentas);
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;

        userNameLabel.setText(usuario.getNombre());
        userRoleLabel.setText(usuario.getDescripcionRol());
        userInitialsLabel.setText(iniciales(usuario.getNombre()));
        welcomeNameLabel.setText(usuario.getNombre());

        // Polimorfismo -> CSS
        rootPane.getStyleClass().removeIf(c -> c.startsWith("role-"));
        rootPane.getStyleClass().add(usuario.getClaseColorRol());

        Set<String> modulos = usuario.getModulosPermitidos();
        aplicarPermisos(modulos);

        // Cajero/Reportes (solo lectura / solo operaciones)
        btnEliminarMed.setVisible(usuario.puedeEliminar());
        btnEliminarMed.setManaged(usuario.puedeEliminar());

        boolean tieneVentas = modulos.contains("VENTAS");
        ventasHoyCard.setVisible(tieneVentas);
        ventasHoyCard.setManaged(tieneVentas);

        cargarDatosIniciales();
        showSection("HOME");
    }

    private void aplicarPermisos(Set<String> modulos) {
        navInventario.setVisible(modulos.contains("INVENTARIO"));
        navInventario.setManaged(modulos.contains("INVENTARIO"));

        navVentas.setVisible(modulos.contains("VENTAS"));
        navVentas.setManaged(modulos.contains("VENTAS"));

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
            ventaMedicamentoCombo.setItems(medicamentos);
            entradaMedicamentoCombo.setItems(medicamentos);

            usuarios.setAll(usuarioDAO.listar());
            tablaUsuarios.setItems(usuarios);

            if (usuarioActual.getModulosPermitidos().contains("VENTAS")) {
                cargarHistorialVentas();
                actualizarResumenVentasHoy();
            }

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
    @FXML private void showVentas() { showSection("VENTAS"); }
    @FXML private void showUsuarios() { showSection("USUARIOS"); }
    @FXML private void showReportes() { showSection("REPORTES"); }
    @FXML private void showConfiguracion() { showSection("CONFIGURACION"); loadConfiguracion(); }

    private void showSection(String seccion) {
        homeView.setVisible(false); homeView.setManaged(false);
        inventarioView.setVisible(false); inventarioView.setManaged(false);
        ventasView.setVisible(false); ventasView.setManaged(false);
        usuariosView.setVisible(false); usuariosView.setManaged(false);
        reportesView.setVisible(false); reportesView.setManaged(false);
        configuracionView.setVisible(false); configuracionView.setManaged(false);

        for (Button b : List.of(navHome, navInventario, navVentas, navUsuarios, navReportes, navConfiguracion)) {
            b.getStyleClass().remove("active");
        }

        switch (seccion) {
            case "INVENTARIO":
                inventarioView.setVisible(true); inventarioView.setManaged(true);
                sectionTitleLabel.setText("Inventario de medicamentos");
                navInventario.getStyleClass().add("active");
                break;
            case "VENTAS":
                ventasView.setVisible(true); ventasView.setManaged(true);
                sectionTitleLabel.setText("Punto de venta");
                navVentas.getStyleClass().add("active");
                cargarHistorialVentas();
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

    // ============ PUNTO DE VENTA (Cajero) ============

    private void configurarColumnasVentas() {
        colCarritoNombre.setCellValueFactory(new PropertyValueFactory<>("nombreMedicamento"));
        colCarritoCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCarritoPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colCarritoSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        colVentaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colVentaFactura.setCellValueFactory(new PropertyValueFactory<>("numeroFactura"));
        colVentaFecha.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFecha() != null ? cellData.getValue().getFecha().format(FMT_FECHA_VENTA) : ""));
        colVentaTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
    }

    private void configurarCombosMedicamento() {
        StringConverter<Medicamento> convertidor = new StringConverter<>() {
            @Override
            public String toString(Medicamento m) {
                return m == null ? "" : m.getNombre() + "  (stock: " + m.getStock() + ")";
            }

            @Override
            public Medicamento fromString(String string) {
                return null; // no se usa: el combo es de solo selección, no editable
            }
        };
        ventaMedicamentoCombo.setConverter(convertidor);
        entradaMedicamentoCombo.setConverter(convertidor);
    }

    @FXML
    private void handleAgregarCarrito() {
        Medicamento m = ventaMedicamentoCombo.getValue();
        if (m == null) {
            Alertas.error("Selecciona un medicamento.");
            return;
        }
        int cantidad;
        try {
            cantidad = Integer.parseInt(ventaCantidadField.getText().trim());
        } catch (NumberFormatException e) {
            Alertas.error("La cantidad debe ser un número entero válido.");
            return;
        }
        if (cantidad <= 0) {
            Alertas.error("La cantidad debe ser mayor a 0.");
            return;
        }

        int yaEnCarrito = carrito.stream()
                .filter(d -> d.getMedicamentoId() == m.getId())
                .mapToInt(DetalleVenta::getCantidad)
                .sum();

        if (yaEnCarrito + cantidad > m.getStock()) {
            Alertas.error("Stock insuficiente. Disponible: " + m.getStock() +
                    (yaEnCarrito > 0 ? " (ya tienes " + yaEnCarrito + " en el carrito)." : "."));
            return;
        }

        try {
            DetalleVenta existente = carrito.stream()
                    .filter(d -> d.getMedicamentoId() == m.getId())
                    .findFirst().orElse(null);

            if (existente != null) {
                existente.setCantidad(existente.getCantidad() + cantidad);
                tablaCarrito.refresh();
            } else {
                carrito.add(new DetalleVenta(m.getId(), m.getNombre(), cantidad, m.getPrecio()));
            }
            ventaCantidadField.clear();
            actualizarTotalCarrito();
        } catch (IllegalArgumentException e) {
            Alertas.error(e.getMessage());
        }
    }

    @FXML
    private void handleQuitarDelCarrito() {
        DetalleVenta sel = tablaCarrito.getSelectionModel().getSelectedItem();
        if (sel == null) {
            Alertas.error("Selecciona un producto del carrito para quitarlo.");
            return;
        }
        carrito.remove(sel);
        actualizarTotalCarrito();
    }

    @FXML
    private void handleVaciarCarrito() {
        carrito.clear();
        actualizarTotalCarrito();
    }

    private void actualizarTotalCarrito() {
        double total = carrito.stream().mapToDouble(DetalleVenta::getSubtotal).sum();
        totalVentaLabel.setText("Total: $" + String.format("%.2f", total));
    }

    @FXML
    private void handleRegistrarVenta() {
        if (carrito.isEmpty()) {
            Alertas.error("Agrega al menos un producto al carrito antes de registrar la venta.");
            return;
        }
        if (!Alertas.confirmar("¿Registrar esta venta? " + totalVentaLabel.getText())) {
            return;
        }
        try {
            List<DetalleVenta> detallesVendidos = new java.util.ArrayList<>(carrito);
            Venta venta = ventaDAO.registrarVenta(usuarioActual.getId(), detallesVendidos);

            Alertas.info("Venta registrada correctamente.\nFactura: " + venta.getNumeroFactura());

            carrito.clear();
            actualizarTotalCarrito();
            cargarDatosIniciales(); // refresca stock, historial y resumen del día

            if (Alertas.confirmar("¿Deseas generar el recibo en PDF?")) {
                generarReciboPDF(venta, detallesVendidos);
            }

        } catch (IllegalArgumentException iae) {
            Alertas.error(iae.getMessage());
        } catch (Exception e) {
            Alertas.error("No se pudo registrar la venta.\n" + e.getMessage());
        }
    }

    private void generarReciboPDF(Venta venta, List<DetalleVenta> detalles) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar recibo de venta");
        chooser.setInitialFileName("recibo_" + venta.getNumeroFactura() + ".pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));

        File archivo = chooser.showSaveDialog(btnRegistrarVenta.getScene().getWindow());
        if (archivo == null) return;

        try {
            Configuracion cfg = configuracionDAO.obtener();
            pdfGeneratorService.generarReciboVenta(venta, detalles, usuarioActual, cfg, archivo);
            Alertas.info("Recibo generado en:\n" + archivo.getAbsolutePath());
        } catch (Exception e) {
            Alertas.error("No se pudo generar el recibo.\n" + e.getMessage());
        }
    }

    @FXML
    private void handleAnularVenta() {
        Venta sel = tablaHistorialVentas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            Alertas.error("Selecciona una venta del historial para anularla.");
            return;
        }
        if (sel.getFecha() == null || !sel.getFecha().toLocalDate().equals(LocalDate.now())) {
            Alertas.error("Solo puedes anular ventas realizadas hoy.");
            return;
        }
        if (!Alertas.confirmar("¿Anular la venta \"" + sel.getNumeroFactura() + "\"? Esto repondrá el stock vendido.")) {
            return;
        }
        try {
            ventaDAO.anularVenta(sel.getId(), usuarioActual.getId());
            Alertas.info("Venta anulada. El stock fue repuesto.");
            cargarDatosIniciales();
        } catch (Exception e) {
            Alertas.error("No se pudo anular la venta.\n" + e.getMessage());
        }
    }

    @FXML
    private void handleRegistrarEntrada() {
        Medicamento m = entradaMedicamentoCombo.getValue();
        if (m == null) {
            Alertas.error("Selecciona un medicamento.");
            return;
        }
        int cantidad;
        try {
            cantidad = Integer.parseInt(entradaCantidadField.getText().trim());
        } catch (NumberFormatException e) {
            Alertas.error("La cantidad debe ser un número entero válido.");
            return;
        }
        if (cantidad <= 0) {
            Alertas.error("La cantidad debe ser mayor a 0.");
            return;
        }
        try {
            movimientoInventarioDAO.registrarEntrada(m.getId(), cantidad, usuarioActual.getId());
            Alertas.info("Ingreso registrado: +" + cantidad + " unidades de \"" + m.getNombre() + "\".");
            entradaCantidadField.clear();
            cargarDatosIniciales();
        } catch (Exception e) {
            Alertas.error("No se pudo registrar el ingreso.\n" + e.getMessage());
        }
    }

    private void cargarHistorialVentas() {
        if (usuarioActual == null || !usuarioActual.getModulosPermitidos().contains("VENTAS")) return;
        try {
            historialVentas.setAll(ventaDAO.listarPorUsuario(usuarioActual.getId()));
        } catch (Exception e) {
            Alertas.error("No se pudo cargar el historial de ventas.\n" + e.getMessage());
        }
    }

    private void actualizarResumenVentasHoy() {
        try {
            double[] resumen = ventaDAO.resumenHoy(usuarioActual.getId());
            int cantidadVentas = (int) resumen[0];
            double total = resumen[1];
            ventasHoyValueLabel.setText(String.format("$%.2f", total));
            ventasHoyCountLabel.setText(cantidadVentas == 1
                    ? "1 venta realizada hoy"
                    : cantidadVentas + " ventas realizadas hoy");
        } catch (Exception e) {
            // No bloquea la carga del dashboard si falla el resumen del día.
        }
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

        if (usuarioActual.getModulosPermitidos().contains("VENTAS")) {
            try {
                List<Venta> ultimas = ventaDAO.ultimasVentas(usuarioActual.getId(), 3);
                if (ultimas.isEmpty()) {
                    agregarActividad("n", "🧾", "Aún no has registrado ventas",
                            "Ve a \"Punto de venta\" para registrar tu primera venta");
                } else {
                    for (Venta v : ultimas) {
                        String fecha = v.getFecha() != null ? v.getFecha().format(FMT_FECHA_VENTA) : "";
                        agregarActividad("g", "✓", "Venta " + v.getNumeroFactura(),
                                fecha + " · $" + String.format("%.2f", v.getTotal()));
                    }
                }
            } catch (Exception e) {
                agregarActividad("a", "⚠", "No se pudo cargar el historial de ventas", "");
            }
        } else {
            agregarActividad("n", "📦", "Inventario", medicamentos.size() + " medicamentos registrados");
        }

        long stockBajo = medicamentos.stream().filter(Medicamento::isStockBajo).count();
        agregarActividad("a", "⚠", "Stock bajo detectado", stockBajo + " artículos en el umbral");
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
