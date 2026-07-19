-- =====================================================================
-- Hampi Care - Script de Base de Datos (H2 - Desarrollo Local)
-- Proyecto Final POO - JavaFX
-- Periodo academico: 2026-A
-- =====================================================================

-- H2 soporta ENUM directamente en la columna

-- -------------------------------------------------------------------
-- TABLA: usuarios
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id             SERIAL        PRIMARY KEY,
    nombre         VARCHAR(100)  NOT NULL,
    correo         VARCHAR(100)  NOT NULL UNIQUE,
    contrasena     VARCHAR(255)  NOT NULL,
    rol            ENUM('ADMIN', 'CAJERO', 'REPORTES') NOT NULL,
    activo         BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- -------------------------------------------------------------------
-- TABLA: clientes
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS clientes (
    id             SERIAL        PRIMARY KEY,
    nombres        VARCHAR(100)  NOT NULL,
    apellidos      VARCHAR(100)  NOT NULL,
    correo         VARCHAR(100)  NOT NULL,
    cedula         VARCHAR(20)   NOT NULL,
    telefono       VARCHAR(20),
    sector         VARCHAR(100)
);

-- -------------------------------------------------------------------
-- TABLA: proveedores
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS proveedores (
    id       SERIAL       PRIMARY KEY,
    nombre   VARCHAR(150) NOT NULL,
    correo   VARCHAR(100),
    telefono VARCHAR(20)
);

