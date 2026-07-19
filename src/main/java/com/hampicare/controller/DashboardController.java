package com.hampicare.controller;

import com.hampicare.model.Medicamento;
import com.hampicare.model.Usuario;
import com.hampicare.app.Main;
import com.hampicare.dao.ConfiguracionDAO;
import com.hampicare.dao.MedicamentoDAO;
import com.hampicare.dao.MovimientoInventarioDAO;
import com.hampicare.dao.UsuarioDAO;
import com.hampicare.dao.VentaDAO;
import com.hampicare.dao.ProveedorDAO;
import com.hampicare.dao.CompraDAO;
import com.hampicare.model.*;
import com.hampicare.service.PDFGeneratorService;
import com.hampicare.util.Alertas;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import org.controlsfx.control.textfield.TextFields;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Element;
import com.hampicare.dao.ClienteDAO;
import com.hampicare.dao.ReporteDAO;
import com.hampicare.model.Cliente;
import java.io.FileOutputStream;
import java.io.File;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class DashboardController {

    // ---- Sidebar ----
    @FXML private javafx.scene.layout.BorderPane rootPane;
    @FXML private Button navHome, navInventario, navVentas, navUsuarios, navReportes, navConfiguracion, navClientes, navCompras, navProveedores;
    @FXML private Label userInitialsLabel, userNameLabel, userRoleLabel;
    @FXML private Label sectionTitleLabel;

    // ---- Secciones ----
    @FXML private VBox homeView, inventarioView, ventasView, usuariosView, reportesView, configuracionView;
    @FXML private javafx.scene.layout.VBox clientesView, proveedoresView, comprasView;

    // ---- Home ----
    @FXML private javafx.scene.text.Text welcomeNameLabel;
    @FXML private Label stockBajoValueLabel, usuariosActivosValueLabel;
    @FXML private HBox barsContainer;
    @FXML private VBox activityContainer;
    @FXML private HBox ventasHoyCard;
    @FXML private Label ventasHoyValueLabel, ventasHoyCountLabel;
    @FXML private Label ventasHoyIconLabel;

    // ---- Inventario (CRUD de los medicamentos) ----
    @FXML private TextField nombreMedField, precioMedField, stockMedField, loteMedField;
    @FXML private ComboBox<String> categoriaMedField;
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
    @FXML private TableColumn<Venta, String> colVentaCliente;
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


    // ---- Clientes ----
    @FXML private TextField nombreCliField, apellidoCliField, cedulaCliField, correoCliField, telefonoCliField, sectorCliField;
    @FXML private Button btnGuardarCli, btnActualizarCli, btnEliminarCli, btnLimpiarCli;
    @FXML private TableView<Cliente> tablaClientes;
    @FXML private TableColumn<Cliente, String> colCliCedula, colCliNombres, colCliApellidos, colCliCorreo, colCliTelefono, colCliSector;

    // ---- Proveedores ----
    @FXML private TextField nombreProvField, correoProvField, telefonoProvField;
    @FXML private Button btnGuardarProv, btnActualizarProv, btnEliminarProv, btnLimpiarProv;
    @FXML private TableView<Proveedor> tablaProveedores;
    @FXML private TableColumn<Proveedor, Integer> colProvId;
    @FXML private TableColumn<Proveedor, String> colProvNombre, colProvCorreo, colProvTelefono;

    // ---- Compras ----
    @FXML private ComboBox<Proveedor> entradaProveedorCombo;
    @FXML private TextField entradaPrecioCompraField;
    @FXML private TableView<Compra> tablaCompras;
    @FXML private TableColumn<Compra, Integer> colCompraId, colCompraCantidad;
    @FXML private TableColumn<Compra, String> colCompraProveedor, colCompraMedicamento;
    @FXML private TableColumn<Compra, Double> colCompraPrecio;
    @FXML private TableColumn<Compra, String> colCompraFecha;

    // ---- Ventas Cliente ----
    @FXML private ComboBox<Cliente> ventaClienteCombo;

    // ---- Search ----
    @FXML private TextField searchMedicamentos, searchVentas, searchClientes, searchProveedores, searchCompras, searchUsuarios;

    // ---- Reportes ----
    @FXML private StackPane reportesStack;
    @FXML private VBox repVentasPanel, repTopMedsPanel, repInventarioPanel, repComprasPanel, repClientesPanel, repCajaPanel;
    @FXML private Button btnRepVentas, btnRepTopMeds, btnRepInventario, btnRepCompras, btnRepClientes, btnRepCaja;
    @FXML private DatePicker repVentasDesde, repVentasHasta, repComprasDesde, repComprasHasta, repCajaDesde, repCajaHasta;
    @FXML private Label repVentasResumen, repComprasResumen;
    @FXML private TableView<ReporteDAO.VentaPeriodo> tablaRepVentas;
    @FXML private TableColumn<ReporteDAO.VentaPeriodo, Integer> colRepVenId;
    @FXML private TableColumn<ReporteDAO.VentaPeriodo, String> colRepVenFactura, colRepVenCliente, colRepVenFecha;
    @FXML private TableColumn<ReporteDAO.VentaPeriodo, Double> colRepVenTotal;
    @FXML private TableView<ReporteDAO.TopMedicamento> tablaRepTopMeds;
    @FXML private TableColumn<ReporteDAO.TopMedicamento, Integer> colRepTopId, colRepTopVendido;
    @FXML private TableColumn<ReporteDAO.TopMedicamento, String> colRepTopNombre, colRepTopCategoria;
    @FXML private TableColumn<ReporteDAO.TopMedicamento, Double> colRepTopIngresos;
    @FXML private TableView<ReporteDAO.InventarioItem> tablaRepInventario;
    @FXML private TableColumn<ReporteDAO.InventarioItem, Integer> colRepInvId, colRepInvStock;
    @FXML private TableColumn<ReporteDAO.InventarioItem, String> colRepInvNombre, colRepInvCategoria, colRepInvLote, colRepInvVence, colRepInvEstado;
    @FXML private TableColumn<ReporteDAO.InventarioItem, Double> colRepInvPrecio;
    @FXML private TableView<ReporteDAO.CompraPeriodo> tablaRepCompras;
    @FXML private TableColumn<ReporteDAO.CompraPeriodo, Integer> colRepCompId, colRepCompCantidad;
    @FXML private TableColumn<ReporteDAO.CompraPeriodo, String> colRepCompProveedor, colRepCompMedicamento, colRepCompFecha;
    @FXML private TableColumn<ReporteDAO.CompraPeriodo, Double> colRepCompPrecio;
    @FXML private TableView<ReporteDAO.ClienteReporte> tablaRepClientes;
    @FXML private TableColumn<ReporteDAO.ClienteReporte, Integer> colRepCliId, colRepCliCompras;
    @FXML private TableColumn<ReporteDAO.ClienteReporte, String> colRepCliNombre, colRepCliCedula;
    @FXML private TableColumn<ReporteDAO.ClienteReporte, Double> colRepCliGastado;
    @FXML private Label repCajaTotalVentas, repCajaNumVentas, repCajaTotalCompras, repCajaNumCompras, repCajaGanancia, repCajaGananciaSub;
    @FXML private BarChart<String, Number> repCajaChart;
    @FXML private CategoryAxis repCajaXAxis;
    @FXML private NumberAxis repCajaYAxis;

    // ---- Configuración ----
    @FXML private TextField empresaField, ivaField, umbralStockField, umbralDiasField;
    @FXML private Button btnGuardarConfig;

    private final MedicamentoDAO medicamentoDAO = new MedicamentoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ConfiguracionDAO configuracionDAO = new ConfiguracionDAO();
    private final VentaDAO ventaDAO = new VentaDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private final CompraDAO compraDAO = new CompraDAO();
    private final ReporteDAO reporteDAO = new ReporteDAO();

    private final ObservableList<Cliente> clientes = FXCollections.observableArrayList();
    private final ObservableList<Proveedor> proveedores = FXCollections.observableArrayList();
    private final ObservableList<Compra> compras = FXCollections.observableArrayList();

    private FilteredList<Medicamento> filteredMedicamentos;
    private FilteredList<Cliente> filteredClientes;
    private FilteredList<Proveedor> filteredProveedores;
    private FilteredList<Compra> filteredCompras;
    private FilteredList<Venta> filteredVentas;
    private FilteredList<Usuario> filteredUsuarios;

    private Cliente clienteSeleccionado;
    private Proveedor proveedorSeleccionado;

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
        System.out.println("[DASHBOARD] initialize() INICIO");
        if (ventasHoyIconLabel != null) ventasHoyIconLabel.setText("$");
        configurarColumnasMedicamentos();
        configurarColumnasUsuarios();
        configurarColumnasVentas();
        configurarCombosMedicamento();
        configurarColumnasClientes();
        configurarColumnasProveedores();
        configurarColumnasCompras();
        configurarColumnasReportes();
        configurarCombosNuevos();
        try {
            System.out.println("[DASHBOARD] Cargando categorias...");
            categoriaMedField.getItems().setAll(medicamentoDAO.obtenerCategoriasUnicas());
            System.out.println("[DASHBOARD] Categorias cargadas OK");
        } catch (java.sql.SQLException e) {
            System.err.println("[DASHBOARD] ERROR cargando categorias: " + e.getMessage());
            e.printStackTrace();
        }
        rolUsuCombo.setItems(FXCollections.observableArrayList(
                Usuario.ROL_ADMIN, Usuario.ROL_CAJERO, Usuario.ROL_REPORTES));

        tablaMedicamentos.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            medicamentoSeleccionado = sel;
            if (sel != null) cargarFormularioMedicamento(sel);
        });

        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) cargarFormularioUsuario(sel);
        });
        if (tablaClientes != null) {
            tablaClientes.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
                clienteSeleccionado = sel;
                if (sel != null) cargarFormularioCliente(sel);
            });
        }
        if (tablaProveedores != null) {
            tablaProveedores.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
                proveedorSeleccionado = sel;
                if (sel != null) cargarFormularioProveedor(sel);
            });
        }

        tablaCarrito.setItems(carrito);
        tablaHistorialVentas.setItems(historialVentas);
    }

    public void setUsuario(Usuario usuario) {
        System.out.println("[DASHBOARD] setUsuario() INICIO - " + usuario.getNombre());
        this.usuarioActual = usuario;

        userNameLabel.setText(usuario.getNombre());
        userRoleLabel.setText(usuario.getDescripcionRol());
        userInitialsLabel.setText(iniciales(usuario.getNombre()));
        welcomeNameLabel.setText(usuario.getNombre());

        rootPane.getStyleClass().removeIf(c -> c.startsWith("role-"));
        rootPane.getStyleClass().add(usuario.getClaseColorRol());

        Set<String> modulos = usuario.getModulosPermitidos();
        aplicarPermisos(modulos);

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

        if (navClientes != null) {
            navClientes.setVisible(modulos.contains("CLIENTES"));
            navClientes.setManaged(modulos.contains("CLIENTES"));
        }
        if (navCompras != null) {
            navCompras.setVisible(modulos.contains("COMPRAS"));
            navCompras.setManaged(modulos.contains("COMPRAS"));
        }
        if (navProveedores != null) {
            navProveedores.setVisible(modulos.contains("PROVEEDORES"));
            navProveedores.setManaged(modulos.contains("PROVEEDORES"));
        }
    }

    private void cargarDatosIniciales() {
        System.out.println("[DASHBOARD] cargarDatosIniciales() INICIO");
        try {
            System.out.println("[DASHBOARD] Cargando medicamentos...");
            medicamentos.setAll(medicamentoDAO.listar());
            filteredMedicamentos = new FilteredList<>(medicamentos, p -> true);
            System.out.println("[DASHBOARD] Medicamentos: " + medicamentos.size());
            tablaMedicamentos.setItems(filteredMedicamentos);
            ventaMedicamentoCombo.setItems(medicamentos);
            entradaMedicamentoCombo.setItems(medicamentos);

            System.out.println("[DASHBOARD] Cargando usuarios...");
            usuarios.setAll(usuarioDAO.listar());
            filteredUsuarios = new FilteredList<>(usuarios, p -> true);
            System.out.println("[DASHBOARD] Usuarios: " + usuarios.size());
            tablaUsuarios.setItems(filteredUsuarios);

            try {
                System.out.println("[DASHBOARD] Cargando clientes...");
                clientes.setAll(clienteDAO.listar());
                filteredClientes = new FilteredList<>(clientes, p -> true);
                System.out.println("[DASHBOARD] Clientes: " + clientes.size());
                tablaClientes.setItems(filteredClientes);
                ventaClienteCombo.setItems(clientes);
            } catch (Exception e) {
                System.err.println("[DASHBOARD] ERROR cargando clientes: " + e.getMessage());
                e.printStackTrace();
            }
            try {
                System.out.println("[DASHBOARD] Cargando proveedores...");
                proveedores.setAll(proveedorDAO.listar());
                filteredProveedores = new FilteredList<>(proveedores, p -> true);
                System.out.println("[DASHBOARD] Proveedores: " + proveedores.size());
                tablaProveedores.setItems(filteredProveedores);
                entradaProveedorCombo.setItems(proveedores);
            } catch (Exception e) {
                System.err.println("[DASHBOARD] ERROR cargando proveedores: " + e.getMessage());
                e.printStackTrace();
            }
            try {
                System.out.println("[DASHBOARD] Cargando compras...");
                compras.setAll(compraDAO.listar());
                filteredCompras = new FilteredList<>(compras, p -> true);
                System.out.println("[DASHBOARD] Compras: " + compras.size());
                tablaCompras.setItems(filteredCompras);
            } catch (Exception e) {
                System.err.println("[DASHBOARD] ERROR cargando compras: " + e.getMessage());
                e.printStackTrace();
            }

            if (usuarioActual.getModulosPermitidos().contains("VENTAS")) {
                System.out.println("[DASHBOARD] Cargando historial de ventas...");
                cargarHistorialVentas();
                System.out.println("[DASHBOARD] Actualizando resumen ventas hoy...");
                actualizarResumenVentasHoy();
            }

            System.out.println("[DASHBOARD] Actualizando stats home...");
            actualizarStatsHome();
            System.out.println("[DASHBOARD] Construyendo grafico...");
            construirGraficoVentas();
            System.out.println("[DASHBOARD] Construyendo actividad...");
            construirActividad();
            System.out.println("[DASHBOARD] Configurando busquedas...");
            configurarBusquedas();
            System.out.println("[DASHBOARD] cargarDatosIniciales() FIN OK");
        } catch (Exception e) {
            System.err.println("[DASHBOARD] ERROR en cargarDatosIniciales: " + e.getMessage());
            e.printStackTrace();
            Alertas.error("No se pudieron cargar los datos desde la base de datos.\n" + e.getMessage());
        }
    }

    private void actualizarStatsHome() {
        long stockBajo = medicamentos.stream().filter(Medicamento::isStockBajo).count();
        long activos = usuarios.stream().filter(Usuario::isActivo).count();
        stockBajoValueLabel.setText(String.valueOf(stockBajo));
        usuariosActivosValueLabel.setText(String.valueOf(activos));
    }

    // ============ NAVEGACIÓN ENTRE SECCIONES ============

    @FXML private void showHome() { showSection("HOME"); }
    @FXML private void showInventario() { showSection("INVENTARIO"); }
    @FXML private void showVentas() { showSection("VENTAS"); }
    @FXML private void showUsuarios() { showSection("USUARIOS"); }
    @FXML private void showReportes() { showSection("REPORTES"); }
    @FXML private void showClientes() { showSection("CLIENTES"); }
    @FXML private void showCompras() { showSection("COMPRAS"); }
    @FXML private void showProveedores() { showSection("PROVEEDORES"); }
    @FXML private void showConfiguracion() { showSection("CONFIGURACION"); loadConfiguracion(); }

    private void showSection(String seccion) {
        homeView.setVisible(false); homeView.setManaged(false);
        inventarioView.setVisible(false); inventarioView.setManaged(false);
        ventasView.setVisible(false); ventasView.setManaged(false);
        usuariosView.setVisible(false); usuariosView.setManaged(false);
        reportesView.setVisible(false); reportesView.setManaged(false);
        configuracionView.setVisible(false); configuracionView.setManaged(false);
        if (clientesView != null) { clientesView.setVisible(false); clientesView.setManaged(false); }
        if (proveedoresView != null) { proveedoresView.setVisible(false); proveedoresView.setManaged(false); }
        if (comprasView != null) { comprasView.setVisible(false); comprasView.setManaged(false); }

        for (Button b : List.of(navHome, navInventario, navVentas, navUsuarios, navReportes, navConfiguracion, navClientes, navCompras, navProveedores)) {
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
                sectionTitleLabel.setText("Ventas");
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
            case "CLIENTES":
                if (clientesView != null) { clientesView.setVisible(true); clientesView.setManaged(true); }
                sectionTitleLabel.setText("Gestión de clientes");
                navClientes.getStyleClass().add("active");
                break;
            case "COMPRAS":
                if (comprasView != null) { comprasView.setVisible(true); comprasView.setManaged(true); }
                sectionTitleLabel.setText("Historial de compras");
                navCompras.getStyleClass().add("active");
                break;
            case "PROVEEDORES":
                if (proveedoresView != null) { proveedoresView.setVisible(true); proveedoresView.setManaged(true); }
                sectionTitleLabel.setText("Directorio de proveedores");
                navProveedores.getStyleClass().add("active");
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
            String categoria = categoriaMedField.getValue() != null ? categoriaMedField.getValue().trim() : (categoriaMedField.getEditor().getText() != null ? categoriaMedField.getEditor().getText().trim() : "");

            if (nombre.isEmpty() || categoria.isEmpty() || precioMedField.getText().trim().isEmpty()
                    || stockMedField.getText().trim().isEmpty() || loteMedField.getText().trim().isEmpty()) {
                Alertas.error("Ningún campo puede quedar vacío.");
                return;
            }

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
            String categoria = categoriaMedField.getValue() != null ? categoriaMedField.getValue().trim() : (categoriaMedField.getEditor().getText() != null ? categoriaMedField.getEditor().getText().trim() : "");
            medicamentoSeleccionado.setCategoria(categoria);
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
    private void handleRegistrarEntrada() {
        Medicamento m = entradaMedicamentoCombo.getValue();
        if (m == null) {
            Alertas.error("Selecciona un medicamento.");
            return;
        }
        int cantidad;
        try {
            cantidad = parseIntPositivo(entradaCantidadField.getText(), "La cantidad");
        } catch (IllegalArgumentException e) {
            Alertas.error(e.getMessage());
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

    @FXML
    private void handleLimpiarMedicamento() {
        limpiarFormularioMedicamento();
    }

    private void cargarFormularioMedicamento(Medicamento m) {
        nombreMedField.setText(m.getNombre());
        categoriaMedField.setValue(m.getCategoria());
        precioMedField.setText(String.valueOf(m.getPrecio()));
        stockMedField.setText(String.valueOf(m.getStock()));
        loteMedField.setText(m.getLote());
        fechaVencMedPicker.setValue(m.getFechaVencimiento());
    }

    private void limpiarFormularioMedicamento() {
        nombreMedField.clear();
        categoriaMedField.setValue(null);
        if (categoriaMedField.getEditor() != null) categoriaMedField.getEditor().clear();
        precioMedField.clear();
        stockMedField.clear();
        loteMedField.clear();
        fechaVencMedPicker.setValue(null);
        medicamentoSeleccionado = null;
        tablaMedicamentos.getSelectionModel().clearSelection();
    }

    // ============ PUNTO DE VENTA ============

    private void configurarColumnasVentas() {
        colCarritoNombre.setCellValueFactory(new PropertyValueFactory<>("nombreMedicamento"));
        colCarritoCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCarritoPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colCarritoSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        colVentaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colVentaCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNombre"));
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
                if (string == null || string.isEmpty()) return null;
                return ventaMedicamentoCombo.getItems().stream()
                        .filter(m -> toString(m).equals(string))
                        .findFirst().orElse(null);
            }
        };
        ventaMedicamentoCombo.setConverter(convertidor);
        entradaMedicamentoCombo.setConverter(convertidor);

        ventaMedicamentoCombo.setEditable(true);
        TextFields.bindAutoCompletion(ventaMedicamentoCombo.getEditor(), ventaMedicamentoCombo.getItems());
    }

    private void configurarCombosNuevos() {
        StringConverter<Cliente> convCliente = new StringConverter<>() {
            @Override
            public String toString(Cliente c) {
                return c == null ? "" : c.getNombres() + " " + c.getApellidos() + " - " + c.getCedula();
            }
            @Override
            public Cliente fromString(String string) {
                if (string == null || string.isEmpty()) return null;
                return ventaClienteCombo.getItems().stream()
                        .filter(c -> toString(c).equals(string))
                        .findFirst().orElse(null);
            }
        };
        if (ventaClienteCombo != null) {
            ventaClienteCombo.setConverter(convCliente);
            ventaClienteCombo.setEditable(true);
            TextFields.bindAutoCompletion(ventaClienteCombo.getEditor(), ventaClienteCombo.getItems());
        }

        StringConverter<Proveedor> convProv = new StringConverter<>() {
            @Override
            public String toString(Proveedor p) {
                return p == null ? "" : p.getNombre();
            }
            @Override
            public Proveedor fromString(String string) {
                return null;
            }
        };
        if (entradaProveedorCombo != null) {
            entradaProveedorCombo.setConverter(convProv);
        }
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
        Cliente cliente = ventaClienteCombo.getValue();
        if (cliente == null) {
            Alertas.error("Debe seleccionar un cliente para registrar la venta.");
            return;
        }
        if (!Alertas.confirmar("¿Registrar esta venta? " + totalVentaLabel.getText())) {
            return;
        }
        try {
            List<DetalleVenta> detallesVendidos = new java.util.ArrayList<>(carrito);
            Venta venta = ventaDAO.registrarVenta(usuarioActual.getId(), cliente.getId(), detallesVendidos);

            Alertas.info("Venta registrada correctamente.\nFactura: " + venta.getNumeroFactura());

            carrito.clear();
            actualizarTotalCarrito();
            cargarDatosIniciales();

        } catch (IllegalArgumentException iae) {
            Alertas.error(iae.getMessage());
        } catch (Exception e) {
            Alertas.error("No se pudo registrar la venta.\n" + e.getMessage());
        }
    }

    @FXML
    private void handleGenerarPDF() {
        Venta v = tablaHistorialVentas.getSelectionModel().getSelectedItem();
        if (v == null) {
            Alertas.error("Debe seleccionar una venta del historial para generar el PDF.");
            return;
        }
        try {
            ClienteDAO cliDAO = new ClienteDAO();
            Cliente c = cliDAO.leer(v.getClienteId());
            if (c == null) {
                c = new Cliente(0, "Consumidor", "Final", "N/A", "9999999999", "N/A", "N/A");
            }
            List<DetalleVenta> detalles = ventaDAO.obtenerDetalle(v.getId());

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Guardar factura PDF");
            chooser.setInitialFileName("Factura_" + v.getNumeroFactura() + ".pdf");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));

            File archivo = chooser.showSaveDialog(btnAnularVenta.getScene().getWindow());
            if (archivo == null) return;

            Document doc = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter.getInstance(doc, new FileOutputStream(archivo));
            doc.open();

            BaseColor brandColor = new BaseColor(111, 168, 175);
            BaseColor lightGray = new BaseColor(230, 230, 230);
            BaseColor darkGray = new BaseColor(130, 130, 130);

            Font fontLogo = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.WHITE);
            Font fontNegocioSmall = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.BLACK);
            Font fontNormal = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
            Font fontNormalGray = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, darkGray);
            Font fontHeader = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.BLACK);
            Font fontHeaderSmall = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.BLACK);

            PdfPTable headerTable = new PdfPTable(3);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1.3f, 4f, 2.5f});

            PdfPCell logoCell = new PdfPCell(new Phrase("HampiCare", fontLogo));
            logoCell.setBackgroundColor(brandColor);
            logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            logoCell.setFixedHeight(60f);
            logoCell.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(logoCell);

            PdfPCell negocioCell = new PdfPCell();
            negocioCell.setBorder(Rectangle.NO_BORDER);
            negocioCell.setPaddingLeft(15f);
            negocioCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            negocioCell.addElement(new Phrase("HampiCare App", fontNegocioSmall));
            negocioCell.addElement(new Phrase("Av. Patria y 12 de Octubre, Quito", fontNormal));
            negocioCell.addElement(new Phrase("170143", fontNormal));
            headerTable.addCell(negocioCell);

            PdfPCell factCell = new PdfPCell();
            factCell.setBorder(Rectangle.NO_BORDER);
            factCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            factCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            String fecStr = v.getFecha() != null ? v.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";

            Paragraph pFact = new Paragraph("Factura# " + v.getNumeroFactura(), fontHeader);
            pFact.setAlignment(Element.ALIGN_RIGHT);
            factCell.addElement(pFact);
            Paragraph pFecL = new Paragraph("Fecha de emisión", fontHeader);
            pFecL.setAlignment(Element.ALIGN_RIGHT);
            factCell.addElement(pFecL);
            Paragraph pFec = new Paragraph(fecStr, fontNormal);
            pFec.setAlignment(Element.ALIGN_RIGHT);
            factCell.addElement(pFec);

            headerTable.addCell(factCell);
            doc.add(headerTable);

            doc.add(new Paragraph(" "));
            PdfPTable lineTable = new PdfPTable(1);
            lineTable.setWidthPercentage(100);
            PdfPCell lineCell = new PdfPCell(new Phrase(""));
            lineCell.setBorder(Rectangle.BOTTOM);
            lineCell.setBorderColorBottom(brandColor);
            lineCell.setBorderWidthBottom(3f);
            lineTable.addCell(lineCell);
            doc.add(lineTable);
            doc.add(new Paragraph(" "));

            PdfPTable billingTable = new PdfPTable(3);
            billingTable.setWidthPercentage(100);
            billingTable.setWidths(new float[]{2f, 2f, 1.5f});

            PdfPCell billTo = new PdfPCell();
            billTo.setBorder(Rectangle.NO_BORDER);
            billTo.addElement(new Phrase("FACTURAR A", fontHeaderSmall));
            billTo.addElement(new Paragraph(c.getNombres() + " " + c.getApellidos(), fontNormal));
            billTo.addElement(new Paragraph(c.getCorreo(), fontNormal));
            billTo.addElement(new Paragraph(c.getTelefono(), fontNormal));
            billTo.addElement(new Paragraph("CI: " + c.getCedula(), fontNormal));
            billingTable.addCell(billTo);

            PdfPCell details = new PdfPCell();
            details.setBorder(Rectangle.NO_BORDER);
            details.addElement(new Phrase("DETALLES", fontHeaderSmall));
            details.addElement(new Paragraph("Venta de productos", fontNormal));
            details.addElement(new Paragraph("farmacéuticos en mostrador.", fontNormal));
            billingTable.addCell(details);

            PdfPCell payment = new PdfPCell();
            payment.setBorder(Rectangle.NO_BORDER);
            payment.addElement(new Phrase("PAGO", fontHeaderSmall));
            payment.addElement(new Paragraph("Vencimiento " + fecStr, fontNormal));
            payment.addElement(new Paragraph(String.format("$%.2f", v.getTotal()), fontNormal));
            billingTable.addCell(payment);

            doc.add(billingTable);
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph(" "));

            PdfPTable itemsTable = new PdfPTable(4);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{4f, 1f, 1.5f, 1.5f});

            String[] headers = {"ARTÍCULOS", "CANT.", "PRECIOS", "MONTO"};
            for (int i = 0; i < headers.length; i++) {
                PdfPCell hc = new PdfPCell(new Phrase(headers[i], fontHeaderSmall));
                hc.setBorder(Rectangle.BOTTOM | Rectangle.TOP);
                hc.setBorderColor(lightGray);
                hc.setPaddingTop(5f);
                hc.setPaddingBottom(5f);
                if (i > 0) hc.setHorizontalAlignment(Element.ALIGN_RIGHT);
                itemsTable.addCell(hc);
            }

            for (DetalleVenta d : detalles) {
                PdfPCell c1 = new PdfPCell();
                c1.setBorder(Rectangle.BOTTOM);
                c1.setBorderColor(lightGray);
                c1.setPaddingTop(8f);
                c1.setPaddingBottom(8f);
                c1.addElement(new Phrase(d.getNombreMedicamento(), fontNormal));
                c1.addElement(new Phrase("Medicina", fontNormalGray));
                itemsTable.addCell(c1);

                PdfPCell c2 = new PdfPCell(new Phrase(String.valueOf(d.getCantidad()), fontNormal));
                c2.setBorder(Rectangle.BOTTOM);
                c2.setBorderColor(lightGray);
                c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
                itemsTable.addCell(c2);

                PdfPCell c3 = new PdfPCell(new Phrase(String.format("$%.2f", d.getPrecioUnitario()), fontNormal));
                c3.setBorder(Rectangle.BOTTOM);
                c3.setBorderColor(lightGray);
                c3.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c3.setVerticalAlignment(Element.ALIGN_MIDDLE);
                itemsTable.addCell(c3);

                PdfPCell c4 = new PdfPCell(new Phrase(String.format("$%.2f", d.getSubtotal()), fontNormal));
                c4.setBorder(Rectangle.BOTTOM);
                c4.setBorderColor(lightGray);
                c4.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c4.setVerticalAlignment(Element.ALIGN_MIDDLE);
                itemsTable.addCell(c4);
            }
            doc.add(itemsTable);
            doc.add(new Paragraph(" "));

            PdfPTable totalsTable = new PdfPTable(2);
            totalsTable.setWidthPercentage(100);
            totalsTable.setWidths(new float[]{7f, 1.5f});

            double sub = v.getTotal() / 1.15;
            double tax = v.getTotal() - sub;

            PdfPCell s1 = new PdfPCell(new Phrase("Subtotal", fontNormalGray));
            s1.setBorder(Rectangle.NO_BORDER);
            s1.setPaddingTop(5f);
            s1.setPaddingBottom(5f);
            totalsTable.addCell(s1);
            PdfPCell s2 = new PdfPCell(new Phrase(String.format("$%.2f", sub), fontNormalGray));
            s2.setBorder(Rectangle.NO_BORDER);
            s2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            s2.setPaddingTop(5f);
            s2.setPaddingBottom(5f);
            totalsTable.addCell(s2);

            PdfPCell t1 = new PdfPCell(new Phrase("Tax (15%)", fontNormalGray));
            t1.setBorder(Rectangle.BOTTOM);
            t1.setBorderColor(lightGray);
            t1.setPaddingTop(5f);
            t1.setPaddingBottom(8f);
            totalsTable.addCell(t1);
            PdfPCell t2 = new PdfPCell(new Phrase(String.format("$%.2f", tax), fontNormalGray));
            t2.setBorder(Rectangle.BOTTOM);
            t2.setBorderColor(lightGray);
            t2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            t2.setPaddingTop(5f);
            t2.setPaddingBottom(8f);
            totalsTable.addCell(t2);

            PdfPCell tot1 = new PdfPCell(new Phrase("Total a pagar", fontHeader));
            tot1.setBorder(Rectangle.BOTTOM);
            tot1.setBorderColor(lightGray);
            tot1.setPaddingTop(8f);
            tot1.setPaddingBottom(8f);
            totalsTable.addCell(tot1);
            PdfPCell tot2 = new PdfPCell(new Phrase(String.format("$%.2f", v.getTotal()), fontHeader));
            tot2.setBorder(Rectangle.BOTTOM);
            tot2.setBorderColor(lightGray);
            tot2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tot2.setPaddingTop(8f);
            tot2.setPaddingBottom(8f);
            totalsTable.addCell(tot2);

            doc.add(totalsTable);
            doc.close();
            Alertas.info("PDF generado con éxito en:\n" + archivo.getAbsolutePath());
        } catch (Exception e) {
            Alertas.error("Error al generar PDF:\n" + e.getMessage());
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

    // ============ CRUD COMPRAS ============

    @FXML
    private void handleRegistrarCompra() {
        Proveedor p = entradaProveedorCombo.getValue();
        Medicamento m = entradaMedicamentoCombo.getValue();
        if (p == null || m == null) {
            Alertas.error("Selecciona un proveedor y un medicamento.");
            return;
        }

        int cantidad;
        double precio;
        try {
            cantidad = Integer.parseInt(entradaCantidadField.getText().trim());
            precio = Double.parseDouble(entradaPrecioCompraField.getText().trim());
        } catch (NumberFormatException e) {
            Alertas.error("La cantidad y precio deben ser valores numéricos válidos.");
            return;
        }
        if (cantidad <= 0 || precio < 0) {
            Alertas.error("La cantidad debe ser mayor a 0 y el precio no puede ser negativo.");
            return;
        }

        try {
            Compra c = new Compra();
            c.setProveedorId(p.getId());
            c.setProveedorNombre(p.getNombre());
            c.setMedicamentoId(m.getId());
            c.setMedicamentoNombre(m.getNombre());
            c.setCantidad(cantidad);
            c.setPrecioCompra(precio);
            c.setFecha(LocalDateTime.now());

            compraDAO.guardar(c);
            compras.add(c);

            m.setStock(m.getStock() + cantidad);
            medicamentoDAO.actualizar(m);

            Alertas.info("Compra registrada: +" + cantidad + " unidades de \"" + m.getNombre() + "\".");
            entradaCantidadField.clear();
            entradaPrecioCompraField.clear();
            tablaMedicamentos.refresh();
            cargarDatosIniciales();
        } catch (Exception e) {
            Alertas.error("No se pudo registrar la compra.\n" + e.getMessage());
        }
    }

    // ============ CRUD CLIENTES ============

    private void configurarColumnasClientes() {
        if (colCliCedula == null) return;
        colCliCedula.setCellValueFactory(new PropertyValueFactory<>("cedula"));
        colCliNombres.setCellValueFactory(new PropertyValueFactory<>("nombres"));
        colCliApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colCliCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colCliTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colCliSector.setCellValueFactory(new PropertyValueFactory<>("sector"));
    }

    @FXML
    private void handleGuardarCliente() {
        try {
            String nombres = nombreCliField.getText().trim();
            String apellidos = apellidoCliField.getText().trim();
            String cedula = cedulaCliField.getText().trim();
            String correo = correoCliField.getText().trim();
            String telefono = telefonoCliField.getText().trim();
            String sector = sectorCliField.getText().trim();

            if (nombres.isEmpty() || apellidos.isEmpty() || cedula.isEmpty()) {
                Alertas.error("Nombres, apellidos y cédula son obligatorios.");
                return;
            }

            Cliente cli = new Cliente(0, nombres, apellidos, correo, cedula, telefono, sector);
            clienteDAO.guardar(cli);
            Alertas.info("Cliente guardado correctamente.");
            limpiarFormularioCliente();
            cargarDatosIniciales();
        } catch (Exception e) {
            Alertas.error("No se pudo guardar el cliente.\n" + e.getMessage());
        }
    }

    @FXML
    private void handleActualizarCliente() {
        if (clienteSeleccionado == null) {
            Alertas.error("Selecciona un cliente de la tabla para editarlo.");
            return;
        }
        try {
            clienteSeleccionado.setNombre(nombreCliField.getText().trim());
            clienteSeleccionado.setApellidos(apellidoCliField.getText().trim());
            clienteSeleccionado.setCedula(cedulaCliField.getText().trim());
            clienteSeleccionado.setCorreo(correoCliField.getText().trim());
            clienteSeleccionado.setTelefono(telefonoCliField.getText().trim());
            clienteSeleccionado.setSector(sectorCliField.getText().trim());

            clienteDAO.actualizar(clienteSeleccionado);
            Alertas.info("Cliente actualizado correctamente.");
            limpiarFormularioCliente();
            cargarDatosIniciales();
        } catch (Exception e) {
            Alertas.error("No se pudo actualizar el cliente.\n" + e.getMessage());
        }
    }

    @FXML
    private void handleEliminarCliente() {
        if (clienteSeleccionado == null) {
            Alertas.error("Selecciona un cliente de la tabla para eliminarlo.");
            return;
        }
        if (!Alertas.confirmar("¿Eliminar al cliente \"" + clienteSeleccionado.getNombre() + " " + clienteSeleccionado.getApellidos() + "\"?")) {
            return;
        }
        try {
            clienteDAO.eliminar(clienteSeleccionado.getId());
            Alertas.info("Cliente eliminado.");
            limpiarFormularioCliente();
            cargarDatosIniciales();
        } catch (Exception e) {
            Alertas.error("No se pudo eliminar el cliente.\n" + e.getMessage());
        }
    }

    @FXML
    private void handleLimpiarCliente() {
        limpiarFormularioCliente();
    }

    private void cargarFormularioCliente(Cliente c) {
        nombreCliField.setText(c.getNombre());
        apellidoCliField.setText(c.getApellidos());
        cedulaCliField.setText(c.getCedula());
        correoCliField.setText(c.getCorreo());
        telefonoCliField.setText(c.getTelefono());
        sectorCliField.setText(c.getSector());
    }

    private void limpiarFormularioCliente() {
        nombreCliField.clear();
        apellidoCliField.clear();
        cedulaCliField.clear();
        correoCliField.clear();
        telefonoCliField.clear();
        sectorCliField.clear();
        clienteSeleccionado = null;
        if (tablaClientes != null) tablaClientes.getSelectionModel().clearSelection();
    }

    // ============ CRUD PROVEEDORES ============

    private void configurarColumnasProveedores() {
        if (colProvId == null) return;
        colProvId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProvNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colProvCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colProvTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
    }

    @FXML
    private void handleGuardarProveedor() {
        try {
            String nombre = nombreProvField.getText().trim();
            String correo = correoProvField.getText().trim();
            String telefono = telefonoProvField.getText().trim();

            if (nombre.isEmpty()) {
                Alertas.error("El nombre del proveedor es obligatorio.");
                return;
            }

            Proveedor p = new Proveedor(0, nombre, correo, telefono);
            proveedorDAO.guardar(p);
            Alertas.info("Proveedor guardado correctamente.");
            limpiarFormularioProveedor();
            cargarDatosIniciales();
        } catch (Exception e) {
            Alertas.error("No se pudo guardar el proveedor.\n" + e.getMessage());
        }
    }

    @FXML
    private void handleActualizarProveedor() {
        if (proveedorSeleccionado == null) {
            Alertas.error("Selecciona un proveedor de la tabla para editarlo.");
            return;
        }
        try {
            proveedorSeleccionado.setNombre(nombreProvField.getText().trim());
            proveedorSeleccionado.setCorreo(correoProvField.getText().trim());
            proveedorSeleccionado.setTelefono(telefonoProvField.getText().trim());

            proveedorDAO.actualizar(proveedorSeleccionado);
            Alertas.info("Proveedor actualizado correctamente.");
            limpiarFormularioProveedor();
            cargarDatosIniciales();
        } catch (Exception e) {
            Alertas.error("No se pudo actualizar el proveedor.\n" + e.getMessage());
        }
    }

    @FXML
    private void handleEliminarProveedor() {
        if (proveedorSeleccionado == null) {
            Alertas.error("Selecciona un proveedor de la tabla para eliminarlo.");
            return;
        }
        if (!Alertas.confirmar("¿Eliminar al proveedor \"" + proveedorSeleccionado.getNombre() + "\"?")) {
            return;
        }
        try {
            proveedorDAO.eliminar(proveedorSeleccionado.getId());
            Alertas.info("Proveedor eliminado.");
            limpiarFormularioProveedor();
            cargarDatosIniciales();
        } catch (Exception e) {
            Alertas.error("No se pudo eliminar el proveedor.\n" + e.getMessage());
        }
    }

    @FXML
    private void handleLimpiarProveedor() {
        limpiarFormularioProveedor();
    }

    private void cargarFormularioProveedor(Proveedor p) {
        nombreProvField.setText(p.getNombre());
        correoProvField.setText(p.getCorreo());
        telefonoProvField.setText(p.getTelefono());
    }

    private void limpiarFormularioProveedor() {
        nombreProvField.clear();
        correoProvField.clear();
        telefonoProvField.clear();
        proveedorSeleccionado = null;
        if (tablaProveedores != null) tablaProveedores.getSelectionModel().clearSelection();
    }

    // ============ COMPRAS COLUMNAS ============

    private void configurarColumnasCompras() {
        if (colCompraId == null) return;
        colCompraId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCompraProveedor.setCellValueFactory(new PropertyValueFactory<>("proveedorNombre"));
        colCompraMedicamento.setCellValueFactory(new PropertyValueFactory<>("medicamentoNombre"));
        colCompraCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCompraPrecio.setCellValueFactory(new PropertyValueFactory<>("precioCompra"));
        colCompraFecha.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFecha() != null ? cellData.getValue().getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : ""));
    }

    // ============ CRUD USUARIOS ============

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

    private void showRepPanel(VBox panel, Button btn) {
        for (VBox p : List.of(repVentasPanel, repTopMedsPanel, repInventarioPanel, repComprasPanel, repClientesPanel, repCajaPanel)) {
            p.setVisible(false); p.setManaged(false);
        }
        for (Button b : List.of(btnRepVentas, btnRepTopMeds, btnRepInventario, btnRepCompras, btnRepClientes, btnRepCaja)) {
            b.getStyleClass().remove("btn-primary");
            if (!b.getStyleClass().contains("btn-ghost")) b.getStyleClass().add("btn-ghost");
        }
        panel.setVisible(true); panel.setManaged(true);
        btn.getStyleClass().remove("btn-ghost");
        btn.getStyleClass().add("btn-primary");
    }

    @FXML private void showRepVentas() {
        showRepPanel(repVentasPanel, btnRepVentas);
        if (repVentasDesde.getValue() == null) {
            repVentasDesde.setValue(LocalDate.now().withDayOfMonth(1));
            repVentasHasta.setValue(LocalDate.now());
            handleRepVentasBuscar();
        }
    }

    @FXML private void showRepTopMeds() {
        showRepPanel(repTopMedsPanel, btnRepTopMeds);
        cargarRepTopMeds();
    }

    @FXML private void showRepInventario() {
        showRepPanel(repInventarioPanel, btnRepInventario);
        cargarRepInventario();
    }

    @FXML private void showRepCompras() {
        showRepPanel(repComprasPanel, btnRepCompras);
        if (repComprasDesde.getValue() == null) {
            repComprasDesde.setValue(LocalDate.now().withDayOfMonth(1));
            repComprasHasta.setValue(LocalDate.now());
            handleRepComprasBuscar();
        }
    }

    @FXML private void showRepClientes() {
        showRepPanel(repClientesPanel, btnRepClientes);
        cargarRepClientes();
    }

    @FXML private void showRepCaja() {
        showRepPanel(repCajaPanel, btnRepCaja);
        if (repCajaDesde.getValue() == null) {
            repCajaDesde.setValue(LocalDate.now().withDayOfMonth(1));
            repCajaHasta.setValue(LocalDate.now());
            handleRepCajaBuscar();
        }
    }

    // ---- Configurar columnas reportes ----
    private void configurarColumnasReportes() {
        colRepVenId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRepVenFactura.setCellValueFactory(new PropertyValueFactory<>("numeroFactura"));
        colRepVenCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        colRepVenFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colRepVenFecha.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().get(getIndex()) == null) setText(null);
                else {
                    var f = getTableView().getItems().get(getIndex()).getFecha();
                    setText(f != null ? f.format(FMT_FECHA_VENTA) : "");
                }
            }
        });
        colRepVenTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colRepVenTotal.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$%.2f", item));
            }
        });

        colRepTopId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRepTopNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colRepTopCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colRepTopVendido.setCellValueFactory(new PropertyValueFactory<>("totalVendido"));
        colRepTopIngresos.setCellValueFactory(new PropertyValueFactory<>("totalIngresos"));
        colRepTopIngresos.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$%.2f", item));
            }
        });

        colRepInvId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRepInvNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colRepInvCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colRepInvStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colRepInvPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colRepInvPrecio.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$%.2f", item));
            }
        });
        colRepInvLote.setCellValueFactory(new PropertyValueFactory<>("lote"));
        colRepInvVence.setCellValueFactory(new PropertyValueFactory<>("fechaVencimiento"));
        colRepInvEstado.setCellValueFactory(new PropertyValueFactory<>(""));
        colRepInvEstado.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().get(getIndex()) == null) { setText(null); return; }
                var inv = getTableView().getItems().get(getIndex());
                if (inv.isStockBajo() && inv.isPorVencer()) setText("Stock bajo + por vencer");
                else if (inv.isStockBajo()) setText("Stock bajo");
                else if (inv.isPorVencer()) setText("Por vencer");
                else setText("OK");
            }
        });

        colRepCompId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRepCompProveedor.setCellValueFactory(new PropertyValueFactory<>("proveedor"));
        colRepCompMedicamento.setCellValueFactory(new PropertyValueFactory<>("medicamento"));
        colRepCompCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colRepCompPrecio.setCellValueFactory(new PropertyValueFactory<>("precioCompra"));
        colRepCompPrecio.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$%.2f", item));
            }
        });
        colRepCompFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colRepCompFecha.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().get(getIndex()) == null) setText(null);
                else {
                    var f = getTableView().getItems().get(getIndex()).getFecha();
                    setText(f != null ? f.format(FMT_FECHA_VENTA) : "");
                }
            }
        });

        colRepCliId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRepCliNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colRepCliCedula.setCellValueFactory(new PropertyValueFactory<>("cedula"));
        colRepCliCompras.setCellValueFactory(new PropertyValueFactory<>("totalCompras"));
        colRepCliGastado.setCellValueFactory(new PropertyValueFactory<>("totalGastado"));
        colRepCliGastado.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$%.2f", item));
            }
        });
    }

    // ---- Cargar datos reportes ----
    @FXML private void handleRepVentasBuscar() {
        LocalDate d = repVentasDesde.getValue() != null ? repVentasDesde.getValue() : LocalDate.now().withDayOfMonth(1);
        LocalDate h = repVentasHasta.getValue() != null ? repVentasHasta.getValue() : LocalDate.now();
        try {
            List<ReporteDAO.VentaPeriodo> lista = reporteDAO.ventasPorPeriodo(d, h);
            tablaRepVentas.setItems(FXCollections.observableArrayList(lista));
            double total = lista.stream().mapToDouble(ReporteDAO.VentaPeriodo::getTotal).sum();
            repVentasResumen.setText(String.format("Total: $%.2f  |  %d ventas", total, lista.size()));
        } catch (Exception e) {
            Alertas.error("Error cargando ventas: " + e.getMessage());
        }
    }

    @FXML private void handleRepVentasPDF() {
        LocalDate d = repVentasDesde.getValue() != null ? repVentasDesde.getValue() : LocalDate.now().withDayOfMonth(1);
        LocalDate h = repVentasHasta.getValue() != null ? repVentasHasta.getValue() : LocalDate.now();
        try {
            List<ReporteDAO.VentaPeriodo> lista = reporteDAO.ventasPorPeriodo(d, h);
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Exportar reporte de ventas");
            chooser.setInitialFileName("reporte_ventas.pdf");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            java.io.File archivo = chooser.showSaveDialog(tablaRepVentas.getScene().getWindow());
            if (archivo == null) return;
            Document doc = new Document(PageSize.LETTER);
            PdfWriter.getInstance(doc, new FileOutputStream(archivo));
            doc.open();
            doc.add(new Paragraph("REPORTE DE VENTAS - HampiCare", new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD)));
            doc.add(new Paragraph("Periodo: " + d + " al " + h));
            doc.add(new Paragraph(" "));
            PdfPTable tabla = new PdfPTable(4);
            tabla.setWidthPercentage(100);
            String[] cols = {"Factura", "Cliente", "Fecha", "Total"};
            for (String c : cols) {
                PdfPCell cell = new PdfPCell(new Phrase(c));
                cell.setBackgroundColor(BaseColor.DARK_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorderWidth(2);
                tabla.addCell(cell);
            }
            double totalGeneral = 0;
            for (ReporteDAO.VentaPeriodo v : lista) {
                tabla.addCell(v.getNumeroFactura());
                tabla.addCell(v.getCliente());
                tabla.addCell(v.getFecha() != null ? v.getFecha().format(FMT_FECHA_VENTA) : "");
                tabla.addCell(String.format("$%.2f", v.getTotal()));
                totalGeneral += v.getTotal();
            }
            doc.add(tabla);
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("TOTAL: $" + String.format("%.2f", totalGeneral) + "  (" + lista.size() + " ventas)", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
            doc.close();
            Alertas.info("PDF exportado:\n" + archivo.getAbsolutePath());
        } catch (Exception e) {
            Alertas.error("Error exportando PDF: " + e.getMessage());
        }
    }

    private void cargarRepTopMeds() {
        try {
            List<ReporteDAO.TopMedicamento> lista = reporteDAO.topMedicamentos();
            tablaRepTopMeds.setItems(FXCollections.observableArrayList(lista));
        } catch (Exception e) {
            Alertas.error("Error cargando top medicamentos: " + e.getMessage());
        }
    }

    @FXML private void handleRepTopMedsPDF() {
        try {
            List<ReporteDAO.TopMedicamento> lista = reporteDAO.topMedicamentos();
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Exportar top medicamentos");
            chooser.setInitialFileName("top_medicamentos.pdf");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            java.io.File archivo = chooser.showSaveDialog(tablaRepTopMeds.getScene().getWindow());
            if (archivo == null) return;
            Document doc = new Document(PageSize.LETTER);
            PdfWriter.getInstance(doc, new FileOutputStream(archivo));
            doc.open();
            doc.add(new Paragraph("TOP MEDICAMENTOS MAS VENDIDOS - HampiCare", new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD)));
            doc.add(new Paragraph(" "));
            PdfPTable tabla = new PdfPTable(4);
            tabla.setWidthPercentage(100);
            String[] cols = {"Medicamento", "Categoria", "Unidades", "Ingresos"};
            for (String c : cols) {
                PdfPCell cell = new PdfPCell(new Phrase(c));
                cell.setBackgroundColor(BaseColor.DARK_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabla.addCell(cell);
            }
            for (ReporteDAO.TopMedicamento m : lista) {
                tabla.addCell(m.getNombre());
                tabla.addCell(m.getCategoria());
                tabla.addCell(String.valueOf(m.getTotalVendido()));
                tabla.addCell(String.format("$%.2f", m.getTotalIngresos()));
            }
            doc.add(tabla);
            doc.close();
            Alertas.info("PDF exportado:\n" + archivo.getAbsolutePath());
        } catch (Exception e) {
            Alertas.error("Error exportando PDF: " + e.getMessage());
        }
    }

    private void cargarRepInventario() {
        try {
            List<ReporteDAO.InventarioItem> lista = reporteDAO.inventarioActual();
            tablaRepInventario.setItems(FXCollections.observableArrayList(lista));
        } catch (Exception e) {
            Alertas.error("Error cargando inventario: " + e.getMessage());
        }
    }

    @FXML private void handleRepInventarioPDF() {
        try {
            List<ReporteDAO.InventarioItem> lista = reporteDAO.inventarioActual();
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Exportar inventario");
            chooser.setInitialFileName("inventario.pdf");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            java.io.File archivo = chooser.showSaveDialog(tablaRepInventario.getScene().getWindow());
            if (archivo == null) return;
            Document doc = new Document(PageSize.LETTER_LANDSCAPE);
            PdfWriter.getInstance(doc, new FileOutputStream(archivo));
            doc.open();
            doc.add(new Paragraph("REPORTE DE INVENTARIO - HampiCare", new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD)));
            doc.add(new Paragraph(" "));
            PdfPTable tabla = new PdfPTable(6);
            tabla.setWidthPercentage(100);
            String[] cols = {"Medicamento", "Categoria", "Stock", "Precio", "Lote", "Estado"};
            for (String c : cols) {
                PdfPCell cell = new PdfPCell(new Phrase(c));
                cell.setBackgroundColor(BaseColor.DARK_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabla.addCell(cell);
            }
            for (ReporteDAO.InventarioItem i : lista) {
                tabla.addCell(i.getNombre());
                tabla.addCell(i.getCategoria());
                tabla.addCell(String.valueOf(i.getStock()));
                tabla.addCell(String.format("$%.2f", i.getPrecio()));
                tabla.addCell(i.getLote());
                String estado = i.isStockBajo() ? "Stock bajo" : "OK";
                if (i.isPorVencer()) estado += " | Por vencer";
                tabla.addCell(estado);
            }
            doc.add(tabla);
            doc.close();
            Alertas.info("PDF exportado:\n" + archivo.getAbsolutePath());
        } catch (Exception e) {
            Alertas.error("Error exportando PDF: " + e.getMessage());
        }
    }

    @FXML private void handleRepComprasBuscar() {
        LocalDate d = repComprasDesde.getValue() != null ? repComprasDesde.getValue() : LocalDate.now().withDayOfMonth(1);
        LocalDate h = repComprasHasta.getValue() != null ? repComprasHasta.getValue() : LocalDate.now();
        try {
            List<ReporteDAO.CompraPeriodo> lista = reporteDAO.comprasPorPeriodo(d, h);
            tablaRepCompras.setItems(FXCollections.observableArrayList(lista));
            double total = lista.stream().mapToDouble(c -> c.getPrecioCompra() * c.getCantidad()).sum();
            repComprasResumen.setText(String.format("Total: $%.2f  |  %d compras", total, lista.size()));
        } catch (Exception e) {
            Alertas.error("Error cargando compras: " + e.getMessage());
        }
    }

    @FXML private void handleRepComprasPDF() {
        LocalDate d = repComprasDesde.getValue() != null ? repComprasDesde.getValue() : LocalDate.now().withDayOfMonth(1);
        LocalDate h = repComprasHasta.getValue() != null ? repComprasHasta.getValue() : LocalDate.now();
        try {
            List<ReporteDAO.CompraPeriodo> lista = reporteDAO.comprasPorPeriodo(d, h);
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Exportar reporte de compras");
            chooser.setInitialFileName("reporte_compras.pdf");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            java.io.File archivo = chooser.showSaveDialog(tablaRepCompras.getScene().getWindow());
            if (archivo == null) return;
            Document doc = new Document(PageSize.LETTER);
            PdfWriter.getInstance(doc, new FileOutputStream(archivo));
            doc.open();
            doc.add(new Paragraph("REPORTE DE COMPRAS - HampiCare", new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD)));
            doc.add(new Paragraph("Periodo: " + d + " al " + h));
            doc.add(new Paragraph(" "));
            PdfPTable tabla = new PdfPTable(5);
            tabla.setWidthPercentage(100);
            String[] cols = {"Proveedor", "Medicamento", "Cantidad", "P. Compra", "Fecha"};
            for (String c : cols) {
                PdfPCell cell = new PdfPCell(new Phrase(c));
                cell.setBackgroundColor(BaseColor.DARK_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabla.addCell(cell);
            }
            double totalGeneral = 0;
            for (ReporteDAO.CompraPeriodo co : lista) {
                tabla.addCell(co.getProveedor());
                tabla.addCell(co.getMedicamento());
                tabla.addCell(String.valueOf(co.getCantidad()));
                tabla.addCell(String.format("$%.2f", co.getPrecioCompra()));
                tabla.addCell(co.getFecha() != null ? co.getFecha().format(FMT_FECHA_VENTA) : "");
                totalGeneral += co.getPrecioCompra() * co.getCantidad();
            }
            doc.add(tabla);
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("TOTAL: $" + String.format("%.2f", totalGeneral) + "  (" + lista.size() + " compras)", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
            doc.close();
            Alertas.info("PDF exportado:\n" + archivo.getAbsolutePath());
        } catch (Exception e) {
            Alertas.error("Error exportando PDF: " + e.getMessage());
        }
    }

    private void cargarRepClientes() {
        try {
            List<ReporteDAO.ClienteReporte> lista = reporteDAO.clientesReporte();
            tablaRepClientes.setItems(FXCollections.observableArrayList(lista));
        } catch (Exception e) {
            Alertas.error("Error cargando clientes: " + e.getMessage());
        }
    }

    @FXML private void handleRepClientesPDF() {
        try {
            List<ReporteDAO.ClienteReporte> lista = reporteDAO.clientesReporte();
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Exportar reporte de clientes");
            chooser.setInitialFileName("reporte_clientes.pdf");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            java.io.File archivo = chooser.showSaveDialog(tablaRepClientes.getScene().getWindow());
            if (archivo == null) return;
            Document doc = new Document(PageSize.LETTER);
            PdfWriter.getInstance(doc, new FileOutputStream(archivo));
            doc.open();
            doc.add(new Paragraph("REPORTE DE CLIENTES - HampiCare", new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD)));
            doc.add(new Paragraph(" "));
            PdfPTable tabla = new PdfPTable(4);
            tabla.setWidthPercentage(100);
            String[] cols = {"Cliente", "Cedula", "Compras", "Total Gastado"};
            for (String c : cols) {
                PdfPCell cell = new PdfPCell(new Phrase(c));
                cell.setBackgroundColor(BaseColor.DARK_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabla.addCell(cell);
            }
            for (ReporteDAO.ClienteReporte cl : lista) {
                tabla.addCell(cl.getNombre());
                tabla.addCell(cl.getCedula());
                tabla.addCell(String.valueOf(cl.getTotalCompras()));
                tabla.addCell(String.format("$%.2f", cl.getTotalGastado()));
            }
            doc.add(tabla);
            doc.close();
            Alertas.info("PDF exportado:\n" + archivo.getAbsolutePath());
        } catch (Exception e) {
            Alertas.error("Error exportando PDF: " + e.getMessage());
        }
    }

    @FXML private void handleRepCajaBuscar() {
        LocalDate d = repCajaDesde.getValue() != null ? repCajaDesde.getValue() : LocalDate.now().withDayOfMonth(1);
        LocalDate h = repCajaHasta.getValue() != null ? repCajaHasta.getValue() : LocalDate.now();
        try {
            ReporteDAO.ResumenCaja res = reporteDAO.resumenCaja(d, h);
            repCajaTotalVentas.setText(String.format("$%.2f", res.getTotalVentas()));
            repCajaNumVentas.setText(res.getCantidadVentas() + " ventas");
            repCajaTotalCompras.setText(String.format("$%.2f", res.getTotalCompras()));
            repCajaNumCompras.setText(res.getCantidadCompras() + " compras");
            repCajaGanancia.setText(String.format("$%.2f", res.getGananciaEstimada()));
            repCajaGananciaSub.setText(res.getGananciaEstimada() >= 0 ? "Ganancia" : "Perdida");

            Map<String, double[]> porDia = reporteDAO.ventasPorDia(d, h);
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Ventas");
            for (Map.Entry<String, double[]> e : porDia.entrySet()) {
                series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()[1]));
            }
            repCajaChart.getData().clear();
            repCajaChart.getData().add(series);
        } catch (Exception e) {
            Alertas.error("Error cargando resumen de caja: " + e.getMessage());
        }
    }

    @FXML private void handleRepCajaPDF() {
        LocalDate d = repCajaDesde.getValue() != null ? repCajaDesde.getValue() : LocalDate.now().withDayOfMonth(1);
        LocalDate h = repCajaHasta.getValue() != null ? repCajaHasta.getValue() : LocalDate.now();
        try {
            ReporteDAO.ResumenCaja res = reporteDAO.resumenCaja(d, h);
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Exportar resumen de caja");
            chooser.setInitialFileName("resumen_caja.pdf");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            java.io.File archivo = chooser.showSaveDialog(repCajaChart.getScene().getWindow());
            if (archivo == null) return;
            Document doc = new Document(PageSize.LETTER);
            PdfWriter.getInstance(doc, new FileOutputStream(archivo));
            doc.open();
            doc.add(new Paragraph("RESUMEN DE CAJA - HampiCare", new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD)));
            doc.add(new Paragraph("Periodo: " + d + " al " + h));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Total Ventas:    $" + String.format("%.2f", res.getTotalVentas()) + "  (" + res.getCantidadVentas() + " transacciones)"));
            doc.add(new Paragraph("Total Compras:   $" + String.format("%.2f", res.getTotalCompras()) + "  (" + res.getCantidadCompras() + " transacciones)"));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("GANANCIA ESTIMADA: $" + String.format("%.2f", res.getGananciaEstimada()),
                    new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)));
            doc.close();
            Alertas.info("PDF exportado:\n" + archivo.getAbsolutePath());
        } catch (Exception e) {
            Alertas.error("Error exportando PDF: " + e.getMessage());
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
                System.out.println("[DASHBOARD] construirActividad: cargando ultimas ventas...");
                List<Venta> ultimas;
                if (usuarioActual.getModulosPermitidos().contains("USUARIOS")) {
                    ultimas = ventaDAO.ultimasVentasTodas(3);
                } else {
                    ultimas = ventaDAO.ultimasVentas(usuarioActual.getId(), 3);
                }
                System.out.println("[DASHBOARD] ultimasVentas: " + ultimas.size() + " registros");
                if (ultimas.isEmpty()) {
                    agregarActividad("n", "\uD83E\uDDFE", "Aún no has registrado ventas",
                            "Ve a \"Ventas\" para registrar tu primera venta");
                } else {
                    for (Venta v : ultimas) {
                        String fecha = v.getFecha() != null ? v.getFecha().format(FMT_FECHA_VENTA) : "";
                        agregarActividad("g", "\u2713", "Venta " + v.getNumeroFactura(),
                                fecha + " \u00B7 $" + String.format("%.2f", v.getTotal()));
                    }
                }
            } catch (Exception e) {
                agregarActividad("a", "\u26A0", "No se pudo cargar el historial de ventas", "");
            }
        } else {
            agregarActividad("n", "\uD83D\uDCE6", "Inventario", medicamentos.size() + " medicamentos registrados");
        }

        long stockBajo = medicamentos.stream().filter(Medicamento::isStockBajo).count();
        agregarActividad("a", "\u26A0", "Stock bajo detectado", stockBajo + " artículos en el umbral");
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

    // ============ HISTORIAL DE VENTAS ============

    private void cargarHistorialVentas() {
        if (usuarioActual == null) return;
        try {
            if (usuarioActual.getModulosPermitidos().contains("USUARIOS")) {
                System.out.println("[DASHBOARD] cargarHistorialVentas: listarTodas (admin)");
                historialVentas.setAll(ventaDAO.listarTodas());
            } else {
                System.out.println("[DASHBOARD] cargarHistorialVentas: listarPorUsuario id=" + usuarioActual.getId());
                historialVentas.setAll(ventaDAO.listarPorUsuario(usuarioActual.getId()));
            }
            filteredVentas = new FilteredList<>(historialVentas, p -> true);
            tablaHistorialVentas.setItems(filteredVentas);
            System.out.println("[DASHBOARD] Historial ventas cargado: " + historialVentas.size() + " registros");
        } catch (Exception e) {
            System.err.println("[DASHBOARD] ERROR cargando historial ventas: " + e.getMessage());
            e.printStackTrace();
            Alertas.error("No se pudo cargar el historial de ventas.\n" + e.getMessage());
        }
    }

    private void actualizarResumenVentasHoy() {
        try {
            System.out.println("[DASHBOARD] resumenHoy: llamando a ventaDAO...");
            double[] resumen;
            if (usuarioActual.getModulosPermitidos().contains("USUARIOS")) {
                resumen = ventaDAO.resumenHoyTodas();
            } else {
                resumen = ventaDAO.resumenHoy(usuarioActual.getId());
            }
            int cantidadVentas = (int) resumen[0];
            double total = resumen[1];
            ventasHoyValueLabel.setText(String.format("$%.2f", total));
            ventasHoyCountLabel.setText(cantidadVentas == 1
                    ? "1 venta realizada hoy"
                    : cantidadVentas + " ventas realizadas hoy");
        } catch (Exception e) {
        }
    }

    // ============ BÚSQUEDAS ============

    private void configurarBusquedas() {
        if (searchMedicamentos != null && filteredMedicamentos != null) {
            searchMedicamentos.textProperty().addListener((obs, old, newVal) ->
                    filteredMedicamentos.setPredicate(m -> {
                        if (newVal == null || newVal.isEmpty()) return true;
                        String lower = newVal.toLowerCase();
                        return m.getNombre().toLowerCase().contains(lower)
                                || m.getCategoria().toLowerCase().contains(lower)
                                || m.getLote().toLowerCase().contains(lower);
                    }));
        }
        if (searchClientes != null && filteredClientes != null) {
            searchClientes.textProperty().addListener((obs, old, newVal) ->
                    filteredClientes.setPredicate(c -> {
                        if (newVal == null || newVal.isEmpty()) return true;
                        String lower = newVal.toLowerCase();
                        return c.getNombre().toLowerCase().contains(lower)
                                || c.getApellidos().toLowerCase().contains(lower)
                                || c.getCedula().contains(newVal);
                    }));
        }
        if (searchProveedores != null && filteredProveedores != null) {
            searchProveedores.textProperty().addListener((obs, old, newVal) ->
                    filteredProveedores.setPredicate(p -> {
                        if (newVal == null || newVal.isEmpty()) return true;
                        String lower = newVal.toLowerCase();
                        return p.getNombre().toLowerCase().contains(lower);
                    }));
        }
        if (searchCompras != null && filteredCompras != null) {
            searchCompras.textProperty().addListener((obs, old, newVal) ->
                    filteredCompras.setPredicate(c -> {
                        if (newVal == null || newVal.isEmpty()) return true;
                        String lower = newVal.toLowerCase();
                        return (c.getProveedorNombre() != null && c.getProveedorNombre().toLowerCase().contains(lower))
                                || (c.getMedicamentoNombre() != null && c.getMedicamentoNombre().toLowerCase().contains(lower));
                    }));
        }
        if (searchVentas != null && filteredVentas != null) {
            searchVentas.textProperty().addListener((obs, old, newVal) ->
                    filteredVentas.setPredicate(v -> {
                        if (newVal == null || newVal.isEmpty()) return true;
                        String lower = newVal.toLowerCase();
                        return v.getNumeroFactura().toLowerCase().contains(lower)
                                || (v.getClienteNombre() != null && v.getClienteNombre().toLowerCase().contains(lower));
                    }));
        }
        if (searchUsuarios != null && filteredUsuarios != null) {
            searchUsuarios.textProperty().addListener((obs, old, newVal) ->
                    filteredUsuarios.setPredicate(u -> {
                        if (newVal == null || newVal.isEmpty()) return true;
                        String lower = newVal.toLowerCase();
                        return u.getNombre().toLowerCase().contains(lower)
                                || u.getCorreo().toLowerCase().contains(lower);
                    }));
        }
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
