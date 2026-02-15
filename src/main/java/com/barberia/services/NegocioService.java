package com.barberia.services;

import com.barberia.dto.negocio.NegocioRequest;
import com.barberia.dto.negocio.NegocioResponse;
import com.barberia.mappers.NegocioMapper;
import com.barberia.models.Negocio;
import com.barberia.repositories.NegocioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NegocioService {

    private final NegocioRepository negocioRepository;
    private final NegocioMapper negocioMapper;

    public NegocioService(NegocioRepository negocioRepository, NegocioMapper negocioMapper) {
        this.negocioRepository = negocioRepository;
        this.negocioMapper = negocioMapper;
    }

    @Transactional
    public NegocioResponse create(NegocioRequest request) {
        // Verificar si el RUC ya existe
        if (request.getRuc() != null && negocioRepository.existsByRuc(request.getRuc())) {
            throw new RuntimeException("Ya existe un negocio con ese RUC");
        }

        Negocio negocio = negocioMapper.toEntity(request);
        Negocio nuevoNegocio = negocioRepository.save(negocio);
        return negocioMapper.toResponse(nuevoNegocio);
    }

    @Transactional(readOnly = true)
    public NegocioResponse findById(Long id) {
        Negocio negocio = negocioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Negocio no encontrado con ID: " + id));
        return negocioMapper.toResponse(negocio);
    }

    @Transactional(readOnly = true)
    public List<NegocioResponse> findAll(String query) {
        List<Negocio> negocios;

        if (query != null && !query.isBlank()) {
            negocios = negocioRepository.findByNombreContainingIgnoreCaseOrRucContainingIgnoreCase(query, query);
        } else {
            negocios = negocioRepository.findAll();
        }

        return negocios.stream()
                .map(negocioMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public NegocioResponse update(Long id, NegocioRequest request) {
        Negocio negocio = negocioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Negocio no encontrado con ID: " + id));

        // Verificar si el RUC ya existe en otro negocio
        if (request.getRuc() != null) {
            Negocio negocioConRuc = negocioRepository.findByRuc(request.getRuc());
            if (negocioConRuc != null && !negocioConRuc.getId().equals(id)) {
                throw new RuntimeException("Ya existe otro negocio con ese RUC");
            }
        }

        negocioMapper.updateEntity(negocio, request);
        Negocio negocioActualizado = negocioRepository.save(negocio);
        return negocioMapper.toResponse(negocioActualizado);
    }

    @Transactional
    public void delete(Long id) {
        Negocio negocio = negocioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Negocio no encontrado con ID: " + id));
        negocio.setEstado("INACTIVO");
        negocioRepository.save(negocio);
    }

    @Transactional
    public void activate(Long id) {
        Negocio negocio = negocioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Negocio no encontrado con ID: " + id));
        negocio.setEstado("ACTIVO");
        negocioRepository.save(negocio);
    }
}
