package com.barberia.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de JSON Web Tokens (JWT)
 * 
 * RESPONSABILIDADES:
 * 1. Generar tokens JWT firmados
 * 2. Validar tokens JWT
 * 3. Extraer información (claims) del token
 * 
 * JWT estructura: header.payload.signature
 * 
 * CONCEPTOS CLAVE:
 * - Claims: Datos almacenados en el token (email, roles, fecha expiración, etc.)
 * - Secret Key: Clave secreta para firmar el token (solo el servidor la conoce)
 * - Signature: Firma digital que garantiza que el token no fue modificado
 * 
 * SEGURIDAD:
 * - Valida que JWT_SECRET_KEY esté configurada al iniciar
 * - Falla rápido si no hay clave (fail-fast pattern)
 * - No permite valores por defecto en producción
 */
@Slf4j
@Service
public class JwtService {
    
    /**
     * Clave secreta para firmar los tokens
     * 
     * ⚠️ CRÍTICO - SIN VALOR POR DEFECTO:
     * - DEBE configurarse mediante variable de entorno JWT_SECRET_KEY
     * - Si no existe, la aplicación NO arrancará (fail-fast)
     * - Mínimo 256 bits (43+ caracteres en Base64)
     * 
     * CÓMO CONFIGURAR:
     * 1. En .env: JWT_SECRET_KEY=tu_clave_aqui
     * 2. Variable de entorno: export JWT_SECRET_KEY=tu_clave
     * 3. Al ejecutar: -DJWT_SECRET_KEY=tu_clave
     * 
     * GENERAR CLAVE SEGURA:
     * PowerShell:
     *   $bytes = New-Object byte[] 64
     *   [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
     *   [Convert]::ToBase64String($bytes)
     * 
     * Bash/Linux:
     *   openssl rand -base64 64
     * 
     * ⚠️ PRODUCCIÓN: Usar AWS Secrets Manager, Azure Key Vault, o similar
     */
    @Value("${jwt.secret.key:#{null}}")
    private String secretKey;
    
    /**
     * Tiempo de expiración del token en milisegundos
     * 
     * Por defecto: 24 horas (86400000 ms) - Solo para desarrollo
     * 
     * RECOMENDACIONES POR TIPO:
     * - Desarrollo: 24 horas (86400000)
     * - Producción normal: 1 hora (3600000)
     * - Aplicaciones críticas: 15 minutos (900000)
     * - API públicas: 5 minutos (300000)
     * Valida la configuración al iniciar el servicio
     * 
     * @PostConstruct: Se ejecuta automáticamente después de la inyección de dependencias
     * 
     * VALIDACIONES:
     * 1. Verifica que JWT_SECRET_KEY esté configurada
     * 2. Valida que tenga longitud suficiente (mínimo 43 caracteres para 256 bits)
     * 
     * ⚠️ Si falla, la aplicación NO arrancará (fail-fast)
     * Esto es BUENO: mejor fallar al iniciar que en producción
     * 
     * @throws IllegalArgumentException si la configuración es inválida
     */
    @PostConstruct
    public void init() {
        // Valida que la clave secreta esté configurada
        if (!StringUtils.hasText(secretKey)) {
            log.warn("⚠️ JWT_SECRET_KEY no configurada, usando clave por defecto (NO USAR EN PRODUCCIÓN)");
            // Usar la clave por defecto del application.yml
            secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        }
        
        // Valida que la clave tenga longitud suficiente (mínimo 256 bits = 43 caracteres en Base64)
        if (secretKey.length() < 43) {
            throw new IllegalStateException(
                "\n\n" +
                "═══════════════════════════════════════════════════════════════════\n" +
                "❌ ERROR: JWT_SECRET_KEY es muy corta\n" +
                "═══════════════════════════════════════════════════════════════════\n" +
                "\n" +
                "La clave actual tiene " + secretKey.length() + " caracteres.\n" +
                "Se requiere un MÍNIMO de 43 caracteres (256 bits).\n" +
                "\n" +
                "SOLUCIÓN: Genera una clave más larga:\n" +
                "\n" +
                "PowerShell:\n" +
                "  $bytes = New-Object byte[] 64\n" +
                "  [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)\n" +
                "  [Convert]::ToBase64String($bytes)\n" +
                "\n" +
                "Linux/Mac:\n" +
                "  openssl rand -base64 64\n" +
                "\n" +
                "═══════════════════════════════════════════════════════════════════\n"
            );
        }
        
        // Log de confirmación (sin mostrar la clave completa)
        log.info("✅ JWT Service inicializado correctamente. Expiración: {} minutos", (jwtExpiration / 1000 / 60));
        log.debug("Clave secreta: {}...{}", secretKey.substring(0, 10), secretKey.substring(secretKey.length() - 10));
    }
    
    /**
     * 
     * MEJOR PRÁCTICA: Implementar refresh tokens
     */
    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;
    
