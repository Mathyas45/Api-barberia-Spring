package com.barberia.mappers;

import com.barberia.dto.negocio.NegocioRequest;
import com.barberia.dto.negocio.NegocioUpdateRequest;
import com.barberia.dto.negocio.NegocioResponse;
import com.barberia.models.Negocio;
import org.springframework.stereotype.Component;

@Component
public class NegocioMapper {

    /**
     * Convierte NegocioRequest a Negocio (entidad)
     * Utilizado al crear un nuevo negocio
     */
    public Negocio toEntity(NegocioRequest request) {
        Negocio negocio = new Negocio();
        negocio.setNombre(request.getNombre());
        negocio.setSlug(request.getSlug());
        negocio.setTipoNegocio(request.getTipoNegocio());
        negocio.setRuc(request.getRuc());
        negocio.setDireccion(request.getDireccion());
        negocio.setTelefono(request.getTelefono());
        negocio.setEmail(request.getEmail());
        negocio.setDescripcion(request.getDescripcion());
        negocio.setHistoria(request.getHistoria());
        negocio.setEstado(true);
        negocio.setColorPrincipal(request.getColorPrincipal());
        negocio.setColorSecundario(request.getColorSecundario());
        negocio.setColorAcento(request.getColorAcento());
        negocio.setFacebook(request.getFacebook());
        negocio.setInstagram(request.getInstagram());
        negocio.setTiktok(request.getTiktok());
        negocio.setWhatsapp(request.getWhatsapp());
        negocio.setMapUrl(request.getMapUrl());
        negocio.setModoMapa(request.getModoMapa() != null ? request.getModoMapa() : "mapa");
        negocio.setConfiguracionCerrada(request.getConfiguracionCerrada() != null ? request.getConfiguracionCerrada() : false);
        return negocio;
    }

    /**
     * Convierte Negocio a NegocioResponse
     */
    public NegocioResponse toResponse(Negocio negocio) {
        NegocioResponse response = new NegocioResponse();
        response.setId(negocio.getId());
        response.setNombre(negocio.getNombre());
        response.setSlug(negocio.getSlug());
        response.setTipoNegocio(negocio.getTipoNegocio());
        response.setRuc(negocio.getRuc());
        response.setDireccion(negocio.getDireccion());
        response.setTelefono(negocio.getTelefono());
        response.setEmail(negocio.getEmail());
        response.setDescripcion(negocio.getDescripcion());
        response.setHistoria(negocio.getHistoria());
        response.setEstado(negocio.getEstado());
        response.setColorPrincipal(negocio.getColorPrincipal());
        response.setColorSecundario(negocio.getColorSecundario());
        response.setColorAcento(negocio.getColorAcento());
        response.setLogoUrl(negocio.getLogoUrl());
        response.setHeroImageUrl(negocio.getHeroImageUrl());
        response.setFacebook(negocio.getFacebook());
        response.setInstagram(negocio.getInstagram());
        response.setTiktok(negocio.getTiktok());
        response.setWhatsapp(negocio.getWhatsapp());
        response.setMapUrl(negocio.getMapUrl());
        response.setModoMapa(negocio.getModoMapa() != null ? negocio.getModoMapa() : "mapa");
        // Contenido dinámico
        response.setHeroTitulo(negocio.getHeroTitulo());
        response.setHeroSubtitulo(negocio.getHeroSubtitulo());
        response.setCtaTexto(negocio.getCtaTexto());
        response.setPorQueElegirnos(negocio.getPorQueElegirnos());
        response.setCiudad(negocio.getCiudad());
        // Testimonios
        response.setTestimonio1Nombre(negocio.getTestimonio1Nombre());
        response.setTestimonio1Texto(negocio.getTestimonio1Texto());
        response.setTestimonio2Nombre(negocio.getTestimonio2Nombre());
        response.setTestimonio2Texto(negocio.getTestimonio2Texto());
        response.setTestimonio3Nombre(negocio.getTestimonio3Nombre());
        response.setTestimonio3Texto(negocio.getTestimonio3Texto());
        // SEO
        response.setSeoTitulo(negocio.getSeoTitulo());
        response.setSeoDescripcion(negocio.getSeoDescripcion());
        response.setSeoKeywords(negocio.getSeoKeywords());
        response.setOgImageUrl(negocio.getOgImageUrl());
        response.setConfiguracionCerrada(negocio.getConfiguracionCerrada());
        response.setCreatedAt(negocio.getCreatedAt());
        response.setUpdatedAt(negocio.getUpdatedAt());
        return response;
    }

