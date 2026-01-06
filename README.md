# 🔐 API Barbería - Spring Security con JWT

Sistema de autenticación y autorización profesional con Spring Boot 3, Java 21 y JWT.

## 🚀 Características

- ✅ Autenticación JWT stateless
- ✅ Roles y permisos granulares
- ✅ Protección de endpoints con @PreAuthorize
- ✅ Arquitectura limpia y profesional
- ✅ Código completamente comentado en español
- ✅ Documentación exhaustiva para aprendizaje
- ✅ Listo para portafolio y entrevistas

## 📋 Requisitos

- Java 21
- Maven 3.8+
- MySQL 8.0+
- IDE (IntelliJ IDEA / VS Code)

## 🛠️ Instalación

### 1. Clonar el repositorio
```bash
cd api-barberia
```

### 2. Configurar MySQL

Crea la base de datos (o déjala crear automáticamente):
```sql
CREATE DATABASE barberia_db;
```

Edita `src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=tu_password
```

### 3. Compilar y ejecutar
```bash
mvn clean install
mvn spring-boot:run
```

La aplicación iniciará en: `http://localhost:8080`

## 🧪 Pruebas Rápidas

### Login con PowerShell

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

# Acceder a endpoint protegido
Invoke-WebRequest -Uri "http://localhost:8080/api/demo/admin" `
    -Method GET `
    -Headers @{ "Authorization" = "Bearer $token" }
```

### Usuarios de Prueba

| Email | Password | Rol | Permisos |
|-------|----------|-----|----------|
| admin@barberia.com | admin123 | ADMIN | Todos |
| manager@barberia.com | manager123 | MANAGER | Gestión |
| user@barberia.com | user123 | USER | Básicos |
| super@barberia.com | super123 | ADMIN + MANAGER | Todos |

## 📁 Estructura del Proyecto

```
src/main/java/com/barberia/
├── models/               # Entidades JPA (Usuario, Rol, Permiso)
├── repositories/         # Repositorios Spring Data JPA
├── dto/                  # DTOs de Request/Response
├── security/             # Componentes de seguridad (JWT, Filtros)
├── config/               # Configuraciones (Security, Excepciones)
├── services/             # Lógica de negocio
├── controllers/          # Endpoints REST
└── utils/                # Utilidades (Inicializador de datos)
```

## 📚 Documentación

### 📖 [SECURITY_GUIDE.md](SECURITY_GUIDE.md)
Guía teórica completa con:
- Flujo detallado de autenticación
- Glosario de términos (Authentication, Authorization, JWT, Claims, etc.)
- Cómo funciona la validación de requests
- Buenas prácticas de la industria
- Preguntas típicas de entrevistas con respuestas

### 📖 [README_PASO_A_PASO.md](README_PASO_A_PASO.md)
Guía de revisión archivo por archivo con:
- Explicación de cada componente
- Conexiones entre archivos
- Checklist de revisión
- Orden de estudio recomendado
- Comandos de testing

## 🔑 Endpoints Principales

### Públicos (No requieren autenticación)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/auth/register` | Registrar nuevo usuario |
| POST | `/api/auth/login` | Iniciar sesión |
| GET | `/api/auth/public` | Endpoint público de prueba |

### Protegidos (Requieren JWT)

| Método | Endpoint | Requiere | Descripción |
|--------|----------|----------|-------------|
| GET | `/api/demo/protected` | Autenticación | Endpoint básico protegido |
| GET | `/api/demo/admin` | Rol ADMIN | Solo administradores |
| GET | `/api/demo/user` | Rol USER | Solo usuarios |
| GET | `/api/demo/read-clients` | Permiso READ_CLIENTS | Permiso específico |
| POST | `/api/demo/create-booking` | Permiso CREATE_BOOKING | Crear reserva |
| DELETE | `/api/demo/delete-user/{id}` | ADMIN + DELETE_USERS | Admin con permiso |

## 🎯 Tecnologías Utilizadas

- **Spring Boot 3.5.9** - Framework principal
- **Spring Security** - Autenticación y autorización
- **Spring Data JPA** - Persistencia de datos
- **MySQL** - Base de datos
- **JWT (jjwt 0.12.5)** - JSON Web Tokens
- **Lombok** - Reducción de boilerplate
- **BCrypt** - Encriptación de passwords

## 📖 Conceptos Implementados

### Spring Security
- SecurityFilterChain
- AuthenticationManager
- UserDetailsService
- PasswordEncoder (BCrypt)
- @PreAuthorize
- @EnableMethodSecurity
- Filtros personalizados

### JWT
- Generación de tokens
- Validación de firma
- Claims personalizados
- Stateless authentication

### Arquitectura
- Clean Architecture
- Separación de responsabilidades
- DTOs
- Repository Pattern
- Service Layer
- Global Exception Handler

## 🎓 Uso Educativo

Este proyecto está diseñado para:

1. **Aprender Spring Security** desde cero hasta nivel profesional
2. **Preparación para entrevistas** técnicas Java Backend
3. **Proyecto de portafolio** con buenas prácticas
4. **Referencia** para implementaciones futuras

## 🔍 Flujo de Autenticación

```
1. Cliente → POST /api/auth/login (email + password)
2. AuthController → AuthenticationService
3. AuthenticationManager valida credenciales
4. CustomUserDetailsService carga usuario desde BD
5. PasswordEncoder verifica password
6. JwtService genera token firmado
7. Cliente recibe token
8. Cliente envía token en header: Authorization: Bearer <token>
9. JwtAuthenticationFilter intercepta request
10. JwtService valida token
11. SecurityContext establece autenticación
12. @PreAuthorize verifica permisos
13. Controller ejecuta lógica
14. Cliente recibe respuesta
```

## 🛡️ Seguridad

### Implementado
- ✅ JWT con firma HMAC-SHA256
- ✅ Passwords encriptados con BCrypt
- ✅ Validación de inputs con Bean Validation
- ✅ Manejo global de excepciones
- ✅ CORS configurado
- ✅ CSRF deshabilitado (API stateless)

### Para Producción
- [ ] Cambiar clave secreta a variable de entorno
- [ ] Implementar refresh tokens
- [ ] Agregar rate limiting
- [ ] Configurar HTTPS
- [ ] Implementar auditoría
- [ ] Agregar logs de seguridad

## 🤝 Contribuciones

Este es un proyecto educativo. Siéntete libre de:
- Hacer fork
- Sugerir mejoras
- Reportar bugs
- Compartir

## 📧 Contacto

Si tienes preguntas sobre la implementación o necesitas ayuda, no dudes en contactarme.

## 📄 Licencia

Este proyecto es de código abierto y está disponible para uso educativo.

---

## 🎉 ¡Felicidades!

Has implementado un sistema de seguridad profesional con:
- Spring Security
- JWT stateless
- Roles y permisos
- Código limpio y documentado
- Arquitectura escalable

**Ahora estás listo para:**
- ✅ Usar este proyecto en tu portafolio
- ✅ Responder preguntas de entrevistas sobre Spring Security
- ✅ Implementar seguridad en proyectos reales
- ✅ Entender cómo funcionan las aplicaciones modernas

---

**⭐ Si te ha servido este proyecto, dale una estrella en GitHub**

**📚 Lee la documentación completa:**
- [SECURITY_GUIDE.md](SECURITY_GUIDE.md) - Teoría completa
- [README_PASO_A_PASO.md](README_PASO_A_PASO.md) - Guía de revisión

**🚀 ¡Éxito en tus proyectos y entrevistas!**
