package com.barberia.services;

import com.barberia.dto.negocio.BusinessPublicResponse;
import com.barberia.models.*;
import com.barberia.models.enums.DiaSemana;
import com.barberia.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para la web pública multi-tenant.
 * NO requiere autenticación — lee datos públicos por slug.
 *
 * FLUJO:
 * 1. El frontend visita /mi-barberia
 * 2. Llama GET /api/public/business/mi-barberia
 * 3. Este servicio busca el negocio por slug
 * 4. Recopila servicios, profesionales, horarios e imágenes de galería
 * 5. Retorna BusinessPublicResponse con todo empaquetado
 */
@Service
public class PublicBusinessService {

    private final NegocioRepository negocioRepository;
    private final ServicioRepository servicioRepository;
    private final ProfesionalRepository profesionalRepository;
    private final HorarioNegocioRepository horarioNegocioRepository;
    private final GaleriaImagenRepository galeriaImagenRepository;

    public PublicBusinessService(
            NegocioRepository negocioRepository,
            ServicioRepository servicioRepository,
            ProfesionalRepository profesionalRepository,
            HorarioNegocioRepository horarioNegocioRepository,
            GaleriaImagenRepository galeriaImagenRepository
    ) {
        this.negocioRepository = negocioRepository;
        this.servicioRepository = servicioRepository;
        this.profesionalRepository = profesionalRepository;
        this.horarioNegocioRepository = horarioNegocioRepository;
        this.galeriaImagenRepository = galeriaImagenRepository;
    }

    /**
     * Obtiene todos los datos públicos de un negocio por su slug.
     *
     * @param slug Slug único del negocio (ej: "barberia-el-estilo")
     * @return BusinessPublicResponse con todos los datos, o null si no existe
     */
    @Transactional(readOnly = true)
    public BusinessPublicResponse getBySlug(String slug) {
        Negocio negocio = negocioRepository.findBySlugAndEstadoTrue(slug)
                .orElse(null);

        if (negocio == null) {
            return null;
        }

        Long negocioId = negocio.getId();

        // Obtener servicios activos del negocio
        List<Servicio> servicios = servicioRepository
                .findByRegEstadoNotAndNegocioIdAndCategoriaEstadoNot(0, negocioId);

        // Obtener profesionales activos del negocio
        List<Profesional> profesionales = profesionalRepository
                .findByRegEstadoNotAndNegocioId(0, negocioId);

        // Obtener horarios del negocio (todos los días)
        List<HorarioNegocio> horarios = horarioNegocioRepository
                .findByNegocioAndDiaSemana(negocioId, null);

        // Obtener galería de imágenes
        List<GaleriaImagen> galeria = galeriaImagenRepository
                .findByNegocioIdOrderByOrdenAsc(negocioId);

        // Construir y retornar la respuesta
        return buildPublicResponse(negocio, servicios, profesionales, horarios, galeria);
    }

    /**
     * Construye el BusinessPublicResponse a partir de las entidades
     */
    private BusinessPublicResponse buildPublicResponse(
            Negocio negocio,
            List<Servicio> servicios,
            List<Profesional> profesionales,
            List<HorarioNegocio> horarios,
            List<GaleriaImagen> galeria
    ) {
        return BusinessPublicResponse.builder()
                .slug(negocio.getSlug())
                .nombre(negocio.getNombre())
                .tipoNegocio(negocio.getTipoNegocio() != null ? negocio.getTipoNegocio() : "barberia")
                .descripcion(negocio.getDescripcion())
                .historia(negocio.getHistoria())
                .logoUrl(negocio.getLogoUrl())
                .heroImageUrl(negocio.getHeroImageUrl())
                .galeriaUrls(galeria.stream()
                        .map(GaleriaImagen::getImagenUrl)
                        .collect(Collectors.toList()))
                .colorPrimary(negocio.getColorPrincipal() != null ? negocio.getColorPrincipal() : "#2563eb")
                .colorSecondary(negocio.getColorSecundario() != null ? negocio.getColorSecundario() : "#1e293b")
                .colorAccent(negocio.getColorAcento() != null ? negocio.getColorAcento() : "#f59e0b")
                .direccion(negocio.getDireccion())
                .telefono(negocio.getTelefono())
                .email(negocio.getEmail())
                .mapUrl(negocio.getMapUrl())
                .modoMapa(negocio.getModoMapa() != null ? negocio.getModoMapa() : "mapa")
                // Contenido dinámico
                .heroTitulo(negocio.getHeroTitulo())
                .heroSubtitulo(negocio.getHeroSubtitulo())
                .ctaTexto(negocio.getCtaTexto())
                .porQueElegirnos(negocio.getPorQueElegirnos())
                .ciudad(negocio.getCiudad())
                // SEO
                .seoTitulo(negocio.getSeoTitulo())
                .seoDescripcion(negocio.getSeoDescripcion())
                .seoKeywords(negocio.getSeoKeywords())
                .ogImageUrl(negocio.getOgImageUrl())
                .redesSociales(BusinessPublicResponse.RedesSocialesDto.builder()
                        .facebook(negocio.getFacebook())
                        .instagram(negocio.getInstagram())
                        .tiktok(negocio.getTiktok())
                        .whatsapp(negocio.getWhatsapp())
                        .build())
                .horarios(buildHorariosPublicos(horarios))
                .servicios(servicios.stream()
                        .filter(Servicio::isEstado) // solo servicios activos
                        .map(this::toServicioPublico)
                        .collect(Collectors.toList()))
                .profesionales(profesionales.stream()
                        .map(this::toProfesionalPublico)
                        .collect(Collectors.toList()))
                .testimonios(buildTestimonios(negocio))
                .build();
    }

