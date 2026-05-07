-- Índices adicionales para acelerar las consultas de análisis más frecuentes.
-- Los índices de V2 cubren lottery_type y draw_date/draw_number.
-- Estos cubren los patrones de consulta de ventana y generación de params.

-- Acelera el ORDER BY draw_number DESC usado en todas las consultas de ventana
CREATE INDEX IF NOT EXISTS idx_lottery_draws_type_drawnum_desc
    ON lottery_draws (lottery_type, draw_number DESC);

-- Índices por columna de número para las consultas UNION ALL de frecuencias
-- (cada columna se escanea con WHERE lottery_type = ? AND draw_number > ?)
CREATE INDEX IF NOT EXISTS idx_lottery_draws_num1 ON lottery_draws (lottery_type, number_1);
CREATE INDEX IF NOT EXISTS idx_lottery_draws_num2 ON lottery_draws (lottery_type, number_2);
CREATE INDEX IF NOT EXISTS idx_lottery_draws_num3 ON lottery_draws (lottery_type, number_3);
CREATE INDEX IF NOT EXISTS idx_lottery_draws_num4 ON lottery_draws (lottery_type, number_4);
CREATE INDEX IF NOT EXISTS idx_lottery_draws_num5 ON lottery_draws (lottery_type, number_5);
CREATE INDEX IF NOT EXISTS idx_lottery_draws_num6 ON lottery_draws (lottery_type, number_6);

-- Índice parcial: solo sorteos con additional_number (Melate R7)
CREATE INDEX IF NOT EXISTS idx_lottery_draws_additional
    ON lottery_draws (lottery_type, additional_number)
    WHERE additional_number IS NOT NULL;

-- saved_predictions: filtro por user_id ya existe (V3).
-- Agregar índice compuesto para el caso más común: user_id + saved_at DESC
CREATE INDEX IF NOT EXISTS idx_saved_predictions_user_saved
    ON saved_predictions (user_id, saved_at DESC);
