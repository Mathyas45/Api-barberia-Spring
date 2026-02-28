package com.barberia.services;

import com.barberia.dto.EstadoRequestGlobal;
import com.barberia.dto.Profesional.ProfesionalRequest;
import com.barberia.dto.Profesional.ProfesionalResponse;
import com.barberia.mappers.ProfesionalMapper;
import com.barberia.models.Profesional;
import com.barberia.repositories.ProfesionalRepository;
import com.barberia.services.common.FechaService;
import com.barberia.services.common.SecurityContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ProfesionalService {
    private final ProfesionalRepository profesionalRepository;
    private final ProfesionalMapper profesionalMapper;
    private final FechaService fechaService;
    private final SecurityContextService securityContextService;
    private final FileStorageService fileStorageService;


    public ProfesionalService(ProfesionalRepository profesionalRepository, ProfesionalMapper profesionalMapper, FechaService fechaService, SecurityContextService securityContextService, FileStorageService fileStorageService) {
        this.profesionalRepository = profesionalRepository;
        this.profesionalMapper = profesionalMapper;
        this.fechaService = fechaService;
        this.securityContextService = securityContextService;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public ProfesionalResponse create(ProfesionalRequest request, MultipartFile foto) {
        Long negocioId = securityContextService.getNegocioIdFromContext();
        Profesional profesional = profesionalMapper.toEntity(request);
        
        // MULTI-TENANT: Verificar nombre único dentro del mismo negocio
        boolean nombreProfesionalRepetido = profesionalRepository.existsByNombreAndRegEstadoNotEliminadoAndNegocioId(profesional.getNombreCompleto(), negocioId);
        if (nombreProfesionalRepetido) {
            throw new RuntimeException("El nombre del profesional ya está registrado.");
        }

        // Guardar foto si se envió
        if (foto != null && !foto.isEmpty()) {
            String fotoUrl = fileStorageService.saveImage(foto, "profesionales");
            profesional.setFotoUrl(fotoUrl);
        }

        Profesional nuevoProfesional = profesionalRepository.save(profesional);
        return profesionalMapper.toResponse(nuevoProfesional);
    }

    @Transactional(readOnly = true)
    public ProfesionalResponse findById(Long id) {
        Profesional profesional = profesionalRepository.findById(id).orElseThrow(() -> new RuntimeException("Profesional no encontrado con ID: " + id));
        ProfesionalResponse response = profesionalMapper.toResponse(profesional);

        if (profesional.getFechaNacimiento() != null) {
            response.setEdad(fechaService.calcularEdad(profesional.getFechaNacimiento()));//lama al servicio para calcular la edad
        }
        return response;
    }
        @Transactional(readOnly = true)
        public List<ProfesionalResponse> findAll(String query) {
            // MULTI-TENANT: Obtener negocioId del JWT
            Long negocioId = securityContextService.getNegocioIdFromContext();
            
            List<Profesional> profesionales;

            if (query != null && !query.isEmpty()) {
                profesionales = profesionalRepository.findByNombreCompletoOrDocumentoIdentidadAndRegEstadoNotAndNegocioId(query, 0, negocioId);
            } else {
                profesionales = profesionalRepository.findByRegEstadoNotAndNegocioId(0, negocioId);
            }

        return profesionales.stream()
                .map(profesional-> {
                        ProfesionalResponse response = profesionalMapper.toResponse(profesional);
                        response.setEdad(fechaService.calcularEdad(profesional.getFechaNacimiento()));
                        return response;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public ProfesionalResponse update(Long id, ProfesionalRequest request, MultipartFile foto) {
        Profesional profesional = profesionalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado con ID: " + id));

        profesionalMapper.updateEntity(profesional, request);

        // Guardar foto si se envió
        if (foto != null && !foto.isEmpty()) {
            // Eliminar foto anterior si existe
            if (profesional.getFotoUrl() != null) {
                fileStorageService.deleteFile(profesional.getFotoUrl());
            }
            String fotoUrl = fileStorageService.saveImage(foto, "profesionales");
            profesional.setFotoUrl(fotoUrl);
        }

        Profesional nuevoProfesional = profesionalRepository.save(profesional);
        return profesionalMapper.toResponse(nuevoProfesional);
    }
    @Transactional
    public ProfesionalResponse cambiarEstado(Long id, EstadoRequestGlobal request) {
        Profesional profesional = profesionalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado con ID: " + id));
        profesional.setRegEstado(request.getRegEstado());
        Profesional actualizadoProfesional = profesionalRepository.save(profesional);
        return profesionalMapper.toResponse(actualizadoProfesional);
    }

    @Transactional
    public ProfesionalResponse eliminar(Long id) {
        Profesional profesional = profesionalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado con ID: " + id));
        profesional.setRegEstado(0);
        Profesional actualizadoProfesional = profesionalRepository.save(profesional);
        return profesionalMapper.toResponse(actualizadoProfesional);
    }

    /**
     * Sube/actualiza la foto de un profesional.
     * Elimina la foto anterior si existía.
     */
    @Transactional
    public ProfesionalResponse uploadFoto(Long id, MultipartFile foto) {
        Profesional profesional = profesionalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado con ID: " + id));

        // Eliminar foto anterior si existe
        if (profesional.getFotoUrl() != null) {
            fileStorageService.deleteFile(profesional.getFotoUrl());
        }

        // Guardar nueva foto en carpeta "profesionales"
        String fotoUrl = fileStorageService.saveImage(foto, "profesionales");
        profesional.setFotoUrl(fotoUrl);

        Profesional actualizado = profesionalRepository.save(profesional);
        return profesionalMapper.toResponse(actualizado);
    }
}
