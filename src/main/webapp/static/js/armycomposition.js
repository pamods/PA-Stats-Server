$(document).ready(function() {
	var GHOSTERY_TIME = 10000;
	
	var unitTypeMapping = {"/pa/units/air/base_flyer/base_flyer.json":["Mobile","Air","NoBuild"],"/pa/units/air/air_scout/air_scout.json":["Scout","Mobile","Air","Basic","FactoryBuild","Mobile","Air","NoBuild"],"/pa/units/commanders/base_commander/base_commander.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/land/assault_bot/assault_bot.json":["Bot","Mobile","Offense","Land","Basic","FactoryBuild","CannonBuildable","Bot","Mobile","Land","NoBuild"],"/pa/units/land/assault_bot_adv/assault_bot_adv.json":["Bot","Mobile","Offense","Land","Advanced","FactoryBuild","CannonBuildable","Bot","Mobile","Land","NoBuild"],"/pa/units/land/base_bot/base_bot.json":["Bot","Mobile","Land","NoBuild"],"/pa/units/land/artillery_short/artillery_short.json":["Structure","Artillery","Defense","FabBuild","Basic","Structure","NoBuild"],"/pa/units/land/anti_nuke_launcher/anti_nuke_launcher.json":["Land","Structure","Defense","NukeDefense","Advanced","Factory","FabAdvBuild","Important","Structure","NoBuild"],"/pa/units/air/air_factory/air_factory.json":["Factory","Construction","Air","Structure","Basic","CmdBuild","FabBuild","FabAdvBuild","Important","Structure","NoBuild"],"/pa/units/land/air_defense_adv/air_defense_adv.json":["Structure","Advanced","Land","AirDefense","Defense","FabAdvBuild","Structure","NoBuild"],"/pa/units/air/air_factory_adv/air_factory_adv.json":["Factory","Construction","Air","Structure","Advanced","Important","Structure","NoBuild"],"/pa/units/land/base_structure/base_structure.json":["Structure","NoBuild"],"/pa/units/land/air_defense/air_defense.json":["Structure","Basic","Land","AirDefense","Defense","CmdBuild","FabBuild","FabAdvBuild","CombatFabAdvBuild","Structure","NoBuild"],"/pa/units/land/avatar_factory/avatar_factory.json":["Factory","Construction","Land","Tank","Structure","Structure","NoBuild"],"/pa/units/land/artillery_long/artillery_long.json":["Land","Structure","Defense","Artillery","Advanced","FabAdvBuild","Important","Structure","NoBuild"],"/pa/units/land/base_vehicle/base_vehicle.json":["Tank","Mobile","Land","NoBuild"],"/pa/units/land/aa_missile_vehicle/aa_missile_vehicle.json":["Tank","Mobile","AirDefense","Land","Basic","FactoryBuild","CannonBuildable","Offense","Tank","Mobile","Land","NoBuild"],"/pa/units/land/land_mine/land_mine.json":["Basic","Land","Defense","CombatFabBuild","CombatFabAdvBuild"],"/pa/units/orbital/base_orbital/base_orbital.json":["Mobile","Orbital","NoBuild"],"/pa/units/orbital/base_orbital_structure/base_orbital_structure.json":["Structure","NoBuild"],"/pa/units/sea/base_ship/base_ship.json":["Naval","Mobile","NoBuild"],"/pa/units/sea/attack_sub/attack_sub.json":["Naval","Mobile","Offense","Basic","Sub","FactoryBuild","Naval","Mobile","NoBuild"],"/pa/units/sea/drone_carrier/drone/drone.json":["Scout","Mobile","Air","Basic","Mobile","Air","NoBuild"],"/pa/units/air/transport/transport.json":["Air","Mobile","Transport","Basic","FactoryBuild","Mobile","Air","NoBuild"],"/pa/units/air/fabrication_aircraft_adv/fabrication_aircraft_adv.json":["Air","Fabber","Construction","Mobile","Advanced","FactoryBuild","Mobile","Air","NoBuild"],"/pa/units/air/gunship/gunship.json":["Gunship","Air","Mobile","Offense","Advanced","FactoryBuild","Mobile","Air","NoBuild"],"/pa/units/air/bomber/bomber.json":["Bomber","Mobile","Offense","Air","Basic","FactoryBuild","Mobile","Air","NoBuild"],"/pa/units/air/fighter/fighter.json":["Fighter","Air","Mobile","Offense","Basic","FactoryBuild","Mobile","Air","NoBuild"],"/pa/units/air/titan_air/titan_air.json":["Bomber","Mobile","Offense","Air","Advanced","Titan","Important","FabOrbBuild","Mobile","Air","NoBuild"],"/pa/units/air/solar_drone/solar_drone.json":["Air","EnergyProduction","Mobile","Basic","FactoryBuild","Economy","Mobile","Air","NoBuild"],"/pa/units/air/fighter_adv/fighter_adv.json":["Fighter","Air","Mobile","Offense","Advanced","FactoryBuild","Mobile","Air","NoBuild"],"/pa/units/air/fabrication_aircraft/fabrication_aircraft.json":["Air","Fabber","Construction","Mobile","Basic","FactoryBuild","Mobile","Air","NoBuild"],"/pa/units/air/support_platform/support_platform.json":["Air","Mobile","MissileDefense","Advanced","FactoryBuild","Mobile","Air","NoBuild"],"/pa/units/air/bomber_adv/bomber_adv.json":["Bomber","Mobile","Offense","Air","Tactical","Advanced","FactoryBuild","Mobile","Air","NoBuild"],"/pa/units/air/bomber_heavy/bomber_heavy.json":["Bomber","Mobile","Offense","Air","Advanced","FactoryBuild","Mobile","Air","NoBuild"],"/pa/units/commanders/imperial_alpha/imperial_alpha.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/imperial_aceal/imperial_aceal.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/quad_base/quad_base.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/quad_armalisk/quad_armalisk.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/raptor_base/raptor_base.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/imperial_able/imperial_able.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/tank_base/tank_base.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/tank_aeson/tank_aeson.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/imperial_base/imperial_base.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/tank_banditks/tank_banditks.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/imperial_aryst0krat/imperial_aryst0krat.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/quad_ajax/quad_ajax.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/land/fabrication_vehicle_adv/fabrication_vehicle_adv.json":["Fabber","Construction","Tank","Mobile","Land","Advanced","FactoryBuild","CannonBuildable","Tank","Mobile","Land","NoBuild"],"/pa/units/land/tank_nuke/tank_nuke.json":["Tank","Heavy","Mobile","Offense","Land","Advanced","FactoryBuild","Important","Tank","Mobile","Land","NoBuild"],"/pa/units/land/tank_light_laser/tank_light_laser.json":["Tank","Mobile","Offense","Land","Basic","FactoryBuild","CannonBuildable","Tank","Mobile","Land","NoBuild"],"/pa/units/land/fabrication_vehicle/fabrication_vehicle.json":["Fabber","Construction","Tank","Mobile","Basic","Land","FactoryBuild","CannonBuildable","Tank","Mobile","Land","NoBuild"],"/pa/units/land/tank_armor/tank_armor.json":["Tank","Heavy","Mobile","Offense","Land","Basic","FactoryBuild","Tank","Mobile","Land","NoBuild"],"/pa/units/land/tank_hover/tank_hover.json":["Tank","Mobile","Offense","Land","Basic","Hover","FactoryBuild","Tank","Mobile","Land","NoBuild"],"/pa/units/land/tank_heavy_armor/tank_heavy_armor.json":["Tank","Heavy","Mobile","Offense","Land","Advanced","FactoryBuild","Tank","Mobile","Land","NoBuild"],"/pa/units/land/tank_heavy_mortar/tank_heavy_mortar.json":["Tank","Mobile","Offense","Land","Artillery","Advanced","FactoryBuild","Tank","Mobile","Land","NoBuild"],"/pa/units/land/bot_tactical_missile/bot_tactical_missile.json":["Bot","Mobile","Offense","Land","Tactical","Advanced","FactoryBuild","Tank","Mobile","Land","NoBuild"],"/pa/units/land/tank_laser_adv/tank_laser_adv.json":["Tank","Mobile","Offense","Land","Advanced","FactoryBuild","Tank","Mobile","Land","NoBuild"],"/pa/units/land/tank_flak/tank_flak.json":["Tank","Mobile","Offense","AirDefense","Land","Advanced","FactoryBuild","Tank","Mobile","Land","NoBuild"],"/pa/units/land/titan_vehicle/titan_vehicle.json":["Tank","Mobile","Offense","Land","Advanced","Titan","Hover","Important","FabOrbBuild","Tank","Mobile","Land","NoBuild"],"/pa/units/land/bot_aa/bot_aa.json":["Bot","Mobile","Offense","AirDefense","Land","Basic","Tank","Mobile","Land","NoBuild"],"/pa/units/land/land_scout/land_scout.json":["Tank","Scout","Mobile","Land","Basic","FactoryBuild","Tank","Mobile","Land","NoBuild"],"/pa/units/land/fabrication_bot_adv/fabrication_bot_adv.json":["Fabber","Construction","Bot","Mobile","Land","Advanced","FactoryBuild","CannonBuildable","Bot","Mobile","Land","NoBuild"],"/pa/units/land/bot_bomb/bot_bomb.json":["Bot","Mobile","Land","Basic","FactoryBuild","CannonBuildable","Offense","SelfDestruct","Bot","Mobile","Land","NoBuild"],"/pa/units/land/bot_sniper/bot_sniper.json":["Bot","Mobile","Offense","Land","Artillery","Advanced","FactoryBuild","Bot","Mobile","Land","NoBuild"],"/pa/units/land/bot_nanoswarm/bot_nanoswarm.json":["Bot","Mobile","Land","Offense","Advanced","Deconstruction","FactoryBuild","Hover","CannonBuildable","Bot","Mobile","Land","NoBuild"],"/pa/units/land/titan_bot/titan_bot.json":["Bot","Mobile","Offense","Land","Advanced","Titan","Important","FabOrbBuild","Bot","Mobile","Land","NoBuild"],"/pa/units/land/fabrication_bot_combat/fabrication_bot_combat.json":["Construction","Bot","Mobile","Offense","Land","Basic","FactoryBuild","CannonBuildable","Bot","Mobile","Land","NoBuild"],"/pa/units/land/fabrication_bot_combat_adv/fabrication_bot_combat_adv.json":["Construction","Bot","Mobile","Land","Advanced","FactoryBuild","CannonBuildable","Bot","Mobile","Land","NoBuild"],"/pa/units/land/bot_spider_adv/bot_spider_adv.json":["Bot","Mobile","Offense","Land","Bot","Mobile","Land","NoBuild"],"/pa/units/commanders/tutorial_titan_commander/tutorial_titan_commander.json":["Bot","Mobile","Offense","Land","Advanced","Titan","Important","Commander","Bot","Mobile","Offense","Land","Advanced","Titan","Important","FabOrbBuild","Bot","Mobile","Land","NoBuild"],"/pa/units/land/fabrication_bot/fabrication_bot.json":["Fabber","Construction","Bot","Mobile","Land","Basic","FactoryBuild","CannonBuildable","Bot","Mobile","Land","NoBuild"],"/pa/units/land/bot_support_commander/bot_support_commander.json":["Bot","Fabber","Construction","SupportCommander","Mobile","Land","Advanced","FactoryBuild","Bot","Mobile","Land","NoBuild"],"/pa/units/land/bot_tesla/bot_tesla.json":["Bot","Mobile","Offense","Artillery","Land","Basic","FactoryBuild","CannonBuildable","Bot","Mobile","Land","NoBuild"],"/pa/units/land/bot_grenadier/bot_grenadier.json":["Bot","Mobile","Offense","Artillery","Land","Basic","FactoryBuild","CannonBuildable","Bot","Mobile","Land","NoBuild"],"/pa/units/sea/torpedo_launcher/torpedo_launcher.json":["Structure","Basic","Naval","Defense","CmdBuild","FabBuild","CombatFabAdvBuild","Structure","NoBuild"],"/pa/units/land/energy_storage/energy_storage.json":["Structure","Basic","CmdBuild","FabBuild","FabAdvBuild","Economy","Structure","NoBuild"],"/pa/units/land/land_barrier/land_barrier.json":["Structure","Basic","Wall","FabBuild","FabAdvBuild","CmdBuild","CombatFabAdvBuild","Structure","NoBuild"],"/pa/units/sea/naval_factory/naval_factory.json":["Factory","Construction","Naval","Structure","CmdBuild","Basic","FabBuild","FabAdvBuild","Important","Structure","NoBuild"],"/pa/units/sea/naval_factory_adv/naval_factory_adv.json":["Factory","Construction","Naval","Structure","Advanced","Important","Structure","NoBuild"],"/pa/units/land/radar/radar.json":["Land","Structure","Basic","FabBuild","Recon","Structure","NoBuild"],"/pa/units/sea/sea_mine/sea_mine.json":["Structure","Naval","Defense","Basic","Structure","NoBuild"],"/pa/units/orbital/deep_space_radar/deep_space_radar.json":["Structure","NoBuild"],"/pa/units/land/laser_defense_adv/laser_defense_adv.json":["Structure","Advanced","Land","SurfaceDefense","Defense","FabAdvBuild","Structure","NoBuild"],"/pa/units/sea/torpedo_launcher_adv/torpedo_launcher_adv.json":["Structure","Advanced","Naval","Defense","FabAdvBuild","Structure","NoBuild"],"/pa/units/orbital/ion_defense/ion_defense.json":["Structure","Basic","Defense","OrbitalDefense","FabBuild","FabAdvBuild","Structure","NoBuild"],"/pa/units/land/nuke_launcher/nuke_launcher.json":["Land","Structure","Offense","Advanced","Nuke","Factory","FabAdvBuild","Important","Structure","NoBuild"],"/pa/units/land/energy_plant/energy_plant.json":["Structure","EnergyProduction","Basic","CmdBuild","FabBuild","Economy","Structure","NoBuild"],"/pa/units/land/laser_defense_single/laser_defense_single.json":["Structure","Basic","Land","SurfaceDefense","Defense","FabBuild","CmdBuild","CombatFabAdvBuild","Structure","NoBuild"],"/pa/units/land/energy_plant_adv/energy_plant_adv.json":["Structure","EnergyProduction","Advanced","FabAdvBuild","Economy","Structure","NoBuild"],"/pa/units/land/radar_adv/radar_adv.json":["Land","Structure","Advanced","Recon","FabAdvBuild","Structure","NoBuild"],"/pa/units/land/metal_extractor_adv/metal_extractor_adv.json":["Structure","Advanced","MetalProduction","FabAdvBuild","Economy","Structure","NoBuild"],"/pa/units/land/unit_cannon/unit_cannon.json":["Structure","Factory","Advanced","Artillery","FabAdvBuild","Important","Structure","NoBuild"],"/pa/units/land/metal_storage/metal_storage.json":["Structure","Basic","CmdBuild","FabBuild","FabAdvBuild","Economy","Structure","NoBuild"],"/pa/units/orbital/delta_v_engine/delta_v_engine.json":["Factory","Construction","Structure","Land","PlanetEngine","Advanced","FabAdvBuild","Important","Structure","NoBuild"],"/pa/units/land/tactical_missile_launcher/tactical_missile_launcher.json":["Land","Structure","Tactical","Defense","Advanced","FabAdvBuild","Structure","NoBuild"],"/pa/units/land/teleporter/teleporter.json":["Structure","Teleporter","CmdBuild","FabBuild","FabAdvBuild","CombatFabAdvBuild","CombatFabBuild","FabOrbBuild","Structure","NoBuild"],"/pa/units/land/bot_factory_adv/bot_factory_adv.json":["Factory","Construction","Land","Bot","Structure","Advanced","Important","Structure","NoBuild"],"/pa/units/land/laser_defense/laser_defense.json":["Structure","Basic","Land","SurfaceDefense","Defense","FabBuild","Structure","NoBuild"],"/pa/units/land/bot_factory/bot_factory.json":["Factory","Construction","Land","Bot","Structure","Basic","CmdBuild","FabBuild","FabAdvBuild","Important","Structure","NoBuild"],"/pa/units/orbital/orbital_launcher/orbital_launcher.json":["Orbital","Factory","Construction","Structure","Land","FabBuild","FabAdvBuild","Basic","Important","Structure","NoBuild"],"/pa/units/land/metal_extractor/metal_extractor.json":["Structure","Basic","MetalProduction","CmdBuild","FabBuild","Economy","Structure","NoBuild"],"/pa/units/land/vehicle_factory/vehicle_factory.json":["Factory","Construction","Land","Tank","Structure","Basic","CmdBuild","FabBuild","FabAdvBuild","Important","Structure","NoBuild"],"/pa/units/land/control_module/control_module.json":["Structure","ControlModule","Advanced","FabAdvBuild","Important","Structure","NoBuild"],"/pa/units/land/vehicle_factory_adv/vehicle_factory_adv.json":["Factory","Construction","Land","Tank","Structure","Advanced","Important","Structure","NoBuild"],"/pa/units/land/artillery_unit_launcher/artillery_unit_launcher.json":["Structure","Artillery","Defense","FabBuild","Basic","Structure","NoBuild"],"/pa/units/land/titan_structure/titan_structure.json":["Structure","Land","Advanced","SelfDestruct","FabAdvBuild","Important","Structure","NoBuild"],"/pa/units/orbital/mining_platform/mining_platform.json":["Orbital","FabOrbBuild","EnergyProduction","MetalProduction","Structure","Economy","Structure","NoBuild"],"/pa/units/orbital/defense_satellite/defense_satellite.json":["Structure","Orbital","Defense","OrbitalDefense","Advanced","FabOrbBuild","Structure","NoBuild"],"/pa/units/orbital/orbital_factory/orbital_factory.json":["Factory","Construction","Orbital","FabOrbBuild","Structure","Advanced","Important","Structure","NoBuild"],"/pa/units/orbital/orbital_battleship/orbital_battleship.json":["Mobile","Offense","Orbital","Fighter","Heavy","Advanced","FactoryBuild","Mobile","Orbital","NoBuild"],"/pa/units/orbital/solar_array/solar_array.json":["Mobile","Orbital","EnergyProduction","Economy","Advanced","FactoryBuild","Mobile","Orbital","NoBuild"],"/pa/units/orbital/orbital_carrier/orbital_carrier.json":["Orbital","Mobile","Transport","Advanced","FactoryBuild","Mobile","Orbital","NoBuild"],"/pa/units/orbital/orbital_lander/orbital_lander.json":["Orbital","Mobile","Transport","Basic","FactoryBuild","Mobile","Orbital","NoBuild"],"/pa/units/orbital/orbital_fabrication_bot/orbital_fabrication_bot.json":["Orbital","Fabber","Construction","Mobile","Basic","FactoryBuild","Mobile","Orbital","NoBuild"],"/pa/units/orbital/orbital_laser/orbital_laser.json":["Mobile","Offense","Orbital","LaserPlatform","Advanced","FactoryBuild","Mobile","Orbital","NoBuild"],"/pa/units/orbital/orbital_probe/orbital_probe.json":["Mobile","Orbital","Basic","Scout","FactoryBuild","Mobile","Orbital","NoBuild"],"/pa/units/orbital/orbital_fighter/orbital_fighter.json":["Mobile","Offense","Orbital","Fighter","Basic","FactoryBuild","Mobile","Orbital","NoBuild"],"/pa/units/orbital/titan_orbital/titan_orbital.json":["Mobile","Offense","Orbital","LaserPlatform","Advanced","Titan","FabOrbBuild","Important","Mobile","Orbital","NoBuild"],"/pa/units/orbital/radar_satellite_adv/radar_satellite_adv.json":["Mobile","Orbital","Advanced","Recon","FactoryBuild","Mobile","Orbital","NoBuild"],"/pa/units/orbital/orbital_railgun/orbital_railgun.json":["Mobile","Offense","Orbital","Fighter","Advanced","FactoryBuild","Mobile","Orbital","NoBuild"],"/pa/units/orbital/radar_satellite/radar_satellite.json":["Mobile","Orbital","Basic","Recon","FactoryBuild","Mobile","Orbital","NoBuild"],"/pa/units/sea/missile_ship/missile_ship.json":["Naval","Mobile","Offense","Advanced","Tactical","FactoryBuild","Naval","Mobile","NoBuild"],"/pa/units/sea/hover_ship/hover_ship.json":["Naval","Mobile","Offense","Advanced","Hover","FactoryBuild","Naval","Mobile","NoBuild"],"/pa/units/sea/fabrication_ship_adv/fabrication_ship_adv.json":["Fabber","Construction","Naval","Mobile","Advanced","FactoryBuild","Naval","Mobile","NoBuild"],"/pa/units/sea/battleship/battleship.json":["Naval","Mobile","Offense","Artillery","Advanced","FactoryBuild","Naval","Mobile","NoBuild"],"/pa/units/sea/fabrication_ship/fabrication_ship.json":["Fabber","Construction","Naval","Mobile","Basic","FactoryBuild","Naval","Mobile","NoBuild"],"/pa/units/sea/nuclear_sub/nuclear_sub.json":["Naval","Mobile","Offense","Advanced","Sub","FactoryBuild","Naval","Mobile","NoBuild"],"/pa/units/sea/drone_carrier/carrier/carrier.json":["Naval","Mobile","Offense","Advanced","FactoryBuild","Naval","Mobile","NoBuild"],"/pa/units/sea/sea_scout/sea_scout.json":["Naval","Scout","Mobile","Offense","Basic","FactoryBuild","Naval","Mobile","NoBuild"],"/pa/units/sea/frigate/frigate.json":["Naval","Mobile","Offense","Basic","AirDefense","FactoryBuild","Naval","Mobile","NoBuild"],"/pa/units/sea/fabrication_barge/fabrication_barge.json":["Offense","Construction","Naval","Mobile","Basic","FactoryBuild","Naval","Mobile","NoBuild"],"/pa/units/sea/destroyer/destroyer.json":["Naval","Mobile","Offense","Basic","FactoryBuild","Naval","Mobile","NoBuild"],"/pa/units/commanders/avatar/avatar.json":["Air","Fabber","Debug","Construction","Mobile","Air","Fabber","Construction","Mobile","Basic","FactoryBuild","Mobile","Air","NoBuild"],"/pa/units/commanders/quad_calyx/quad_calyx.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/imperial_thechessknight/imperial_thechessknight.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/quad_tokamaktech/quad_tokamaktech.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/raptor_stickman9000/raptor_stickman9000.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/raptor_nemicus/raptor_nemicus.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/imperial_stelarch/imperial_stelarch.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/quad_twoboots/quad_twoboots.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/quad_theflax/quad_theflax.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/imperial_toddfather/imperial_toddfather.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/imperial_mjon/imperial_mjon.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/quad_osiris/quad_osiris.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/raptor_centurion/raptor_centurion.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/quad_raventhornn/quad_raventhornn.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/raptor_beniesk/raptor_beniesk.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/imperial_gamma/imperial_gamma.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/quad_xinthar/quad_xinthar.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/raptor_iwmiked/raptor_iwmiked.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/imperial_gnugfur/imperial_gnugfur.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/quad_xenosentryprime/quad_xenosentryprime.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/quad_mobiousblack/quad_mobiousblack.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/imperial_enzomatrix/imperial_enzomatrix.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/raptor_beast/raptor_beast.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/raptor_betadyne/raptor_betadyne.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/imperial_delta/imperial_delta.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/quad_shadowdaemon/quad_shadowdaemon.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/raptor_majuju/raptor_majuju.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/raptor_diremachine/raptor_diremachine.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/raptor_zaazzaa/raptor_zaazzaa.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/raptor_rallus/raptor_rallus.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/imperial_kapowaz/imperial_kapowaz.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/imperial_theta/imperial_theta.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/imperial_invictus/imperial_invictus.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/quad_sacrificiallamb/quad_sacrificiallamb.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/quad_spartandano/quad_spartandano.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/imperial_fiveleafclover/imperial_fiveleafclover.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/quad_spiderofmean/quad_spiderofmean.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/imperial_progenitor/imperial_progenitor.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/quad_potbelly79/quad_potbelly79.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/quad_gambitdfa/quad_gambitdfa.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/imperial_sangudo/imperial_sangudo.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/imperial_seniorhelix/imperial_seniorhelix.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/imperial_chronoblip/imperial_chronoblip.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/tutorial_player_commander/tutorial_player_commander.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/tutorial_ai_commander_2/tutorial_ai_commander_2.json":["Commander","Construction","Mobile","Offense","Land","Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/tutorial_ai_commander/tutorial_ai_commander.json":["Commander","Construction","Mobile","Offense","Land","Commander","Construction","Mobile","Offense","Land","NoBuild"],"/pa/units/commanders/tutorial_ai_commander_3/tutorial_ai_commander_3.json":["Commander","Construction","Mobile","Offense","Land","NoBuild"]};

	var parseColor = function(clr) {
		if (clr == '' || clr == undefined) {
			return [0,0,0];
		} else {
			var m = clr.match(/^rgb\s*\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*\)$/i);
			return [m[1]/255,m[2]/255,m[3]/255];
		}
	};
	
	var getIconForSpec = function(spec) {
		return imageBaseUrl + "strategic_icons/icon_si_" + nameForSpec(spec)+".png";
	};
	
	var isStructure = function(spec) {
		if(unitTypeMapping[spec] === undefined) {
			console.log("missing mapping for spec!!! "+spec);
		}
		return unitTypeMapping[spec] && unitTypeMapping[spec].indexOf("Structure") !== -1;
	};
	
    var start = /[^\/]*$/;  // ^ : start , \/ : '/', $ : end // as wildcard: /*.json 
    var end = /[.]json$/;
    
    var nameForSpec = function(spec) {
    	return spec.substring(spec.search(start), spec.search(end));
    };
    
	function ArmyUnit(spc, currentShowType) {
		var self = this;
		
		self.spec = ko.observable(spc);
		self.name = ko.computed(function() {return nameForSpec(self.spec());});
		self.icon = ko.computed(function() {return imageBaseUrl +"units/"+ self.name() +".png"});
		self.sicon = ko.computed(function() {return getIconForSpec(self.spec());});
		self.linkToPaDb = ko.computed(function() {
			return "http://pa-db.com/unit/"+self.name();
		});
		
		self.aliveCount = ko.observable(0);
		self.constructedCount = ko.observable(0);
		self.lostCount = ko.observable(0);
		self.currentShowType = currentShowType; 
		self.count = ko.computed(function() {
			if (self.currentShowType() === 'ALIVE') {
				return self.aliveCount();
			} else if (self.currentShowType() === 'CONSTRUCTED') {
				return self.constructedCount();
			} else if (self.currentShowType() === 'LOST') {
				return Math.abs(self.lostCount());
			}
		});
		self.visible = ko.computed(function() {return self.count() != 0;});
	}
	
	function PlayerArmy(i, n, pClr, sClr, currentShowType, eventLocationsHandler) {
		var self = this;
		self.id = i;
		self.name = ko.observable(n);
		self.primaryColor = ko.observable(pClr);
		self.secondaryColor = ko.observable(sClr);
		self.units = ko.observableArray([]);
		
		self.currentShowType = currentShowType;
		
		self.visibleUnits = ko.computed(function() {
			return ko.utils.arrayFilter(self.units(), function(unit) {
				return unit.visible();
			});
		});
		
		self.hasUnits = ko.computed(function() {
			return self.visibleUnits().length > 0;
		});
			
		self.unitSpecIndex = {};
		
		self.playerConcerningChanges = function(evt, direction) {
			var change = evt.change * direction;
			
			if (!evt.is_ghost) {
				var index = self.unitSpecIndex[evt.spec];
				var found = index != undefined;
				var ux = undefined;
				if (found) {// found is executed once per event in the game
					// so it needs to be fast
					var unt = self.units()[index];
					
					unt.aliveCount(unt.aliveCount()+change);
					if (evt.change > 0) {
						unt.constructedCount(unt.constructedCount()+change);
					}
					if (evt.change < 0) {
						unt.lostCount(unt.lostCount()+change);
					}
					ux = unt;
				} else { // else is only executed once per unit type that occurs in the entire game
					// so the following code can be slow
					var newUnit = new ArmyUnit(evt.spec, self.currentShowType);
					newUnit.aliveCount(change);
					if (evt.change > 0) {
						newUnit.constructedCount(change);
					}
					if (evt.change < 0) {
						newUnit.lostCount(change);
					}
					ux = newUnit;
					self.units.push(newUnit);
					self.units.sort(function(left, right) {
						return left.spec() == right.spec() ? 0 : (left.spec() < right.spec() ? -1 : 1);
					});
					// sorting breaks the index, so rebuild it
					self.unitSpecIndex = {};
					for (var i = 0; i < self.units().length; i++) {
						var unt = self.units()[i];
						self.unitSpecIndex[unt.spec()] = i;
					}
				}
			}
			if (eventLocationsHandler) {
				eventLocationsHandler(self.primaryColor(), evt, direction);
			}
		}
	}
	
	function ArmyCompositionModel(start, globe) {
		var self = this;

		self.startTime = ko.observable(start);
		self.endTime = ko.observable(start);
		self.selectedTime = ko.observable(start);
		
		self.globe = globe;
		
		self.showTypes = ko.observableArray(['ALIVE', 'CONSTRUCTED', 'LOST']);
		self.currentShowType = ko.observable('ALIVE');
		
		self.formattedSelectedTime = ko.computed(function() {
			return fmtTime(self.startTime(), self.selectedTime());
		});
		
		self.timeSliderOptions = ko.computed(function() {
			return {
				min: self.startTime(),
				max: self.endTime(),
				range: 'min',
				step: 1000
			};
		});
		
		self.wasOnEnd = true;
		
		self.addEvent = function(player, spec, timestamp, typ, x, y, z, planetId) {
			var _addEvent = function(player, spec, timestamp, typ, x, y, z, planetId, isGhost) {
				var evt = {
					player: player,
					spec: spec,
					timestamp: timestamp,
					change: typ == 0 ? +1 : -1,
					x: x,
					y: y,
					z: z,
					planet_id: planetId,
					is_ghost: isGhost,
				};
				self.indexEvent(evt);
			};
			
			_addEvent(player, spec, timestamp, typ, x, y, z, planetId, false);
			
			if (typ !== 0) {
				// add ghosts of dead units
				_addEvent(player, spec, timestamp, 0, x, y, z, planetId, true);
				_addEvent(player, spec, timestamp+GHOSTERY_TIME, 2, x, y, z, planetId, true);
			}
			
			if (self.endTime() < timestamp) {
				self.endTime(timestamp);
			}
		};
		
		self.lockWasOnEnd = function() {
			var prevEnd = self.endTime();
			var border = (prevEnd - self.startTime()) * 0.05;
			self.wasOnEnd = prevEnd - self.selectedTime() < border;
		};
		
		self.selectEnd = function() {
			self.selectedTime(self.endTime());
		};
		
		self.maySelectEnd = function() {
			if (self.wasOnEnd) {
				self.selectEnd();
			}
		};
		
		self.players = ko.observableArray([]);
		
		self.visiblePlayers = ko.computed(function() {
			return ko.utils.arrayFilter(self.players(), function(player) {
				return player.hasUnits();
			});
		}, self);
		
		self.addPlayer = function (id, name, pColor, sColor) {
			self.players.push(new PlayerArmy(id, name, pColor, sColor, self.currentShowType, globe.eventsHandler));
		};
		
		var findPlayer = function(playerId) {
			for (var i = 0; i < self.players().length; i++) {
				if (self.players()[i].id == playerId) {
					return self.players()[i];
				}
			}
			return undefined;
		};
		
		self.changeSpecForPlayer = function(evt, direction) {
			var player = findPlayer(evt.player);
			player.playerConcerningChanges(evt, direction);
		};
		
		self.timeBefore = self.selectedTime();
		self.directionBefore = 1;
		
		self.eventIndex = {};
		
		var hashBucketSize = 1000;
		
		self.indexEvent = function(event) {
			var hashBucket = Math.floor(event.timestamp / hashBucketSize);
			
			if (self.eventIndex[hashBucket] === undefined) {
				self.eventIndex[hashBucket] = [];
			}
			self.eventIndex[hashBucket].push(event);
			
			self.eventIndex[hashBucket].sort(function(a, b) {
				return a.timestamp - b.timestamp;
			});
		};
		
		self.getEventsBetween = function(startInclusive, endInclusive) {
			var firstBucket = Math.floor(startInclusive / hashBucketSize);
			var lastBucket = Math.ceil(endInclusive / hashBucketSize);
			
			var results = [];
			
			for (var i = firstBucket; i <= lastBucket; i++) {
				if (self.eventIndex[i] !== undefined) {
					for (var j = 0; j < self.eventIndex[i].length; j++) {
						var evt = self.eventIndex[i][j];
						if (evt.timestamp >= startInclusive && evt.timestamp <= endInclusive) {
							results.push(evt);
						}
					}
				}
			}
			
			return results;
		};		
		
		self.selectedTime.subscribe(function(newT) {
			var direction = newT > self.timeBefore ? 1 : -1;
			var toR = newT > self.timeBefore;
			
			var a = self.timeBefore > newT ? newT : self.timeBefore;
			var b = self.timeBefore > newT ? self.timeBefore : newT;
			
			var changedDirection = direction !== self.directionBefore;
			
			if (!changedDirection) {
				if (direction === 1) {
					a += 1;
				} else {
					b -= 1;
				}
			}
			
			var effectedEvents = self.getEventsBetween(a, b);
			
			for (var i = toR ? 0 : effectedEvents.length-1; toR ? i < effectedEvents.length : i >= 0; i+=direction) {
				self.changeSpecForPlayer(effectedEvents[i], direction);
			}
			
			self.timeBefore = newT;
			self.directionBefore = direction;
		});
		
		self.newPlayersHandler = function(event, data) {
			ko.tasks.processImmediate(function() {
				for (var i = 0; i < data.value.length; i++) {
					var p = data.value[i];
					armyModel.addPlayer(p.playerId, p.name, p.pColor, p.sColor);
				}
			});
			armyModel.selectEnd();
		};
		
		var knownIds = {};
		
		var nothingReceived = true;
		
		var newEventsQueue = [];
		var workingOnQueue = false;
		
		self.newArmyEventsHandler = function(event, input) {
			newEventsQueue.extend(input.value);
			newEventsQueue.sort(function(a, b) {
				return a.time - b.time;
			});
			var processingSize = 250;
			if (!workingOnQueue) {
				var worker = function() {
					workingOnQueue = true;
					if (newEventsQueue.length > 0) {
						var takeNum = newEventsQueue.length > processingSize ? processingSize : newEventsQueue.length;
						var spliced = newEventsQueue.splice(0, takeNum);
						self.processNewEvents(spliced);
						setImmediate(worker, 0);
					} else {
						workingOnQueue = false;
					}
				};
				worker();
			}
		};
		
		self.processNewEvents = function(input) {
			armyModel.lockWasOnEnd();
			
			var data = {value: []};
			
			// TODO this filters double events, I am not sure I even have them
			// but something causes events to be processed twice in some cases, so this is the first try to prevent it
			for (var i = 0; i < input.length; i++) {
				if (!knownIds[input[i].id]) {
					data.value.push(input[i]);
					knownIds[input[i].id] = true;
				} else {
					console.log("filter out: ");
					console.log(input[i]);
				}
			}
			if (data.value.length === 0) {
				return;
			}
			
			ko.tasks.processImmediate(function() {
				data.value.sort(function(a, b) {
					return a.time - b.time;
				});
				var minTime = data.value[data.value.length-1].time;
				for (var i = 0; i < data.value.length; i++) {
					if (minTime > data.value[i].time) {
						minTime = data.value[i].time;
					}
				}
				
				var reSimUntil = undefined; 
				if (minTime <= self.selectedTime()) { // = since there may be new elements for the current timestamp as well
					reSimUntil = self.selectedTime();
					ko.tasks.processImmediate(function() {
						self.selectedTime(minTime-5000);
					});
				}
				
				for (var i = 0; i < data.value.length; i++) {
					var evt = data.value[i];
					armyModel.addEvent(evt.playerId, evt.spec, evt.time, evt.watchType, evt.x, evt.y, evt.z, evt.planetId);
				}
				
				if (reSimUntil) {
					ko.tasks.processImmediate(function() {
						self.selectedTime(reSimUntil);
					});
				}
			});
			
			if (nothingReceived) {
				armyModel.selectEnd();
				nothingReceived = false;
			} else {
				armyModel.maySelectEnd();
			}
		};	
	}
	
	function GlobeViewModel(planetInfo) {
		var self = this;
		var hasWebGL = true;
		var webGLError = "";
		
		if (!window.WebGLRenderingContext) {
			hasWebGL = false;
			webGLError = 'Your browser has no idea what WebGL is, check out <a href="http://get.webgl.org">http://get.webgl.org</a>';
		} else if (!document.createElement('canvas').getContext("webgl")) {
			hasWebGL = false;
			webGLError = 'Your browser seems to have troubles to initialize WebGL, check out <a href="http://get.webgl.org/troubleshooting">http://get.webgl.org/troubleshooting</a>';
		}
		
		self.knownPlanets = ko.observableArray([0]);
		self.selectedPlanet = ko.observable(0);
		self.planetsCount = ko.computed(function() {
			return self.knownPlanets().length;
		});
		
		if (hasWebGL) {
			self.widget = new Cesium.CesiumWidget('globediv', {
				imageryProvider : new Cesium.TileCoordinatesImageryProvider({
					color: Cesium.Color.BLACK,
				})
			});
			
			var layers = self.widget.centralBody.imageryLayers;
		    var white = layers.addImageryProvider(new Cesium.SingleTileImageryProvider({
		        url : imageBaseUrl+'white.png',
		    }));
			layers.lower(white);
			
			self.ellipsoid = self.widget.centralBody.ellipsoid;
			self.scene = self.widget.scene;
			
			self.scene.screenSpaceCameraController.enableTilt = false;
			self.scene.screenSpaceCameraController.enableLook = false;
			self.scene.screenSpaceCameraController.maximumZoomDistance = 25000000;
			self.scene.screenSpaceCameraController.minimumZoomDistance = 500000;
			self.scene.camera.controller.constrainedAxis = undefined;
			
			// unused, should be used once more plantes are listed
			self.planetInfoMap = ko.observable(planetInfo);
			
			self.textureAtlas = self.scene.context.createTextureAtlas();
			self.billboards = new Cesium.BillboardCollection();
			self.billboards.textureAtlas = self.textureAtlas;
			self.scene.primitives.add(self.billboards);
			
			self.imagesMap = {};
			self.billboardsMap = {};
			
			self.currentBillboardsByPlanet = {};
			
			self.getImagePath = getIconForSpec;
			
			function loadImageIntoAtlas(imgPath, callback) {
				var image = new Image();
				image.crossOrigin = "anonymous"; // Resolve CORS issues
				// TODO this introduces (?) a race condition that can lead to images being loaded multiple times
				// should not affect program correctness apart from that though...
				image.onload = function() {
					var newIndex = self.textureAtlas.addImage(image);
					self.imagesMap[imgPath] = newIndex;
					if (callback) {
						callback(newIndex);
					}
				};
				image.src = imgPath;
			}
			
			var starIndex = undefined;
			loadImageIntoAtlas(imageBaseUrl+"redstar.png", function(index) {
				starIndex = index;
			});
			
			function addToListInMap(map, key, obj) {
				var value = map[key];
				if (value === undefined) {
					value = [];
				}
				value.push(obj);
				map[key] = value;
			}
			
			function addBillboard(planet_id, x, y, z, spec, color, imageIndex, isGhost) {
				var distance = Math.sqrt(x * x + y * y + z * z);
				var planetSizeFactor = 6378137/distance;
				var cartesian3Position = new Cesium.Cartesian3(x*planetSizeFactor, y*planetSizeFactor, z*planetSizeFactor);
				var handle = self.billboards.add({
					position: cartesian3Position,
					color: new Cesium.Color(color[0], color[1], color[2], 1),
					imageIndex: imageIndex
				});
				
				if (isGhost) {
					handle.extra = self.billboards.add({
						position: cartesian3Position,
						color: new Cesium.Color(1, 1, 1, 0.2),
						imageIndex: starIndex
					});
				}
				
				var key = x+"/"+y+"/"+z+"/"+spec+"/"+isGhost+"/"+planet_id;
				addToListInMap(self.billboardsMap, key, handle);

				var planetMap = self.currentBillboardsByPlanet[planet_id+""];
				if (planetMap === undefined) {
					planetMap = {};
				}
				addToListInMap(planetMap, key, handle);
				self.currentBillboardsByPlanet[planet_id+""] = planetMap;

				handle.setShow(self.selectedPlanet() === planet_id);
				if (handle.extra) {
					handle.extra.setShow(self.selectedPlanet() === planet_id);
				}
				
				return handle;
			}
			
			var planetBefore = 0;
			self.selectedPlanet.subscribe(function(v) {
				var o = self.currentBillboardsByPlanet[planetBefore+""];
				var n = self.currentBillboardsByPlanet[v+""];
				
				var setVisible = function(a, visible) {
					for (p in a) {
						if (a.hasOwnProperty(p)) {
							for (var i = 0; i < a[p].length; i++) {
								a[p][i].setShow(visible);
								if (a[p][i].extra) {
									a[p][i].extra.setShow(visible);
								}
							}
						}
					}
				}
				setVisible(o, false);
				setVisible(n, true);
				
				planetBefore = v;
			});
			
			function removeFromMap(map, key) {
				var value = map[key];
				if (value !== undefined && value.length > 0) {
					var handle = value[value.length-1];
					value.length = value.length -1;
					map[key] = value;
					return handle;
				}
			}
			
			function removeBillboard(planet_id, x, y, z, spec, isGhost) {
				var key = x+"/"+y+"/"+z+"/"+spec+"/"+isGhost+"/"+planet_id;
				
				var handle = removeFromMap(self.billboardsMap, key);
				if (handle) {
					self.billboards.remove(handle);
					if (handle.extra) {
						self.billboards.remove(handle.extra);
					}
				}
				
				if (self.currentBillboardsByPlanet[planet_id+""]) {
					removeFromMap(self.currentBillboardsByPlanet[planet_id+""], key);
				}
			}
			
			function handleNewImageCase(planet_id, x, y, z, spec, parsedColor, imgPath, isGhost) {
				var billBoard = addBillboard(planet_id, x, y, z, spec, parsedColor, undefined, isGhost);
				loadImageIntoAtlas(imgPath, function(i) {
					billBoard.setImageIndex(i);
				});
			}
		} else {
			$('#globediv').append("<div>"+webGLError+"</div>");
		}
		
		self.eventsHandler = function(pColor, evt, direction) {
			if (!hasWebGL) {
				return;
			}
			if (!evt.is_ghost && !isStructure(evt.spec)) {
				return;
			}
			if (self.knownPlanets.indexOf(evt.planet_id) === -1) {
				self.knownPlanets.push(evt.planet_id);
			}
			
			var changeCnt = evt.change * direction;
			
			var imgPath = self.getImagePath(evt.spec);
			var imgIndex = self.imagesMap[imgPath];
			var parsedColor = parseColor(pColor);
			var addElseRemove = changeCnt > 0;
			
			changeCnt = Math.abs(changeCnt);
			
			for (var i = 0; i < changeCnt; i++) {
				if (addElseRemove) {
					if (imgIndex) {
						addBillboard(evt.planet_id, evt.x, evt.y, evt.z, evt.spec, parsedColor, imgIndex, evt.is_ghost);						
					} else {
						handleNewImageCase(evt.planet_id, evt.x, evt.y, evt.z, evt.spec, parsedColor, imgPath, evt.is_ghost);
					}
				} else {
					removeBillboard(evt.planet_id, evt.x, evt.y, evt.z, evt.spec, evt.is_ghost);
				}
			}
		};
	}
	
	var cometInfo = $("#armyDataSource").data("comet-info");
	
	var globeModel = new GlobeViewModel(cometInfo.planets);
	var armyModel = new ArmyCompositionModel(cometInfo.gameStart, globeModel);
	
	$(document).on("new-players", armyModel.newPlayersHandler);
	$(document).on("new-army-events", armyModel.newArmyEventsHandler);

	if (!cometInfo.hasComet) {
		$.getJSON(queryUrl + "/events?gameId="+$.urlParam("gameId"), function(armyBaseData) {
			var playerInfo = armyBaseData.playerInfo;

			for (playerId in playerInfo) {
				var player = playerInfo[playerId];
				player.playerId = playerId;
				player.pColor = player.primaryColor;
				player.sColor = player.secondaryColor;
				$(document).trigger('new-players', {value: [player]});
			}
			
			var playerEvents = armyBaseData.playerEvents;
			for (playerId in playerEvents) {
				var pEvents = playerEvents[playerId];
				for (var i = 0; i < pEvents.length; i++) {
					pEvents[i].playerId = playerId;
				}
				$(document).trigger('new-army-events', {value: pEvents});
			}
		});
	}
	
	ko.applyBindings(armyModel, document.getElementById('armycomposition'));
});