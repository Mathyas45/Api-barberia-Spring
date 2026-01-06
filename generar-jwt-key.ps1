# ============================================
# SCRIPT PARA GENERAR CLAVE JWT SEGURA
# ============================================
# Este script genera una clave secreta aleatoria para JWT
# 
# USO:
# 1. Abre PowerShell en la carpeta del proyecto
# 2. Ejecuta: .\generar-jwt-key.ps1
# 3. Copia la clave generada
# 4. Pegala en el archivo .env en la linea JWT_SECRET_KEY=

Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "  GENERADOR DE CLAVE JWT" -ForegroundColor Cyan
Write-Host "============================================`n" -ForegroundColor Cyan

# Genera 64 bytes aleatorios
$bytes = New-Object byte[] 64
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)

# Convierte a Base64
$key = [Convert]::ToBase64String($bytes)

Write-Host "Clave JWT generada exitosamente!`n" -ForegroundColor Green

Write-Host "Longitud: $($key.Length) caracteres`n" -ForegroundColor Yellow

Write-Host "TU CLAVE JWT (copiala):" -ForegroundColor White
Write-Host "----------------------------------------" -ForegroundColor Gray
Write-Host $key -ForegroundColor Yellow
Write-Host "----------------------------------------`n" -ForegroundColor Gray

Write-Host "SIGUIENTE PASO:" -ForegroundColor Cyan
Write-Host "1. Abre el archivo .env" -ForegroundColor White
Write-Host "2. Busca la linea: JWT_SECRET_KEY=..." -ForegroundColor White
Write-Host "3. Reemplaza el valor con la clave de arriba`n" -ForegroundColor White

Write-Host "TIP: Esta clave es UNICA y PRIVADA." -ForegroundColor Magenta
Write-Host "   - Usala solo en TU entorno" -ForegroundColor Magenta
Write-Host "   - NO la compartas ni la subas a Git`n" -ForegroundColor Magenta

# Preguntar si quiere actualizar el .env automaticamente
Write-Host "Quieres actualizar el archivo .env automaticamente? (S/N): " -ForegroundColor Cyan -NoNewline
$respuesta = Read-Host

if ($respuesta -eq "S" -or $respuesta -eq "s" -or $respuesta -eq "Y" -or $respuesta -eq "y") {
    if (Test-Path ".env") {
        # Leer el contenido actual
        $contenido = Get-Content ".env" -Raw
        
        # Reemplazar la linea JWT_SECRET_KEY
        $nuevoContenido = $contenido -replace "JWT_SECRET_KEY=.*", "JWT_SECRET_KEY=$key"
        
        # Guardar
        $nuevoContenido | Set-Content ".env" -NoNewline
        
        Write-Host "`nArchivo .env actualizado correctamente!`n" -ForegroundColor Green
        Write-Host "Ahora puedes ejecutar: mvn spring-boot:run`n" -ForegroundColor White
    } else {
        Write-Host "`nNo se encontro el archivo .env" -ForegroundColor Red
        Write-Host "Crea el archivo .env primero`n" -ForegroundColor Yellow
    }
} else {
    Write-Host "`nOK, actualiza manualmente el archivo .env`n" -ForegroundColor Yellow
}

Write-Host "============================================`n" -ForegroundColor Cyan