-- -------------------------------------------------------------------
-- TABLA: medicamentos
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS medicamentos (
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
-- TABLA: compras
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS compras (
    id               SERIAL        PRIMARY KEY,
    proveedor_id     INTEGER       NOT NULL REFERENCES proveedores(id) ON DELETE RESTRICT,
    medicamento_id   INTEGER       NOT NULL REFERENCES medicamentos(id) ON DELETE RESTRICT,
    cantidad         INTEGER       NOT NULL CHECK (cantidad > 0),
    precio_compra    DECIMAL(10,2) NOT NULL CHECK (precio_compra > 0),
    fecha            TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- -------------------------------------------------------------------
-- TABLA: ventas
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ventas (
    id             SERIAL        PRIMARY KEY,
    usuario_id     INTEGER       NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    cliente_id     INTEGER       REFERENCES clientes(id) ON DELETE SET NULL,
    numero_factura VARCHAR(20)   NOT NULL UNIQUE,
    fecha          TIMESTAMP     NOT NULL DEFAULT NOW(),
    total          DECIMAL(10,2) NOT NULL DEFAULT 0
);

-- -------------------------------------------------------------------
-- TABLA: detalle_ventas
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS detalle_ventas (
    id              SERIAL        PRIMARY KEY,
    venta_id        INTEGER       NOT NULL REFERENCES ventas(id)       ON DELETE CASCADE,
    medicamento_id  INTEGER       NOT NULL REFERENCES medicamentos(id) ON DELETE RESTRICT,
    cantidad        INTEGER       NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario > 0),
    subtotal        DECIMAL(10,2) NOT NULL
);

-- -------------------------------------------------------------------
-- TABLA: movimientos_inventario
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS movimientos_inventario (
    id               SERIAL           PRIMARY KEY,
    medicamento_id   INTEGER          NOT NULL REFERENCES medicamentos(id) ON DELETE CASCADE,
    tipo             ENUM('ENTRADA', 'SALIDA') NOT NULL,
    cantidad         INTEGER          NOT NULL CHECK (cantidad > 0),
    fecha            TIMESTAMP        NOT NULL DEFAULT NOW(),
    usuario_id       INTEGER          NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    factura_pdf_path VARCHAR(255)
);

-- -------------------------------------------------------------------
-- TABLA: permisos
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS permisos (
    id          SERIAL       PRIMARY KEY,
    modulo      VARCHAR(50)  NOT NULL,
    accion      VARCHAR(50)  NOT NULL,
    descripcion VARCHAR(150),
    CONSTRAINT uq_modulo_accion UNIQUE (modulo, accion)
);

-- -------------------------------------------------------------------
-- TABLA: usuario_permisos
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario_permisos (
    id         SERIAL  PRIMARY KEY,
    usuario_id INTEGER NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    permiso_id INTEGER NOT NULL REFERENCES permisos(id) ON DELETE CASCADE,
    concedido  BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_usuario_permiso UNIQUE (usuario_id, permiso_id)
);

-- -------------------------------------------------------------------
-- TABLA: configuracion
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS configuracion (
    id                      SERIAL        PRIMARY KEY,
    nombre_empresa          VARCHAR(150)  NOT NULL DEFAULT 'Hampi Care',
    iva                     DECIMAL(5,2)  NOT NULL DEFAULT 15.00,
    umbral_stock_bajo       INTEGER       NOT NULL DEFAULT 10,
    umbral_dias_vencimiento INTEGER       NOT NULL DEFAULT 30
);


-- =====================================================================
-- =====================================================================
-- DATOS DE PRUEBA (solo se insertan si las tablas estan vacias)
-- =====================================================================

-- Verificar si ya existen datos; si es asi, omitir insercion
-- H2: SELECT ... WHERE条件不成立时不返回行 -> 不执行
MERGE INTO configuracion (id, nombre_empresa, iva, umbral_stock_bajo, umbral_dias_vencimiento)
KEY (id)
VALUES (1, 'HampiCare', 15.00, 10, 30);

INSERT INTO permisos (modulo, accion, descripcion)
SELECT 'inventario', 'ver', 'Consultar stock de medicamentos' WHERE NOT EXISTS (SELECT 1 FROM permisos WHERE modulo='inventario' AND accion='ver');
INSERT INTO permisos (modulo, accion, descripcion)
SELECT 'inventario', 'crear', 'Registrar ingreso de mercaderia' WHERE NOT EXISTS (SELECT 1 FROM permisos WHERE modulo='inventario' AND accion='crear');
INSERT INTO permisos (modulo, accion, descripcion)
SELECT 'inventario', 'editar', 'Editar datos de medicamentos' WHERE NOT EXISTS (SELECT 1 FROM permisos WHERE modulo='inventario' AND accion='editar');
INSERT INTO permisos (modulo, accion, descripcion)
SELECT 'inventario', 'eliminar', 'Eliminar medicamentos' WHERE NOT EXISTS (SELECT 1 FROM permisos WHERE modulo='inventario' AND accion='eliminar');
INSERT INTO permisos (modulo, accion, descripcion)
SELECT 'ventas', 'ver', 'Consultar historial de ventas' WHERE NOT EXISTS (SELECT 1 FROM permisos WHERE modulo='ventas' AND accion='ver');
INSERT INTO permisos (modulo, accion, descripcion)
SELECT 'ventas', 'crear', 'Registrar nueva venta' WHERE NOT EXISTS (SELECT 1 FROM permisos WHERE modulo='ventas' AND accion='crear');
INSERT INTO permisos (modulo, accion, descripcion)
SELECT 'usuarios', 'ver', 'Consultar usuarios' WHERE NOT EXISTS (SELECT 1 FROM permisos WHERE modulo='usuarios' AND accion='ver');
INSERT INTO permisos (modulo, accion, descripcion)
SELECT 'usuarios', 'crear', 'Crear usuarios' WHERE NOT EXISTS (SELECT 1 FROM permisos WHERE modulo='usuarios' AND accion='crear');
INSERT INTO permisos (modulo, accion, descripcion)
SELECT 'usuarios', 'editar', 'Editar usuarios' WHERE NOT EXISTS (SELECT 1 FROM permisos WHERE modulo='usuarios' AND accion='editar');
INSERT INTO permisos (modulo, accion, descripcion)
SELECT 'usuarios', 'eliminar', 'Eliminar usuarios' WHERE NOT EXISTS (SELECT 1 FROM permisos WHERE modulo='usuarios' AND accion='eliminar');
INSERT INTO permisos (modulo, accion, descripcion)
SELECT 'reportes', 'ver', 'Ver reportes y estadisticas' WHERE NOT EXISTS (SELECT 1 FROM permisos WHERE modulo='reportes' AND accion='ver');
INSERT INTO permisos (modulo, accion, descripcion)
SELECT 'reportes', 'exportar', 'Exportar reportes a PDF' WHERE NOT EXISTS (SELECT 1 FROM permisos WHERE modulo='reportes' AND accion='exportar');
INSERT INTO permisos (modulo, accion, descripcion)
SELECT 'configuracion', 'editar', 'Modificar parametros del sistema' WHERE NOT EXISTS (SELECT 1 FROM permisos WHERE modulo='configuracion' AND accion='editar');

INSERT INTO usuarios (nombre, correo, contrasena, rol)
SELECT 'Administrador General', 'admin@hampicare.com', 'admin123', 'ADMIN' WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE correo='admin@hampicare.com');
INSERT INTO usuarios (nombre, correo, contrasena, rol)
SELECT 'Carlos Cajero', 'cajero@hampicare.com', 'cajero123', 'CAJERO' WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE correo='cajero@hampicare.com');
INSERT INTO usuarios (nombre, correo, contrasena, rol)
SELECT 'Ana Reportes', 'reportes@hampicare.com', 'reportes123', 'REPORTES' WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE correo='reportes@hampicare.com');

