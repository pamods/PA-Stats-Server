UPDATE timepoint_stats SET apm = 0;

UPDATE settings set report_version=6;

CREATE TABLE observed_players (
	p integer REFERENCES player(id) ON DELETE CASCADE NOT NULL,
	g integer REFERENCES game(id) ON DELETE CASCADE NOT NULL,
	UNIQUE(p, g)
);