    /**
     * Construye la lista de horarios públicos.
     * Incluye TODOS los días de la semana, marcando como "cerrado" los que no tienen horario.
     */
    private List<BusinessPublicResponse.HorarioPublicoDto> buildHorariosPublicos(List<HorarioNegocio> horarios) {
        // Agrupar horarios por día
        Map<DiaSemana, List<HorarioNegocio>> horariosPorDia = horarios.stream()
                .collect(Collectors.groupingBy(HorarioNegocio::getDiaSemana));

        List<BusinessPublicResponse.HorarioPublicoDto> resultado = new ArrayList<>();

        // Iterar todos los días de la semana en orden
        for (DiaSemana dia : DiaSemana.values()) {
            List<HorarioNegocio> horariosDelDia = horariosPorDia.getOrDefault(dia, Collections.emptyList());

            if (horariosDelDia.isEmpty()) {
                // Día cerrado
                resultado.add(BusinessPublicResponse.HorarioPublicoDto.builder()
                        .dia(capitalizarDia(dia.name()))
                        .apertura("")
                        .cierre("")
                        .cerrado(true)
                        .build());
            } else {
                // Tomar el primer horario (si hay varios turnos, tomar el rango completo)
                HorarioNegocio primero = horariosDelDia.get(0);
                HorarioNegocio ultimo = horariosDelDia.get(horariosDelDia.size() - 1);
                resultado.add(BusinessPublicResponse.HorarioPublicoDto.builder()
                        .dia(capitalizarDia(dia.name()))
                        .apertura(primero.getHoraInicio().toString())
                        .cierre(ultimo.getHoraFin().toString())
                        .cerrado(false)
                        .build());
            }
        }

        return resultado;
    }

    private BusinessPublicResponse.ServicioPublicoDto toServicioPublico(Servicio servicio) {
        return BusinessPublicResponse.ServicioPublicoDto.builder()
                .id(servicio.getId())
                .nombre(servicio.getNombre())
                .descripcion(servicio.getDescripcion())
                .precio(servicio.getPrecio() != null ? servicio.getPrecio().doubleValue() : 0.0)
                .duracionMinutos(servicio.getDuracionMinutosAprox())
                .categoria(servicio.getCategoria() != null ? servicio.getCategoria().getNombre() : null)
                .imagenUrl(servicio.getImagenUrl())
                .build();
    }

    private BusinessPublicResponse.ProfesionalPublicoDto toProfesionalPublico(Profesional profesional) {
        return BusinessPublicResponse.ProfesionalPublicoDto.builder()
                .id(profesional.getId())
                .nombre(profesional.getNombreCompleto())
                .especialidad(profesional.getEspecialidad())
                .fotoUrl(profesional.getFotoUrl())
                .descripcion(profesional.getDescripcion())
                .build();
    }

    /**
     * Construye la lista de testimonios públicos a partir de los campos
     * testimonio1..3 del negocio. Solo incluye los que tienen nombre y texto.
     */
    private List<BusinessPublicResponse.TestimonioPublicoDto> buildTestimonios(Negocio negocio) {
        List<BusinessPublicResponse.TestimonioPublicoDto> resultado = new ArrayList<>();

        if (negocio.getTestimonio1Nombre() != null && !negocio.getTestimonio1Nombre().isBlank()
                && negocio.getTestimonio1Texto() != null && !negocio.getTestimonio1Texto().isBlank()) {
            resultado.add(BusinessPublicResponse.TestimonioPublicoDto.builder()
                    .autor(negocio.getTestimonio1Nombre())
                    .contenido(negocio.getTestimonio1Texto())
                    .build());
        }
        if (negocio.getTestimonio2Nombre() != null && !negocio.getTestimonio2Nombre().isBlank()
                && negocio.getTestimonio2Texto() != null && !negocio.getTestimonio2Texto().isBlank()) {
            resultado.add(BusinessPublicResponse.TestimonioPublicoDto.builder()
                    .autor(negocio.getTestimonio2Nombre())
                    .contenido(negocio.getTestimonio2Texto())
                    .build());
        }
        if (negocio.getTestimonio3Nombre() != null && !negocio.getTestimonio3Nombre().isBlank()
                && negocio.getTestimonio3Texto() != null && !negocio.getTestimonio3Texto().isBlank()) {
            resultado.add(BusinessPublicResponse.TestimonioPublicoDto.builder()
                    .autor(negocio.getTestimonio3Nombre())
                    .contenido(negocio.getTestimonio3Texto())
                    .build());
        }

        return resultado;
    }

    /**
     * Capitaliza el nombre del día: "LUNES" → "Lunes"
     */
    private String capitalizarDia(String dia) {
        if (dia == null || dia.isEmpty()) return dia;
        return dia.charAt(0) + dia.substring(1).toLowerCase();
    }
}