INSERT INTO usuario_permisos (usuario_id, permiso_id, concedido)
SELECT u.id, p.id, TRUE
FROM usuarios u, permisos p
WHERE u.correo = 'cajero@hampicare.com' AND p.modulo = 'inventario' AND p.accion = 'crear'
AND NOT EXISTS (SELECT 1 FROM usuario_permisos up WHERE up.usuario_id = u.id AND up.permiso_id = p.id);

-- 20 Clientes
INSERT INTO clientes (nombres, apellidos, correo, cedula, telefono, sector)
SELECT * FROM (VALUES
    ('Consumidor', 'Final', 'consumidor@hampicare.com', '9999999999', '0999999999', 'N/A'),
    ('Ana', 'Garcia', 'ana.garcia0@ejemplo.com', '1746913810', '0995108603', 'Valle de los Chillos'),
    ('Luis', 'Hernandez', 'luis.hernandez1@ejemplo.com', '1783197857', '0992458591', 'El Condado'),
    ('Carmen', 'Rodriguez', 'carmen.rodriguez2@ejemplo.com', '1713999315', '0992571945', 'Valle de los Chillos'),
    ('Sofia', 'Morales', 'sofia.morales3@ejemplo.com', '1790801586', '0991445199', 'La Mariscal'),
    ('Pedro', 'Ortiz', 'pedro.ortiz4@ejemplo.com', '1766306997', '0994698379', 'La Carolina'),
    ('Roberto', 'Ramirez', 'roberto.ramirez5@ejemplo.com', '1710872248', '0993678638', 'Calderon'),
    ('Carmen', 'Flores', 'carmen.flores6@ejemplo.com', '1747295260', '0993608513', 'Valle de los Chillos'),
    ('Miguel', 'Hernandez', 'miguel.hernandez7@ejemplo.com', '1722448136', '0997374122', 'Sur'),
    ('Paula', 'Rivera', 'paula.rivera8@ejemplo.com', '1791030736', '0995437923', 'Conocoto'),
    ('Maria', 'Cruz', 'maria.cruz9@ejemplo.com', '1781971316', '0993094235', 'San Rafael'),
    ('Diego', 'Martinez', 'diego.martinez10@ejemplo.com', '1784093639', '0995918715', 'Sangolqui'),
    ('Laura', 'Rivera', 'laura.rivera11@ejemplo.com', '1787490893', '0994226067', 'Calderon'),
    ('Carlos', 'Rodriguez', 'carlos.rodriguez12@ejemplo.com', '1798753260', '0994823498', 'Conocoto'),
    ('Lucia', 'Martinez', 'lucia.martinez13@ejemplo.com', '1741244663', '0992694522', 'Quitumbe'),
    ('Jorge', 'Cruz', 'jorge.cruz14@ejemplo.com', '1795320121', '0997120868', 'Centro'),
    ('Paula', 'Rivera', 'paula.rivera15@ejemplo.com', '1738119557', '0995479144', 'Calderon'),
    ('Carlos', 'Chavez', 'carlos.chavez16@ejemplo.com', '1795225343', '0993871230', 'La Mariscal'),
    ('Sofia', 'Gonzalez', 'sofia.gonzalez17@ejemplo.com', '1772043515', '0997366205', 'Cumbaya'),
    ('Marta', 'Sanchez', 'marta.sanchez18@ejemplo.com', '1753524491', '0991938483', 'Valle de los Chillos')
) WHERE NOT EXISTS (SELECT 1 FROM clientes LIMIT 1);

-- 7 Proveedores
INSERT INTO proveedores (nombre, correo, telefono)
SELECT * FROM (VALUES
    ('Distribuidora FarmaEcuador', 'ventas@farmaecuador.com', '0991234567'),
    ('Laboratorios Andinos S.A.', 'contacto@andinos.com', '0987654321'),
    ('Medcorp Global', 'pedidos@medcorp.com', '0999111222'),
    ('Farmaceutica del Pacifico', 'info@farma-pacifico.com', '0988333444'),
    ('Suministros Medicos Quito', 'ventas@suministrosquito.com', '0999555666'),
    ('Grupofarma S.A.', 'contacto@grupofarma.ec', '022554433'),
    ('Insumedical', 'logistica@insumedical.com', '0998777666')
) WHERE NOT EXISTS (SELECT 1 FROM proveedores LIMIT 1);

