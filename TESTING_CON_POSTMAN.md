# 🧪 GUÍA DE TESTING CON POSTMAN

## 📋 ÍNDICE

1. [Login por Email](#1-login-por-email)
2. [Testing SIN JWT (Modo Dev)](#2-testing-sin-jwt-modo-dev)
3. [Testing CON JWT (Modo Prod)](#3-testing-con-jwt-modo-prod)
4. [Configuración CORS](#4-configuración-cors)
5. [Ejemplos Completos](#5-ejemplos-completos)

---

## 1️⃣ LOGIN POR EMAIL

### ✅ Confirmación: Login es por EMAIL, no por username

El sistema ya está configurado para login por email:

```java
// LoginRequest.java
{
  "email": "admin@barberia.com",    // ← Email, no username
  "password": "admin123"
}

// CustomUserDetailsService.java
loadUserByUsername(String email) {  // ← Recibe email
    Usuario usuario = usuarioRepository.findByEmail(email);
}
```

---

## 2️⃣ TESTING SIN JWT (MODO DEV)

### 🎯 Para: Probar rápidamente en Postman sin necesidad de tokens

### Paso 1: Activar modo desarrollo

**En .env:**
```bash
SPRING_PROFILES_ACTIVE=dev
```

**O en application.yml:**
```yaml
spring:
  profiles:
    active: dev
```

### Paso 2: Iniciar la aplicación

```powershell
# Generar clave JWT primero (obligatorio)
$bytes = New-Object byte[] 64
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$key = [Convert]::ToBase64String($bytes)

# Guardar en .env
"JWT_SECRET_KEY=$key" | Out-File -Append .env

# Iniciar app
mvn spring-boot:run
```

**Deberías ver:**
```
✅ Perfil activo: dev
⚠️  MODO DESARROLLO: Seguridad JWT DESHABILITADA
```

### Paso 3: Probar en Postman SIN token

**Login (devuelve token pero no es necesario usarlo):**
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@barberia.com",
  "password": "admin123"
}
```

**Endpoint protegido SIN Authorization header:**
```http
GET http://localhost:8080/api/demo/admin
# ✅ Funciona sin token en modo dev
```

**Endpoint con @PreAuthorize:**
```http
GET http://localhost:8080/api/demo/read-clients
# ✅ También funciona sin validar permisos en modo dev
```

### 🎯 Ventajas del modo dev:

- ✅ No necesitas copiar/pegar tokens
- ✅ Pruebas rápidas de lógica de negocio
- ✅ Ideal para desarrollo inicial
- ✅ Sin errores 401/403

---

## 3️⃣ TESTING CON JWT (MODO PROD)

### 🎯 Para: Probar el sistema completo con seguridad real

### Paso 1: Activar modo producción

**En .env:**
```bash
SPRING_PROFILES_ACTIVE=prod
```

### Paso 2: Iniciar la aplicación

```powershell
mvn spring-boot:run
```

**Deberías ver:**
```
✅ Perfil activo: prod
🔒 Seguridad JWT HABILITADA
```

### Paso 3: Hacer login y obtener token

**Request:**
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@barberia.com",
  "password": "admin123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBiYXJiZXJpYS5jb20iLCJyb2xlcyI6WyJBRE1JTiJdLCJwZXJtaXNzaW9ucyI6WyJSRUFEX0NMSUVOVFM...",
  "type": "Bearer",
  "email": "admin@barberia.com",
  "name": "Admin",
  "roles": ["ADMIN"],
  "permissions": ["READ_CLIENTS", "CREATE_CLIENTS", "DELETE_USERS"]
}
```

### Paso 4: Usar el token en requests

**Copiar token y agregarlo al header:**
```http
GET http://localhost:8080/api/demo/admin
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 📦 Postman: Variables de entorno

**Crear Environment "Barberia":**
```json
{
  "base_url": "http://localhost:8080",
  "token": ""  // Se llenará automáticamente
}
```

**Script para guardar token automáticamente:**

En el request de login, pestaña **Tests**:
```javascript
// Guarda el token automáticamente
if (pm.response.code === 200) {
    const response = pm.response.json();
    pm.environment.set("token", response.token);
    console.log("✅ Token guardado:", response.token.substring(0, 20) + "...");
}
```

**Usar token en otros requests:**
```
Authorization: Bearer {{token}}
```

---

## 4️⃣ CONFIGURACIÓN CORS

### ✅ CORS ya está configurado

**Modo DEV (permisivo):**
```java
// Acepta requests desde cualquier origen
- Postman ✅
- localhost:3000 (React) ✅
- localhost:5173 (Vite) ✅
- localhost:4200 (Angular) ✅
```

**Modo PROD (restrictivo):**
```java
// Solo acepta requests de dominios específicos
allowedOrigins:
  - http://localhost:3000
  - https://mi-barberia.com  // ← Cambiar por tu dominio
```

### Cambiar dominios permitidos en producción:

Edita [SecurityConfig.java](src/main/java/com/barberia/config/SecurityConfig.java):
```java
@Profile("!dev")
public CorsConfigurationSource corsConfigurationSourceProd() {
    configuration.setAllowedOrigins(Arrays.asList(
        "https://tu-dominio.com",        // ← Tu dominio
        "https://app.tu-dominio.com"     // ← Subdominios
    ));
}
```

---

## 5️⃣ EJEMPLOS COMPLETOS

### 📁 Collection de Postman

**Estructura recomendada:**

```
📂 Barberia API
├── 📁 Auth (Público)
│   ├── 🟢 POST Login
│   ├── 🟢 POST Register
│   └── 🟢 GET Public
│
├── 📁 Demo (Protegido)
│   ├── 🔒 GET Protected
│   ├── 🔒 GET Admin (ADMIN)
│   ├── 🔒 GET Read Clients (Permiso)
│   └── 🔒 DELETE Delete User (ADMIN + Permiso)
│
└── 📁 Variables
    ├── base_url: http://localhost:8080
    └── token: (se llena automáticamente)
```

### 🟢 1. Login (Público)

```http
POST {{base_url}}/api/auth/login
Content-Type: application/json

{
  "email": "admin@barberia.com",
  "password": "admin123"
}
```

**Tests (guardar token):**
```javascript
if (pm.response.code === 200) {
    const response = pm.response.json();
    pm.environment.set("token", response.token);
}
```

### 🟢 2. Register (Público)

```http
POST {{base_url}}/api/auth/register
Content-Type: application/json

{
  "name": "Nuevo Usuario",
  "email": "nuevo@barberia.com",
  "password": "password123"
}
```

### 🔒 3. Endpoint Protegido Simple

```http
GET {{base_url}}/api/demo/protected
Authorization: Bearer {{token}}
```

### 🔒 4. Endpoint con Rol ADMIN

```http
GET {{base_url}}/api/demo/admin
Authorization: Bearer {{token}}
```

**Requiere:**
- ✅ Token válido
- ✅ Rol ADMIN

### 🔒 5. Endpoint con Permiso Específico

```http
GET {{base_url}}/api/demo/read-clients
Authorization: Bearer {{token}}
```

**Requiere:**
- ✅ Token válido
- ✅ Permiso READ_CLIENTS

### 🔒 6. Endpoint con Rol Y Permiso

```http
DELETE {{base_url}}/api/demo/delete-user/1
Authorization: Bearer {{token}}
```

**Requiere:**
- ✅ Token válido
- ✅ Rol ADMIN
- ✅ Permiso DELETE_USERS

---

## 📊 TABLA COMPARATIVA: DEV vs PROD

| Aspecto | Modo DEV | Modo PROD |
|---------|----------|-----------|
| **JWT requerido** | ❌ No | ✅ Sí |
| **Header Authorization** | ❌ Opcional | ✅ Obligatorio |
| **@PreAuthorize** | ❌ Ignorado | ✅ Validado |
| **CORS** | ✅ Permisivo | 🔒 Restrictivo |
| **Uso recomendado** | Testing Postman | Producción/React |
| **Velocidad testing** | ⚡ Rápido | 🐢 Necesitas login |

---

## 🔄 WORKFLOW RECOMENDADO

### Fase 1: Desarrollo Backend (AHORA)

```bash
# .env
SPRING_PROFILES_ACTIVE=dev
```

1. Desarrolla endpoints
2. Prueba en Postman SIN token
3. Itera rápidamente

### Fase 2: Testing Completo

```bash
# .env
SPRING_PROFILES_ACTIVE=prod
```

1. Prueba con JWT completo
2. Valida roles y permisos
3. Asegura que todo funciona

### Fase 3: Frontend React

```bash
# Backend en PROD
SPRING_PROFILES_ACTIVE=prod

# Frontend en localhost:3000
npm start
```

1. Backend con JWT activo
2. Frontend envía tokens en cada request
3. CORS permite localhost:3000

### Fase 4: Producción

```bash
SPRING_PROFILES_ACTIVE=prod

# Actualizar CORS con dominio real
allowedOrigins: ["https://mi-barberia.com"]
```

---

## 🚀 COMANDOS RÁPIDOS

### Cambiar entre modos:

```powershell
# Modo DEV (sin JWT)
notepad .env  # Cambiar a: SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run

# Modo PROD (con JWT)
notepad .env  # Cambiar a: SPRING_PROFILES_ACTIVE=prod
mvn spring-boot:run
```

### Verificar modo activo:

Al iniciar la app, busca en los logs:
```
✅ Perfil activo: dev    # Sin JWT
✅ Perfil activo: prod   # Con JWT
```

---

## ❓ PREGUNTAS FRECUENTES

### **¿El login siempre es por email?**
✅ Sí, el campo es `email` en LoginRequest y RegisterRequest.

### **¿Puedo probar sin JWT?**
✅ Sí, usa `SPRING_PROFILES_ACTIVE=dev` y todos los endpoints estarán abiertos.

### **¿CORS funciona en Postman?**
✅ Sí, en modo dev acepta cualquier origen incluyendo Postman.

### **¿Qué pasa si olvido el token en modo prod?**
❌ Error 403 Forbidden. Debes incluir `Authorization: Bearer <token>`.

### **¿Cómo guardo el token automáticamente en Postman?**
✅ Usa un script en la pestaña Tests del request de login.

---

## 📚 ARCHIVOS RELACIONADOS

- [SecurityConfig.java](src/main/java/com/barberia/config/SecurityConfig.java) - Configuración completa
- [AuthController.java](src/main/java/com/barberia/controllers/AuthController.java) - Endpoints de login
- [DemoController.java](src/main/java/com/barberia/controllers/DemoController.java) - Ejemplos protegidos
- [Barberia_API.postman_collection.json](Barberia_API.postman_collection.json) - Collection completa

---

**🎯 Resumen:** Usa modo `dev` para desarrollo rápido sin JWT, y modo `prod` cuando necesites probar la seguridad completa antes de conectar React.
