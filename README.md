# HampiCare — Sistema de Gestion de Farmacia

Proyecto final de **Programacion Orientada a Objetos (Periodo 2026-A)**. Aplicacion de escritorio desarrollada en JavaFX con arquitectura MVC, persistencia en PostgreSQL (Supabase) y empaquetado como `.exe` via Launch4j.

## Desarrolladores
- **Ivory Cando**
- **Francisco Lanche**

---

## Arquitectura del Sistema

```
src/main/java/com/hampicare/
├── app/
│   ├── Main.java                 # Punto de entrada JavaFX
│   └── Launcher.java             # Launcher para shade plugin
├── controller/
│   ├── LoginController.java      # Autenticacion de usuarios
│   └── DashboardController.java  # Controlador principal (CRUD, busquedas, reportes, PDF)
├── dao/
│   ├── ICRUD.java                # Interfaz generica CRUD
│   ├── UsuarioDAO.java           # CRUD usuarios + autenticacion
│   ├── ClienteDAO.java           # CRUD clientes
│   ├── ProveedorDAO.java         # CRUD proveedores
│   ├── MedicamentoDAO.java       # CRUD medicamentos + categorias
│   ├── CompraDAO.java            # CRUD compras
│   ├── VentaDAO.java             # Registro de ventas + detalle + movimientos
│   ├── MovimientoInventarioDAO.java  # Registro de movimientos
│   ├── ReporteDAO.java           # Consultas para reportes
│   └── ConfiguracionDAO.java     # Configuracion del sistema
├── db/
│   └── Conexion.java             # Singleton PostgreSQL (Supabase)
└── model/
    ├── Usuario.java
    ├── Cliente.java
    ├── Proveedor.java
    ├── Medicamento.java
    ├── Compra.java
    ├── Venta.java
    ├── DetalleVenta.java
    ├── MovimientoInventario.java
    ├── Configuracion.java
    └── ReporteUsuario.java

src/main/resources/
├── Script.sql                    # Script PostgreSQL para Supabase
├── com/hampicare/db/
│   └── ScriptH2.sql              # Script H2 para referencia/desarrollo local
└── com/hampicare/view/
    ├── login.fxml
    └── dashboard.fxml
```

---

## Modulos del Sistema

### Roles de Usuario
| Rol | Modulos Permitidos |
|-----|-------------------|
| **ADMIN** | HOME, VENTAS, INVENTARIO, CLIENTES, PROVEEDORES, COMPRAS, USUARIOS, REPORTES — ve datos de todos los usuarios |
| **CAJERO** | HOME, VENTAS, INVENTARIO, CLIENTES — solo ve sus propios datos |
| **REPORTES** | HOME + REPORTES (6 sub-secciones) |

### HOME (Panel Principal)
- Tarjetas de resumen: total usuarios, medicamentos, ventas hoy (monto y cantidad), stock bajo
- Alertas automaticas de stock bajo y vencimiento proximo
- Actividad reciente (ultimas 3 ventas)
- Accesos rapidos a cada modulo

### INVENTARIO (Medicamentos)
- CRUD completo de medicamentos
- ComboBox editable para categorias (seleccionar existente o escribir nueva)
- Busqueda en tiempo real por nombre, categoria o lote
- Alertas de stock bajo y vencimiento

### VENTAS
- Registro de ventas con seleccion de cliente y medicamentos
- AutoComplete (ControlsFX) en ComboBox de cliente y medicamento
- Calculo automatico de subtotal y total
- Registro en `detalle_ventas` y `movimientos_inventario` (SALIDA)
- Historial de ventas con nombre del cliente visible
- Generacion de PDF con iTextPDF
- Admin ve todas las ventas; cajero solo las suyas

### CLIENTES
- CRUD completo de clientes
- Busqueda en tiempo real por nombre, cedula, correo o sector

### PROVEEDORES
- CRUD completo de proveedores
- Busqueda en tiempo real por nombre o telefono

