package com.barberia.mappers;

import com.barberia.dto.Categoria.CategoriaRequest;
import com.barberia.dto.Categoria.CategoriaResponse;
import com.barberia.models.Categoria;
import com.barberia.models.Negocio;
import com.barberia.models.Usuario;
import com.barberia.repositories.NegocioRepository;
import com.barberia.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component// sirve para que spring lo detecte como un bean es decir un componente gestionado por el contenedor de spring
public class CategoriaMapper {

    public Categoria toEntity(CategoriaRequest request) {
        Categoria categoria = new Categoria(); // esto sirve para crear una nueva instancia de la clase Categoria, la instancia es un objeto que representa una categoria en el sistema
        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());
        categoria.setRegEstado(1); // Por defecto activo
        return categoria;
    }

    public CategoriaResponse toResponse(Categoria categoria) {
        CategoriaResponse response = new CategoriaResponse(); // esto sirve para crear una nueva instancia de la clase CategoriaResponse, la instancia es un objeto que representa la respuesta de la categoria en el sistema
        response.setId(categoria.getId());
        response.setNombre(categoria.getNombre());
        response.setDescripcion(categoria.getDescripcion());
        response.setCreatedAt(categoria.getCreatedAt());
        response.setUpdatedAt(categoria.getUpdatedAt());
        if(categoria.getUsuarioRegistroId() != null) {
            response.setUsuarioRegistroId(categoria.getUsuarioRegistroId().getId());
        }
        if (categoria.getNegocio() != null) {
            response.setNegocioId(categoria.getNegocio().getId());
        }
        response.setRegEstado(categoria.getRegEstado());
        return response;
    }

    public  Categoria updateEntity(Categoria categoria, CategoriaRequest request) {
        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());
        return categoria;
    }
}
