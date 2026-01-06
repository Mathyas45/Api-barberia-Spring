# 📊 Resumen de Permisos por Rol

Este documento muestra qué puede hacer cada rol en el sistema.

## 🎭 Roles Disponibles

### 👑 ADMIN (Administrador)
**Email:** admin@barberia.com  
**Password:** admin123

**Permisos:**
- ✅ READ_CLIENTS - Ver lista de clientes
- ✅ CREATE_CLIENTS - Crear nuevos clientes
- ✅ UPDATE_CLIENTS - Actualizar información de clientes
- ✅ DELETE_CLIENTS - Eliminar clientes
- ✅ READ_BOOKING - Ver reservas
- ✅ CREATE_BOOKING - Crear reservas
- ✅ MANAGE_BOOKING - Gestionar todas las reservas
- ✅ DELETE_USERS - Eliminar usuarios del sistema
- ✅ SPECIAL_ACCESS - Acceso a funciones especiales

**Endpoints accesibles:**
- ✅ `/api/demo/admin` - Solo para ADMIN
- ✅ `/api/demo/manager-or-admin` - ADMIN o MANAGER
- ✅ `/api/demo/read-clients` - Con permiso READ_CLIENTS
- ✅ `/api/demo/create-booking` - Con permiso CREATE_BOOKING
- ✅ `/api/demo/delete-user/{id}` - ADMIN + DELETE_USERS
- ✅ `/api/demo/special-access` - ADMIN directo
- ✅ Todos los demás endpoints protegidos

---

### 📊 MANAGER (Gerente)
**Email:** manager@barberia.com  
**Password:** manager123

**Permisos:**
- ✅ READ_CLIENTS - Ver lista de clientes
- ✅ CREATE_CLIENTS - Crear nuevos clientes
- ✅ UPDATE_CLIENTS - Actualizar información de clientes
- ✅ READ_BOOKING - Ver reservas
- ✅ CREATE_BOOKING - Crear reservas
- ✅ MANAGE_BOOKING - Gestionar reservas
- ❌ DELETE_CLIENTS - NO puede eliminar clientes
- ❌ DELETE_USERS - NO puede eliminar usuarios
- ❌ SPECIAL_ACCESS - NO tiene acceso especial

**Endpoints accesibles:**
- ❌ `/api/demo/admin` - Solo ADMIN
- ✅ `/api/demo/manager-or-admin` - ADMIN o MANAGER
- ✅ `/api/demo/read-clients` - Con permiso READ_CLIENTS
- ✅ `/api/demo/create-booking` - Con permiso CREATE_BOOKING
- ❌ `/api/demo/delete-user/{id}` - Requiere ADMIN + permiso
- ❌ `/api/demo/special-access` - Requiere ADMIN o (MANAGER + SPECIAL_ACCESS)
- ✅ Endpoints protegidos básicos

---

### 👤 USER (Usuario Normal)
**Email:** user@barberia.com  
**Password:** user123

**Permisos:**
- ✅ READ_CLIENTS - Ver lista de clientes
- ✅ READ_BOOKING - Ver reservas
- ✅ CREATE_BOOKING - Crear sus propias reservas
- ❌ CREATE_CLIENTS - NO puede crear clientes
- ❌ UPDATE_CLIENTS - NO puede actualizar clientes
- ❌ DELETE_CLIENTS - NO puede eliminar clientes
- ❌ MANAGE_BOOKING - NO puede gestionar todas las reservas
- ❌ DELETE_USERS - NO puede eliminar usuarios
- ❌ SPECIAL_ACCESS - NO tiene acceso especial

**Endpoints accesibles:**
- ❌ `/api/demo/admin` - Solo ADMIN
- ✅ `/api/demo/user` - Solo USER
- ❌ `/api/demo/manager-or-admin` - Requiere ADMIN o MANAGER
- ✅ `/api/demo/read-clients` - Con permiso READ_CLIENTS
- ✅ `/api/demo/create-booking` - Con permiso CREATE_BOOKING
- ❌ `/api/demo/delete-user/{id}` - Requiere ADMIN + permiso
- ❌ `/api/demo/special-access` - Sin permisos suficientes
- ✅ Endpoints protegidos básicos

---

### 🌟 SUPER USER (Múltiples Roles)
**Email:** super@barberia.com  
**Password:** super123

**Roles:** ADMIN + MANAGER

**Permisos:**
- ✅ TODOS los permisos de ADMIN
- ✅ TODOS los permisos de MANAGER
- ✅ Acceso a endpoints que requieren cualquiera de los dos roles

**Endpoints accesibles:**
- ✅ TODOS los endpoints (tiene rol ADMIN)

---

## 📋 Matriz de Acceso

| Endpoint | ADMIN | MANAGER | USER | Requiere |
|----------|-------|---------|------|----------|
| `/api/auth/login` | ✅ | ✅ | ✅ | Público |
| `/api/auth/register` | ✅ | ✅ | ✅ | Público |
| `/api/auth/public` | ✅ | ✅ | ✅ | Público |
| `/api/demo/protected` | ✅ | ✅ | ✅ | Autenticación |
| `/api/demo/admin` | ✅ | ❌ | ❌ | Rol ADMIN |
| `/api/demo/user` | ❌ | ❌ | ✅ | Rol USER |
| `/api/demo/manager-or-admin` | ✅ | ✅ | ❌ | ADMIN o MANAGER |
| `/api/demo/read-clients` | ✅ | ✅ | ✅ | Permiso READ_CLIENTS |
| `/api/demo/create-booking` | ✅ | ✅ | ✅ | Permiso CREATE_BOOKING |
| `/api/demo/delete-user/{id}` | ✅ | ❌ | ❌ | ADMIN + DELETE_USERS |
| `/api/demo/special-access` | ✅ | ❌ | ❌ | ADMIN o (MANAGER + SPECIAL_ACCESS) |

