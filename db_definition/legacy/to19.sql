ALTER TABLE v2_game add column automatch boolean default false;

update v2_game set automatch = true where 1 = (select count(*) from v2_planet_json where planet ilike '%ranked%' and id = v2_game.planet);  

update v2_settings set report_version = 19;

analyze;