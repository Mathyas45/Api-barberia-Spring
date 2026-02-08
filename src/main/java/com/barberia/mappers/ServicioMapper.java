package com.barberia.mappers;

import com.barberia.dto.servicio.ServicioRequest;
import com.barberia.dto.servicio.ServicioResponse;
import com.barberia.dto.servicio.ServicioUpdateRequest;
import com.barberia.models.Categoria;
import com.barberia.models.Negocio;
import com.barberia.models.Servicio;
import com.barberia.models.Usuario;
import com.barberia.repositories.CategoriaRepository;
import com.barberia.repositories.NegocioRepository;
import com.barberia.repositories.ServicioRepository;
import com.barberia.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
// sirve para que spring lo detecte como un bean es decir un componente gestionado por el contenedor de spring
public class ServicioMapper {

    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private CategoriaMapper categoriaMapper;

    public Servicio toEntity(ServicioRequest request) {
        Servicio servicio = new Servicio();
        servicio.setNombre(request.getNombre());
        servicio.setDescripcion(request.getDescripcion());
        Categoria categoria = categoriaRepository.findById(request.getCategoria())
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrado con ID: " + request.getCategoria()));
        servicio.setCategoria(categoria);
        servicio.setEstado(request.getEstado());
        servicio.setPrecio(request.getPrecio());
        servicio.setDuracionMinutosAprox(request.getDuracionMinutos());
        servicio.setRegEstado(1); // Por defecto activo
        
        return servicio;
    }

    public ServicioResponse toResponse(Servicio servicio) {
        ServicioResponse response = new ServicioResponse(); // esto sirve para crear una nueva instancia de la clase ServicioResponse, la instancia es un objeto que representa la respuesta del servicio en el sistema
        response.setId(servicio.getId());
        response.setEstado(servicio.isEstado());
        response.setNombre(servicio.getNombre());
        response.setDescripcion(servicio.getDescripcion());
        response.setCategoria(categoriaMapper.toResponse(servicio.getCategoria()));
        response.setPrecio(servicio.getPrecio());
        response.setRegEstado(servicio.getRegEstado());
        response.setDuracionMinutos(servicio.getDuracionMinutosAprox());
        if(servicio.getUsuarioRegistroId() != null) {
            response.setUsuarioRegistroId(servicio.getUsuarioRegistroId().getId());
        }
        if (servicio.getNegocio() != null) {
            response.setNegocioId(servicio.getNegocio().getId());
        }
        return response;
    }

    public Servicio updateEntity(Servicio servicio, ServicioUpdateRequest request) {
        servicio.setNombre(request.getNombre());
        servicio.setDescripcion(request.getDescripcion());
        Categoria categoria = categoriaRepository.findById(request.getCategoria())
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrado con ID: " + request.getCategoria()));
        servicio.setCategoria(categoria);
        servicio.setPrecio(request.getPrecio());
        servicio.setDuracionMinutosAprox(request.getDuracionMinutos());
        servicio.setEstado(request.getEstado());
        servicio.setRegEstado(2); // Por defecto actualizado
        

        return servicio;
    }

}