-- 20 Medicamentos
INSERT INTO medicamentos (nombre, categoria, precio, stock, lote, fecha_vencimiento, proveedor_id)
SELECT * FROM (VALUES
    ('Paracetamol 500mg', 'Analgesico', 1.5, 28, 'L-2026-01', '2028-04-17', 1),
    ('Ibuprofeno 400mg', 'Antiinflamatorio', 2.0, 100, 'L-2026-02', '2028-04-04', 1),
    ('Amoxicilina 500mg', 'Antibiotico', 3.25, 88, 'L-2026-03', '2027-05-26', 2),
    ('Loratadina 10mg', 'Antialergico', 1.75, 74, 'L-2026-04', '2026-09-08', 2),
    ('Omeprazol 20mg', 'Gastrointestinal', 2.5, 165, 'L-2026-05', '2028-06-27', 3),
    ('Aspirina 100mg', 'Analgesico', 1.2, 100, 'L-2026-06', '2028-05-29', 3),
    ('Diclofenaco 50mg', 'Antiinflamatorio', 2.8, 187, 'L-2026-07', '2026-12-31', 4),
    ('Azitromicina 500mg', 'Antibiotico', 5.0, 121, 'L-2026-08', '2027-08-10', 4),
    ('Cetirizina 10mg', 'Antialergico', 1.9, 184, 'L-2026-09', '2028-06-05', 5),
    ('Pantoprazol 40mg', 'Gastrointestinal', 3.1, 56, 'L-2026-10', '2027-07-10', 5),
    ('Losartan 50mg', 'Antihipertensivo', 4.5, 55, 'L-2026-11', '2027-02-09', 6),
    ('Enalapril 20mg', 'Antihipertensivo', 3.8, 163, 'L-2026-12', '2027-01-26', 6),
    ('Metformina 850mg', 'Antidiabetico', 2.2, 87, 'L-2026-13', '2027-09-10', 7),
    ('Glibenclamida 5mg', 'Antidiabetico', 1.8, 169, 'L-2026-14', '2028-02-19', 7),
    ('Simvastatina 20mg', 'Hipolipemiante', 3.5, 169, 'L-2026-15', '2027-06-16', 1),
    ('Atorvastatina 40mg', 'Hipolipemiante', 4.2, 112, 'L-2026-16', '2027-05-25', 2),
    ('Vitamina C 1g', 'Suplemento', 1.5, 55, 'L-2026-17', '2027-01-05', 3),
    ('Complejo B', 'Suplemento', 2.5, 146, 'L-2026-18', '2027-08-18', 4),
    ('Salbutamol Inhalador', 'Broncodilatador', 6.5, 32, 'L-2026-19', '2026-09-27', 5),
    ('Lansoprazol 30mg', 'Gastrointestinal', 3.0, 48, 'L-2026-20', '2028-05-17', 6)
) WHERE NOT EXISTS (SELECT 1 FROM medicamentos LIMIT 1);

-- 15 Compras y sus respectivos movimientos de inventario de ENTRADA
INSERT INTO compras (proveedor_id, medicamento_id, cantidad, precio_compra)
SELECT * FROM (VALUES
    (2, 6, 97, 0.72),
    (4, 20, 18, 1.8),
    (4, 13, 86, 1.32),
    (4, 17, 42, 0.9),
    (5, 1, 97, 0.9),
    (6, 4, 97, 1.05),
    (5, 9, 92, 1.14),
    (3, 4, 47, 1.05),
    (4, 6, 68, 0.72),
    (1, 9, 74, 1.14),
    (7, 6, 74, 0.72),
    (1, 10, 91, 1.86),
    (5, 20, 35, 1.8),
    (2, 12, 30, 2.28),
    (5, 17, 10, 0.9)
) WHERE NOT EXISTS (SELECT 1 FROM compras LIMIT 1);

INSERT INTO movimientos_inventario (medicamento_id, tipo, cantidad, usuario_id)
SELECT * FROM (VALUES
    (6, 'ENTRADA', 97, 1),
    (20, 'ENTRADA', 18, 1),
    (13, 'ENTRADA', 86, 1),
    (17, 'ENTRADA', 42, 1),
    (1, 'ENTRADA', 97, 1),
    (4, 'ENTRADA', 97, 1),
    (9, 'ENTRADA', 92, 1),
    (4, 'ENTRADA', 47, 1),
    (6, 'ENTRADA', 68, 1),
    (9, 'ENTRADA', 74, 1),
    (6, 'ENTRADA', 74, 1),
    (10, 'ENTRADA', 91, 1),
    (20, 'ENTRADA', 35, 1),
    (12, 'ENTRADA', 30, 1),
    (17, 'ENTRADA', 10, 1)
) WHERE NOT EXISTS (SELECT 1 FROM movimientos_inventario LIMIT 1);

