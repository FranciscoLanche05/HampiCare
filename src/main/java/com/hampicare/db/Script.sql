-- =====================================================================
-- Hampi Care - Script de Base de Datos (PostgreSQL)
-- Proyecto Final POO - JavaFX
-- Periodo academico: 2026-A

-- -------------------------------------------------------------------
-- TIPOS ENUMERADOS
-- (equivalente exacto a los ENUM de MySQL, pero en PostgreSQL son
--  tipos de primera clase reutilizables en cualquier tabla)
-- -------------------------------------------------------------------
CREATE TYPE rol_usuario      AS ENUM ('ADMIN', 'CAJERO', 'REPORTES');
CREATE TYPE tipo_movimiento  AS ENUM ('ENTRADA', 'SALIDA');


-- -------------------------------------------------------------------
-- TABLA: usuarios
-- -------------------------------------------------------------------
CREATE TABLE usuarios (
      id             SERIAL        PRIMARY KEY,
      nombre         VARCHAR(100)  NOT NULL,
      correo         VARCHAR(100)  NOT NULL UNIQUE,
      contrasena     VARCHAR(255)  NOT NULL,
      rol            rol_usuario   NOT NULL,
      activo         BOOLEAN       NOT NULL DEFAULT TRUE,
      fecha_creacion TIMESTAMP     NOT NULL DEFAULT NOW()
);


-- -------------------------------------------------------------------
-- TABLA: proveedores
-- -------------------------------------------------------------------
CREATE TABLE proveedores (
     id       SERIAL       PRIMARY KEY,
     nombre   VARCHAR(150) NOT NULL,
     contacto VARCHAR(100),
     telefono VARCHAR(20)
);


-- -------------------------------------------------------------------
-- TABLA: medicamentos   (recurso principal del sistema)
-- -------------------------------------------------------------------
CREATE TABLE medicamentos (
      id                SERIAL        PRIMARY KEY,
      nombre            VARCHAR(150)  NOT NULL,
      categoria         VARCHAR(80)   NOT NULL,
      precio            DECIMAL(10,2) NOT NULL CHECK (precio > 0),
      stock             INTEGER       NOT NULL DEFAULT 0 CHECK (stock >= 0),
      lote              VARCHAR(50),
      fecha_vencimiento DATE,
      proveedor_id      INTEGER       REFERENCES proveedores(id) ON DELETE SET NULL
);


-- -------------------------------------------------------------------
-- TABLA: ventas   (tabla de relacion: usuario <-> medicamentos)
-- -------------------------------------------------------------------
CREATE TABLE ventas (
    id             SERIAL        PRIMARY KEY,
    usuario_id     INTEGER       NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    numero_factura VARCHAR(20)   NOT NULL UNIQUE,
    fecha          TIMESTAMP     NOT NULL DEFAULT NOW(),
    total          DECIMAL(10,2) NOT NULL DEFAULT 0
);


-- -------------------------------------------------------------------
-- TABLA: detalle_ventas
-- -------------------------------------------------------------------
CREATE TABLE detalle_ventas (
    id              SERIAL        PRIMARY KEY,
    venta_id        INTEGER       NOT NULL REFERENCES ventas(id)       ON DELETE CASCADE,
    medicamento_id  INTEGER       NOT NULL REFERENCES medicamentos(id) ON DELETE RESTRICT,
    cantidad        INTEGER       NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario > 0),
    subtotal        DECIMAL(10,2) NOT NULL
);


-- -------------------------------------------------------------------
-- TABLA: movimientos_inventario   (auditoria de entradas / salidas)
-- -------------------------------------------------------------------
CREATE TABLE movimientos_inventario (
        id               SERIAL           PRIMARY KEY,
        medicamento_id   INTEGER          NOT NULL REFERENCES medicamentos(id) ON DELETE CASCADE,
        tipo             tipo_movimiento  NOT NULL,
        cantidad         INTEGER          NOT NULL CHECK (cantidad > 0),
        fecha            TIMESTAMP        NOT NULL DEFAULT NOW(),
        usuario_id       INTEGER          NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
        factura_pdf_path VARCHAR(255)
);


-- -------------------------------------------------------------------
-- TABLA: permisos   (catalogo modulo + accion)
-- -------------------------------------------------------------------
CREATE TABLE permisos (
      id          SERIAL       PRIMARY KEY,
      modulo      VARCHAR(50)  NOT NULL,
      accion      VARCHAR(50)  NOT NULL,
      descripcion VARCHAR(150),
      CONSTRAINT uq_modulo_accion UNIQUE (modulo, accion)
);


-- -------------------------------------------------------------------
-- TABLA: usuario_permisos   (overrides granulares por usuario)
-- -------------------------------------------------------------------
CREATE TABLE usuario_permisos (
      id         SERIAL  PRIMARY KEY,
      usuario_id INTEGER NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
      permiso_id INTEGER NOT NULL REFERENCES permisos(id) ON DELETE CASCADE,
      concedido  BOOLEAN NOT NULL DEFAULT TRUE,
      CONSTRAINT uq_usuario_permiso UNIQUE (usuario_id, permiso_id)
);


