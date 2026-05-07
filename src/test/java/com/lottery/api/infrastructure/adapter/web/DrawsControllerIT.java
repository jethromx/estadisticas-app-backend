package com.lottery.api.infrastructure.adapter.web;

import com.lottery.api.TestcontainersConfig;
import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para el endpoint GET /{type}/draws con paginación
 * y para verificar que el caché Caffeine funciona correctamente.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@ActiveProfiles("test")
@Transactional
@WithMockUser
@DisplayName("DrawsController — Tests de Integración")
class DrawsControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private LotteryDrawRepositoryPort repository;
    @Autowired private CacheManager cacheManager;

    private static final String BASE = "/api/v1/lottery/MELATE/draws";

    @BeforeEach
    void setUp() {
        // Limpiar caché entre tests para evitar contaminación
        for (String name : cacheManager.getCacheNames()) {
            var cache = cacheManager.getCache(name);
            if (cache != null) cache.clear();
        }

        repository.saveAll(List.of(
            draw(1, LocalDate.of(2025, 1, 1)),
            draw(2, LocalDate.of(2025, 1, 8)),
            draw(3, LocalDate.of(2025, 1, 15))
        ));
    }

    @Test
    @DisplayName("sin parámetros devuelve todos los sorteos como lista plana")
    void getDraws_noParams_returnsFlatList() throws Exception {
        mockMvc.perform(get(BASE))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @DisplayName("con page=0&size=2 devuelve página con metadatos")
    void getDraws_withPagination_returnsPageMetadata() throws Exception {
        mockMvc.perform(get(BASE).param("page", "0").param("size", "2"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content").isArray())
               .andExpect(jsonPath("$.content.length()").value(2))
               .andExpect(jsonPath("$.totalElements").value(3))
               .andExpect(jsonPath("$.totalPages").value(2))
               .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @DisplayName("segunda llamada sin paginación es servida desde caché")
    void getDraws_secondCall_servedFromCache() throws Exception {
        mockMvc.perform(get(BASE).param("limit", "5000")).andExpect(status().isOk());

        var cache = cacheManager.getCache("draws");
        assertThat(cache).isNotNull();
        assertThat(cache != null ? cache.get("MELATE-5000") : null).isNotNull();
    }

    @Test
    @DisplayName("page=1 devuelve segunda página")
    void getDraws_secondPage_returnsRemainingItems() throws Exception {
        mockMvc.perform(get(BASE).param("page", "1").param("size", "2"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content.length()").value(1))
               .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @DisplayName("size mayor a 500 devuelve 400")
    void getDraws_sizeOverMax_returns400() throws Exception {
        mockMvc.perform(get(BASE).param("page", "0").param("size", "501"))
               .andExpect(status().isBadRequest());
    }

    private LotteryDraw draw(int number, LocalDate date) {
        return LotteryDraw.builder()
                .lotteryType(LotteryType.MELATE)
                .drawNumber(number)
                .drawDate(date)
                .numbers(List.of(1, 2, 3, 4, 5, 6))
                .build();
    }
}
