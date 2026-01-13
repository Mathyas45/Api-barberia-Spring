package com.barberia.services;

import com.barberia.dto.Categoria.CategoriaRequest;
import com.barberia.dto.Categoria.CategoriaResponse;
import com.barberia.dto.EstadoRequestGlobal;
import com.barberia.mappers.CategoriaMapper;
import com.barberia.models.Categoria;
import com.barberia.repositories.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;


    public CategoriaService(CategoriaRepository categoriaRepository, CategoriaMapper categoriaMapper) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaMapper = categoriaMapper;
    }

    @Transactional
    public CategoriaResponse create(CategoriaRequest request) {
       Categoria categoria = categoriaMapper.toEntity(request);

        boolean  nombreProfesionaRepetido = categoriaRepository.existsByNombreAndRegEstadoNotEliminado(categoria.getNombre());
        if (nombreProfesionaRepetido) {
            throw new RuntimeException("El nombre de la categoria ya está registrado.");
        }

       Categoria nuevaCategoria = categoriaRepository.save(categoria);
       return categoriaMapper.toResponse(nuevaCategoria);
    }

    @Transactional(readOnly = true)
    public CategoriaResponse findById(Long id) {
        Categoria categoria = categoriaRepository.findById(id).orElseThrow(() -> new RuntimeException("Categoria no encontrado con ID: " + id));
        return categoriaMapper.toResponse(categoria);
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> findAll(String query) {
        List<Categoria> categorias;

        if (query != null && !query.isEmpty()) {
            categorias = categoriaRepository.findByNombreAndRegEstado(query, 0);
        } else {
            categorias = categoriaRepository.findByRegEstadoNot(0);
        }

        return categorias.stream()
                .map(categoriaMapper::toResponse)
                .collect(Collectors.toList());
    }
    @Transactional
    public CategoriaResponse update(Long id, CategoriaRequest request) {
        Categoria categoriaExistente = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrado con ID: " + id));

        categoriaMapper.updateEntity(categoriaExistente, request);

        Categoria categoriaActualizada = categoriaRepository.save(categoriaExistente);
        return categoriaMapper.toResponse(categoriaActualizada);
    }
    @Transactional
    public CategoriaResponse cambiarEstado(Long id, EstadoRequestGlobal regEstado) {
        Categoria categoriaExistente = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrado con ID: " + id));

        categoriaExistente.setRegEstado(regEstado.getRegEstado());

        Categoria categoriaActualizada = categoriaRepository.save(categoriaExistente);
        return categoriaMapper.toResponse(categoriaActualizada);
    }
    @Transactional
    public CategoriaResponse eliminar(Long id ) {
        Categoria categoriaExistente = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrado con ID: " + id));

        categoriaExistente.setRegEstado(0); // Establecer el estado a eliminado (0)
        Categoria categoriaActualizada = categoriaRepository.save(categoriaExistente);
        return categoriaMapper.toResponse(categoriaActualizada);
    }

}
