# 🔐 Token JWT y Multi-Tenant: Flujo Automático

## 🎯 El Problema Resuelto

**Antes:** El frontend tenía que enviar `negocioId` manualmente en cada request.  
**Ahora:** El `negocioId` viene automáticamente del token JWT del usuario autenticado.

---

## 📊 Cómo Funciona

### 1. Login/Registro → Token JWT con negocioId

```javascript
// Request: Login
POST /api/auth/login
{
  "email": "admin@elestilo.com",
  "password": "admin123"
}

// Response
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "admin@elestilo.com",
  "name": "Admin Barbería El Estilo",
  "negocioId": 1,  ← GUARDAR EN LOCALSTORAGE
  "roles": ["ADMIN"],
  "permissions": [...]
}
```

### 2. Frontend Guarda el negocioId

```javascript
// En tu componente de login (React/Angular/Vue)
const response = await login(credentials);

// Guardar en localStorage
localStorage.setItem('token', response.token);
localStorage.setItem('negocioId', response.negocioId);  ← IMPORTANTE
localStorage.setItem('user', JSON.stringify({
  email: response.email,
  name: response.name,
  roles: response.roles
}));
```

### 3. JWT Token Contiene el negocioId

```json
// Decodificando el JWT (https://jwt.io)
{
  "sub": "admin@elestilo.com",
  "negocioId": 1,  ← CLAIM MULTI-TENANT
  "roles": ["ADMIN"],
  "permissions": ["usuarios:crear", "citas:leer", ...],
  "iat": 1735589250,
  "exp": 1735675650
}
```

---

## 🔄 Flujo en Producción

### Escenario 1: ADMIN crea un nuevo usuario

#### ❌ Antes (Manual - Solo para testing)

```javascript
// Frontend tenía que enviar negocioId manualmente
POST /api/auth/register
Authorization: Bearer <token>

{
  "name": "Juan Barbero",
  "email": "juan@elestilo.com",
  "password": "123456",
  "roleName": "BARBERO",
  "negocioId": 1  ← Usuario tenía que ponerlo manualmente
}
```

**Problema:** El usuario podía poner cualquier negocioId, incluso de otro negocio.

#### ✅ Ahora (Automático - Producción)

```javascript
// Frontend NO envía negocioId
// El backend lo extrae del token JWT automáticamente
POST /api/auth/register
Authorization: Bearer <token>

{
  "name": "Juan Barbero",
  "email": "juan@elestilo.com",
  "password": "123456",
  "roleName": "BARBERO"
  // negocioId NO se envía, viene del token
}
```

**Backend:**
```java
@PostMapping("/register")
public ResponseEntity<AuthResponse> register(
    @Valid @RequestBody RegisterRequest request,
    @AuthenticationPrincipal UserDetails userDetails
) {
    // Extraer negocioId del usuario autenticado
    Usuario usuarioAutenticado = usuarioRepository.findByEmail(userDetails.getUsername())
        .orElseThrow();
    
    // Usar el negocio del usuario autenticado
    Long negocioId = usuarioAutenticado.getNegocioId();
    
    // Crear usuario en el MISMO negocio
    return authenticationService.register(request, negocioId);
}
```

---

## 🛡️ Seguridad Multi-Tenant

### Validación Automática

```java
// El backend SIEMPRE valida:
// 1. El token es válido y no ha expirado
// 2. El negocioId viene del token (no se puede falsificar)
// 3. El usuario solo puede crear/ver datos de su propio negocio

@GetMapping("/usuarios")
public List<Usuario> listarUsuarios(@AuthenticationPrincipal UserDetails userDetails) {
    // Extraer negocioId del usuario autenticado
    Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
        .orElseThrow();
    
    // Solo retorna usuarios del MISMO negocio
    return usuarioRepository.findAllByNegocioId(usuario.getNegocioId());
}
```

### Ventajas de Seguridad

✅ **No se puede falsificar negocioId**
- Viene del token JWT firmado por el servidor
- Si alguien modifica el token, la firma se invalida

