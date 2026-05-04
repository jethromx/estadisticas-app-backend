package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.SavePredictionCommand;
import com.lottery.api.domain.model.SavedPrediction;
import com.lottery.api.domain.port.out.SavedPredictionRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SavePredictionService — Tests Unitarios")
class SavePredictionServiceTest {

    @Mock private SavedPredictionRepositoryPort repository;
    @InjectMocks private SavePredictionService service;

    @Test
    @DisplayName("debe guardar predicción con todos los campos del comando")
    void execute_savesAllCommandFields() {
        SavePredictionCommand command = new SavePredictionCommand(
                "Mi predicción", "2024-12-01", "[[1,2,3,4,5,6]]",
                LotteryType.MELATE, "{\"algorithm\":\"hot-cold\"}", "user-123");
        ArgumentCaptor<SavedPrediction> captor = ArgumentCaptor.forClass(SavedPrediction.class);
        when(repository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        SavedPrediction result = service.execute(command);

        SavedPrediction saved = captor.getValue();
        assertThat(saved.getId()).isNotBlank();
        assertThat(saved.getLabel()).isEqualTo("Mi predicción");
        assertThat(saved.getLatestDrawDate()).isNotNull();
        assertThat(saved.getCombosJson()).isEqualTo("[[1,2,3,4,5,6]]");
        assertThat(saved.getLotteryType()).isEqualTo(LotteryType.MELATE);
        assertThat(saved.getGenerationParamsJson()).isEqualTo("{\"algorithm\":\"hot-cold\"}");
        assertThat(saved.getUserId()).isEqualTo("user-123");
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("debe aceptar latestDrawDate nulo sin error")
    void execute_nullDrawDate_noError() {
        SavePredictionCommand command = new SavePredictionCommand(
                "Sin fecha", null, "[[7,8,9]]", LotteryType.REVANCHA, null, "user-1");
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SavedPrediction result = service.execute(command);

        assertThat(result.getLatestDrawDate()).isNull();
    }

    @Test
    @DisplayName("debe aceptar campos opcionales como null")
    void execute_nullOptionalFields_savedCorrectly() {
        SavePredictionCommand command = new SavePredictionCommand(
                "Sin tipo", null, "[[1,2]]", null, null, null);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SavedPrediction result = service.execute(command);

        assertThat(result.getLotteryType()).isNull();
        assertThat(result.getGenerationParamsJson()).isNull();
        assertThat(result.getUserId()).isNull();
    }
}