---

## 🧪 Casos de Prueba Recomendados

### Test 1: Admin accede a todo
```powershell
# Login como ADMIN
$body = @{ email = "admin@barberia.com"; password = "admin123" } | ConvertTo-Json
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body $body
$token = ($response.Content | ConvertFrom-Json).token

# Debería funcionar
Invoke-WebRequest -Uri "http://localhost:8080/api/demo/admin" -Headers @{ "Authorization" = "Bearer $token" }
Invoke-WebRequest -Uri "http://localhost:8080/api/demo/manager-or-admin" -Headers @{ "Authorization" = "Bearer $token" }
Invoke-WebRequest -Uri "http://localhost:8080/api/demo/read-clients" -Headers @{ "Authorization" = "Bearer $token" }
```

---

### Test 2: Manager no puede acceder a endpoints ADMIN
```powershell
# Login como MANAGER
$body = @{ email = "manager@barberia.com"; password = "manager123" } | ConvertTo-Json
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body $body
$token = ($response.Content | ConvertFrom-Json).token

# Debería FALLAR (403)
Invoke-WebRequest -Uri "http://localhost:8080/api/demo/admin" -Headers @{ "Authorization" = "Bearer $token" }

# Debería FUNCIONAR
Invoke-WebRequest -Uri "http://localhost:8080/api/demo/manager-or-admin" -Headers @{ "Authorization" = "Bearer $token" }
Invoke-WebRequest -Uri "http://localhost:8080/api/demo/read-clients" -Headers @{ "Authorization" = "Bearer $token" }
```

---

### Test 3: User tiene acceso limitado
```powershell
# Login como USER
$body = @{ email = "user@barberia.com"; password = "user123" } | ConvertTo-Json
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body $body
$token = ($response.Content | ConvertFrom-Json).token

# Debería FALLAR (403)
Invoke-WebRequest -Uri "http://localhost:8080/api/demo/admin" -Headers @{ "Authorization" = "Bearer $token" }
Invoke-WebRequest -Uri "http://localhost:8080/api/demo/manager-or-admin" -Headers @{ "Authorization" = "Bearer $token" }

# Debería FUNCIONAR
Invoke-WebRequest -Uri "http://localhost:8080/api/demo/user" -Headers @{ "Authorization" = "Bearer $token" }
Invoke-WebRequest -Uri "http://localhost:8080/api/demo/read-clients" -Headers @{ "Authorization" = "Bearer $token" }
Invoke-WebRequest -Uri "http://localhost:8080/api/demo/create-booking" -Method POST -Headers @{ "Authorization" = "Bearer $token" }
```

---

## 💡 Cómo Agregar Nuevos Permisos

### 1. Crear el Permiso en DataInitializer
```java
Permiso nuevoPermiso = createPermiso("NUEVO_PERMISO", "Descripción del permiso");
```

### 2. Asignar a un Rol
```java
Rol adminRole = createRole(
    "ADMIN",
    "Administrador",
    Set.of(readClients, createClients, nuevoPermiso)  // ← Agregar aquí
);
```

### 3. Proteger un Endpoint
```java
@GetMapping("/mi-endpoint")
@PreAuthorize("hasAuthority('NUEVO_PERMISO')")
public ResponseEntity<String> miEndpoint() {
    return ResponseEntity.ok("Acceso con nuevo permiso");
}
```

---

## 🔍 Debugging de Permisos

### Ver permisos del usuario actual
```java
@GetMapping("/mi-info")
public ResponseEntity<Map<String, Object>> miInfo() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    
    Map<String, Object> info = new HashMap<>();
    info.put("email", auth.getName());
    info.put("authorities", auth.getAuthorities());
    
    return ResponseEntity.ok(info);
}
```

### Verificar en la respuesta de login
```json
{
  "token": "...",
  "email": "admin@barberia.com",
  "roles": ["ADMIN"],
  "permissions": [
    "READ_CLIENTS",
    "CREATE_CLIENTS",
    "UPDATE_CLIENTS",
    "DELETE_CLIENTS",
    "READ_BOOKING",
    "CREATE_BOOKING",
    "MANAGE_BOOKING",
    "DELETE_USERS",
    "SPECIAL_ACCESS"
  ]
}
```

---

## ✅ Checklist de Testing por Rol

### ADMIN
- [ ] Puede hacer login
- [ ] Recibe todos los permisos en el token
- [ ] Puede acceder a `/api/demo/admin`
- [ ] Puede acceder a `/api/demo/manager-or-admin`
- [ ] Puede eliminar usuarios
- [ ] Tiene acceso especial

### MANAGER
- [ ] Puede hacer login
- [ ] Recibe permisos correctos (sin DELETE_USERS)
- [ ] NO puede acceder a `/api/demo/admin`
- [ ] Puede acceder a `/api/demo/manager-or-admin`
- [ ] Puede gestionar reservas
- [ ] NO puede eliminar usuarios

### USER
- [ ] Puede hacer login
- [ ] Recibe permisos básicos
- [ ] NO puede acceder a `/api/demo/admin`
- [ ] Puede acceder a `/api/demo/user`
- [ ] Puede leer clientes y reservas
- [ ] Puede crear reservas
- [ ] NO puede modificar ni eliminar

---

**📚 Archivos relacionados:**
- [SECURITY_GUIDE.md](SECURITY_GUIDE.md) - Teoría completa
- [TESTING_GUIDE.md](TESTING_GUIDE.md) - Guía de testing
- [README_PASO_A_PASO.md](README_PASO_A_PASO.md) - Revisión del código
