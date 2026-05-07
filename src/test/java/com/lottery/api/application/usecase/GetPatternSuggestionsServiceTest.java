package com.lottery.api.application.usecase;

import com.lottery.api.domain.exception.LotteryException;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.NumberFrequency;
import com.lottery.api.domain.model.PatternSuggestion;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetPatternSuggestionsService — Tests Unitarios")
class GetPatternSuggestionsServiceTest {

    @Mock private LotteryDrawRepositoryPort repositoryPort;

    private GetPatternSuggestionsService service;

    @BeforeEach
    void setUp() {
        // Semilla fija para resultados deterministas en STATISTICAL_RANDOM
        service = new GetPatternSuggestionsService(repositoryPort, new Random(42L));
    }

    private List<NumberFrequency> melateFrequencies() {
        return IntStream.rangeClosed(1, 56)
                .mapToObj(n -> NumberFrequency.builder()
                        .number(n)
                        .frequency((long) (100 + n))  // frecuencias distintas: 101-156
                        .build())
                .toList();
    }

    @Test
    @DisplayName("debe retornar 4 sugerencias, una por metodología")
    void getPatternSuggestions_returnsFourMethodologies() {
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(melateFrequencies());

        List<PatternSuggestion> suggestions = service.getPatternSuggestions(LotteryType.MELATE);

        assertThat(suggestions).hasSize(4);
        assertThat(suggestions).extracting(PatternSuggestion::getMethodology)
                .containsExactlyInAnyOrder(
                        GetPatternSuggestionsService.METHODOLOGY_HOT,
                        GetPatternSuggestionsService.METHODOLOGY_COLD,
                        GetPatternSuggestionsService.METHODOLOGY_BALANCED,
                        GetPatternSuggestionsService.METHODOLOGY_RANDOM);
    }

    @Test
    @DisplayName("HOT_NUMBERS debe contener los 6 números más frecuentes")
    void getSuggestionByMethodology_hotNumbers_returnsTopFrequent() {
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(melateFrequencies());

        PatternSuggestion suggestion = service.getSuggestionByMethodology(LotteryType.MELATE, "HOT_NUMBERS");

        assertThat(suggestion.getSuggestedNumbers()).hasSize(6);
        // Los 6 más frecuentes son números 51-56 (frecuencias 152-157)
        assertThat(suggestion.getSuggestedNumbers()).containsExactlyInAnyOrder(51, 52, 53, 54, 55, 56);
        assertThat(suggestion.getSuggestedAdditional()).isNotNull(); // Melate tiene adicional
        assertThat(suggestion.getConfidenceScore()).isEqualTo(0.65);
    }

    @Test
    @DisplayName("COLD_NUMBERS debe contener los 6 números menos frecuentes")
    void getSuggestionByMethodology_coldNumbers_returnsLeastFrequent() {
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(melateFrequencies());

        PatternSuggestion suggestion = service.getSuggestionByMethodology(LotteryType.MELATE, "COLD_NUMBERS");

        assertThat(suggestion.getSuggestedNumbers()).hasSize(6);
        // Los 6 menos frecuentes son 1-6 (frecuencias 102-107)
        assertThat(suggestion.getSuggestedNumbers()).containsExactlyInAnyOrder(1, 2, 3, 4, 5, 6);
    }

    @Test
    @DisplayName("BALANCED debe contener la cantidad correcta de números")
    void getSuggestionByMethodology_balanced_returnsCorrectCount() {
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(melateFrequencies());

        PatternSuggestion suggestion = service.getSuggestionByMethodology(LotteryType.MELATE, "BALANCED");

        assertThat(suggestion.getSuggestedNumbers()).hasSize(LotteryType.MELATE.getNumbersCount());
        assertThat(suggestion.getMethodology()).isEqualTo(GetPatternSuggestionsService.METHODOLOGY_BALANCED);
    }

    @Test
    @DisplayName("STATISTICAL_RANDOM debe retornar números en el rango válido")
    void getSuggestionByMethodology_statisticalRandom_numbersInValidRange() {
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(melateFrequencies());

        PatternSuggestion suggestion = service.getSuggestionByMethodology(LotteryType.MELATE, "STATISTICAL_RANDOM");

        assertThat(suggestion.getSuggestedNumbers()).hasSize(LotteryType.MELATE.getNumbersCount());
        assertThat(suggestion.getSuggestedNumbers())
                .allMatch(n -> n >= LotteryType.MELATE.getMinNumber() && n <= LotteryType.MELATE.getMaxNumber());
    }

    @Test
    @DisplayName("metodología desconocida debe lanzar LotteryException")
    void getSuggestionByMethodology_unknownMethodology_throwsException() {
        assertThatThrownBy(() -> service.getSuggestionByMethodology(LotteryType.MELATE, "INVALID"))
                .isInstanceOf(LotteryException.class)
                .hasMessageContaining("Metodología desconocida");
    }

    @Test
    @DisplayName("Revancha no debe tener número adicional sugerido")
    void getSuggestion_revancha_noAdditional() {
        when(repositoryPort.getNumberFrequencies(LotteryType.REVANCHA)).thenReturn(
                IntStream.rangeClosed(1, 56)
                        .mapToObj(n -> NumberFrequency.builder().number(n).frequency((long) n).build())
                        .toList());

        PatternSuggestion suggestion = service.getSuggestionByMethodology(LotteryType.REVANCHA, "HOT_NUMBERS");

        assertThat(suggestion.getSuggestedAdditional()).isNull();
    }

    @Test
    @DisplayName("metodología en minúsculas también funciona")
    void getSuggestionByMethodology_lowercaseInput_works() {
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(melateFrequencies());

        PatternSuggestion suggestion = service.getSuggestionByMethodology(LotteryType.MELATE, "hot_numbers");

        assertThat(suggestion.getMethodology()).isEqualTo(GetPatternSuggestionsService.METHODOLOGY_HOT);
    }
}
