DROP TABLE IF EXISTS game CASCADE;
DROP TABLE IF EXISTS player CASCADE;
DROP TABLE IF EXISTS timepoint_stats CASCADE;
DROP TABLE IF EXISTS settings CASCADE;
DROP TABLE IF EXISTS observed_players CASCADE;

CREATE TABLE settings (
	report_version integer
);

CREATE TABLE game (
	id serial PRIMARY KEY,
	ident varchar UNIQUE NOT NULL,
	startTime timestamp NOT NULL,
	endTime timestamp
);

CREATE TABLE player (
	id serial PRIMARY KEY,
	display_name varchar NOT NULL UNIQUE
);

CREATE TABLE observed_players (
	p integer REFERENCES player(id) ON DELETE CASCADE NOT NULL,
	g integer REFERENCES game(id) ON DELETE CASCADE NOT NULL,
	UNIQUE(p, g)
);

create index on observed_players (g);
create index on observed_players (p);

CREATE TABLE timepoint_stats (
	id serial PRIMARY KEY,
	timepoint timestamp NOT NULL,
	p integer REFERENCES player(id) ON DELETE CASCADE NOT NULL,
	g integer REFERENCES game(id) on DELETE CASCADE NOT NULL,
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
	apm integer NOT NULL,
	UNIQUE(p, g, timepoint)
);

create index on timepoint_stats (g);