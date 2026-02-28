package com.barberia.services;

import com.barberia.models.GaleriaImagen;
import com.barberia.models.Negocio;
import com.barberia.repositories.GaleriaImagenRepository;
import com.barberia.repositories.NegocioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Servicio para gestionar las imágenes de galería de un negocio.
 * Las imágenes se usan en la sección "Galería" de la web pública.
 *
 * OPERACIONES:
 * - Listar imágenes por negocio (ordenadas)
 * - Subir nueva imagen (auto-orden al final)
 * - Eliminar imagen (elimina archivo físico + registro BD)
 * - Reordenar imágenes
 */
@Service
public class GaleriaImagenService {

    private final GaleriaImagenRepository galeriaImagenRepository;
    private final NegocioRepository negocioRepository;
    private final FileStorageService fileStorageService;

    /** Máximo de imágenes por negocio */
    private static final int MAX_IMAGENES = 20;

    public GaleriaImagenService(
            GaleriaImagenRepository galeriaImagenRepository,
            NegocioRepository negocioRepository,
            FileStorageService fileStorageService) {
        this.galeriaImagenRepository = galeriaImagenRepository;
        this.negocioRepository = negocioRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Lista todas las imágenes de galería de un negocio, ordenadas
     */
    @Transactional(readOnly = true)
    public List<GaleriaImagen> findByNegocioId(Long negocioId) {
        return galeriaImagenRepository.findByNegocioIdOrderByOrdenAsc(negocioId);
    }

    /**
     * Sube una nueva imagen a la galería
     *
     * @param negocioId ID del negocio
     * @param file Archivo de imagen
     * @return GaleriaImagen creada
     */
    @Transactional
    public GaleriaImagen upload(Long negocioId, MultipartFile file) {
        Negocio negocio = negocioRepository.findById(negocioId)
                .orElseThrow(() -> new RuntimeException("Negocio no encontrado con ID: " + negocioId));

        // Validar límite de imágenes
        long count = galeriaImagenRepository.countByNegocioId(negocioId);
        if (count >= MAX_IMAGENES) {
            throw new RuntimeException("Se ha alcanzado el máximo de " + MAX_IMAGENES + " imágenes en la galería");
        }

        // Guardar archivo
        String imagenUrl = fileStorageService.saveGaleria(file);

        // Obtener siguiente orden
        Integer maxOrden = galeriaImagenRepository.findMaxOrdenByNegocioId(negocioId);
        int nuevoOrden = (maxOrden != null ? maxOrden : 0) + 1;

        // Crear registro
        GaleriaImagen imagen = GaleriaImagen.builder()
                .negocio(negocio)
                .imagenUrl(imagenUrl)
                .orden(nuevoOrden)
                .build();

        return galeriaImagenRepository.save(imagen);
    }

    /**
     * Elimina una imagen de la galería (archivo físico + registro BD)
     *
     * @param negocioId ID del negocio (seguridad multi-tenant)
     * @param imagenId ID de la imagen a eliminar
     */
    @Transactional
    public void delete(Long negocioId, Long imagenId) {
        GaleriaImagen imagen = galeriaImagenRepository.findByIdAndNegocioId(imagenId, negocioId)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada"));

        // Eliminar archivo físico
        fileStorageService.deleteFile(imagen.getImagenUrl());

        // Eliminar registro
        galeriaImagenRepository.delete(imagen);
    }

    /**
     * Actualiza el orden de las imágenes
     *
     * @param negocioId ID del negocio
     * @param imagenesIds Lista de IDs en el nuevo orden deseado
     */
    @Transactional
    public void reordenar(Long negocioId, List<Long> imagenesIds) {
        for (int i = 0; i < imagenesIds.size(); i++) {
            GaleriaImagen imagen = galeriaImagenRepository.findByIdAndNegocioId(imagenesIds.get(i), negocioId)
                    .orElseThrow(() -> new RuntimeException("Imagen no encontrada"));
            imagen.setOrden(i + 1);
            galeriaImagenRepository.save(imagen);
        }
    }
}
