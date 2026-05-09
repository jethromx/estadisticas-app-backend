package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.SavedPrediction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GetPredictionsUseCase {
    List<SavedPrediction> execute(String userId);
    Page<SavedPrediction> executePaged(String userId, Pageable pageable);
}
