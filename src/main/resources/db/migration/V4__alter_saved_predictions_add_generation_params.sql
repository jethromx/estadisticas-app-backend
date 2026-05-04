ALTER TABLE saved_predictions
    ADD COLUMN IF NOT EXISTS lottery_type           VARCHAR(20),
    ADD COLUMN IF NOT EXISTS generation_params_json TEXT;

CREATE INDEX IF NOT EXISTS idx_saved_predictions_lottery_type
    ON saved_predictions (lottery_type);

COMMENT ON COLUMN saved_predictions.lottery_type
    IS 'Tipo de juego: MELATE, REVANCHA, REVANCHITA, GANA_GATO. NULL en predicciones anteriores a Feature 1.';

COMMENT ON COLUMN saved_predictions.generation_params_json
    IS 'JSON con parámetros de generación: algoritmo, ventana de sorteos, etc. NULL en predicciones anteriores.';
