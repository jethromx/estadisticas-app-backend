CREATE TABLE IF NOT EXISTS user_activity_log (
    id            BIGSERIAL     PRIMARY KEY,
    user_id       VARCHAR(36)   NOT NULL,
    endpoint      VARCHAR(255)  NOT NULL,
    http_method   VARCHAR(10)   NOT NULL,
    action        VARCHAR(100)  NOT NULL,
    lottery_type  VARCHAR(20),
    timestamp     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metadata_json TEXT,

    CONSTRAINT fk_activity_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_activity_user_id   ON user_activity_log (user_id);
CREATE INDEX IF NOT EXISTS idx_activity_timestamp  ON user_activity_log (timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_activity_action     ON user_activity_log (action);

COMMENT ON TABLE  user_activity_log               IS 'Registro de actividad por usuario para métricas de uso';
COMMENT ON COLUMN user_activity_log.action        IS 'Nombre de la acción derivada del endpoint: SAVE_PREDICTION, ANALYZE, SYNC, etc.';
COMMENT ON COLUMN user_activity_log.metadata_json IS 'Datos adicionales de contexto (predictionId, etc.)';
