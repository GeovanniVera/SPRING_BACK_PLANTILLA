# Backend API - Sistema de Gestión de Usuarios y RBAC

Este proyecto es una aplicación backend robusta construida con **Spring Boot 3** y **Java 17**, diseñada para proporcionar un sistema seguro y escalable de gestión de usuarios y control de acceso basado en roles (RBAC).

## Descripción General y Propósito

El objetivo principal de este sistema es resolver la problemática de la **administración de identidad y acceso** en aplicaciones empresariales. Proporciona una arquitectura limpia y modular para:

*   **Autenticación Segura**: Implementación de Login, Registro y Refresh Tokens mediante **JWT (JSON Web Tokens)**.
*   **Gestión de Usuarios**: Ciclo de vida completo de usuarios con estados complejos.
*   **Control de Acceso Granular (RBAC)**: Administración dinámica de Roles y Privilegios, permitiendo definir con precisión qué acciones puede realizar cada usuario.
*   **Auditoría y Seguridad**: Estructura preparada para trazar accesos, modificaciones y proteger contra ataques de fuerza bruta.

La arquitectura sigue los principios de **separación de responsabilidades**, organizando el código en módulos de dominio (Auth, Users, RBAC) para facilitar el mantenimiento y la escalabilidad.

## Stack Tecnológico

El sistema ha sido construido utilizando las siguientes tecnologías y herramientas:

*   **Lenguaje**: Java 17
*   **Framework Principal**: Spring Boot 3.2.2
*   **Seguridad**: Spring Security 6, JWT (JJWT 0.11.5)
*   **Base de Datos**: MySQL 8.0 (con Spring Data JPA)
*   **Vistas (Backend Rendering)**: Thymeleaf
*   **Gestión de Dependencias**: Maven
*   **Validación**: Hibernate Validator (Bean Validation)
*   **Contenedores**: Docker & Docker Compose

## 🔒 Reglas de Normalización y Seguridad

Para mantener la consistencia y seguridad de los datos, el sistema aplica automáticamente las siguientes reglas en los DTOs de entrada y servicios:

### 1. Sanitización Automática
*   **Username / Email**: Se convierten a **minúsculas** y se eliminan espacios en blanco (`trim().toLowerCase()`).
    *   Ejemplo: `"  UserName "` -> `"username"`
*   **Roles y Privilegios**: Se convierten a **mayúsculas** y se eliminan espacios (`trim().toUpperCase()`).
    *   Ejemplo: `"  role_admin "` -> `"ROLE_ADMIN"`

### 2. Política de Contraseñas Estricta
Al registrar un nuevo usuario, la contraseña debe cumplir con los siguientes requisitos mínimos:
*   🔑 Mínimo **12 caracteres**.
*   🔡 Al menos una letra **minúscula** (`a-z`).
*   🔠 Al menos una letra **mayúscula** (`A-Z`).
*   🔢 Al menos un **número** (`0-9`).
*   🔣 Al menos un **carácter especial** (`!@#$%^&*...`).

> Si la contraseña no cumple con estos criterios, el sistema rechazará el registro con un mensaje de error descriptivo.

## 🛡️ Verificación de Cuenta & Ciclo de Vida

El sistema implementa un ciclo de vida de usuario robusto controlado por la enumeración `UserStatus`.

### Estados del Usuario (`UserStatus`)
1.  **PENDING_VERIFICATION**: Estado inicial al registrarse. El usuario no puede hacer login.
2.  **ACTIVE**: El usuario ha verificado su correo y puede operar normalmente.
3.  **INACTIVE**: Deshabilitado administrativamente (soft delete).
4.  **BLOCKED**: Bloqueado temporalmente por seguridad.

### Protección de Cuenta (Anti-Brute Force)
Para mitigar ataques de fuerza bruta, el sistema monitorea intentos de login fallidos:
*   **Lógica**: Tras **5 intentos fallidos consecutivos**, la cuenta pasa a estado `BLOCKED`.
*   **Duración**: El bloqueo dura **15 minutos**.
*   **Desbloqueo**: Automático tras expirar el tiempo, o manual por un administrador.

### Flujo de Activación y Reenvío
1.  **Registro**: Se crea el usuario en `PENDING_VERIFICATION` y se envía un correo con un token.
2.  **Verificación**: Al hacer clic en el enlace (`/verify`), el usuario pasa a `ACTIVE`.
3.  **Reenvío de Token**: Si el correo se pierde o el token expira, se puede solicitar uno nuevo mediante el endpoint de reenvío.

## 🧹 Mantenimiento y Robustez de Datos

El sistema está diseñado para mantenerse limpio y performante automáticamente.

