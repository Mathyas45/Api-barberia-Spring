package com.barberia.controllers;

import com.barberia.dto.ApiResponse;
import com.barberia.dto.Categoria.CategoriaRequest;
import com.barberia.dto.Categoria.CategoriaResponse;
import com.barberia.dto.EstadoRequestGlobal;
import com.barberia.services.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;
    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }
    @PostMapping({"/register"})
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('CREATE_CATEGORIES')")
    public ResponseEntity<ApiResponse<CategoriaResponse>> create(
            @Valid @RequestBody CategoriaRequest request) {
        try {
            CategoriaResponse categoriaResponse = categoriaService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.<CategoriaResponse>builder()
                            .code(201)
                            .success(true)
                            .message("Categoría creada exitosamente")
                            .data(categoriaResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<CategoriaResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error al crear la categoría: " + e.getMessage())
                            .build()
            );
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or  hasAuthority('READ_PROFESSIONALS')")
    public ResponseEntity<ApiResponse<CategoriaResponse>> findById(@PathVariable Long id){
        try {
            CategoriaResponse categoriaResponse = categoriaService.findById(id);
            return ResponseEntity.ok(
                    ApiResponse.<CategoriaResponse>builder()
                            .code(200)
                            .success(true)
                            .message("Categoría encontrada")
                            .data(categoriaResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.<CategoriaResponse>builder()
                            .code(404)
                            .success(false)
                            .message("Error al encontrar la categoría: " + e.getMessage())
                            .build()
            );

        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('READ_CATEGORIES')")
    public ResponseEntity<ApiResponse<List<CategoriaResponse>>> findAll(@RequestParam(required = false) String query) {
        try {
            List<CategoriaResponse> categoriaResponses = categoriaService.findAll(query);
            return ResponseEntity.ok(
                    ApiResponse.<List<CategoriaResponse>>builder()
                            .code(200)
                            .success(true)
                            .message("Categorías encontradas")
                            .data(categoriaResponses)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<List<CategoriaResponse>>builder()
                            .code(400)
                            .success(false)
                            .message("Error al listar las categorías: " + e.getMessage())
                            .build()
            );
        }
    }
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('UPDATE_CATEGORIES')")
    public ResponseEntity<ApiResponse<CategoriaResponse>> update(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        try {
            CategoriaResponse response = categoriaService.update(id, request);
            return ResponseEntity.ok(ApiResponse.<CategoriaResponse>builder()
                    .code(200)
                    .success(true)
                    .message("Categoría actualizada exitosamente")
                    .data(response)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<CategoriaResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error al actualizar la categoría: " + e.getMessage())
                            .build()
            );
        }
    }
    @PutMapping("/estado/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('UPDATE_CATEGORIES')")
    public ResponseEntity<ApiResponse<CategoriaResponse>> cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequestGlobal request) {
        try {
            CategoriaResponse response = categoriaService.cambiarEstado(id, request);
            return ResponseEntity.ok(ApiResponse.<CategoriaResponse>builder()
                    .code(200)
                    .success(true)
                    .message("Estado de la categoría cambiado exitosamente")
                    .data(response)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<CategoriaResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error al cambiar el estado de la categoría: " + e.getMessage())
                            .build()
            );
        }
    }
    @PutMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('DELETE_CATEGORIES')")
    public ResponseEntity<ApiResponse<CategoriaResponse>> eliminar(@PathVariable Long id) {
        try {
            CategoriaResponse response = categoriaService.eliminar(id);
            return ResponseEntity.ok(ApiResponse.<CategoriaResponse>builder()
                    .code(200)
                    .success(true)
                    .message("Categoría eliminada exitosamente")
                    .data(response)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<CategoriaResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error al eliminar la categoría: " + e.getMessage())
                            .build()
            );
        }
    }



}
