# 🚀 CONFIGURACIÓN INICIAL - Guía Rápida

## 📋 PASO A PASO PARA EMPEZAR

### ✅ Paso 1: Generar clave JWT

Abre PowerShell en la carpeta del proyecto y ejecuta:

```powershell
.\generar-jwt-key.ps1
```

**¿Qué hace este script?**
- Genera una clave secreta aleatoria de 512 bits
- Te la muestra en pantalla
- Opcionalmente, actualiza el archivo `.env` automáticamente

**Ejemplo de salida:**
```
============================================
  GENERADOR DE CLAVE JWT
============================================

✅ Clave JWT generada exitosamente!

Longitud: 88 caracteres

TU CLAVE JWT (cópiala):
----------------------------------------
K9mN2pQ7rS1tU4vW5xY6zA0bC1dE2fF3gG4hH5iI6jJ7kK8lL9mM0nN1oO2pP3qQ4rR5sS6tT7uU8vV9wW0xX1yY2zA3bB4
----------------------------------------
```

### ✅ Paso 2: Verificar el archivo .env

Ya está creado en: `c:\Barberia_proyecto\api-barberia\.env`

**Contenido:**
```bash
# BASE DE DATOS
DB_HOST=localhost
DB_PORT=3306
DB_NAME=barberia_db
DB_USERNAME=root
DB_PASSWORD=root

# JWT (CLAVE GENERADA EN EL PASO 1)
JWT_SECRET_KEY=tu_clave_aqui
JWT_EXPIRATION=86400000

# PERFIL ACTIVO
# dev  = Sin JWT (Postman sin token)
# prod = Con JWT (producción)
SPRING_PROFILES_ACTIVE=dev
```

**Edita solo si es necesario:**
- `DB_PASSWORD` → Tu contraseña de MySQL
- `JWT_SECRET_KEY` → Clave generada en Paso 1 (si el script no la actualizó)

### ✅ Paso 3: Verificar que application.yml usa las variables

Abre `src/main/resources/application.yml`

Debe tener:
```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:barberia_db}?...
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:root}

jwt:
  secret:
    key: ${JWT_SECRET_KEY:#{null}}
  expiration: ${JWT_EXPIRATION:86400000}
```

**✅ Si tiene esto, está bien configurado.**

### ✅ Paso 4: Iniciar la aplicación

```powershell
mvn spring-boot:run
```

**Deberías ver:**
```
✅ JWT Service inicializado correctamente
   Clave secreta: K9mN2pQ7rS...wW0xX1yY2zA3bB4
   Expiración: 1440 minutos

✅ Perfil activo: dev
⚠️  MODO DESARROLLO: Seguridad JWT DESHABILITADA

Tomcat started on port 8080
```

### ✅ Paso 5: Probar en Postman

**Login (devuelve token pero no lo necesitas en modo dev):**
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@barberia.com",
  "password": "admin123"
}
```

**Endpoint protegido SIN token:**
```http
GET http://localhost:8080/api/demo/admin
```

✅ **Funciona sin Authorization header porque estás en modo `dev`**

---

## 🔄 CAMBIAR ENTRE MODOS

### Modo DEV (sin JWT - para Postman)

**En .env:**
```bash
SPRING_PROFILES_ACTIVE=dev
```

**Reinicia:**
```powershell
mvn spring-boot:run
```

**Postman:**
```
✅ Todos los endpoints funcionan sin token
```

### Modo PROD (con JWT - para React/Producción)

**En .env:**
```bash
SPRING_PROFILES_ACTIVE=prod
```

**Reinicia:**
```powershell
mvn spring-boot:run
```

**Postman:**
```
1. POST /api/auth/login → Obtener token
2. Copiar token
3. Authorization: Bearer <token>
```

---

## 📊 ¿QUÉ ES CADA COSA?

### `.env` - Variables de Entorno
```
Archivo con configuración sensible
├── DB_PASSWORD = Tu contraseña de MySQL
├── JWT_SECRET_KEY = Clave para firmar tokens
└── SPRING_PROFILES_ACTIVE = Modo (dev/prod)

⚠️ NO se sube a Git (está en .gitignore)
```

### `SPRING_PROFILES_ACTIVE` - Perfil Activo
```
dev  → Sin JWT (Postman fácil)
prod → Con JWT (Seguridad completa)
```

Spring Boot lee este valor y activa el código correspondiente:

```java
@Profile("dev")    // ← Se ejecuta solo si SPRING_PROFILES_ACTIVE=dev
public SecurityFilterChain dev() {
    // Sin JWT
}

