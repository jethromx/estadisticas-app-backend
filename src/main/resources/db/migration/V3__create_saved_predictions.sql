CREATE TABLE IF NOT EXISTS saved_predictions (
    id               VARCHAR(36)   PRIMARY KEY,
    label            VARCHAR(255)  NOT NULL,
    saved_at         TIMESTAMP     NOT NULL,
    latest_draw_date DATE,
    combos_json      TEXT          NOT NULL,
    user_id          VARCHAR(255),
    created_at       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_saved_predictions_saved_at ON saved_predictions (saved_at DESC);
CREATE INDEX IF NOT EXISTS idx_saved_predictions_user_id  ON saved_predictions (user_id);

COMMENT ON TABLE  saved_predictions                  IS 'Predicciones de combinaciones generadas por el usuario, para comparar con sorteos posteriores';
COMMENT ON COLUMN saved_predictions.combos_json      IS 'JSON con el arreglo de combinaciones generadas (GeneratedCombo[])';
COMMENT ON COLUMN saved_predictions.latest_draw_date IS 'Fecha del sorteo más reciente conocido al momento de guardar; sorteos posteriores son los comparados';
COMMENT ON COLUMN saved_predictions.user_id          IS 'Reservado para soporte multi-usuario futuro; NULL = predicción global';