✅ **Aislamiento automático**
- El usuario solo ve datos de su negocio
- No puede acceder a datos de otros negocios

✅ **Consistencia garantizada**
- Todas las operaciones usan el mismo negocioId
- No hay riesgo de mezclar datos entre negocios

---

## 💻 Implementación en Frontend

### React Example

```typescript
// src/services/auth.service.ts
export const login = async (credentials: LoginRequest): Promise<AuthResponse> => {
  const response = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(credentials)
  });
  
  const data = await response.json();
  
  // Guardar en localStorage
  localStorage.setItem('token', data.token);
  localStorage.setItem('negocioId', data.negocioId.toString());
  
  return data;
};

// src/services/api.service.ts
export const createUsuario = async (request: CreateUsuarioRequest) => {
  const token = localStorage.getItem('token');
  // NO necesitamos enviar negocioId, viene del token automáticamente
  
  const response = await fetch('/api/auth/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`  ← negocioId está aquí
    },
    body: JSON.stringify(request)  // Sin negocioId
  });
  
  return response.json();
};

// src/components/Dashboard.tsx
const Dashboard = () => {
  const negocioId = localStorage.getItem('negocioId');
  
  return (
    <div>
      <h1>Barbería {negocioId === '1' ? 'El Estilo' : 'Corte Moderno'}</h1>
      {/* El usuario solo ve datos de su negocio */}
    </div>
  );
};
```

### Angular Example

```typescript
// auth.service.ts
@Injectable()
export class AuthService {
  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/login', credentials)
      .pipe(
        tap(response => {
          localStorage.setItem('token', response.token);
          localStorage.setItem('negocioId', response.negocioId.toString());
        })
      );
  }
}

// usuario.service.ts
@Injectable()
export class UsuarioService {
  createUsuario(request: CreateUsuarioRequest): Observable<Usuario> {
    // NO enviamos negocioId, el backend lo extrae del token
    return this.http.post<Usuario>('/api/auth/register', request);
  }
}

// http-interceptor.ts
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = localStorage.getItem('token');
    
    if (token) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`  ← negocioId incluido
        }
      });
    }
    
    return next.handle(req);
  }
}
```

---

## 🧪 Testing

### Modo Desarrollo (Postman)

Para pruebas en Postman, **puedes enviar negocioId manualmente**:

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "Test Usuario",
  "email": "test@test.com",
  "password": "123456",
  "roleName": "BARBERO",
  "negocioId": 1  ← OK en desarrollo para testing
}
```

### Modo Producción (Frontend)

En producción, el negocioId viene del token automáticamente:

```javascript
// Frontend NO envía negocioId
{
  "name": "Juan Barbero",
  "email": "juan@elestilo.com",
  "password": "123456",
  "roleName": "BARBERO"
  // negocioId se extrae del token JWT
}
```

---

## 📋 Checklist de Integración Frontend

- [ ] Guardar `negocioId` en localStorage al login/register
- [ ] Incluir token en header `Authorization: Bearer <token>` en todos los requests
- [ ] NO enviar `negocioId` manualmente en requests (viene del token)
- [ ] Mostrar nombre del negocio en el dashboard usando negocioId
- [ ] Limpiar localStorage al logout
- [ ] Validar que el token no ha expirado
- [ ] Redirigir al login si el token es inválido

---

## 🔍 Debugging

### Ver el contenido del JWT

```javascript
// En la consola del navegador
const token = localStorage.getItem('token');
const payload = JSON.parse(atob(token.split('.')[1]));
console.log('negocioId:', payload.negocioId);
console.log('roles:', payload.roles);
console.log('permissions:', payload.permissions);
```

### Verificar en https://jwt.io

1. Copia el token del localStorage
2. Ve a https://jwt.io
3. Pega el token en el campo "Encoded"
4. Verifica que el payload contenga `negocioId`

---

**El sistema ahora maneja negocioId automáticamente desde el token JWT** ✅
