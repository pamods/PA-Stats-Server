ALTER TABLE v2_player add column rating real default 1600;

ALTER TABLE v2_game add column rated boolean default false;

update v2_settings set report_version = 18;

analyze;