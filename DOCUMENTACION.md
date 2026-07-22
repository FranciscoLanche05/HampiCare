# HampiCare — Documentacion del Sistema

## 1. Informacion General

**Nombre del Sistema:** HampiCare — Sistema de Gestion de Farmacia
**Proyecto Academico:** Programacion Orientada a Objetos (Periodo 2026-A)
**Institucion:** Escuela Politecnica Nacional
**Desarrolladores:**
- Ivory Cando
- Francisco Lanche

**Tecnologias Utilizadas:**
- Lenguaje: Java 17
- Framework de interfaz grafica: JavaFX 21
- Arquitectura: MVC (Model-View-Controller)
- Base de datos: PostgreSQL alojada en Supabase
- Sistema de build: Apache Maven
- Empaquetado: Maven Shade Plugin + Launch4j
- Libreria de autocompletado: ControlsFX 11.2.1
- Generacion de PDF: iTextPDF 5.5.13.3

---

## 2. Descripcion General

HampiCare es una aplicacion de escritorio disenada para la gestion integral de una farmacia. Permite administrar medicamentos, clientes, proveedores, ventas, compras, inventario, usuarios y generar reportes estadisticos. El sistema esta orientado a ser utilizado por personal de una farmacia con diferentes niveles de acceso segun el rol del usuario.

---

## 3. Arquitectura del Sistema

### 3.1 Patron de Diseno MVC

El sistema sigue el patron Model-View-Controller:

- **Model (Modelo):** Clases que representan las entidades del negocio (Usuario, Cliente, Medicamento, Venta, etc.). Cada modelo encapsula los atributos y proporciona getters y setters.
- **View (Vista):** Archivos FXML que definen la interfaz grafica de cada pantalla (login, dashboard con todos los modulos).
- **Controller (Controlador):** Clases Java que conectan las vistas con los modelos y la logica de negocio. Manejan eventos de usuario, validaciones y operaciones CRUD.

### 3.2 Patron Singleton

La conexion a la base de datos se gestiona mediante el patron Singleton en la clase `Conexion.java`, garantizando una unica instancia de conexion compartida en toda la aplicacion.

### 3.3 Capa de Acceso a Datos (DAO)

Cada entidad tiene su clase DAO (Data Access Object) que encapsula todas las operaciones SQL contra la base de datos. Esto desacopla la logica de negocio del acceso directo a BD.

---

## 4. Modulos del Sistema

### 4.1 Modulo de Inicio de Sesion (Login)

**Pantalla:** `login.fxml` + `LoginController.java`

**Funcionalidades:**
- Formulario con campos de correo electronico y contrasena
- Autenticacion contra la tabla `usuarios` en la base de datos
- Validacion de campos vacios con mensajes de error
- Redireccion al dashboard segun el rol del usuario (ADMIN, CAJERO, REPORTES)
- Mensajes de error visuales en caso de credenciales incorrectas

**Credenciales de prueba:**
- admin@hampicare.com / admin123 (Rol: ADMIN)
- cajero@hampicare.com / cajero123 (Rol: CAJERO)
- reportes@hampicare.com / reportes123 (Rol: REPORTES)

### 4.2 Panel Principal (HOME)

**Pantalla:** Seccion HOME dentro de `dashboard.fxml` + `DashboardController.java`

**Funcionalidades:**
- Tarjetas de resumen con estadisticas en tiempo real:
  - Total de usuarios registrados
  - Total de medicamentos en inventario
  - Ventas del dia (monto total y cantidad)
  - Medicamentos con stock bajo
- Alertas automaticas de:
  - Medicamentos con stock por debajo del umbral configurado
  - Medicamentos proximos a vencer (dias configurables)
- Seccion de actividad reciente (ultimas 3 ventas realizadas)
- Accesos rapidos a cada modulo del sistema

### 4.3 Modulo de Inventario (Medicamentos)

**Pantalla:** Seccion INVENTARIO dentro de `dashboard.fxml` + `DashboardController.java`

