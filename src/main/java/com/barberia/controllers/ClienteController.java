package com.barberia.controllers;

import com.barberia.dto.ApiResponse;
import com.barberia.dto.cliente.ClienteRequest;
import com.barberia.dto.cliente.ClienteResponse;
import com.barberia.services.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    @Autowired
    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<ClienteResponse>> createCliente(@RequestBody ClienteRequest request) {
        try {
            ClienteResponse clienteResponse = clienteService.create(request);
            ApiResponse<ClienteResponse> response = ApiResponse.<ClienteResponse>builder()
                    .success(true)
                    .message("Cliente creado exitosamente")
                    .data(clienteResponse)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<ClienteResponse> response = ApiResponse.<ClienteResponse>builder()
                    .success(false)
                    .message("Error al crear el cliente: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteResponse>> findById(@PathVariable Long id){
        try {
            ClienteResponse response = clienteService.findById(id);
            return ResponseEntity.ok(ApiResponse.<ClienteResponse>builder()
                    .success(true)
                    .message("Cliente encontrado")
                    .data(response)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.<ClienteResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClienteResponse>>> findAll(@RequestParam(required = false) String query) {
        try {
            List<ClienteResponse> response = clienteService.findAll(query);
            return ResponseEntity.ok(ApiResponse.<List<ClienteResponse>>builder()
                    .success(true)
                    .message("Clientes encontrados")
                    .data(response)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.<List<ClienteResponse>>builder()
                    .success(false)
                    .message("Error al listar clientes: " + e.getMessage())
                    .build());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<ClienteResponse>> update(@PathVariable Long id, @RequestBody ClienteRequest request) {
        try {

            ClienteResponse clienteResponse = clienteService.update(id, request);
            ApiResponse<ClienteResponse> response = ApiResponse.<ClienteResponse>builder()
                    .success(true)
                    .message("Cliente actualizado exitosamente")
                    .data(clienteResponse)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.<ClienteResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            clienteService.delete(id);
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Cliente eliminado exitosamente")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.<Void>builder()
                    .success(false)
                    .message("Error al eliminar el cliente: " + e.getMessage())
                    .build());
        }
    }


}
