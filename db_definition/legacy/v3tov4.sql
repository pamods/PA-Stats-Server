THIS HAS BEEN ALREADY EXECUTED

ALTER TABLE timepoint_stats RENAME COLUMN metal_income TO metal_income_net;
ALTER TABLE timepoint_stats RENAME COLUMN energy_income TO energy_income_net;

ALTER TABLE timepoint_stats ADD COLUMN metal_income integer NOT NULL DEFAULT 0;
ALTER TABLE timepoint_stats ADD COLUMN metal_spending integer NOT NULL DEFAULT 0;
ALTER TABLE timepoint_stats ADD COLUMN energy_income integer NOT NULL DEFAULT 0;
ALTER TABLE timepoint_stats ADD COLUMN energy_spending integer NOT NULL DEFAULT 0;
ALTER TABLE timepoint_stats ADD COLUMN apm integer NOT NULL DEFAULT 0;