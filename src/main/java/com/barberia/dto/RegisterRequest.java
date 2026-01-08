package com.barberia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la solicitud de registro de usuario del sistema (MULTI-TENANT)
 * 
 * IMPORTANTE:
 * - Este endpoint NO es para registro público de clientes
 * - Es para que el ADMIN cree usuarios del panel administrativo (barberos, recepcionistas, etc.)
 * - Los clientes tienen su propio flujo de registro
 * 
 * MULTI-TENANT:
 * - Cada usuario debe pertenecer a un negocio (negocioId obligatorio)
 * - Los usuarios solo pueden gestionar datos de su propio negocio
 * 
 * LÓGICA DE ROL:
 * - El campo roleName es OBLIGATORIO en TODOS los casos
 * - El rol ADMIN ya existe en la BD (creado con script SQL inicial)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String name;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
    
    /**
     * ID del rol a asignar (OBLIGATORIO)
     * 
     * El rol debe existir previamente en la base de datos.
     * Ejemplo: 1 = ADMIN, 2 = MANAGER, 3 = USER
     * 
     * Es responsabilidad del frontend obtener la lista de roles disponibles
     * desde el endpoint GET /api/roles y enviar el ID correcto.
     */
    @NotNull(message = "El ID del rol es obligatorio")
    private Long rolId;
    
    /**
     * ID del negocio al que pertenece el usuario (MULTI-TENANT)
     * 
     * OBLIGATORIO:
     * - Todo usuario debe pertenecer a un negocio
     * - Define qué barbería puede gestionar este usuario
     * 
     * EJEMPLOS:
     * - negocioId = 1 → Usuario de "Barbería El Estilo"
     * - negocioId = 2 → Usuario de "Barbería Corte Moderno"
     * 
     * SEGURIDAD:
     * - Un ADMIN de negocio 1 solo puede crear usuarios para negocio 1
     * - No puede crear usuarios para otros negocios
     */
    @NotNull(message = "El negocioId es obligatorio")
    private Long negocioId;
}

