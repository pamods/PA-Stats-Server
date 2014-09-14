CREATE TABLE v2_planet_json (
	id serial PRIMARY KEY,
	planet text NOT NULL
);

insert into v2_planet_json (id, planet) select o.id, '{"name": "'||o.planet_name||'","planets": [{"name": "'||o.planet_name||'","mass": 1000,"starting_planet": true,"required_thrust_to_move": 0,"position_x": 15000,"position_y": 0,"velocity_x": 0,"velocity_y": 182,"planet": {"temperature":'||o.temperature||',"seed": '||o.seed||',"radius": '||o.radius||',"biome": "'||o.biome||'","waterHeight": '||o.water_height||',"heightRange": '||o.height_range||',"metalDensity": 50,"biomeScale": 100,"metalClusters": 50,"metal_density": 50,"metal_clusters": 50,"index": 0}}]}' from v2_planet o;

ALTER TABLE v2_game DROP CONSTRAINT v2_game_planet_fkey;

ALTER TABLE v2_game
  ADD CONSTRAINT v2_game_planet_json_fkey FOREIGN KEY (planet)
      REFERENCES v2_planet_json (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;

drop table v2_planet;

select setval('v2_planet_json_id_seq', (select max(id)+1 from v2_planet_json where id < 530000), false);

update v2_settings set report_version = 17;

analyze;