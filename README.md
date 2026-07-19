# Hampi Care 💊 — Sistema de Gestión de Farmacia

Este es el proyecto final para la asignatura de **Programación Orientada a Objetos (Periodo 2026-A)**. Consiste en una aplicación de escritorio robusta diseñada en JavaFX con arquitectura MVC, gestión de dependencias a través de Maven y persistencia de datos utilizando PostgreSQL alojado en la nube (Supabase).

## 👥 Desarrolladores
* **Ivory Cando**
* **Francisco Lanche**

---

## 🚀 Características del Sistema
* **Arquitectura POO Limpia:** Implementación rigurosa de los 4 pilares (Abstracción, Encapsulamiento, Herencia y Polimorfismo).
* **Patrón Singleton:** Conexión única y eficiente a la base de datos en la nube a través de `Conexion.java`.
* **Base de Datos en la Nube (Supabase):** Configurada para que todos los desarrolladores compartan la misma información en tiempo real sin necesidad de contenedores o bases de datos locales.
* **Empaquetado Seguro (.exe):** Las credenciales de la base de datos se inyectan en tiempo de ejecución (JVM Options) para mayor seguridad usando Launch4j, evitando la exposición de un archivo `.env`.

---

## 🛠️ Requisitos Previos
Antes de levantar o modificar la aplicación, asegúrate de tener instalado lo siguiente:
1. **Java JDK 17 o superior** (configurado en tus variables de entorno).
2. **Apache Maven**.
3. **Entorno de Desarrollo (IDE)** (IntelliJ IDEA, Eclipse o NetBeans).

---

## 📦 Instrucciones de Ejecución y Configuración

El proyecto ya no depende de Docker ni de bases de datos locales. La conexión a PostgreSQL (Supabase) se realiza mediante parámetros del sistema de la JVM para proteger las credenciales.

### 1. Ejecución desde el Entorno de Desarrollo (IDE)
Para correr el proyecto desde IntelliJ IDEA (o cualquier otro IDE), debes pasarle a la JVM las credenciales como argumentos. 
En IntelliJ IDEA, ve a **Run -> Edit Configurations**, selecciona tu clase `Main` y en el campo **VM options** pega lo siguiente (remplazando con tu contraseña):

```bash
-DSUPABASE_URL="jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require" -DSUPABASE_USER="postgres.jswqhccogcenejfbnwuy" -DSUPABASE_PASSWORD="tu_contrasena"
```

### 2. Generación del Archivo `.exe` con Launch4j
Para generar un ejecutable de escritorio que los usuarios puedan usar fácilmente sin exponer las contraseñas en archivos de texto:
1. Compila el `.jar` usando Maven: `mvn clean package`.
2. Abre **Launch4j** y configura tu `.jar` de entrada.
3. Ve a la pestaña **JRE** y en el recuadro **JVM options** pega exactamente la misma cadena que usas en el IDE:
   `-DSUPABASE_URL="..." -DSUPABASE_USER="..." -DSUPABASE_PASSWORD="..."`
4. ¡Genera el `.exe`! Tus credenciales quedarán empaquetadas en el arranque del ejecutable de forma transparente.
