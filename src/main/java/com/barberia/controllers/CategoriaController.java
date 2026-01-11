package com.barberia.controllers;

import com.barberia.dto.ApiResponse;
import com.barberia.dto.Categoria.CategoriaRequest;
import com.barberia.dto.Categoria.CategoriaResponse;
import com.barberia.services.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
