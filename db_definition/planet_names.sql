CREATE OR REPLACE FUNCTION convert_to_json(v_input text)
RETURNS JSON AS $$
DECLARE v_json_value JSON DEFAULT NULL;
BEGIN
    BEGIN
        v_json_value := v_input::JSON;
    EXCEPTION WHEN OTHERS THEN
        RETURN '{}'::JSON;
    END;
RETURN v_json_value;
END;
$$ LANGUAGE plpgsql;

ALTER TABLE v2_planet_json add column name text default 'System';
update v2_planet_json set name = convert_to_json(planet)->>'name' where name = 'System';
create index on v2_planet_json (name);