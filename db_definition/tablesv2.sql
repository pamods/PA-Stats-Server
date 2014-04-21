DROP TABLE IF EXISTS v2_settings CASCADE;
DROP TABLE IF EXISTS v2_game CASCADE;
DROP TABLE IF EXISTS v2_player_display_name CASCADE;
DROP TABLE IF EXISTS v2_display_name_history  CASCADE;
DROP TABLE IF EXISTS v2_player CASCADE;
DROP TABLE IF EXISTS v2_player_game_rel  CASCADE;
DROP TABLE IF EXISTS v2_timepoint_stats CASCADE;
DROP TABLE IF EXISTS v2_planet_json CASCADE;
DROP TABLE IF EXISTS v2_teams CASCADE;
DROP TABLE IF EXISTS v2_army_events CASCADE;
DROP TABLE IF EXISTS v2_spec_keys CASCADE;

DROP SEQUENCE IF EXISTS team_id;

CREATE TABLE v2_settings (
	report_version integer
);

INSERT INTO v2_settings (report_version) VALUES (17);

CREATE TABLE v2_planet_json (
	id serial PRIMARY KEY,
	planet text NOT NULL
);

CREATE TABLE v2_game (
	id serial PRIMARY KEY,
	ident varchar UNIQUE NOT NULL,
	start_time timestamp NOT NULL,
	end_time timestamp NOT NULL,
	planet integer references v2_planet_json (id) NOT NULL,
	pa_version varchar default 'unknown',
	winner varchar default 'unknown',
	winner_team integer
);

CREATE TABLE v2_player_display_name (
	id serial PRIMARY KEY,
	display_name varchar UNIQUE
);

CREATE TABLE v2_player (
	id serial PRIMARY KEY,
	uber_name varchar UNIQUE,
	current_display_name integer REFERENCES v2_player_display_name(id)
);

CREATE TABLE v2_display_name_history (
	id serial PRIMARY KEY,
	name_id integer REFERENCES v2_player_display_name NOT NULL,
	player_id integer REFERENCES v2_player NOT NULL,
	replaced_on timestamp NOT NULL
);

CREATE TABLE v2_teams (
	id integer PRIMARY KEY,
	ingame_id integer default 0 NOT NULL,
	primary_color varchar,
	secondary_color varchar
);

CREATE SEQUENCE team_id start 1;

CREATE TABLE v2_player_game_rel (
	id serial PRIMARY KEY,
	p integer REFERENCES v2_player(id) ON DELETE CASCADE NOT NULL,
	g integer REFERENCES v2_game(id) ON DELETE CASCADE NOT NULL,
	t integer REFERENCES v2_teams(id) ON DELETE CASCADE NOT NULL,
	locked boolean default 'false' NOT NULL,
	died boolean default 'false' NOT NULL,
	UNIQUE(g, p)
);
   
CREATE TABLE v2_timepoint_stats (
	id serial PRIMARY KEY,
	player_game integer REFERENCES v2_player_game_rel(id) ON DELETE CASCADE NOT NULL,
	timepoint timestamp NOT NULL,
	army_count integer NOT NULL,
	metal_income integer NOT NULL,
	energy_income integer NOT NULL,
	metal_income_net integer NOT NULL,
	energy_income_net integer NOT NULL,
	metal_spending integer NOT NULL,
	energy_spending integer NOT NULL,
	metal_stored integer NOT NULL,
	energy_stored integer NOT NULL,
	metal_collected integer NOT NULL,
	energy_collected integer NOT NULL,
	metal_wasted integer NOT NULL,
	energy_wasted integer NOT NULL,
	apm integer NOT NULL
);

create index on v2_timepoint_stats (player_game);

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