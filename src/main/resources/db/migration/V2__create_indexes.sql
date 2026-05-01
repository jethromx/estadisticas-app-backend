CREATE INDEX idx_lottery_draws_type      ON lottery_draws (lottery_type);
CREATE INDEX idx_lottery_draws_date      ON lottery_draws (draw_date);
CREATE INDEX idx_lottery_draws_type_date ON lottery_draws (lottery_type, draw_date DESC);
CREATE INDEX idx_lottery_draws_type_num  ON lottery_draws (lottery_type, draw_number);
