-- Corrige predicciones guardadas sin lottery_type (guardadas antes de V4).
-- Como los tres juegos comparten el rango 1-56, MELATE es un fallback válido.
UPDATE saved_predictions
SET lottery_type = 'MELATE'
WHERE lottery_type IS NULL;