**Funcionalidades:**
- CRUD completo de medicamentos:
  - Crear: formulario con nombre, categoria, precio, stock, lote, fecha de vencimiento y proveedor
  - Leer: tabla con todos los medicamentos registrados
  - Actualizar: edicion de registros existentes
  - Eliminar: borrado con confirmacion
- ComboBox editable para categorias: permite seleccionar una categoria existente o escribir una nueva
- Busqueda en tiempo real por nombre, categoria o numero de lote
- Alertas de stock bajo y vencimiento proximo
- Validacion: no se permiten nombres duplicados de medicamentos

**Campos del medicamento:**
- id (auto-generado)
- nombre (texto, obligatorio)
- categoria (texto, obligatorio)
- precio (decimal, mayor a 0)
- stock (entero, mayor o igual a 0)
- lote (texto)
- fecha_vencimiento (fecha)
- proveedor_id (referencia a proveedores)

### 4.4 Modulo de Clientes

**Pantalla:** Seccion CLIENTES dentro de `dashboard.fxml` + `DashboardController.java`

**Funcionalidades:**
- CRUD completo de clientes:
  - Crear: formulario con nombres, apellidos, correo, cedula, telefono y sector
  - Leer: tabla con todos los clientes
  - Actualizar: edicion de registros existentes
  - Eliminar: borrado con confirmacion
- Busqueda en tiempo real por nombre, cedula, correo o sector
- La cedula es unica para cada cliente (validacion de duplicados)

**Campos del cliente:**
- id (auto-generado)
- nombres (texto, obligatorio)
- apellidos (texto, obligatorio)
- correo (texto)
- cedula (texto, unico)
- telefono (texto)
- sector (texto)

### 4.5 Modulo de Proveedores

**Pantalla:** Seccion PROVEEDORES dentro de `dashboard.fxml` + `DashboardController.java`

**Funcionalidades:**
- CRUD completo de proveedores:
  - Crear: formulario con nombre, correo y telefono
  - Leer: tabla con todos los proveedores
  - Actualizar: edicion de registros existentes
  - Eliminar: borrado con confirmacion
- Busqueda en tiempo real por nombre o telefono

**Campos del proveedor:**
- id (auto-generado)
- nombre (texto, obligatorio)
- correo (texto)
- telefono (texto)

### 4.6 Modulo de Compras

**Pantalla:** Seccion COMPRAS dentro de `dashboard.fxml` + `DashboardController.java`

**Funcionalidades:**
- CRUD completo de compras a proveedores:
  - Crear: seleccion de proveedor, medicamento, cantidad y precio de compra
  - Leer: tabla con historial de compras (muestra nombre del proveedor y medicamento)
  - Actualizar: edicion de registros existentes
  - Eliminar: borrado con confirmacion
- Al registrar una compra, se crea automaticamente un registro en `movimientos_inventario` con tipo ENTRADA
- Busqueda en tiempo real por proveedor o medicamento

**Campos de la compra:**
- id (auto-generado)
- proveedor_id (referencia a proveedores)
- medicamento_id (referencia a medicamentos)
- cantidad (entero, mayor a 0)
- precio_compra (decimal, mayor a 0)
- fecha (timestamp, automatico)

### 4.7 Modulo de Ventas

**Pantalla:** Seccion VENTAS dentro de `dashboard.fxml` + `DashboardController.java`

**Funcionalidades:**
- Registro de ventas:
  - Seleccion de cliente (ComboBox con autocompletado via ControlsFX)
  - Seleccion de medicamentos (ComboBox con autocompletado)
  - Cantidad de unidades por medicamento
  - Calculo automatico de subtotal por linea y total de la venta
  - Numero de factura auto-generado (formato FAC-XXXX)
- Al registrar una venta se ejecutan tres operaciones en transaccion:
  1. Insertar registro en `ventas` con el total
  2. Insertar detalle en `detalle_ventas` por cada producto vendido
  3. Insertar movimiento en `movimientos_inventario` con tipo SALIDA
  4. Actualizar el stock del medicamento (restar cantidad vendida)
