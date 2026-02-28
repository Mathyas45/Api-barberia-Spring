package com.barberia.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UsuarioUpdateRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    @NotEmpty(message = "Debe asignar al menos un rol")
    private List<Long> rolesIds;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    /**
     * Tipo de usuario: 1 = Interno, 2 = Cliente
     */
    private Integer tipoUsuario;
}
