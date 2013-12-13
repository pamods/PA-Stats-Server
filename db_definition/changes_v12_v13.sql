CREATE TABLE v2_teams (
	id integer PRIMARY KEY,
	ingame_id integer default 0 NOT NULL,
	primary_color varchar,
	secondary_color varchar
);

alter table v2_player_game_rel add column t integer REFERENCES v2_teams(id) ON DELETE CASCADE;

alter table v2_game add column winner_team integer;

CREATE SEQUENCE team_id start 2;
insert into v2_teams (id, primary_color, secondary_color) VALUES (1);

update v2_player_game_rel set t = 1;
alter table v2_player_game_rel alter column t set not null;

insert into v2_planet (id, seed, temperature, water_height, height_range, radius, biome, planet_name) VALUES (5390000, 0, 0, 0, 0, 0, 'unknown', 'legacy data');
update v2_game set planet = 5390000 where planet is null;
alter table v2_game alter column planet set not null;

update v2_settings set report_version = 13;

analyze;