- Historial de ventas:
  - Tabla con numero de factura, fecha, cliente, total y vendedor
  - El admin ve todas las ventas; el cajero solo las suyas
- Generacion de PDF de la venta con iTextPDF

**Campos de la venta:**
- id (auto-generado)
- usuario_id (referencia al cajero que registro la venta)
- cliente_id (referencia al cliente)
- numero_factura (texto, unico)
- total (decimal)
- fecha (timestamp, automatico)

**Campos del detalle de venta:**
- venta_id (referencia a ventas)
- medicamento_id (referencia a medicamentos)
- cantidad (entero, mayor a 0)
- precio_unitario (decimal, mayor a 0)
- subtotal (decimal)

### 4.8 Modulo de Usuarios (Solo ADMIN)

**Pantalla:** Seccion USUARIOS dentro de `dashboard.fxml` + `DashboardController.java`

**Funcionalidades:**
- CRUD completo de usuarios:
  - Crear: formulario con nombre, correo, contrasena, rol y estado activo/inactivo
  - Leer: tabla con todos los usuarios
  - Actualizar: edicion de registros existentes
  - Eliminar: borrado con confirmacion
- Activar o desactivar usuarios sin eliminarlos
- Busqueda en tiempo real por nombre, correo o rol
- Solo el usuario con rol ADMIN puede acceder a este modulo

**Campos del usuario:**
- id (auto-generado)
- nombre (texto, obligatorio)
- correo (texto, unico, obligatorio)
- contrasena (texto, obligatorio)
- rol (ADMIN, CAJERO o REPORTES)
- activo (booleano, verdadero por defecto)
- fecha_creacion (timestamp, automatico)

### 4.9 Modulo de Reportes (Solo rol REPORTES)

**Pantalla:** Seccion REPORTES dentro de `dashboard.fxml` + `DashboardController.java`

**Sub-modulos:**

#### 4.9.1 Ventas por Periodo
- Filtros: fecha de inicio y fecha de fin (DatePicker)
- Tabla con detalle de ventas en el periodo seleccionado
- Grafico de barras (JavaFX BarChart) mostrando ventas por dia
- Boton para exportar a PDF

#### 4.9.2 Top Medicamentos
- Tabla con los medicamentos mas vendidos ordenados por cantidad
- Muestra nombre, categoria, cantidad vendida e ingresos generados
- Boton para exportar a PDF

#### 4.9.3 Inventario Actual
- Tabla con todos los medicamentos y su estado actual
- Muestra stock, precio, lote, fecha de vencimiento y proveedor
- Boton para exportar a PDF

#### 4.9.4 Compras por Periodo
- Filtros: fecha de inicio y fecha de fin (DatePicker)
- Tabla con historial de compras en el periodo seleccionado
- Muestra proveedor, medicamento, cantidad, precio y fecha
- Boton para exportar a PDF

#### 4.9.5 Clientes
- Tabla con estadisticas de clientes
- Muestra nombre, cedula, correo, telefono, sector y total de compras
- Boton para exportar a PDF

#### 4.9.6 Resumen de Caja
- Tarjetas de resumen con totales del dia
- Muestra total de ventas, total de compras y balance
- Boton para exportar a PDF

---

## 5. Base de Datos

### 5.1 Motor de Base de Datos
- PostgreSQL alojado en la plataforma Supabase
- Conexion mediante JDBC con driver PostgreSQL 42.7.3
- Credenciales inyectadas via propiedades del sistema (JVM options) para mayor seguridad

### 5.2 Diagrama de Tablas

| # | Tabla | Descripcion | Registros Iniciales |
|---|-------|------------|-------------------|
| 1 | usuarios | Usuarios del sistema con sus roles | 3 |
| 2 | clientes | Clientes de la farmacia | 20 |
| 3 | proveedores | Proveedores de medicamentos | 7 |
| 4 | medicamentos | Catalogo de medicamentos | 20 |
| 5 | ventas | Cabecera de ventas realizadas | 15 |
| 6 | detalle_ventas | Productos vendidos por cada venta | 30 |
| 7 | compras | Compras a proveedores | 15 |
| 8 | movimientos_inventario | Log de entradas y salidas de stock | 45 |
| 9 | permisos | Permisos del sistema | 13 |
| 10 | usuario_permisos | Asignacion de permisos por usuario | 1 |
| 11 | configuracion | Parametros del sistema (IVA, umbrales) | 1 |