### COMPRAS
- CRUD completo de compras a proveedores
- Registro en `movimientos_inventario` (ENTRADA)
- Busqueda en tiempo real por proveedor o medicamento

### USUARIOS (solo ADMIN)
- CRUD completo de usuarios
- Activar/desactivar usuarios
- Busqueda en tiempo real por nombre, correo o rol

### REPORTES (solo rol REPORTES)
- **Ventas por periodo**: Tabla + grafico de barras + exportar PDF
- **Top Medicamentos**: Medicamentos mas vendidos + exportar PDF
- **Inventario**: Estado actual del stock + exportar PDF
- **Compras por periodo**: Historial de compras + exportar PDF
- **Clientes**: Estadisticas de clientes + exportar PDF
- **Resumen de Caja**: Totales de ventas y compras del dia

---

## Base de Datos (PostgreSQL — Supabase)

### Tablas (11)
| Tabla | Descripcion |
|-------|------------|
| `usuarios` | Usuarios del sistema (admin, cajeros, reportes) |
| `clientes` | Clientes de la farmacia |
| `proveedores` | Proveedores de medicamentos |
| `medicamentos` | Catalogo de medicamentos |
| `ventas` | Cabecera de ventas |
| `detalle_ventas` | Detalle de productos por venta |
| `compras` | Registro de compras a proveedores |
| `movimientos_inventario` | Log de entradas y salidas de stock |
| `permisos` | Permisos del sistema (modulo + accion) |
| `usuario_permisos` | Asignacion de permisos por usuario |
| `configuracion` | Parametros del sistema (IVA, umbrales) |

### Datos Iniciales
- 3 usuarios (admin, cajero, reportes)
- 20 clientes
- 7 proveedores
- 20 medicamentos
- 15 compras con movimientos de entrada
- 15 ventas con 30 detalles y movimientos de salida
- 13 permisos del sistema

### Credenciales de Prueba
| Usuario | Contrasena | Rol |
|---------|-----------|-----|
| admin@hampicare.com | admin123 | ADMIN |
| cajero@hampicare.com | cajero123 | CAJERO |
| reportes@hampicare.com | reportes123 | REPORTES |

---

## Dependencias

| Dependencia | Version | Uso |
|------------|---------|-----|
| JavaFX Controls | 21.0.6 | Interfaz grafica |
| JavaFX FXML | 21.0.6 | Layouts FXML |
| PostgreSQL Driver | 42.7.3 | Conexion a Supabase |
| ControlsFX | 11.2.1 | AutoComplete en ComboBox |
| iTextPDF | 5.5.13.3 | Generacion de PDFs |
| Maven Shade | 3.5.1 | Empaquetado en JAR |

---

## Requisitos Previos
- Java JDK 17 o superior
- Apache Maven
- Cuenta en Supabase con PostgreSQL

---

## Ejecucion

### 1. Configurar base de datos en Supabase
1. Crear un proyecto en Supabase
2. Ir a **SQL Editor** y ejecutar el contenido de `src/main/resources/Script.sql`
3. Las credenciales se obtienen en **Settings > Database > Connection string > URI**

### 2. Ejecutar desde IDE
En IntelliJ IDEA, ir a **Run > Edit Configurations** y en **VM Options** pegar:
```
-DSUPABASE_URL="jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require" -DSUPABASE_USER="postgres.tu_usuario" -DSUPABASE_PASSWORD="tu_contrasena"
```

### 3. Generar ejecutable (.exe)
1. Compilar el JAR: `mvn clean package`
2. Abrir Launch4j y configurar el JAR de entrada
3. En **JRE > JVM Options** pegar la misma cadena de credenciales
4. Generar el `.exe`

---

## Tecnologias
- **Lenguaje**: Java 17
- **Framework UI**: JavaFX 21
- **Arquitectura**: MVC (Model-View-Controller)
- **Base de datos**: PostgreSQL (Supabase)
- **Build**: Maven
- **Empaquetado**: Maven Shade + Launch4j
