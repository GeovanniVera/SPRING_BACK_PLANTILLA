# Backend API - Sistema de Gestión de Usuarios y RBAC

Este proyecto es una aplicación backend robusta construida con **Spring Boot 3** y **Java 17**, diseñada para proporcionar un sistema seguro y escalable de gestión de usuarios y control de acceso basado en roles (RBAC).

## Descripción General y Propósito

El objetivo principal de este sistema es resolver la problemática de la **administración de identidad y acceso** en aplicaciones empresariales. Proporciona una arquitectura limpia y modular para:

*   **Autenticación Segura**: Implementación de Login y Registro mediante **JWT (JSON Web Tokens)**.
*   **Gestión de Usuarios**: Ciclo de vida completo de usuarios (creación, actualización, deshabilitación).
*   **Control de Acceso Granular (RBAC)**: Administración dinámica de Roles y Privilegios, permitiendo definir con precisión qué acciones puede realizar cada usuario.
*   **Auditoría y Seguridad**: Estructura preparada para trazar accesos y modificaciones.

La arquitectura sigue los principios de **separación de responsabilidades**, organizando el código en módulos de dominio (Auth, Users, RBAC) para facilitar el mantenimiento y la escalabilidad.

## Stack Tecnológico

El sistema ha sido construido utilizando las siguientes tecnologías y herramientas:

*   **Lenguaje**: Java 17
*   **Framework Principal**: Spring Boot 3.2.2
*   **Seguridad**: Spring Security 6, JWT (JJWT 0.11.5)
*   **Base de Datos**: MySQL (con Spring Data JPA)
*   **Gestión de Dependencias**: Maven
*   **Validación**: Hibernate Validator (Bean Validation)

## 🛡️ Módulo de Auditoría

El sistema incorpora un módulo transversal de auditoría diseñado para garantizar la trazabilidad y la seguridad de las operaciones.

### Funcionalidades
1.  **Rastreo de Peticiones (`X-Request-Id`)**:
    *   Intercepta todas las peticiones HTTP.
    *   Si no existe, genera un ID único (UUID) y lo asocia al hilo de ejecución.
    *   Este ID permite correlacionar logs y eventos a través de todas las capas de la aplicación.

2.  **Registro de Eventos**:
    Se almacenan eventos críticos (Login, Fallos de Auth, Creación de usuarios, etc.) en la base de datos con la siguiente información:
    *   **Actor**: Quién realizó la acción.
    *   **Contexto**: Dirección IP, User-Agent, Método HTTP.
    *   **Resultado**: Éxito o Fallo (`SUCCESS` / `FAIL`).
    *   **Detalles**: JSON flexible con información específica del evento.

3.  **Integración con Seguridad**:
    *   Captura automáticamente intentos de acceso no autorizado (`401 Unauthorized`) y denegaciones de acceso (`403 Forbidden`).

## Referencia de la API

A continuación se detallan los endpoints disponibles en el sistema.

### Autenticación (`Auth Controller`)
**Base URL**: `/api/auth`

| Método | Endpoint | Descripción | Entrada (Body) | Salida (JSON Exitoso) |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/login` | Inicia sesión y obtiene un token JWT. | **LoginRequest**<br>`{ "username": "string", "password": "string" }` | **ApiResponse<LoginResponse>**<br>`{ "data": { "token": "jwt...", "username": "...", "roles": [...] } }` |
| `POST` | `/register` | Registra un nuevo usuario en el sistema. | **RegisterRequest**<br>`{ "username": "...", "password": "...", "alias": "...", "nombre": "...", "apellidoPaterno": "...", "apellidoMaterno": "..." }` | **ApiResponse<RegisterResponse>**<br>`{ "data": { "idPublic": "uuid", "username": "...", "roles": [...] } }` |

### Gestión de Usuarios (`User Controller`)
**Base URL**: `/api/users`

| Método | Endpoint | Descripción | Entrada | Salida (JSON Exitoso) |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/me` | Obtiene el perfil del usuario autenticado. | *N/A* (Requiere Token) | **ApiResponse<UserResponse>** |
| `GET` | `/{idPublic}` | Obtiene un usuario por su ID público. | `@PathVariable idPublic` (UUID) | **ApiResponse<UserResponse>** |
| `GET` | `/` | Lista todos los usuarios. | *N/A* | **ApiResponse<List<UserResponse>>** |
| `POST` | `/` | Crea un usuario administrativo (requiere permisos). | **CreateUserRequest**<br>`{ "username": "...", "password": "...", "nombre": "...", "roles": ["ROLE_USER"] }` | **ApiResponse<UserResponse>** |
| `PUT` | `/{idPublic}` | Actualiza datos básicos del usuario. | **UpdateUserRequest**<br>`{ "alias": "...", "nombre": "...", "segundoNombre": "..." }` | **ApiResponse<UserResponse>** |
| `PATCH` | `/{idPublic}/enabled` | Activa o desactiva un usuario. | **EnableUserRequest**<br>`{ "enabled": boolean }` | `204 No Content` |
| `PUT` | `/{idPublic}/roles` | Asigna roles a un usuario. | **AssignRolesRequest**<br>`{ "roles": ["ROLE_ADMIN", ...] }` | `204 No Content` |

### Roles (`Role Controller`)
**Base URL**: `/api/rbac/roles`

