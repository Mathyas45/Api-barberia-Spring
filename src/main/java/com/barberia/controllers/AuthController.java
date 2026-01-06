package com.barberia.controllers;

import com.barberia.dto.auth.AuthResponse;
import com.barberia.dto.auth.LoginRequest;
import com.barberia.dto.auth.RegisterRequest;
import com.barberia.services.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de autenticación
 * 
 * RESPONSABILIDAD:
 * Exponer endpoints públicos para login y registro
 * 
 * @RestController: Combina @Controller + @ResponseBody
 *                  Retorna JSON automáticamente
 * 
 * @RequestMapping: Define la ruta base para todos los endpoints
 * 
 * @CrossOrigin: Permite peticiones desde otros dominios (frontend)
 *               En producción, especifica los orígenes permitidos
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")  // En producción: origins = "https://tu-dominio.com"
public class AuthController {
    
    private final AuthenticationService authenticationService;
    
    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
    
    /**
     * Endpoint de registro de usuarios del sistema (MULTI-TENANT)
     * 
     * ⚠️ IMPORTANTE:
     * - Este endpoint NO es para registro público de clientes
     * - Es para que el ADMIN cree usuarios del panel administrativo
     * - Los clientes tienen su propio flujo de registro (separado)
     * 
     * MULTI-TENANT:
     * - Cada usuario debe pertenecer a un negocio (negocioId obligatorio)
     * - Los usuarios solo pueden gestionar datos de su propio negocio
     * 
     * POST /api/auth/register
     * 
     * Request Body (roleName Y negocioId OBLIGATORIOS):
     * {
     *   "name": "Juan Barbero",
     *   "email": "juan@barberia.com",
     *   "password": "123456",
     *   "roleName": "BARBERO",
     *   "negocioId": 1
     * }
     * 
     * El rol ADMIN debe existir en la BD (creado con script SQL inicial)
     * El negocio debe existir en la BD
     * 
     * Response:
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "type": "Bearer",
     *   "email": "juan@barberia.com",
     *   "name": "Juan Barbero",
     *   "roles": ["BARBERO"],
     *   "permissions": ["citas:leer", "citas:actualizar"]
     * }
     * 
     * ACCESO:
     * - En modo dev (SPRING_PROFILES_ACTIVE=dev): público (sin token, para pruebas)
     * - En modo prod: solo ADMIN (con token JWT)
     * 
     * SEGURIDAD MULTI-TENANT:
     * - Un ADMIN de negocio 1 solo puede crear usuarios para negocio 1
     * - No puede crear usuarios para otros negocios (validación en producción)
     * 
     * @Valid: Valida el request según las anotaciones en RegisterRequest
     * @param request Datos del nuevo usuario (name, email, password, roleName, negocioId)
     * @return Respuesta con token JWT
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Endpoint de login
     * 
     * POST /api/auth/login
     * 
     * Body:
     * {
     *   "email": "juan@example.com",
     *   "password": "123456"
     * }
     * 
     * Response:
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "type": "Bearer",
     *   "email": "juan@example.com",
     *   "name": "Juan Pérez",
     *   "roles": ["ADMIN"],
     *   "permissions": ["READ_CLIENTS", "CREATE_BOOKING"]
     * }
     * 
     * IMPORTANTE:
     * Si las credenciales son incorrectas, AuthenticationService lanza excepción
     * Deberías tener un @ControllerAdvice para manejar excepciones globalmente
     * 
     * @param request Credenciales del usuario
     * @return Respuesta con token JWT
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint de prueba público
     * 
     * GET /api/auth/public
     * 
     * No requiere autenticación
     * Útil para verificar que el servidor está funcionando
     * 
     * @return Mensaje de bienvenida
     */
    @GetMapping("/public")
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("Este es un endpoint público - No requiere autenticación");
    }
}
