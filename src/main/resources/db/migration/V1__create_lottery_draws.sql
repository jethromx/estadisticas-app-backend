CREATE TABLE IF NOT EXISTS lottery_draws (
    id                   BIGSERIAL PRIMARY KEY,
    lottery_type         VARCHAR(20)    NOT NULL,
    draw_number          INTEGER        NOT NULL,
    draw_date            DATE           NOT NULL,
    number_1             INTEGER        NOT NULL,
    number_2             INTEGER        NOT NULL,
    number_3             INTEGER        NOT NULL,
    number_4             INTEGER        NOT NULL,
    number_5             INTEGER        NOT NULL,
    number_6             INTEGER,
    number_7             INTEGER,
    number_8             INTEGER,
    additional_number    INTEGER,
    jackpot_amount       DECIMAL(20, 2),
    first_prize_winners  INTEGER,
    created_at           TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_lottery_draw UNIQUE (lottery_type, draw_number)
);

COMMENT ON TABLE  lottery_draws                 IS 'Histórico de sorteos de juegos de pronósticos de Lotería Nacional';
COMMENT ON COLUMN lottery_draws.lottery_type    IS 'Tipo: MELATE, REVANCHA, REVANCHITA, GANA_GATO';
COMMENT ON COLUMN lottery_draws.draw_number     IS 'Número de concurso (CONCURSO en el CSV)';
COMMENT ON COLUMN lottery_draws.number_1        IS 'Primer número sorteado';
COMMENT ON COLUMN lottery_draws.number_6        IS 'Sexto número (NULL en GanaGato-5)';
COMMENT ON COLUMN lottery_draws.number_7        IS 'Séptimo número (GanaGato F7)';
COMMENT ON COLUMN lottery_draws.number_8        IS 'Octavo número (GanaGato F8)';
COMMENT ON COLUMN lottery_draws.additional_number IS 'Número adicional de Melate (R7)';
COMMENT ON COLUMN lottery_draws.jackpot_amount  IS 'Monto de la bolsa en pesos mexicanos';
