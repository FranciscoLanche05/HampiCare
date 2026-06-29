# Hampi Care 💊 — Sistema de Gestión de Farmacia

Este es el proyecto final para la asignatura de **Programación Orientada a Objetos (Periodo 2026-A)**. Consiste en una aplicación de escritorio robusta diseñada en JavaFX con arquitectura MVC, gestión de dependencias a través de Maven y persistencia de datos utilizando PostgreSQL.

## 👥 Desarrolladores
* **Ivory Cando**
* **Francisco Lanche**

---

## 🚀 Características del Sistema
* **Arquitectura POO Limpia:** Implementación rigurosa de los 4 pilares (Abstracción, Encapsulamiento, Herencia y Polimorfismo).
* **Patrón Singleton:** Conexión única y eficiente a la base de datos a través de `Conexion.java`.
* **Autodespliegue Inteligente:** El sistema detecta automáticamente si la base de datos `hampicare_db` existe de manera local o en contenedor. Si no existe, la crea de forma autónoma e inyecta el script completo con enums, tablas y datos de prueba preconfigurados.
* **Doble Entorno (Docker / Local):** Diseñado para funcionar de manera transparente tanto en contenedores Docker para desarrollo colaborativo como en un servidor PostgreSQL local.

---

## 🛠️ Requisitos Previos
Antes de levantar la aplicación, asegúrate de tener instalado lo siguiente:
1.  **Java JDK 17 o superior** (configurado en tus variables de entorno).
2.  **Apache Maven**.
3.  **Entorno de Base de Datos** (Elige una de las dos opciones descritas abajo):
    * **Opción A:** Docker y Docker Compose instalado.
    * **Opción B:** PostgreSQL instalado localmente y pgAdmin.

---

## 📦 Instrucciones de Levantamiento (Base de Datos)

El sistema está configurado para escuchar en el **puerto 5432** mediante las credenciales:
* **Usuario:** `adminHampiCare`
* **Contraseña:** `adminHampi`

> ⚠️ **Importante:** No mantengas levantados ambos servicios (Docker y Local) al mismo tiempo, ya que generarán un conflicto por el uso del puerto 5432.

### Opción A: Despliegue con Docker (Recomendado para Desarrollo)
Para asegurar que ambos desarrolladores manejen la misma versión y entorno, ejecuta el siguiente comando en la raíz del proyecto (donde se encuentra el archivo `docker-compose.yml`):

```bash
docker-compose up -d