| Método | Endpoint | Descripción | Entrada | Salida (JSON Exitoso) |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/` | Lista todos los roles disponibles. | *N/A* | **ApiResponse<List<RoleDto>>** |
| `POST` | `/` | Crea un nuevo rol. | **CreateRoleRequest**<br>`{ "name": "ROLE_NUEVO", "description": "...", "privileges": ["PRIV_READ"] }` | **ApiResponse<RoleDto>** |
| `GET` | `/{roleName}` | Obtiene detalles de un rol específico. | `@PathVariable roleName` (String) | **ApiResponse<RoleDto>** |
| `PUT` | `/{roleName}` | Actualiza un rol existente. | **CreateRoleRequest** | **ApiResponse<RoleDto>** |
| `DELETE` | `/{roleName}` | Elimina un rol. | `@PathVariable roleName` (String) | `204 No Content` |
| `PUT` | `/{roleName}/privileges` | Asigna privilegios a un rol. | **AssignPrivilegesRequest**<br>`{ "privileges": ["PRIV_1", "PRIV_2"] }` | **ApiResponse<Void>** |

### Privilegios (`Privilege Controller`)
**Base URL**: `/api/rbac/privileges`

| Método | Endpoint | Descripción | Entrada | Salida (JSON Exitoso) |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/` | Lista todos los privilegios. | *N/A* | **ApiResponse<List<PrivilegeDto>>** |
| `POST` | `/` | Crea un nuevo privilegio. | **CreatePrivilegeRequest**<br>`{ "name": "PRIV_NUEVO", "description": "..." }` | **ApiResponse<PrivilegeDto>** |
| `GET` | `/{name}` | Obtiene un privilegio por nombre. | `@PathVariable name` (String) | **ApiResponse<PrivilegeDto>** |
| `PUT` | `/{name}` | Actualiza un privilegio. | **CreatePrivilegeRequest** | **ApiResponse<PrivilegeDto>** |
| `DELETE` | `/{name}` | Elimina un privilegio. | `@PathVariable name` (String) | `204 No Content` |

---

## Requisitos e Instalación

### Requisitos Previos
1.  **Java JDK 17** instalado (`java -version`).
2.  **Maven** instalado (`mvn -version`).
3.  **MySQL Server** en ejecución.

### Instalación y Ejecución

1.  **Clonar el repositorio**:
    ```bash
    git clone <url-del-repo>
    cd backend
    ```

2.  **Configuración de Base de Datos**:
    Edita el archivo `src/main/resources/application.properties` y configura tus credenciales de MySQL:
    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/tu_base_de_datos
    spring.datasource.username=tu_usuario
    spring.datasource.password=tu_contraseña
    ```

3.  **Compilar el proyecto**:
    ```bash
    mvn clean install
    ```

4.  **Ejecutar la aplicación**:
    El proyecto usa el plugin de Spring Boot, puedes ejecutarlo directamente con:
    ```bash
    mvn spring-boot:run
    ```
    O ejecutar el JAR generado:
    ```bash
    java -jar target/tuapp-0.0.1-SNAPSHOT.jar
    ```

5.  **Verificación**:
    La aplicación se iniciará por defecto en el puerto `8080`.

### Ejecución con Docker

**Construir y levantar los contenedores**

Desde la raíz del proyecto, ejecuta:

```bash
docker-compose up -d --build
```

Este comando compilará el JAR de Spring Boot dentro de un contenedor y levantará una instancia de MySQL vinculada automáticamente.

**Verificar el estado**

```bash
docker-compose ps
```

**Detener los servicios**

```bash
docker-compose down
```

> **Nota sobre variables de entorno**: El archivo `docker-compose.yaml` ya contiene las variables de entorno necesarias para que el Backend se comunique con la base de datos dentro de la red de Docker. No necesitas modificar el `application.properties` local para la ejecución con Docker.

## Módulo de Email (Opcional)

Este proyecto incluye un sistema de mensajería desacoplado. Puedes elegir entre enviar correos reales o simplemente verlos en la consola de logs.

### 1. Activación y Desactivación

El módulo se controla mediante la propiedad `app.email.enabled`.

*   `false` (**Default**): Utiliza un `MockEmailService`. Los correos no se envían, solo se registran en el log de la aplicación.
*   `true`: Utiliza `SmtpEmailService` para envíos reales vía protocolo SMTP.

### 2. Configuración vía Docker / Environment

Para envíos reales, configura las siguientes variables en tu archivo `.env` o `docker-compose.yaml`:

| Variable | Descripción | Ejemplo (Gmail) |
| :--- | :--- | :--- |
| `APP_EMAIL_ENABLED` | Activa el servicio real | `true` |
| `SPRING_MAIL_HOST` | Servidor SMTP | `smtp.gmail.com` |
| `SPRING_MAIL_PORT` | Puerto SMTP | `587` |
| `SPRING_MAIL_USERNAME` | Tu correo | `tu-app@gmail.com` |
| `SPRING_MAIL_PASSWORD` | Contraseña de aplicación | `abcd-efgh-ijkl-mnop` |

### 3. Personalización de Plantillas

Las plantillas están ubicadas en `src/main/resources/templates/mail/`.

*   `layout.html`: Cambia aquí el logo y el pie de página global.
*   `welcome-email.html`: Plantilla de ejemplo para nuevos registros.

## Pruebas de Email con MailHog

Al utilizar el entorno de Docker (`docker-compose up`), el sistema intercepta automáticamente todos los correos salientes y los redirige al servicio **MailHog**, evitando envíos reales a destinatarios durante el desarrollo.

### 📧 Ver correos interceptados

Para visualizar los correos enviados por el sistema, accede desde tu navegador a:

👉 **[http://localhost:8025](http://localhost:8025)**

Aquí podrás ver la bandeja de entrada simulada, inspeccionar el contenido HTML de los correos y verificar que las plantillas se renderizan correctamente.

### ⚡ Prueba Rápida

Ejecuta el siguiente comando para generar un correo de prueba instantáneamente:

```bash
curl -X POST "http://localhost:8080/api/test/send-verification?email=tu@email.com"
```

Deberías ver una respuesta JSON confirmando el envío y, si estás usanto Docker, el correo aparecerá en MailHog.