-- 15 Ventas con sus detalles y movimientos de SALIDA
INSERT INTO ventas (usuario_id, cliente_id, numero_factura, total)
SELECT * FROM (VALUES
    (2, 20, 'FAC-0001', 8.7),
    (2, 10, 'FAC-0002', 4.0),
    (2, 19, 'FAC-0003', 13.0),
    (2, 3, 'FAC-0004', 31.1),
    (2, 17, 'FAC-0005', 22.0),
    (2, 12, 'FAC-0006', 8.5),
    (2, 8, 'FAC-0007', 4.5),
    (2, 19, 'FAC-0008', 14.5),
    (2, 8, 'FAC-0009', 6.0),
    (2, 3, 'FAC-0010', 27.9),
    (2, 19, 'FAC-0011', 29.8),
    (2, 4, 'FAC-0012', 25.4),
    (2, 4, 'FAC-0013', 6.6),
    (2, 4, 'FAC-0014', 5.6),
    (2, 18, 'FAC-0015', 10.4)
) WHERE NOT EXISTS (SELECT 1 FROM ventas LIMIT 1);

INSERT INTO detalle_ventas (venta_id, medicamento_id, cantidad, precio_unitario, subtotal)
SELECT * FROM (VALUES
    (1, 16, 1, 4.2, 4.2),
    (1, 1, 3, 1.5, 4.5),
    (2, 2, 2, 2.0, 4.0),
    (3, 3, 4, 3.25, 13.0),
    (4, 5, 5, 2.5, 12.5),
    (4, 20, 2, 3.0, 6.0),
    (4, 16, 3, 4.2, 12.6),
    (5, 14, 2, 1.8, 3.6),
    (5, 7, 3, 2.8, 8.4),
    (5, 18, 4, 2.5, 10.0),
    (6, 17, 1, 1.5, 1.5),
    (6, 15, 2, 3.5, 7.0),
    (7, 11, 1, 4.5, 4.5),
    (8, 8, 1, 5.0, 5.0),
    (8, 19, 1, 6.5, 6.5),
    (8, 20, 1, 3.0, 3.0),
    (9, 2, 3, 2.0, 6.0),
    (10, 8, 2, 5.0, 10.0),
    (10, 9, 5, 1.9, 9.5),
    (10, 16, 2, 4.2, 8.4),
    (11, 16, 4, 4.2, 16.8),
    (11, 8, 2, 5.0, 10.0),
    (11, 20, 1, 3.0, 3.0),
    (12, 14, 4, 1.8, 7.2),
    (12, 12, 4, 3.8, 15.2),
    (12, 20, 1, 3.0, 3.0),
    (13, 13, 3, 2.2, 6.6),
    (14, 7, 2, 2.8, 5.6),
    (15, 5, 2, 2.5, 5.0),
    (15, 14, 3, 1.8, 5.4)
) WHERE NOT EXISTS (SELECT 1 FROM detalle_ventas LIMIT 1);

INSERT INTO movimientos_inventario (medicamento_id, tipo, cantidad, usuario_id)
SELECT * FROM (VALUES
    (16, 'SALIDA', 1, 2),
    (1, 'SALIDA', 3, 2),
    (2, 'SALIDA', 2, 2),
    (3, 'SALIDA', 4, 2),
    (5, 'SALIDA', 5, 2),
    (20, 'SALIDA', 2, 2),
    (16, 'SALIDA', 3, 2),
    (14, 'SALIDA', 2, 2),
    (7, 'SALIDA', 3, 2),
    (18, 'SALIDA', 4, 2),
    (17, 'SALIDA', 1, 2),
    (15, 'SALIDA', 2, 2),
    (11, 'SALIDA', 1, 2),
    (8, 'SALIDA', 1, 2),
    (19, 'SALIDA', 1, 2),
    (20, 'SALIDA', 1, 2),
    (2, 'SALIDA', 3, 2),
    (8, 'SALIDA', 2, 2),
    (9, 'SALIDA', 5, 2),
    (16, 'SALIDA', 2, 2),
    (16, 'SALIDA', 4, 2),
    (8, 'SALIDA', 2, 2),
    (20, 'SALIDA', 1, 2),
    (14, 'SALIDA', 4, 2),
    (12, 'SALIDA', 4, 2),
    (20, 'SALIDA', 1, 2),
    (13, 'SALIDA', 3, 2),
    (7, 'SALIDA', 2, 2),
    (5, 'SALIDA', 2, 2),
    (14, 'SALIDA', 3, 2)
) WHERE NOT EXISTS (SELECT 1 FROM movimientos_inventario WHERE tipo='SALIDA' LIMIT 1);

