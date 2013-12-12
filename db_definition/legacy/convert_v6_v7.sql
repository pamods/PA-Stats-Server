INSERT INTO v2_settings (report_version) VALUES (7);

insert into v2_game (id, ident, start_time, end_time) SELECT id, ident, starttime, endtime FROM game;

insert into v2_player_display_name (display_name) SELECT display_name FROM player;

insert into v2_player (id, current_display_name) 
SELECT p.id, d.id FROM player p, v2_player_display_name d 
where p.display_name = d.display_name;

insert into v2_player_game_rel (p, g, earliest_unlock_time) SELECT distinct p, g, now()::timestamp from timepoint_stats;

insert into v2_timepoint_stats (player_game, timepoint, army_count, metal_income, energy_income, metal_income_net, energy_income_net, metal_spending, energy_spending, metal_stored, energy_stored, metal_collected, energy_collected, metal_wasted, energy_wasted, apm) 
SELECT r.id, timepoint, army_count, metal_income, energy_income, metal_income_net, energy_income_net, metal_spending, energy_spending, metal_stored, energy_stored, metal_collected, energy_collected, metal_wasted, energy_wasted, apm 
FROM timepoint_stats t, v2_player_game_rel r WHERE r.p = t.p AND r.g = t.g;

insert into v2_player_game_rel (p, g, earliest_unlock_time) SELECT o.p, o.g, now()::timestamp from observed_players o WHERE (select count(*) from v2_player_game_rel r where o.g = r.g AND o.p = r.p) = 0;

select setval('v2_game_id_seq', (select max(id)+1 from v2_game), false);
select setval('v2_player_id_seq', (select max(id)+1 from v2_player), false);
select setval('v2_timepoint_stats_id_seq', (select max(id)+1 from v2_timepoint_stats), false);

analyze;