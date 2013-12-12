executed!

ALTER TABLE v2_player_game_rel DROP COLUMN earliest_unlock_time;
ALTER TABLE v2_player_game_rel ADD COLUMN locked boolean NOT NULL DEFAULT false;