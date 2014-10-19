ALTER TABLE v2_timepoint_stats add column sim_speed integer default 100 NOT NULL
update v2_settings set report_version = 20;