### 5.3 Relaciones entre Tablas

- `ventas.usuario_id` referencia `usuarios.id`
- `ventas.cliente_id` referencia `clientes.id`
- `detalle_ventas.venta_id` referencia `ventas.id`
- `detalle_ventas.medicamento_id` referencia `medicamentos.id`
- `medicamentos.proveedor_id` referencia `proveedores.id`
- `compras.proveedor_id` referencia `proveedores.id`
- `compras.medicamento_id` referencia `medicamentos.id`
- `movimientos_inventario.medicamento_id` referencia `medicamentos.id`
- `movimientos_inventario.usuario_id` referencia `usuarios.id`
- `usuario_permisos.usuario_id` referencia `usuarios.id`
- `usuario_permisos.permiso_id` referencia `permisos.id`

### 5.4 Politicas de Eliminacion (ON DELETE)

- `CASCADE`: al eliminar un registro padre, se eliminan sus hijos (ventas -> detalle_ventas, usuarios -> usuario_permisos)
- `SET NULL`: al eliminar un registro padre, el campo hijo se pone en NULL (clientes en ventas, proveedores en medicamentos)
- `RESTRICT`: no permite eliminar un registro padre si tiene hijos dependientes (usuarios en ventas, medicamentos en detalle_ventas)

---

## 6. Seguridad y Autenticacion

### 6.1 Autenticacion
- El usuario ingresa correo y contrasena
- El sistema valida las credenciales contra la tabla `usuarios`
- Si son correctas, se crea un objeto `Usuario` con su rol y se redirige al dashboard
- Si son incorrectas, se muestra un mensaje de error

### 6.2 Control de Acceso por Rol
- **ADMIN:** Acceso total a todos los modulos y ve datos de todos los usuarios
- **CAJERO:** Acceso limitado a HOME, VENTAS, INVENTARIO y CLIENTES. Solo ve sus propios datos
- **REPORTES:** Acceso a HOME y REPORTES (6 sub-secciones)

### 6.3 Proteccion de Credenciales
- Las credenciales de la base de datos NO se almacenan en archivos de texto plano
- Se inyectan en tiempo de ejecucion via propiedades del sistema JVM (-DSUPABASE_URL, -DSUPABASE_USER, -DSUPABASE_PASSWORD)
- Al generar el ejecutable (.exe) con Launch4j, las credenciales se configuran en la pestaña JRE > JVM Options

---

## 7. Dependencias del Proyecto

| Dependencia | Grupo | Version | Proposito |
|------------|-------|---------|-----------|
| javafx-controls | org.openjfx | 21.0.6 | Componentes de interfaz grafica |
| javafx-fxml | org.openjfx | 21.0.6 | Carga y manejo de archivos FXML |
| postgresql | org.postgresql | 42.7.3 | Driver JDBC para PostgreSQL |
| controlsfx | org.controlsfx | 11.2.1 | Componentes avanzados (AutoComplete) |
| itextpdf | com.itextpdf | 5.5.13.3 | Generacion de documentos PDF |
| maven-shade-plugin | org.apache.maven.plugins | 3.5.1 | Empaquetado en JAR executable |

---

## 8. Instrucciones de Instalacion y Ejecucion

