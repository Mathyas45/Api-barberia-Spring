# Solución: Error "Implicitly declared classes are not supported at language level '21'" en IntelliJ IDEA

## Problema

IntelliJ IDEA muestra el error en múltiples archivos:
```
Implicitly declared classes are not supported at language level '21'
```

Este error ocurre cuando IntelliJ no está configurado correctamente para usar Java 21.

## Solución 1: Configurar el SDK del Proyecto (MÁS COMÚN)

### Paso 1: Abrir la Configuración del Proyecto
1. En IntelliJ IDEA: `File` → `Project Structure...` (o `Ctrl + Alt + Shift + S`)

### Paso 2: Configurar el SDK
En la sección **Project**:
- **SDK:** Selecciona Java 21 (o "Download JDK..." si no está instalado)
- **Language level:** Selecciona **21 - Implicitly declared classes, patterns, etc.**

### Paso 3: Configurar los Módulos
En la sección **Modules**:
1. Selecciona tu módulo (api-barberia)
2. En la pestaña **Sources**:
   - **Language level:** Selecciona **21 - Implicitly declared classes, patterns, etc.**

### Paso 4: Aplicar y Cerrar
- Click en **Apply** → **OK**

## Solución 2: Configurar el Compilador de Java

1. `File` → `Settings` (o `Ctrl + Alt + S`)
2. Navega a: `Build, Execution, Deployment` → `Compiler` → `Java Compiler`
3. En **Project bytecode version:** Selecciona **21**
4. En **Per-module bytecode version:** Asegúrate que todos los módulos estén en **21**
5. Click en **Apply** → **OK**

## Solución 3: Configurar Maven en IntelliJ

1. Abre la vista **Maven** (panel lateral derecho)
2. Click derecho en el proyecto → **Reload project** (icono de refresh)
3. Si no funciona, intenta: **Ctrl + Shift + O** para reimportar Maven

## Solución 4: Invalidar Caché

Si las soluciones anteriores no funcionan:

1. `File` → `Invalidate Caches...`
2. Marca todas las opciones:
   - ✅ Clear file system cache and Local History
   - ✅ Clear downloaded shared indexes
   - ✅ Clear VCS Log caches and indexes
   - ✅ Invalidate and Restart
3. Click en **Invalidate and Restart**

## Verificación Final

Después de aplicar las configuraciones:

1. **Reimportar Maven:**
   - Abre la vista Maven
   - Click derecho en el proyecto → **Reload project**

2. **Recompilar:**
   - `Build` → `Rebuild Project`

3. **Verificar errores:**
   - Todos los errores de "Implicitly declared classes" deberían desaparecer

## Configuración Recomendada Completa

En `File` → `Project Structure...`:

```
PROJECT
├── SDK: Java 21 (corretto-21 o OpenJDK 21)
└── Language level: 21 - Implicitly declared classes, patterns, etc.

MODULES
└── api-barberia
    ├── Sources
    │   └── Language level: 21 - Implicitly declared classes, patterns, etc.
    └── Dependencies
        └── Module SDK: <Project SDK> (Java 21)
```

En `File` → `Settings` → `Build, Execution, Deployment` → `Compiler` → `Java Compiler`:

```
Project bytecode version: 21
Target bytecode version: 21

Per-module bytecode version:
  api-barberia: 21
```

## Si Java 21 no aparece en IntelliJ

### Opción A: Descargar desde IntelliJ
1. `File` → `Project Structure...` → `Platform Settings` → `SDKs`
2. Click en `+` → `Download JDK...`
3. Selecciona:
   - **Vendor:** Amazon Corretto
   - **Version:** 21
4. Click en **Download**

### Opción B: Agregar Java Instalado Manualmente
1. Instala Java 21 siguiendo la guía [INSTALAR_JAVA.md](INSTALAR_JAVA.md)
2. En IntelliJ: `File` → `Project Structure...` → `Platform Settings` → `SDKs`
3. Click en `+` → `Add JDK...`
4. Navega a la ubicación donde instalaste Java 21:
   - Ejemplo: `C:\Program Files\Amazon Corretto\jdk21.0.5_11`
5. Selecciona la carpeta y click en **OK**

## Notas Importantes

- **"Implicitly declared classes"** es una característica PREVIEW de Java 21
- IntelliJ necesita saber explícitamente que vas a usar Java 21
- El pom.xml ya está configurado correctamente, solo falta configurar el IDE
- VS Code puede que no muestre errores porque usa configuración diferente

## Resumen Rápido (Pasos Mínimos)

1. `Ctrl + Alt + Shift + S` (Project Structure)
2. **Project** → SDK: Java 21, Language level: 21
3. **Modules** → api-barberia → Sources → Language level: 21
4. **Apply** → **OK**
5. Maven panel → Click derecho → **Reload project**
6. `Build` → `Rebuild Project`

¡Listo! Los errores deberían desaparecer.

## Captura de Pantalla de Referencia

La configuración correcta debería verse así en Project Structure:

```
┌─ Project Settings ────────────────────────────────────┐
│ ▶ Project                                             │
│   Name: api-barberia                                  │
│   SDK: corretto-21 (Amazon Corretto version 21.x.x)   │
│   Language level: 21 - Implicitly declared classes... │
│                                                        │
│ ▶ Modules                                             │
│   └─ api-barberia                                     │
│      ├─ Sources                                       │
│      │  Language level: 21 - Implicitly declared...  │
│      └─ Dependencies                                  │
│         Module SDK: <Project SDK>                     │
└────────────────────────────────────────────────────────┘
```

## ¿Aún tienes problemas?

Si después de todos estos pasos sigues viendo errores, intenta:

1. Cierra IntelliJ completamente
2. Elimina la carpeta `.idea` en la raíz del proyecto
3. Elimina el archivo `*.iml` en la raíz del proyecto
4. Abre IntelliJ y vuelve a importar el proyecto como proyecto Maven
5. IntelliJ recreará la configuración desde cero usando el pom.xml