-- -------------------------------------------------------------------
-- TABLA: configuracion   (parametros del sistema, fila unica)
-- -------------------------------------------------------------------
CREATE TABLE configuracion (
       id                      SERIAL        PRIMARY KEY,
       nombre_empresa          VARCHAR(150)  NOT NULL DEFAULT 'Hampi Care',
       iva                     DECIMAL(5,2)  NOT NULL DEFAULT 15.00,
       umbral_stock_bajo       INTEGER       NOT NULL DEFAULT 10,
       umbral_dias_vencimiento INTEGER       NOT NULL DEFAULT 30
);


-- =====================================================================
-- DATOS DE PRUEBA
-- =====================================================================

-- Parametros iniciales del sistema
INSERT INTO configuracion (nombre_empresa, iva, umbral_stock_bajo, umbral_dias_vencimiento)
VALUES ('Hampi Care', 15.00, 10, 30);

-- Catalogo completo de permisos
INSERT INTO permisos (modulo, accion, descripcion) VALUES
       ('inventario',    'ver',      'Consultar stock de medicamentos'),
       ('inventario',    'crear',    'Registrar ingreso de mercaderia'),
       ('inventario',    'editar',   'Editar datos de medicamentos'),
       ('inventario',    'eliminar', 'Eliminar medicamentos'),
       ('ventas',        'ver',      'Consultar historial de ventas'),
       ('ventas',        'crear',    'Registrar nueva venta'),
       ('usuarios',      'ver',      'Consultar usuarios'),
       ('usuarios',      'crear',    'Crear usuarios'),
       ('usuarios',      'editar',   'Editar usuarios'),
       ('usuarios',      'eliminar', 'Eliminar usuarios'),
       ('reportes',      'ver',      'Ver reportes y estadisticas'),
       ('reportes',      'exportar', 'Exportar reportes a PDF'),
       ('configuracion', 'editar',   'Modificar parametros del sistema');

-- Usuarios de prueba (uno por cada rol de la rubrica)
-- NOTA: contrasenas en texto plano SOLO para desarrollo.
--       En la version final usar hash (ej. jBCrypt).
INSERT INTO usuarios (nombre, correo, contrasena, rol) VALUES
       ('Administrador General', 'admin@hampicare.com',    'admin123',    'ADMIN'),
       ('Carlos Cajero',         'cajero@hampicare.com',   'cajero123',   'CAJERO'),
       ('Ana Reportes',          'reportes@hampicare.com', 'reportes123', 'REPORTES');

-- Permiso extra del cajero: puede registrar ingresos de mercaderia
INSERT INTO usuario_permisos (usuario_id, permiso_id, concedido)
SELECT 2, id, TRUE FROM permisos WHERE modulo = 'inventario' AND accion = 'crear';

-- Proveedores de prueba
INSERT INTO proveedores (nombre, contacto, telefono) VALUES
         ('Distribuidora FarmaEcuador', 'Juan Perez',  '0991234567'),
         ('Laboratorios Andinos S.A.',  'Maria Lopez', '0987654321');

-- Medicamentos de prueba
-- (Ibuprofeno: stock bajo a proposito para probar alertas)
-- (Ibuprofeno: fecha proxima a vencer para probar alertas de vencimiento)
INSERT INTO medicamentos (nombre, categoria, precio, stock, lote, fecha_vencimiento, proveedor_id) VALUES
       ('Paracetamol 500mg', 'Analgesico',       1.50, 120, 'L-2026-01', '2027-06-30', 1),
       ('Ibuprofeno 400mg',  'Antiinflamatorio', 2.00,   8, 'L-2026-02', '2026-07-15', 1),
       ('Amoxicilina 500mg', 'Antibiotico',      3.25,  45, 'L-2026-03', '2026-12-01', 2),
       ('Loratadina 10mg',   'Antialergico',     1.75,  60, 'L-2026-04', '2027-03-20', 2);

-- Movimientos de entrada iniciales
INSERT INTO movimientos_inventario (medicamento_id, tipo, cantidad, usuario_id) VALUES
        (1, 'ENTRADA', 120, 1),
        (2, 'ENTRADA',  20, 1);

-- Venta de prueba realizada por el cajero
INSERT INTO ventas (usuario_id, numero_factura, total) VALUES
    (2, 'FAC-0001', 5.00);

INSERT INTO detalle_ventas (venta_id, medicamento_id, cantidad, precio_unitario, subtotal) VALUES
       (1, 1, 2, 1.50, 3.00),
       (1, 2, 1, 2.00, 2.00);

-- Movimientos de salida asociados a la venta anterior
INSERT INTO movimientos_inventario (medicamento_id, tipo, cantidad, usuario_id) VALUES
        (1, 'SALIDA', 2, 2),
        (2, 'SALIDA', 1, 2);