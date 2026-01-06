# Guía de Instalación de Java 21

## Problema Actual

Tu sistema no tiene Java instalado o configurado correctamente. Necesitas Java 21 para ejecutar este proyecto Spring Boot.

## Opción 1: Instalar Amazon Corretto 21 (Recomendado)

Amazon Corretto es una distribución gratuita de OpenJDK con soporte a largo plazo.

### Pasos:

1. **Descargar el instalador:**
   - Ir a: https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/downloads-list.html
   - Descargar el instalador MSI para Windows x64
   - Archivo: `amazon-corretto-21-x64-windows-jdk.msi`

2. **Instalar:**
   - Ejecutar el archivo MSI descargado
   - Marcar la opción "Add to PATH" durante la instalación
   - Marcar la opción "Set JAVA_HOME variable"
   - Completar la instalación

3. **Verificar instalación:**
   ```powershell
   # Abrir una NUEVA terminal PowerShell y ejecutar:
   java -version
   # Deberías ver algo como: openjdk version "21.x.x"
   
   $env:JAVA_HOME
   # Deberías ver la ruta de instalación, ejemplo: C:\Program Files\Amazon Corretto\jdk21.x.x_xxx
   ```

## Opción 2: Instalar Oracle OpenJDK 21

1. **Descargar:**
   - Ir a: https://jdk.java.net/21/
   - Descargar el archivo ZIP para Windows
   - Ejemplo: `openjdk-21.x.x_windows-x64_bin.zip`

2. **Instalar manualmente:**
   ```powershell
   # Extraer el ZIP a una ubicación, por ejemplo:
   # C:\Program Files\Java\jdk-21
   
   # Configurar JAVA_HOME permanentemente (ejecutar PowerShell como Administrador):
   [System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\Java\jdk-21', 'Machine')
   
   # Agregar al PATH (ejecutar PowerShell como Administrador):
   $currentPath = [System.Environment]::GetEnvironmentVariable('Path', 'Machine')
   [System.Environment]::SetEnvironmentVariable('Path', "$currentPath;%JAVA_HOME%\bin", 'Machine')
   ```

3. **Reiniciar la terminal** para que los cambios surtan efecto.

## Después de Instalar Java

Una vez que Java esté instalado correctamente:

1. **Verificar que funciona:**
   ```powershell
   # Abrir una NUEVA terminal PowerShell
   cd "c:\Barberia_proyecto\api-barberia"
   java -version
   $env:JAVA_HOME
   ```

2. **Compilar el proyecto:**
   ```powershell
   .\mvnw.cmd clean compile
   ```

3. **Ejecutar la aplicación:**
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

## Solución Temporal (Si no quieres reiniciar)

Si acabas de instalar Java y no quieres reiniciar la terminal, puedes configurar JAVA_HOME temporalmente:

```powershell
# Reemplaza la ruta con donde se instaló Java
$env:JAVA_HOME = "C:\Program Files\Amazon Corretto\jdk21.0.x_xxx"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Verificar
java -version

# Ahora intenta compilar
.\mvnw.cmd clean compile
```

## Notas Importantes

- **Usar Java 21:** Este proyecto requiere Java 21 específicamente (no versiones anteriores)
- **Reiniciar la terminal:** Después de instalar Java, DEBES abrir una nueva terminal
- **Verificar PATH:** Asegúrate de que tanto `java -version` como `$env:JAVA_HOME` funcionen
- **Maven Wrapper:** No necesitas instalar Maven, el proyecto usa Maven Wrapper (mvnw.cmd)

## ¿Qué es JAVA_HOME?

JAVA_HOME es una variable de entorno que apunta a la carpeta donde está instalado el JDK (Java Development Kit). Muchas herramientas como Maven la usan para encontrar Java.

## Solución de Problemas

### Error: "java -version" no funciona después de instalar

**Solución:**
1. Cierra TODAS las ventanas de PowerShell abiertas
2. Abre una NUEVA terminal PowerShell
3. Intenta de nuevo

### Error: "JAVA_HOME no está definido" después de instalar

**Solución:**
```powershell
# Configurar JAVA_HOME temporalmente (ajusta la ruta):
$env:JAVA_HOME = "C:\Program Files\Amazon Corretto\jdk21.0.5_11"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

Para hacerlo permanente, ejecuta PowerShell como **Administrador**:
```powershell
[System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\Amazon Corretto\jdk21.0.5_11', 'Machine')
```

## Una vez resuelto

Cuando Java esté instalado y funcionando:

1. El error "Implicitly declared classes are not supported at language level '21'" debería desaparecer
2. Podrás compilar: `.\mvnw.cmd clean compile`
3. Podrás ejecutar: `.\mvnw.cmd spring-boot:run`
4. El IDE (VS Code/IntelliJ) detectará Java automáticamente

## Recomendación

**Usa Amazon Corretto 21** - Es la opción más fácil porque el instalador MSI configura todo automáticamente (JAVA_HOME, PATH, etc.).