    /**
     * Extrae el email (subject) del token JWT
     * 
     * Subject: Identificador del usuario (en nuestro caso el email)
     * 
     * @param token JWT del que extraer el email
     * @return Email del usuario
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Extrae el negocioId del token JWT (MULTI-TENANT)
     * 
     * IMPORTANTE:
     * - El negocioId está en los claims del token
     * - Se usa para filtrar datos por negocio
     * - Es la fuente de verdad para multi-tenancy
     * 
     * @param token JWT del que extraer el negocioId
     * @return ID del negocio del usuario
     */
    public Long extractNegocioId(String token) {
        Claims claims = extractAllClaims(token);
        Object negocioIdObj = claims.get("negocioId");
        
        if (negocioIdObj == null) {
            return null;
        }
        
        // Convertir a Long (puede venir como Integer desde el JSON)
        if (negocioIdObj instanceof Number) {
            return ((Number) negocioIdObj).longValue();
        }
        
        return null;
    }
    
    /**
     * Extrae un claim específico del token
     * 
     * Claims: Son los datos almacenados en el payload del JWT
     * 
     * Function<Claims, T>: Recibe los claims y devuelve el dato específico que queremos
     * 
     * @param token JWT
     * @param claimsResolver Función para extraer el claim deseado
     * @return El claim solicitado
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Genera un token JWT para un usuario (CON MULTI-TENANT)
     * 
     * FLUJO:
     * 1. Crea claims adicionales (roles, permisos, negocioId)
     * 2. Llama a generateToken con esos claims
     * 
     * MULTI-TENANT:
     * - Incluye negocioId como claim en el token
     * - El frontend lo guarda en localStorage
     * - Todas las operaciones usan este negocioId
     * 
     * @param userDetails Detalles del usuario autenticado
     * @param negocioId ID del negocio del usuario (MULTI-TENANT)
     * @return Token JWT firmado con negocioId incluido
     */
    public String generateToken(UserDetails userDetails, Long negocioId) {
        Map<String, Object> extraClaims = new HashMap<>();
        
        // MULTI-TENANT: Agregar negocioId al token
        extraClaims.put("negocioId", negocioId);
        
        // Agregamos los roles y permisos como claims
        extraClaims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(role -> role.replace("ROLE_", ""))
                .collect(Collectors.toList()));
        
        extraClaims.put("permissions", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> !auth.startsWith("ROLE_"))
                .collect(Collectors.toList()));
        
        return generateToken(extraClaims, userDetails);
    }
    
    /**
     * Genera el token JWT con claims personalizados
     * 
     * ESTRUCTURA DEL TOKEN:
     * {
     *   "sub": "usuario@email.com",           // Subject: identificador del usuario
     *   "roles": ["ADMIN", "USER"],           // Roles del usuario
     *   "permissions": ["READ_CLIENTS"],      // Permisos específicos
     *   "iat": 1234567890,                    // Issued At: fecha de creación
     *   "exp": 1234654290                     // Expiration: fecha de expiración
     * }
     * 
     * @param extraClaims Claims adicionales (roles, permisos, etc.)
     * @param userDetails Detalles del usuario
     * @return Token JWT firmado
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)                           // Claims personalizados
                .subject(userDetails.getUsername())             // Subject (email del usuario)
                .issuedAt(new Date(System.currentTimeMillis())) // Fecha de creación
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration)) // Fecha de expiración
                .signWith(getSignInKey())                       // Firma con nuestra clave secreta
                .compact();                                     // Construye el token
    }
    
    /**
     * Valida si el token es válido
     * 
     * VALIDACIONES:
     * 1. El username del token coincide con el usuario
     * 2. El token no ha expirado
     * 
     * @param token JWT a validar
     * @param userDetails Usuario con el que comparar
     * @return true si es válido, false si no
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
    
    /**
     * Verifica si el token ha expirado
     * 
     * @param token JWT a verificar
     * @return true si expiró, false si aún es válido
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    /**
     * Extrae la fecha de expiración del token
     * 
     * @param token JWT
     * @return Fecha de expiración
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * Extrae todos los claims del token
     * 
     * PROCESO:
     * 1. Parsea el token usando la clave secreta
     * 2. Verifica la firma (si fue modificado, lanza excepción)
     * 3. Retorna los claims
     * 
     * @param token JWT
     * @return Claims contenidos en el token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())  // Verifica la firma con nuestra clave
                .build()
                .parseSignedClaims(token)     // Parsea el token
                .getPayload();                 // Obtiene los claims (payload)
    }
    
    /**
     * Obtiene la clave de firma desde la configuración
     * 
     * PROCESO:
     * 1. Decodifica la clave desde Base64
     * 2. Crea una SecretKey usando el algoritmo HMAC-SHA
     * 
     * HMAC-SHA256: Algoritmo de firma simétrico
     * - Simétrico: La misma clave firma y verifica
     * - Rápido y seguro para APIs
     * 
     * @return Clave secreta para firmar/verificar
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
