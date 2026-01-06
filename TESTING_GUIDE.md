# 🧪 Guía de Testing y Desarrollo

## 🎯 Dos Modos de Operación

### 1️⃣ Modo Desarrollo (SIN Seguridad)

Para pruebas iniciales sin complicarte con JWT.

**Activar:**

Agrega en `application.properties`:
```properties
spring.profiles.active=dev
```

**¿Qué hace?**
- ✅ Deshabilita JWT
- ✅ Todos los endpoints accesibles sin autenticación
- ✅ Perfecto para probar con Postman al inicio

**Ejemplo de prueba:**
```powershell
# GET sin autenticación
Invoke-WebRequest -Uri "http://localhost:8080/api/demo/admin" -Method GET

# POST sin autenticación
Invoke-WebRequest -Uri "http://localhost:8080/api/auth/register" `
    -Method POST `
    -ContentType "application/json" `
    -Body '{"name":"Test","email":"test@test.com","password":"123456"}'
```

---

### 2️⃣ Modo Producción (CON Seguridad JWT)

El modo normal con toda la seguridad implementada.

**Activar:**

Comenta o elimina la línea de `application.properties`:
```properties
# spring.profiles.active=dev
```

**¿Qué hace?**
- ✅ JWT obligatorio
- ✅ Endpoints protegidos según @PreAuthorize
- ✅ Autenticación y autorización completas

**Ejemplo de prueba:**
```powershell
# 1. Login
$body = @{
    email = "admin@barberia.com"
    password = "admin123"
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body

$token = ($response.Content | ConvertFrom-Json).token

# 2. Request con JWT
Invoke-WebRequest -Uri "http://localhost:8080/api/demo/admin" `
    -Method GET `
    -Headers @{ "Authorization" = "Bearer $token" }
```

---

## 📋 Guía de Testing Paso a Paso

### FASE 1: Testing Sin Seguridad (Desarrollo)

**Objetivo:** Probar que tus endpoints funcionan correctamente.

1. **Activa el perfil dev:**
   ```properties
   spring.profiles.active=dev
   ```

2. **Reinicia la aplicación:**
   ```bash
   mvn spring-boot:run
   ```

3. **Prueba endpoints libremente:**
   ```powershell
   # Registrar usuario
   Invoke-WebRequest -Uri "http://localhost:8080/api/auth/register" `
       -Method POST `
       -ContentType "application/json" `
       -Body '{"name":"Juan","email":"juan@test.com","password":"123456"}'
   
   # Acceder a endpoint protegido (sin JWT)
   Invoke-WebRequest -Uri "http://localhost:8080/api/demo/protected"
   
   # Acceder a endpoint admin (sin JWT)
   Invoke-WebRequest -Uri "http://localhost:8080/api/demo/admin"
   ```

4. **Verifica que todo funciona:**
   - ✅ Los endpoints responden correctamente
   - ✅ Puedes crear usuarios
   - ✅ Puedes acceder a cualquier endpoint

---

### FASE 2: Testing Con Seguridad (Producción)

**Objetivo:** Probar la autenticación y autorización.

1. **Desactiva el perfil dev:**
   ```properties
   # spring.profiles.active=dev
   ```

2. **Reinicia la aplicación:**
   ```bash
   mvn spring-boot:run
   ```

3. **Intenta acceder sin JWT (debe fallar):**
   ```powershell
   Invoke-WebRequest -Uri "http://localhost:8080/api/demo/protected"
   # ❌ Debe retornar 401 Unauthorized
   ```

4. **Haz login:**
   ```powershell
   $body = @{
       email = "admin@barberia.com"
       password = "admin123"
   } | ConvertTo-Json
   
   $response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
       -Method POST `
       -ContentType "application/json" `
       -Body $body
   
   $auth = $response.Content | ConvertFrom-Json
   $token = $auth.token
   
   Write-Host "Token obtenido: $token"
   ```

5. **Accede con JWT (debe funcionar):**
   ```powershell
   Invoke-WebRequest -Uri "http://localhost:8080/api/demo/protected" `
       -Method GET `
       -Headers @{ "Authorization" = "Bearer $token" }
   # ✅ Debe retornar 200 OK
   ```

6. **Prueba autorización por roles:**
   ```powershell
   # Con usuario ADMIN
   Invoke-WebRequest -Uri "http://localhost:8080/api/demo/admin" `
       -Headers @{ "Authorization" = "Bearer $token" }
   # ✅ Debe funcionar
   
   # Con usuario USER
   $bodyUser = @{
       email = "user@barberia.com"
       password = "user123"
   } | ConvertTo-Json
   
   $responseUser = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
       -Method POST `
       -ContentType "application/json" `
       -Body $bodyUser
   
   $tokenUser = ($responseUser.Content | ConvertFrom-Json).token
   
   Invoke-WebRequest -Uri "http://localhost:8080/api/demo/admin" `
       -Headers @{ "Authorization" = "Bearer $tokenUser" }
   # ❌ Debe retornar 403 Forbidden
   ```

---

## 🛠️ Testing con Postman

### Configuración Inicial

1. **Crea una colección:** "Barberia API"

2. **Crea una variable de entorno:**
   - Key: `token`
   - Value: (se llenará automáticamente)

### Requests

#### 1. Login

**Request:**
- Method: POST
- URL: `http://localhost:8080/api/auth/login`
- Body (JSON):
```json
{
  "email": "admin@barberia.com",
  "password": "admin123"
}
```

**Tests (para guardar el token automáticamente):**
```javascript
pm.test("Login exitoso", function () {
    pm.response.to.have.status(200);
    var jsonData = pm.response.json();
    pm.environment.set("token", jsonData.token);
});
```

#### 2. Endpoint Protegido

**Request:**
- Method: GET
- URL: `http://localhost:8080/api/demo/protected`
- Headers:
  - Key: `Authorization`
  - Value: `Bearer {{token}}`

#### 3. Endpoint Admin

**Request:**
- Method: GET
- URL: `http://localhost:8080/api/demo/admin`
- Headers:
  - Key: `Authorization`
  - Value: `Bearer {{token}}`

---

## 📊 Matriz de Testing

### Endpoints Públicos

| Endpoint | Método | Sin JWT | Con JWT | Resultado Esperado |
|----------|--------|---------|---------|-------------------|
| `/api/auth/login` | POST | ✅ | ✅ | 200 OK |
| `/api/auth/register` | POST | ✅ | ✅ | 201 Created |
| `/api/auth/public` | GET | ✅ | ✅ | 200 OK |

### Endpoints Protegidos

| Endpoint | Método | Sin JWT | Con JWT ADMIN | Con JWT USER | Resultado |
|----------|--------|---------|---------------|--------------|-----------|
| `/api/demo/protected` | GET | ❌ 401 | ✅ 200 | ✅ 200 | Cualquier autenticado |
| `/api/demo/admin` | GET | ❌ 401 | ✅ 200 | ❌ 403 | Solo ADMIN |
| `/api/demo/user` | GET | ❌ 401 | ❌ 403 | ✅ 200 | Solo USER |
| `/api/demo/read-clients` | GET | ❌ 401 | ✅ 200 | ✅ 200 | Con permiso |

---

## 🐛 Solución de Problemas

### Error: "401 Unauthorized" en todos los endpoints

**Causa:** Spring Security está activada pero no enviaste el JWT.

**Solución:**
1. Haz login: `POST /api/auth/login`
2. Copia el token de la respuesta
3. Agrégalo en el header: `Authorization: Bearer <token>`

---

### Error: "403 Forbidden" en endpoint específico

**Causa:** No tienes el rol o permiso requerido.

**Solución:**
1. Verifica el `@PreAuthorize` del endpoint
2. Verifica tus roles en la respuesta de login
3. Usa un usuario con los permisos correctos

---

### Error: "Cannot create bean 'securityFilterChain'"

**Causa:** Hay dos configuraciones de seguridad activas.

**Solución:**
- Si quieres modo dev: `spring.profiles.active=dev`
- Si quieres modo normal: Comenta esa línea

---

### No se crean usuarios de prueba

**Causa:** Ya hay datos en la base de datos.

**Solución:**
```sql
-- Eliminar todos los datos
DROP DATABASE barberia_db;
CREATE DATABASE barberia_db;

-- Reiniciar la aplicación
```

---

## 📝 Checklist de Testing

### ✅ Fase 1: Testing Básico (Sin Seguridad)

- [ ] La aplicación inicia correctamente
- [ ] Se crean las tablas en MySQL
- [ ] Se crean los usuarios de prueba
- [ ] Puedes acceder a `/api/auth/public`
- [ ] Puedes registrar un usuario
- [ ] Puedes acceder a endpoints protegidos sin JWT

### ✅ Fase 2: Testing de Autenticación

- [ ] Login con credenciales correctas retorna token
- [ ] Login con credenciales incorrectas retorna 401
- [ ] Token se puede decodificar en jwt.io
- [ ] Token contiene roles y permisos
- [ ] Sin token retorna 401 en endpoints protegidos
- [ ] Con token válido retorna 200 en endpoints protegidos

### ✅ Fase 3: Testing de Autorización

- [ ] Usuario ADMIN puede acceder a `/api/demo/admin`
- [ ] Usuario USER NO puede acceder a `/api/demo/admin` (403)
- [ ] Usuario con permiso puede acceder a endpoints específicos
- [ ] Usuario sin permiso recibe 403

### ✅ Fase 4: Testing de Errores

- [ ] Token expirado retorna 401
- [ ] Token inválido retorna 401
- [ ] Email duplicado en registro retorna 400
- [ ] Campos vacíos en login retornan 400
- [ ] Email inválido retorna 400

---

## 🎓 Recomendación de Aprendizaje

1. **Semana 1:** Prueba en modo dev sin seguridad
   - Familiarízate con los endpoints
   - Entiende el flujo de datos
   - No te preocupes por JWT aún

2. **Semana 2:** Activa la seguridad
   - Entiende el flujo de login
   - Aprende a usar JWT
   - Experimenta con diferentes roles

3. **Semana 3:** Profundiza en @PreAuthorize
   - Prueba diferentes combinaciones
   - Crea tus propios permisos
   - Entiende SpEL expressions

4. **Semana 4:** Lee el código fuente
   - Sigue el flujo completo
   - Entiende cada componente
   - Prepárate para entrevistas

---

**🎯 Con esta guía puedes:**
- ✅ Probar fácilmente sin autenticación al inicio
- ✅ Activar seguridad cuando estés listo
- ✅ Entender el comportamiento esperado
- ✅ Solucionar problemas comunes

**📚 Archivos relacionados:**
- [README.md](README.md) - Información general
- [SECURITY_GUIDE.md](SECURITY_GUIDE.md) - Teoría completa
- [README_PASO_A_PASO.md](README_PASO_A_PASO.md) - Revisión del código