    /**
     * Actualiza los datos de un negocio existente desde NegocioRequest
     */
    public Negocio updateEntity(Negocio negocio, NegocioRequest request) {
        negocio.setNombre(request.getNombre());
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            negocio.setSlug(request.getSlug());
        }
        if (request.getTipoNegocio() != null) {
            negocio.setTipoNegocio(request.getTipoNegocio());
        }
        negocio.setRuc(request.getRuc());
        negocio.setDireccion(request.getDireccion());
        negocio.setTelefono(request.getTelefono());
        negocio.setEmail(request.getEmail());
        negocio.setDescripcion(request.getDescripcion());
        negocio.setHistoria(request.getHistoria());
        negocio.setColorPrincipal(request.getColorPrincipal());
        negocio.setColorSecundario(request.getColorSecundario());
        negocio.setColorAcento(request.getColorAcento());
        negocio.setFacebook(request.getFacebook());
        negocio.setInstagram(request.getInstagram());
        negocio.setTiktok(request.getTiktok());
        negocio.setWhatsapp(request.getWhatsapp());
        negocio.setMapUrl(request.getMapUrl());
        if (request.getConfiguracionCerrada() != null) {
            negocio.setConfiguracionCerrada(request.getConfiguracionCerrada());
        }
        return negocio;
    }


    /**
     * Actualiza los datos de un negocio existente desde NegocioUpdateRequest
     * Método sobrecargado para actualización desde formularios
     */
    public Negocio updateEntity(Negocio negocio, NegocioUpdateRequest request) {
        negocio.setNombre(request.getNombre());
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            negocio.setSlug(request.getSlug());
        }
        if (request.getTipoNegocio() != null) {
            negocio.setTipoNegocio(request.getTipoNegocio());
        }
        negocio.setRuc(request.getRuc());
        negocio.setDireccion(request.getDireccion());
        negocio.setTelefono(request.getTelefono());
        negocio.setEmail(request.getEmail());
        negocio.setDescripcion(request.getDescripcion());
        negocio.setHistoria(request.getHistoria());
        negocio.setColorPrincipal(request.getColorPrincipal());
        negocio.setColorSecundario(request.getColorSecundario());
        negocio.setColorAcento(request.getColorAcento());
        negocio.setFacebook(request.getFacebook());
        negocio.setInstagram(request.getInstagram());
        negocio.setTiktok(request.getTiktok());
        negocio.setWhatsapp(request.getWhatsapp());
        negocio.setMapUrl(request.getMapUrl());
        negocio.setModoMapa(request.getModoMapa() != null ? request.getModoMapa() : "mapa");
        // Contenido dinámico
        negocio.setHeroTitulo(request.getHeroTitulo());
        negocio.setHeroSubtitulo(request.getHeroSubtitulo());
        negocio.setCtaTexto(request.getCtaTexto());
        negocio.setPorQueElegirnos(request.getPorQueElegirnos());
        negocio.setCiudad(request.getCiudad());
        // Testimonios
        negocio.setTestimonio1Nombre(request.getTestimonio1Nombre());
        negocio.setTestimonio1Texto(request.getTestimonio1Texto());
        negocio.setTestimonio2Nombre(request.getTestimonio2Nombre());
        negocio.setTestimonio2Texto(request.getTestimonio2Texto());
        negocio.setTestimonio3Nombre(request.getTestimonio3Nombre());
        negocio.setTestimonio3Texto(request.getTestimonio3Texto());
        // SEO
        negocio.setSeoTitulo(request.getSeoTitulo());
        negocio.setSeoDescripcion(request.getSeoDescripcion());
        negocio.setSeoKeywords(request.getSeoKeywords());
        negocio.setOgImageUrl(request.getOgImageUrl());
        if (request.getConfiguracionCerrada() != null) {
            negocio.setConfiguracionCerrada(request.getConfiguracionCerrada());
        }
        return negocio;
    }
}