### Limpieza Automática (`UserCleanupTask`)
Una tarea programada (`@Scheduled`) se ejecuta cada 12 horas para purgar la base de datos:
*   **Objetivo**: Usuarios en estado `PENDING_VERIFICATION`.
*   **Condición**: Creados hace más de **48 horas** (configurable vía `app.auth.verification-expiration-hours`).
*   **Acción**: Eliminación física del usuario y su token asociado para liberar recursos y evitar cuentas basura.

### Integridad de Base de Datos
*   **Indices**: Se han añadido índices (`@Index`) en las tablas pivote `users_roles` y `roles_privileges` para maximizar el rendimiento de las validaciones de seguridad en cada petición.
*   **Constraints**: Garantía de unicidad a nivel de esquema en `username`, `email` (implícito en username), `role.name` y `privilege.name`.

### Seeds Idempotentes
El servicio `RbacBootstrapService` asegura que el entorno sea reproducible:
*   Al iniciar, verifica si existen los roles `ROLE_ADMIN` y `ROLE_USER`.
*   Si la base de datos está vacía, crea un usuario administrador por defecto.

## ⚠️ Manejo de Errores y Excepciones

El sistema cuenta con una arquitectura de manejo de errores centralizada y estandarizada:

*   **GlobalExceptionHandler**: Un `@RestControllerAdvice` captura todas las excepciones (validación, seguridad, negocio) y las transforma en respuestas JSON uniformes.
*   **Excepciones de Dominio**: Se utilizan excepciones semánticas propias del negocio en lugar de excepciones genéricas (`RuntimeException`):
    *   `BusinessException` (Base)
    *   `UserNotFoundException` (404)
    *   `UserAlreadyExistsException` (409)
    *   `WeakPasswordException` (400)
    *   `AccountLockedException` (403)
*   **Formato de Respuesta de Error**:
    ```json
    {
      "status": 404,
      "error": "Not Found",
      "message": "User not found with username: test@example.com",
      "details": null,
      "path": "/api/users/test@example.com",
      "timestamp": "..."
    }
    ```

## 🎨 Interfaz Visual (Backend Rendering)

Endpoints que sirven HTML para interacción directa con el usuario final desde el correo:

*   **URL**: `/api/auth/verify?token=...`
*   **Comportamiento**: Valida el token y renderiza una plantilla HTML.

### Plantillas (Thymeleaf)
*   `verify-success.html`: Éxito. Botón redirige al Frontend (`app.frontend-url`).
*   `verify-error.html`: Token inválido o expirado.

## Referencia de la API

Endpoints principales del sistema de Autenticación. **Base URL**: `/api/auth`

| Método | Endpoint | Descripción | Entrada / Salida |
| :--- | :--- | :--- | :--- |
| `POST` | `/login` | Inicia sesión. Retorna Access (30m) y Refresh Token (7d). | **In**: `LoginRequest`<br>**Out**: `ApiResponse<LoginResponse>` |
| `POST` | `/register` | Registra usuario (Estado: PENDING). | **In**: `RegisterRequest`<br>**Out**: `ApiResponse<RegisterResponse>` |
| `POST` | `/refresh` | Obtiene nuevo Access Token usando Refresh Token. | **In**: `TokenRefreshRequest`<br>**Out**: `TokenRefreshResponse` |
| `POST` | `/logout` | Invalida la sesión (Borra Refresh Token). | *N/A* (Requiere Auth)<br>**Out**: `200 OK` |
| `POST` | `/resend-verification` | Reenvía correo de activación. | **Query**: `?email=...`<br>**Out**: `ApiResponse` |
| `GET` | `/verify` | Valida token (Endpoint Visual). | **Query**: `?token=...`<br>**Out**: Vista HTML |

👉 [Consulta la Especificación Completa de la API aquí](API_SPECIFICATION.md)

## 🐳 Ejecución con Docker

### Estructura de Servicios (`docker-compose.yml`)
1.  **`db`**: MySQL 8.0 (Puerto 3307 externo, 3306 interno).
2.  **`mailhog`**: Servidor SMTP de pruebas (Web UI: `http://localhost:8025`).
3.  **`app`**: Backend Spring Boot (Puerto 8080).

### Variables de Entorno Clave

| Variable | Descripción |
| :--- | :--- |
| `APP_FRONTEND_URL` | URL del cliente (ej. `http://localhost:4200`) para redirecciones. |
| `APP_JWT_SECRET` | Clave secreta para firmar tokens. |
| `APP_JWT_EXPIRATION_MS` | Duración Access Token (Default: 30 min). |
| `APP_AUTH_VERIFICATION_EXPIRATION_HOURS` | Tiempo antes de purgar usuarios no verificados (Default: 48h). |

### Comandos de Ejecución

**Levantar todo el entorno:**
```bash
docker-compose up -d --build
```
> La app estará disponible en `http://localhost:8080`.

**Limpieza Total (Purgar Datos):**
```bash
docker-compose down -v
```
> **Advertencia**: Esto eliminará todos los datos persistentes en MySQL.
