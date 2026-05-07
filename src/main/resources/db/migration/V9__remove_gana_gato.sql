-- Remove GanaGato game data and update comments
DELETE FROM lottery_draws WHERE lottery_type = 'GANA_GATO';

COMMENT ON COLUMN lottery_draws.lottery_type IS 'Tipo: MELATE, REVANCHA, REVANCHITA';