### 8.1 Requisitos Previos
- Java Development Kit (JDK) 17 o superior
- Apache Maven
- Cuenta en Supabase (https://supabase.com)

### 8.2 Configuracion de la Base de Datos
1. Crear un proyecto en Supabase
2. Ir al SQL Editor del panel de Supabase
3. Copiar y pegar el contenido del archivo `src/main/resources/Script.sql`
4. Ejecutar el script para crear las 11 tablas y los datos iniciales
5. Obtener las credenciales de conexion en Settings > Database > Connection string > URI

### 8.3 Ejecucion desde IDE
1. Clonar el repositorio
2. Abrir el proyecto en IntelliJ IDEA
3. Configurar las VM Options con las credenciales de Supabase:
   -DSUPABASE_URL="jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require" -DSUPABASE_USER="postgres.tu_usuario" -DSUPABASE_PASSWORD="tu_contrasena"
4. Ejecutar la clase `Main.java`

### 8.4 Generacion de Ejecutable (.exe)
1. Compilar el JAR: `mvn clean package`
2. Abrir Launch4j
3. Configurar el JAR de entrada (target/hampicare-1.0-SNAPSHOT.jar)
4. Configurar el archivo de salida (.exe)
5. En la pestana JRE, pegar las mismas JVM Options con las credenciales
6. Generar el ejecutable

---

## 9. Estructura de Archivos del Proyecto

```
HampiCare/
├── pom.xml
├── README.md
├── .gitignore
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── hampicare/
│       │           ├── app/
│       │           │   ├── Main.java
│       │           │   └── Launcher.java
│       │           ├── controller/
│       │           │   ├── LoginController.java
│       │           │   └── DashboardController.java
│       │           ├── dao/
│       │           │   ├── ICRUD.java
│       │           │   ├── UsuarioDAO.java
│       │           │   ├── ClienteDAO.java
│       │           │   ├── ProveedorDAO.java
│       │           │   ├── MedicamentoDAO.java
│       │           │   ├── CompraDAO.java
│       │           │   ├── VentaDAO.java
│       │           │   ├── MovimientoInventarioDAO.java
│       │           │   ├── ReporteDAO.java
│       │           │   └── ConfiguracionDAO.java
│       │           ├── db/
│       │           │   └── Conexion.java
│       │           └── model/
│       │               ├── Usuario.java
│       │               ├── Cliente.java
│       │               ├── Proveedor.java
│       │               ├── Medicamento.java
│       │               ├── Compra.java
│       │               ├── Venta.java
│       │               ├── DetalleVenta.java
│       │               ├── MovimientoInventario.java
│       │               ├── Configuracion.java
│       │               └── ReporteUsuario.java
│       └── resources/
│           ├── Script.sql
│           └── com/
│               └── hampicare/
│                   ├── db/
│                   │   └── ScriptH2.sql
│                   ├── images/
│                   │   └── logo_hampicare.ico
│                   └── view/
│                       ├── login.fxml
│                       └── dashboard.fxml
```

---

## 10. Tecnologias Aplicadas (Pilares POO)

### Abstraccion
- Clases modelo que representan entidades del mundo real (Usuario, Medicamento, Venta, etc.)
- Interfaces como ICRUD que definen contratos genericos para operaciones de persistencia
- Metodos con nombres descriptivos que ocultan la complejidad de las consultas SQL

### Encapsulamiento
- Atributos privados en todas las clases modelo con getters y setters publicos
- Conexion a base de datos gestionada internamente por la clase Conexion (patron Singleton)
- Consultas SQL encapsuladas en clases DAO, separadas de la logica de presentacion

### Herencia
- La clase Main extiende Application de JavaFX para iniciar la aplicacion
- Las clases modelo extienden propiedades JavaFX (StringProperty, IntegerProperty, etc.) para enlace directo con la interfaz

### Polimorfismo
- Uso de interfaces genericas (ICRUD<T>) implementadas por multiples DAO
- Sobrecarga de metodos en VentaDAO (listarTodas, listarPorUsuario, resumenHoy, resumenHoyTodas)
- Metodos mapear() en cada DAO que transforman ResultSet a objetos del modelo

---

## 11. Repositorio

- **Plataforma:** GitHub
- **URL:** https://github.com/FranciscoLanche05/HampiCare.git
- **Ramas:**
  - `main`: rama principal estable
  - `developFranciscoLanche`: rama de desarrollo de Francisco
  - `developIvoryCando`: rama de desarrollo de Ivory