@Profile("!dev")   // ← Se ejecuta si NO es dev (es prod)
public SecurityFilterChain prod() {
    // Con JWT
}
```

### `JWT_SECRET_KEY` - Clave Secreta
```
¿Qué es?
Una clave aleatoria que firma los tokens JWT

¿Por qué generarla?
Para que nadie más pueda crear tokens válidos

¿Cómo se usa?
- Login → Genera token firmado con esta clave
- Request → Valida que el token fue firmado con esta clave
```

---

## ❓ PREGUNTAS FRECUENTES

### **¿Por qué no puedo usar la clave que viene por defecto?**
Porque es insegura. Si subes tu código a Git, todos verían la clave.

### **¿Cada cuánto debo generar una nueva clave?**
- Desarrollo: Una vez (la que generaste ahora)
- Producción: Cada deploy importante o si sospechas filtración

### **¿Qué pasa si pierdo la clave?**
Genera una nueva con el script. Los tokens existentes dejarán de funcionar.

### **¿Dónde está el archivo .env?**
En la raíz del proyecto: `c:\Barberia_proyecto\api-barberia\.env`

### **¿Por qué no veo el archivo .env en VS Code?**
Está oculto por defecto. Busca en Windows Explorer o usa:
```powershell
notepad .env
```

### **¿Qué es "!dev" en @Profile?**
Es una negación: "cualquier perfil que NO sea dev"
- Si `SPRING_PROFILES_ACTIVE=dev` → NO se ejecuta
- Si `SPRING_PROFILES_ACTIVE=prod` → SÍ se ejecuta
- Si no hay perfil → SÍ se ejecuta (default)

---

## 🔧 SOLUCIÓN DE PROBLEMAS

### Error: "JWT_SECRET_KEY no está configurada"
```
Solución:
1. .\generar-jwt-key.ps1
2. Copiar la clave
3. Pegar en .env en JWT_SECRET_KEY=
```

### Error: "Cannot load driver class: com.mysql.cj.jdbc.Driver"
```
Solución:
1. Verifica que MySQL esté instalado
2. Verifica el pom.xml tenga mysql-connector-java
```

### Error: "Access denied for user 'root'@'localhost'"
```
Solución:
1. Abre .env
2. Cambia DB_PASSWORD=tu_contraseña_real
```

### La app arranca pero Postman da 403
```
Solución:
Estás en modo prod. Cambia a dev:
1. Abre .env
2. SPRING_PROFILES_ACTIVE=dev
3. Reinicia: mvn spring-boot:run
```

---

## 📁 ESTRUCTURA DE ARCHIVOS

```
api-barberia/
├── .env                          ← TUS variables (NO subir a Git)
├── .env.example                  ← Plantilla (SÍ sube a Git)
├── generar-jwt-key.ps1          ← Script para generar clave
├── CONFIGURACION_INICIAL.md     ← Esta guía
│
├── src/main/resources/
│   └── application.yml          ← Configuración que usa .env
│
└── src/main/java/com/barberia/
    └── config/
        └── SecurityConfig.java  ← Lee SPRING_PROFILES_ACTIVE
```

---

## ✅ CHECKLIST

Antes de empezar a desarrollar:

```
✅ Archivo .env existe
✅ JWT_SECRET_KEY generada con el script
✅ DB_PASSWORD configurada
✅ SPRING_PROFILES_ACTIVE=dev (para Postman)
✅ mvn spring-boot:run funciona
✅ Postman puede hacer login
✅ Postman puede acceder a /api/demo/admin sin token
```

---

## 🎯 SIGUIENTE PASO

```powershell
# 1. Genera tu clave JWT
.\generar-jwt-key.ps1

# 2. (El script actualizará .env automáticamente)

# 3. Inicia la aplicación
mvn spring-boot:run

# 4. Abre Postman y prueba
POST http://localhost:8080/api/auth/login
Body: { "email": "admin@barberia.com", "password": "admin123" }

GET http://localhost:8080/api/demo/admin
# ✅ SIN Authorization header
```

---

**🎉 ¡Listo! Ahora todo está configurado y entendible.**
