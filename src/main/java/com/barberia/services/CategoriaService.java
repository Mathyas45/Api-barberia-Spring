package com.barberia.services;

import com.barberia.dto.Categoria.CategoriaRequest;
import com.barberia.dto.Categoria.CategoriaResponse;
import com.barberia.mappers.CategoriaMapper;
import com.barberia.models.Categoria;
import com.barberia.repositories.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


}
