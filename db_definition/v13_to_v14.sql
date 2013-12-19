CREATE TABLE v2_spec_keys (
	id serial PRIMARY KEY,
	spec varchar
);

CREATE TABLE v2_army_events (
	id serial PRIMARY KEY,
	player_game integer REFERENCES v2_player_game_rel(id) ON DELETE CASCADE NOT NULL,
	spec_id integer REFERENCES v2_spec_keys(id),
	x real,
	y real,
	z real,
	planet_id integer,
	watchType integer,
	timepoint timestamp
);

create index on v2_army_events (player_game);

update v2_settings set report_version = 14;

analyze;