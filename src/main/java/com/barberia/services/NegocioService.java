package com.barberia.services;

import com.barberia.dto.negocio.NegocioRequest;
import com.barberia.dto.negocio.NegocioUpdateRequest;
import com.barberia.dto.negocio.NegocioResponse;
import com.barberia.mappers.NegocioMapper;
import com.barberia.models.Negocio;
import com.barberia.repositories.NegocioRepository;
import com.barberia.services.common.SecurityContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NegocioService {

    private final NegocioRepository negocioRepository;
    private final NegocioMapper negocioMapper;
    private final FileStorageService fileStorageService;
    private final SecurityContextService securityContextService;


    public NegocioService(NegocioRepository negocioRepository, NegocioMapper negocioMapper, 
                         FileStorageService fileStorageService, SecurityContextService securityContextService) {
        this.negocioRepository = negocioRepository;
        this.negocioMapper = negocioMapper;
        this.fileStorageService = fileStorageService;
        this.securityContextService = securityContextService;
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
        // MULTI-TENANT: Obtener negocioId del JWT
        Long negocioId = securityContextService.getNegocioIdFromContext();

        List<Negocio> negocios;

        if (query != null && !query.isBlank()) {
            negocios = negocioRepository.findByNombreContainingIgnoreCaseOrRucContainingIgnoreCaseAndNegocioId(query, negocioId);
        } else {
            negocios = negocioRepository.findAllByNegocioId(negocioId);
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

    /**
     * Actualiza la configuración completa del negocio incluyendo logo y hero opcionales
     * @param id ID del negocio
     * @param request Datos del negocio a actualizar
     * @param logo Archivo de imagen del logo (opcional)
     * @param heroImage Archivo de imagen del hero/portada (opcional)
     * @return NegocioResponse con los datos actualizados
     */
    @Transactional
    public NegocioResponse updateConfiguracion(Long id, NegocioUpdateRequest request, MultipartFile logo, MultipartFile heroImage) {
        Negocio negocio = negocioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Negocio no encontrado con ID: " + id));

        // Verificar si el RUC ya existe en otro negocio
        if (request.getRuc() != null && !request.getRuc().isBlank()) {
            Negocio negocioConRuc = negocioRepository.findByRuc(request.getRuc());
            if (negocioConRuc != null && !negocioConRuc.getId().equals(id)) {
                throw new RuntimeException("Ya existe otro negocio con ese RUC");
            }
        }

        // Verificar si el slug ya existe en otro negocio
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String nuevoSlug = Negocio.generarSlug(request.getSlug());
            request.setSlug(nuevoSlug);
            negocioRepository.findBySlugAndEstadoTrue(nuevoSlug).ifPresent(n -> {
                if (!n.getId().equals(id)) {
                    throw new RuntimeException("Ya existe otro negocio con el slug: " + nuevoSlug);
                }
            });
        }

        // Actualizar datos básicos usando el mapper sobrecargado
        negocioMapper.updateEntity(negocio, request);

        // Actualizar logo si se envió uno nuevo
        if (logo != null && !logo.isEmpty()) {
            // Eliminar logo anterior si existe
            if (negocio.getLogoUrl() != null && !negocio.getLogoUrl().isBlank()) {
                fileStorageService.deleteFile(negocio.getLogoUrl());
            }
            // Guardar nuevo logo
            String logoUrl = fileStorageService.saveLogo(logo);
            negocio.setLogoUrl(logoUrl);
        }

        // Actualizar hero image si se envió una nueva
        if (heroImage != null && !heroImage.isEmpty()) {
            // Eliminar hero anterior si existe
            if (negocio.getHeroImageUrl() != null && !negocio.getHeroImageUrl().isBlank()) {
                fileStorageService.deleteFile(negocio.getHeroImageUrl());
            }
            // Guardar nueva hero image
            String heroUrl = fileStorageService.saveHero(heroImage);
            negocio.setHeroImageUrl(heroUrl);
        }

        Negocio negocioActualizado = negocioRepository.save(negocio);
        return negocioMapper.toResponse(negocioActualizado);
    }

    /**
     * Elimina el logo del negocio
     */
    @Transactional
    public NegocioResponse deleteLogo(Long id) {
        Negocio negocio = negocioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Negocio no encontrado con ID: " + id));

        // Eliminar logo del sistema de archivos
        if (negocio.getLogoUrl() != null) {
            fileStorageService.deleteFile(negocio.getLogoUrl());
            negocio.setLogoUrl(null);
            negocioRepository.save(negocio);
        }

        return negocioMapper.toResponse(negocio);
    }

    @Transactional
    public void delete(Long id) {
        Negocio negocio = negocioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Negocio no encontrado con ID: " + id));
        negocio.setEstado(false); // Suponiendo que 0 es el estado para "eliminado"
        negocioRepository.save(negocio);
    }

    @Transactional
    public void activate(Long id) {
        Negocio negocio = negocioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Negocio no encontrado con ID: " + id));
        negocio.setEstado(true); // Suponiendo que 1 es el estado para "activo"
        negocioRepository.save(negocio);
    }
}
