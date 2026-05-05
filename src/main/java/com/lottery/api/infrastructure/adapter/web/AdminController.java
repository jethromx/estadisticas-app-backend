package com.lottery.api.infrastructure.adapter.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lottery.api.domain.model.User;
import com.lottery.api.domain.port.in.CreateUserUseCase;
import com.lottery.api.domain.port.in.DeleteUserUseCase;
import com.lottery.api.domain.port.in.GetAdminMetricsUseCase;
import com.lottery.api.domain.port.in.GetAllUsersUseCase;
import com.lottery.api.domain.port.in.UpdateUserUseCase;
import com.lottery.api.domain.port.out.SavedPredictionRepositoryPort;
import com.lottery.api.infrastructure.adapter.web.dto.request.CreateUserRequest;
import com.lottery.api.infrastructure.adapter.web.dto.request.UpdateUserRequest;
import com.lottery.api.infrastructure.adapter.web.dto.response.AdminMetricsResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.AdminUserResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.SavedPredictionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "Admin", description = "Administración del sistema (solo ADMIN)")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final GetAllUsersUseCase getAllUsersUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final GetAdminMetricsUseCase getAdminMetricsUseCase;
    private final SavedPredictionRepositoryPort savedPredictionRepositoryPort;
    private final ObjectMapper objectMapper;

    @Operation(summary = "Listar todos los usuarios")
    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> getUsers() {
        List<AdminUserResponse> users = getAllUsersUseCase.execute().stream()
                .map(u -> new AdminUserResponse(
                        u.getId(), u.getUsername(), u.getEmail(),
                        u.getRole().name(), u.isActive(), u.getCreatedAt()))
                .toList();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Crear usuario (con rol)")
    @PostMapping("/users")
    public ResponseEntity<AdminUserResponse> createUser(@RequestBody @Valid CreateUserRequest req) {
        User user = createUserUseCase.execute(req.username(), req.email(), req.password(), req.role());
        AdminUserResponse resp = new AdminUserResponse(
                user.getId(), user.getUsername(), user.getEmail(),
                user.getRole().name(), user.isActive(), user.getCreatedAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @Operation(summary = "Actualizar usuario")
    @PutMapping("/users/{id}")
    public ResponseEntity<AdminUserResponse> updateUser(@PathVariable String id, @RequestBody @Valid UpdateUserRequest req) {
        log.info("PUT /admin/users/{} - updating user", id);
        log.info("Request: username={}, email={}, role={}, active={}", req.username(), req.email(), req.role(), req.active());
        
        User user = updateUserUseCase.execute(id, req.username(), req.email(), req.role(), req.active(), req.password());
        
        AdminUserResponse resp = new AdminUserResponse(
                user.getId(), user.getUsername(), user.getEmail(),
                user.getRole().name(), user.isActive(), user.getCreatedAt());
        
        log.info("Successfully updated user: {}", user.getUsername());
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "Eliminar usuario")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        deleteUserUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Métricas de uso de la plataforma")
    @GetMapping("/metrics")
    public ResponseEntity<AdminMetricsResponse> getMetrics() {
        Map<String, Object> metrics = getAdminMetricsUseCase.execute();
        long totalUsers = (long) metrics.get("totalUsers");
        long totalPredictions = (long) metrics.get("totalPredictions");
        @SuppressWarnings("unchecked")
        Map<String, Long> actionBreakdown = (Map<String, Long>) metrics.get("actionBreakdown");
        return ResponseEntity.ok(new AdminMetricsResponse(totalUsers, totalPredictions, actionBreakdown));
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        return ResponseEntity.ok(Map.of("version", "NEW_CODE_v2", "userId_test", "working"));
    }

    @Operation(summary = "Listar todas las predicciones (todos los usuarios)")
    @GetMapping("/predictions")
    public ResponseEntity<List<Map<String, Object>>> getAllPredictions() {
        List<Map<String, Object>> predictions = savedPredictionRepositoryPort.findAll().stream()
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", p.getId());
                    m.put("label", p.getLabel());
                    m.put("savedAt", p.getSavedAt() != null ? p.getSavedAt().toString() : null);
                    m.put("latestDrawDate", p.getLatestDrawDate() != null ? p.getLatestDrawDate().toString() : null);
                    m.put("combos", parseJson(p.getCombosJson(), p.getId()));
                    m.put("lotteryType", p.getLotteryType() != null ? p.getLotteryType().name() : null);
                    m.put("generationParams", p.getGenerationParamsJson() != null ? parseJson(p.getGenerationParamsJson(), p.getId()) : null);
                    String uid = p.getUserId();
                    log.info("DEBUG prediction {} userId={}", p.getId(), uid);
                    m.put("userId", uid != null ? uid : "NO_USER_ID");
                    return m;
                })
                .toList();
        return ResponseEntity.ok(predictions);
    }

    private JsonNode parseJson(String json, String predictionId) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            log.error("Error parsing JSON for prediction {}: {}", predictionId, e.getMessage());
            return objectMapper.createObjectNode();
        }
    }
}
