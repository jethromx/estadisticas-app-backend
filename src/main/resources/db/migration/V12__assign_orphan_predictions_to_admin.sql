-- Predicciones guardadas antes de implementar autenticación multi-usuario
-- tienen user_id = NULL. Las asignamos al admin para que sean visibles.
UPDATE saved_predictions
SET user_id = 'a0000000-0000-0000-0000-000000000001'
WHERE user_id IS NULL;
