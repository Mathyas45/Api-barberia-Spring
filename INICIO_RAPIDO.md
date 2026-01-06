# INICIO RAPIDO - 3 PASOS

## YA ESTA TODO LISTO! Solo sigue estos 3 pasos:

### PASO 1: Verifica que tienes estos archivos

```
✓ .env (ya creado - tiene tu clave JWT)
✓ application.yml (ya configurado)
✓ SecurityConfig.java (ya tiene perfiles dev/prod)
```

### PASO 2: Inicia la aplicacion

```powershell
mvn spring-boot:run
```

**Deberas ver:**
```
Clave JWT generada exitosamente!
JWT Service inicializado correctamente
Perfil activo: dev
MODO DESARROLLO: Seguridad JWT DESHABILITADA
Tomcat started on port 8080
```

### PASO 3: Prueba en Postman

**Login:**
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@barberia.com",
  "password": "admin123"
}
```

**Endpoint protegido SIN token (porque estas en modo dev):**
```
GET http://localhost:8080/api/demo/admin
```

✓ **Funciona sin Authorization header!**

---

## CAMBIAR ENTRE MODOS

### Modo DEV (actual - sin JWT)
```
En .env:
SPRING_PROFILES_ACTIVE=dev
```

### Modo PROD (con JWT para React)
```
En .env:
SPRING_PROFILES_ACTIVE=prod

Reinicia: mvn spring-boot:run
```

---

## DONDE ESTA TODO

### Archivo .env (configuracion)
```
c:\Barberia_proyecto\api-barberia\.env
```

**Contiene:**
- Base de datos (localhost, root, root)
- Clave JWT (ya generada)
- Perfil activo (dev o prod)

### Cambiar perfil

```powershell
notepad .env
```

Busca: `SPRING_PROFILES_ACTIVE=dev`
Cambia a: `SPRING_PROFILES_ACTIVE=prod`

---

## QUE SIGNIFICA CADA MODO

**dev** = Desarrollo
- Sin JWT
- Postman sin token
- CORS abierto
- Rapido para probar

**prod** = Produccion
- Con JWT obligatorio
- Necesitas login y token
- CORS restrictivo
- Para React o produccion

---

## USUARIOS DE PRUEBA

```
admin@barberia.com / admin123
manager@barberia.com / manager123
user@barberia.com / user123
```

---

## ERRORES COMUNES

### "JWT_SECRET_KEY no configurada"
```
✓ YA LO ARREGLAMOS
El script ya actualizo tu .env
```

### "Access denied for user 'root'"
```
Solucion:
1. notepad .env
2. Cambia DB_PASSWORD=tu_password_mysql
```

### "Postman da 403"
```
Estas en modo prod
Cambia a dev:
1. notepad .env
2. SPRING_PROFILES_ACTIVE=dev
3. mvn spring-boot:run
```

---

## TODO LISTO!

```powershell
mvn spring-boot:run
```

Abre Postman y prueba los endpoints!
