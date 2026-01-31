# Especificación de la API

Este documento sirve como referencia técnica completa para los desarrolladores, detallando cada endpoint con sus modelos de datos (JSON) y lógica de negocio.

## 📚 Tabla de Contenidos

1.  [Autenticación](#autenticación)
2.  [Usuarios](#usuarios)
3.  [Roles y Privilegios](#roles-y-privilegios)
4.  [Tabla de Estados](#tabla-de-estados-userstatus)

---

## Autenticación

Endpoints relacionados con el registro, inicio de sesión y gestión de sesiones. **Base URL**: `/api/auth`

### `POST /api/auth/login`

**Descripción**: Inicia sesión en el sistema. Verifica credenciales y estado del usuario.
*   Reglas:
    *   Solo usuarios con estado `ACTIVE` pueden loguearse.
    *   Usuarios bloqueados (`BLOCKED`) recibirán un error 403.
    *   Credenciales incorrectas incrementan el contador de fallos. 5 fallos bloquean la cuenta.

**Request Body** (`LoginRequest`):
```json
{
  "username": "usuario",
  "password": "Password123!"
}
```

**Success Response (200 OK)** (`ApiResponse<LoginResponse>`):
```json
{
  "status": 200,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbjc...",
    "refreshToken": "uuid-refresh-token...",
    "idPublic": "uuid-user-id...",
    "username": "usuario",
    "nombre": "Juan",
    "tag": "Juan#1234",
    "roles": ["ROLE_USER"]
  },
  "path": "/api/auth/login",
  "timestamp": "2023-10-27T10:00:00"
}
```

**Error Responses**:
*   `401 Unauthorized`: Credenciales inválidas.
*   `403 Forbidden`: Cuenta bloqueada (`AccountLockedException`) o no verificada (`DisabledException`).

### `POST /api/auth/register`

**Descripción**: Registra un nuevo usuario en el sistema.
*   Reglas:
    *   El usuario se crea en estado `PENDING_VERIFICATION`.
    *   Se envía un correo electrónico de verificación automáticamente.
    *   Asigna el rol por defecto `ROLE_USER`.

**Request Body** (`RegisterRequest`):
```json
{
  "username": "juanperez",
  "password": "Password123!",
  "alias": "JuanP",
  "nombre": "Juan",
  "segundoNombre": "Antonio",
  "apellidoPaterno": "Perez",
  "apellidoMaterno": "Gomez",
  "clientType": "web"
}
```
*   `clientType` (Opcional): "web" (default) o "mobile". Define el flujo de redirección.
```

**Success Response (200 OK)** (`ApiResponse<RegisterResponse>`):
```json
{
  "status": 200,
  "message": "Usuario registrado exitosamente. Por favor, verifica tu correo electrónica.",
  "data": {
    "idPublic": "uuid...",
    "username": "juanperez",
    "alias": "JuanP",
    "tag": "JuanP#1234",
    "roles": ["ROLE_USER"]
  },
  "path": "/api/auth/register",
  "timestamp": "..."
}
```

**Error Responses**:
*   `409 Conflict`: El usuario ya existe (`UserAlreadyExistsException`).
*   `400 Bad Request`: Contraseña débil (`WeakPasswordException`) o validación fallida.

### `POST /api/auth/refresh`

**Descripción**: Obtiene un nuevo Access Token utilizando un Refresh Token válido.

**Request Body** (`TokenRefreshRequest`):
```json
{
  "refreshToken": "uuid-refresh-token..."
}
```

**Success Response (200 OK)** (`TokenRefreshResponse`):
```json
{
  "accessToken": "eyJhbjc... (nuevo)",
  "refreshToken": "uuid... (rotado)"
}
```

**Error Responses**:
*   `400 Bad Request`: Token inválido o expirado (`InvalidTokenException`).

### `POST /api/auth/logout`

**Descripción**: Cierra la sesión del usuario invalidando su Refresh Token. Requiere autenticación (Bearer Token).

**Request Body**: *Vacío*

**Success Response (200 OK)**: *Sin contenido o mensaje simple.*

### `POST /api/auth/resend-verification`

**Descripción**: Reenvía el correo de verificación para una cuenta no activada.

**Query Params**: `?email=usuario`

**Success Response (200 OK)**:
```json
{
  "status": 200,
  "message": "Verification email resent successfully",
  "data": null,
  "path": "/api/auth/resend-verification",
  "timestamp": "..."
}
```

### `POST /api/auth/forgot-password`

**Descripción**: Inicia el proceso de recuperación de contraseña enviando un correo con un token de reseteo. URL dinámica según el cliente.

**Request Body** (`ForgotPasswordRequest`):
```json
{
  "email": "usuario@example.com",
  "clientType": "mobile"
}
```
*   `clientType` (Opcional): "web" (default) o "mobile".

**Success Response (200 OK)**:
```json
{
  "status": 200,
  "message": "Password reset email sent",
  "data": null,
  "path": "/api/auth/forgot-password",
  "timestamp": "..."
}
```

### `POST /api/auth/reset-password`

**Descripción**: Restablece la contraseña utilizando un token válido.

**Request Body** (`ResetPasswordRequest`):
```json
{
  "token": "uuid-reset-token...",
  "newPassword": "NewPassword123!"
}
```

**Success Response (200 OK)**:
```json
{
  "status": 200,
  "message": "Password has been reset successfully",
  "data": null,
  "path": "/api/auth/reset-password",
  "timestamp": "..."
}
```

**Error Responses**:
*   `400 Bad Request`: Token inválido/expirado o contraseña débil.
*   **Ejemplo Token Expirado**:
    ```json
    {
      "status": 400,
      "error": "Bad Request",
      "message": "Password reset token expired",
      "details": null,
      "path": "/api/auth/reset-password",
      "timestamp": "..."
    }
    ```

---

## Usuarios

Gestión de perfiles y administración de usuarios. **Base URL**: `/api/users`

### `GET /api/users/me`

**Descripción**: Obtiene el perfil del usuario autenticado actual.
*   Permissions: `USERS_READ_SELF`

**Success Response (200 OK)** (`ApiResponse<UserResponse>`):
```json
{
  "status": 200,
  "message": "Profile retrieved successfully",
  "data": {
    "idPublic": "uuid...",
    "username": "juanperez",
    "alias": "JuanP",
    "nombre": "Juan",
    "apellidoPaterno": "Perez",
    "email": "juanperez",
    "status": "ACTIVE",
    "roles": ["ROLE_USER"]
  }
}
```

### `GET /api/users`

**Descripción**: Lista todos los usuarios.
*   Permissions: `USERS_READ_ALL`

**Success Response (200 OK)**: Lista de objetos `UserResponse`.

### `PUT /api/users/{idPublic}`

**Descripción**: Actualiza la información de perfil de un usuario.
*   Permissions: `ADMIN` o `USERS_UPDATE`

**Request Body** (`UpdateUserRequest`):
```json
{
  "alias": "NuevoAlias",
  "nombre": "NuevoNombre",
  "apellidoPaterno": "NuevoApellido"
}
```

**Success Response (200 OK)**: Objeto `UserResponse` actualizado.

**Error Responses**:
*   `404 Not Found`: Usuario no encontrado (`UserNotFoundException`).

### `PUT /api/users/{idPublic}/roles`

**Descripción**: Asigna o reemplaza los roles de un usuario.
*   Permissions: `ADMIN` o `USERS_UPDATE`

**Request Body** (`AssignRolesRequest`):
```json
{
  "roles": ["ROLE_ADMIN", "ROLE_USER"]
}
```

**Success Response (204 No Content)**.

### `PATCH /api/users/{idPublic}/enabled`

**Descripción**: Activa o bloquea manualmente a un usuario.
*   Permissions: `ADMIN` o `USERS_UPDATE`

**Request Body** (`EnableUserRequest`):
```json
{
  "enabled": false
}
```
*   `true` -> `ACTIVE`
*   `false` -> `BLOCKED`

**Success Response (204 No Content)**.

---

## Tabla de Estados (`UserStatus`)

| Estado | Descripción | Login Permitido | Transiciones Comunes |
| :--- | :--- | :---: | :--- |
| `PENDING_VERIFICATION` | Cuenta creada, correo no verificado. | ❌ | -> `ACTIVE` (al verificar email) |
| `ACTIVE` | Cuenta operativa y verificada. | ✅ | -> `BLOCKED` (intentos fallidos o admin) |
| `BLOCKED` | Bloqueo temporal (seguridad) o permanente (admin). | ❌ | -> `ACTIVE` (tras tiempo o desbloqueo admin) |
| `INACTIVE` | Cuenta eliminada lógicamente (Soft Delete). | ❌ | *Terminal* |

