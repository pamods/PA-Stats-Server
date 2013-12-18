CREATE TABLE v2_spec_keys (
	id serial PRIMARY KEY,
	spec varchar
);

CREATE TABLE v2_army_events (
	id serial PRIMARY KEY,
	spec_id integer REFERENCES v2_spec_keys(id),
	x real,
	y real,
	z real,
	planet_id integer,
	watchType integer,
	timepoint timestamp
);

update v2_settings set report_version = 14;

analyze;