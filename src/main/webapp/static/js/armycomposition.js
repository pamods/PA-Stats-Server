$(document).ready(function() {
	var GHOSTERY_TIME = 10000;
	
	var unitTypeMapping = {"/pa/units/sea/missile_ship/missile_ship.json":["Tactical","Naval","Advanced","NoBuild","FactoryBuild","Mobile"],"/pa/units/orbital/delta_v_engine/delta_v_engine.json":["Construction","Advanced","Orbital","NoBuild","FabAdvBuild","Factory","Structure","Land"],"/pa/units/land/tank_armor/tank_armor.json":["Tank","Basic","NoBuild","FactoryBuild","Mobile","Land","Heavy"],"/pa/units/land/radar_adv/radar_adv.json":["Advanced","NoBuild","FabAdvBuild","Recon","Structure","Land"],"/pa/units/air/missile/missile.json":[],"/pa/units/commanders/tank_base/tank_base.json":["Construction","NoBuild","Commander","Mobile","Land"],"/pa/units/land/tank_laser_adv/tank_laser_adv.json":["Tank","Advanced","NoBuild","FactoryBuild","Mobile","Land"],"/pa/units/air/base_flyer/base_flyer.json":["Mobile","Air","NoBuild"],"/pa/units/sea/torpedo_launcher_adv/torpedo_launcher_adv.json":["Defense","Naval","Advanced","NoBuild","FabAdvBuild","Structure"],"/pa/units/orbital/orbital_lander/orbital_lander.json":["Advanced","Orbital","NoBuild","Transport","FactoryBuild","Mobile"],"/pa/units/land/tank_light_laser/tank_light_laser.json":["Tank","Basic","NoBuild","FactoryBuild","Mobile","Land"],"/pa/units/land/fabrication_bot_combat/fabrication_bot_combat.json":["Bot","Basic","Construction","NoBuild","FactoryBuild","Mobile","Land"],"/pa/units/air/fighter_adv/fighter_adv.json":["Air","Fighter","Advanced","NoBuild","FactoryBuild","Mobile"],"/pa/units/commanders/avatar/avatar.json":["Construction","Debug","Air","Fabber","NoBuild","Mobile"],"/pa/units/land/assault_bot_adv/assault_bot_adv.json":["Bot","Advanced","NoBuild","FactoryBuild","Mobile","Land"],"/pa/units/air/air_scout/air_scout.json":["Basic","Air","Scout","NoBuild","FactoryBuild","Mobile"],"/pa/units/sea/sea_scout/sea_scout.json":["Naval","Basic","Scout","NoBuild","FactoryBuild","Mobile"],"/pa/units/land/land_barrier/land_barrier.json":["Basic","Wall","NoBuild","FabBuild","FabAdvBuild","CmdBuild","Structure"],"/pa/units/land/bot_factory/bot_factory.json":["Bot","Basic","Construction","NoBuild","FabBuild","FabAdvBuild","CmdBuild","Factory","Structure","Land"],"/pa/units/land/bot_aa/bot_aa.json":["Bot","Tank","Basic","AirDefense","NoBuild","FactoryBuild","Mobile","Land"],"/pa/units/air/bomber/bomber.json":["Basic","Air","Bomber","NoBuild","FactoryBuild","Mobile"],"/pa/units/orbital/orbital_fighter/orbital_fighter.json":["Basic","Fighter","Orbital","NoBuild","FactoryBuild","Mobile"],"/pa/units/land/radar/radar.json":["Basic","NoBuild","FabBuild","Recon","Structure","Land"],"/pa/units/land/bot_factory_adv/bot_factory_adv.json":["Bot","Construction","Advanced","NoBuild","Factory","Structure","Land"],"/pa/units/land/amphibious_bot/amphibious_bot.json":["Bot","Basic","NoBuild","Mobile","Land"],"/pa/units/land/metal_extractor/metal_extractor.json":["Basic","Economy","NoBuild","MetalProduction","FabBuild","CmdBuild","Structure"],"/pa/units/land/tank_heavy_armor/tank_heavy_armor.json":["Tank","Advanced","NoBuild","FactoryBuild","Mobile","Land","Heavy"],"/pa/units/orbital/radar_satellite_adv/radar_satellite_adv.json":["Advanced","Orbital","FabOrbBuild","NoBuild","Recon","Mobile"],"/pa/units/air/fighter/fighter.json":["Basic","Air","Fighter","NoBuild","FactoryBuild","Mobile"],"/pa/units/commanders/raptor_centurion/raptor_centurion.json":["Construction","NoBuild","Commander","Mobile","Land"],"/pa/units/land/metal_extractor_adv/metal_extractor_adv.json":["Advanced","Economy","NoBuild","MetalProduction","FabAdvBuild","Structure"],"/pa/units/air/bomber_adv/bomber_adv.json":["Air","Advanced","Bomber","NoBuild","FactoryBuild","Mobile"],"/pa/units/commanders/raptor_nemicus/raptor_nemicus.json":["Construction","NoBuild","Commander","Mobile","Land"],"/pa/units/sea/fabrication_sub/fabrication_sub.json":["Naval","Basic","Fabber","NoBuild","Mobile"],"/pa/units/orbital/orbital_fabrication_bot/orbital_fabrication_bot.json":["Construction","Advanced","Orbital","Fabber","NoBuild","FactoryBuild","Mobile"],"/pa/units/commanders/imperial_theta/imperial_theta.json":["Construction","NoBuild","Commander","Mobile","Land"],"/pa/units/land/fabrication_vehicle/fabrication_vehicle.json":["Tank","Basic","Construction","Fabber","NoBuild","FactoryBuild","Mobile","Land"],"/pa/units/commanders/quad_base/quad_base.json":["Construction","NoBuild","Commander","Mobile","Land"],"/pa/units/land/air_defense_adv/air_defense_adv.json":["Defense","Advanced","AirDefense","NoBuild","FabAdvBuild","Structure","Land"],"/pa/units/orbital/radar_satellite/radar_satellite.json":["Basic","Orbital","NoBuild","FactoryBuild","Recon","Mobile"],"/pa/units/land/nuke_launcher/nuke_launcher.json":["Nuke","Advanced","NoBuild","FabAdvBuild","Offense","Factory","Structure","Land"],"/pa/units/land/energy_plant_adv/energy_plant_adv.json":["Advanced","Economy","NoBuild","FabAdvBuild","EnergyProduction","Structure"],"/pa/units/air/gunship/gunship.json":["Air","Advanced","Gunship","NoBuild","FactoryBuild","Mobile"],"/pa/units/orbital/base_orbital/base_orbital.json":["Mobile","Orbital","NoBuild"],"/pa/units/land/energy_storage/energy_storage.json":["Basic","Economy","NoBuild","FabBuild","FabAdvBuild","CmdBuild","Structure"],"/pa/units/land/tactical_missile_launcher/tactical_missile_launcher.json":["Tactical","Defense","Advanced","NoBuild","FabAdvBuild","Structure","Land"],"/pa/units/commanders/tank_aeson/tank_aeson.json":["Construction","NoBuild","Commander","Mobile","Land"],"/pa/units/land/air_defense/air_defense.json":["Defense","Basic","AirDefense","NoBuild","FabBuild","FabAdvBuild","CmdBuild","Structure","Land"],"/pa/units/land/bot_artillery_adv/bot_artillery_adv.json":["Bot","Advanced","NoBuild","Artillery","FactoryBuild","Mobile","Land"],"/pa/units/sea/torpedo_launcher/torpedo_launcher.json":["Defense","Naval","Basic","NoBuild","FabBuild","CmdBuild","Structure"],"/pa/units/commanders/raptor_rallus/raptor_rallus.json":["Construction","NoBuild","Commander","Mobile","Land"],"/pa/units/air/air_factory_adv/air_factory_adv.json":["Construction","Air","Advanced","NoBuild","Factory","Structure"],"/pa/units/air/fabrication_aircraft_adv/fabrication_aircraft_adv.json":["Construction","Air","Advanced","Fabber","NoBuild","FactoryBuild","Mobile"],"/pa/units/land/base_structure/base_structure.json":["Structure","NoBuild"],"/pa/units/land/energy_plant/energy_plant.json":["Basic","Economy","NoBuild","FabBuild","CmdBuild","EnergyProduction","Structure"],"/pa/units/land/fabrication_bot_combat_adv/fabrication_bot_combat_adv.json":["Bot","Construction","Advanced","NoBuild","FactoryBuild","Mobile","Land"],"/pa/units/air/missile_orbital/missile_orbital.json":[],"/pa/units/orbital/orbital_laser/orbital_laser.json":["LaserPlatform","Advanced","Orbital","FabOrbBuild","NoBuild","Mobile"],"/pa/units/land/tank_amphibious_adv/tank_amphibious_adv.json":["Tank","Mobile","Land","NoBuild"],"/pa/units/sea/battleship/battleship.json":["Naval","Advanced","NoBuild","FactoryBuild","Mobile"],"/pa/units/orbital/deep_space_radar/deep_space_radar.json":["Advanced","Orbital","NoBuild","FabBuild","FabAdvBuild","Recon","Structure"],"/pa/units/sea/fabrication_ship_adv/fabrication_ship_adv.json":["Naval","Construction","Advanced","Fabber","NoBuild","FactoryBuild","Mobile"],"/pa/units/sea/base_ship/base_ship.json":["Naval","Mobile","NoBuild"],"/pa/units/sea/naval_factory_adv/naval_factory_adv.json":["Naval","Construction","Advanced","NoBuild","Factory","Structure"],"/pa/units/commanders/imperial_base/imperial_base.json":["Construction","NoBuild","Commander","Mobile","Land"],"/pa/units/commanders/imperial_delta/imperial_delta.json":["Construction","NoBuild","Commander","Mobile","Land"],"/pa/units/sea/destroyer/destroyer.json":["Naval","Basic","NoBuild","FactoryBuild","Mobile"],"/pa/units/land/fabrication_vehicle_adv/fabrication_vehicle_adv.json":["Tank","Construction","Advanced","Fabber","NoBuild","FactoryBuild","Mobile","Land"],"/pa/units/air/missile_orbital_booster/missile_orbital_booster.json":[],"/pa/units/orbital/mining_platform/mining_platform.json":["Advanced","Orbital","Economy","NoBuild","Mobile","EnergyProduction"],"/pa/units/land/laser_defense/laser_defense.json":["Defense","Basic","NoBuild","FabBuild","Structure","Land","SurfaceDefense"],"/pa/units/land/artillery_long/artillery_long.json":["Defense","Advanced","NoBuild","Artillery","FabAdvBuild","Structure","Land"],"/pa/units/orbital/defense_satellite/defense_satellite.json":["Defense","Advanced","Orbital","FabOrbBuild","NoBuild","OrbitalDefense","Mobile"],"/pa/units/land/land_scout/land_scout.json":["Tank","Basic","Scout","NoBuild","FactoryBuild","Mobile","Land"],"/pa/units/land/base_bot/base_bot.json":["Bot","Mobile","Land","NoBuild"],"/pa/units/sea/nuclear_sub/nuclear_sub.json":["Naval","Advanced","Sub","NoBuild","Mobile"],"/pa/units/land/bot_bomb/bot_bomb.json":["Bot","Basic","NoBuild","FactoryBuild","Mobile","Land"],"/pa/units/land/base_unit/base_unit.json":["Land","NoBuild","Structure"],"/pa/units/land/fabrication_bot_adv/fabrication_bot_adv.json":["Bot","Construction","Advanced","Fabber","NoBuild","FactoryBuild","Mobile","Land"],"/pa/units/commanders/imperial_alpha/imperial_alpha.json":["Construction","NoBuild","Commander","Mobile","Land"],"/pa/units/land/assault_bot/assault_bot.json":["Bot","Basic","NoBuild","FactoryBuild","Mobile","Land"],"/pa/units/land/unit_cannon/unit_cannon.json":["Structure","Advanced","NoBuild"],"/pa/units/commanders/imperial_progenitor/imperial_progenitor.json":["Construction","NoBuild","Commander","Mobile","Land"],"/pa/units/air/fabrication_aircraft/fabrication_aircraft.json":["Basic","Construction","Air","Fabber","NoBuild","FactoryBuild","Mobile"],"/pa/units/orbital/solar_array/solar_array.json":["Advanced","Orbital","FabOrbBuild","Economy","NoBuild","Mobile","EnergyProduction"],"/pa/units/land/vehicle_factory/vehicle_factory.json":["Tank","Basic","Construction","NoBuild","FabBuild","FabAdvBuild","CmdBuild","Factory","Structure","Land"],"/pa/units/sea/sea_mine/sea_mine.json":["Defense","Naval","Basic","NoBuild","Structure"],"/pa/units/land/base_vehicle/base_vehicle.json":["Tank","Mobile","Land","NoBuild"],"/pa/units/land/aa_missile_vehicle/aa_missile_vehicle.json":["Tank","Basic","AirDefense","NoBuild","FactoryBuild","Mobile","Land"],"/pa/units/sea/fabrication_ship/fabrication_ship.json":["Naval","Basic","Construction","Fabber","NoBuild","FactoryBuild","Mobile"],"/pa/units/land/tank_heavy_mortar/tank_heavy_mortar.json":["Tank","Advanced","NoBuild","Artillery","FactoryBuild","Mobile","Land"],"/pa/units/land/laser_defense_single/laser_defense_single.json":["Defense","Basic","NoBuild","FabBuild","CmdBuild","Structure","Land","SurfaceDefense"],"/pa/units/commanders/quad_osiris/quad_osiris.json":["Construction","NoBuild","Commander","Mobile","Land"],"/pa/units/land/tank_hover/tank_hover.json":["Tank","Basic","NoBuild","Mobile","Land"],"/pa/units/orbital/orbital_launcher/orbital_launcher.json":["Construction","Orbital","NoBuild","FabBuild","FabAdvBuild","Factory","Structure","Land"],"/pa/units/land/artillery_short/artillery_short.json":["Defense","Basic","NoBuild","Artillery","FabBuild","Structure"],"/pa/units/land/vehicle_factory_adv/vehicle_factory_adv.json":["Tank","Construction","Advanced","NoBuild","Factory","Structure","Land"],"/pa/units/commanders/raptor_base/raptor_base.json":["Construction","NoBuild","Commander","Mobile","Land"],"/pa/units/land/land_mine/land_mine.json":["Defense","Basic","CombatFabAdvBuild","CombatFabBuild","Land"],"/pa/units/land/metal_storage/metal_storage.json":["Basic","Economy","NoBuild","FabBuild","FabAdvBuild","CmdBuild","Structure"],"/pa/units/land/bot_spider_adv/bot_spider_adv.json":["Bot","Mobile","Land","NoBuild"],"/pa/units/sea/attack_sub/attack_sub.json":["Naval","Basic","Sub","NoBuild","Mobile"],"/pa/units/commanders/base_commander/base_commander.json":["Construction","NoBuild","Commander","Mobile","Land"],"/pa/units/orbital/orbital_egg/orbital_egg.json":["Orbital","Mobile","NoBuild"],"/pa/units/sea/naval_factory/naval_factory.json":["Naval","Basic","Construction","NoBuild","FabBuild","FabAdvBuild","CmdBuild","Factory","Structure"],"/pa/units/land/fabrication_bot/fabrication_bot.json":["Bot","Basic","Construction","Fabber","NoBuild","FactoryBuild","Mobile","Land"],"/pa/units/air/air_factory/air_factory.json":["Basic","Construction","Air","NoBuild","FabBuild","FabAdvBuild","CmdBuild","Factory","Structure"],"/pa/units/air/transport/transport.json":["Air","Advanced","NoBuild","Transport","FactoryBuild","Mobile"],"/pa/units/land/laser_defense_adv/laser_defense_adv.json":["Defense","Advanced","NoBuild","FabAdvBuild","Structure","Land","SurfaceDefense"],"/pa/units/land/teleporter/teleporter.json":["CombatFabAdvBuild","Teleporter","FabOrbBuild","NoBuild","FabBuild","FabAdvBuild","CmdBuild","Recon","Structure"],"/pa/units/sea/sonar_adv/sonar_adv.json":["Structure","Recon","NoBuild"],"/pa/units/land/anti_nuke_launcher/anti_nuke_launcher.json":["Defense","NukeDefense","Advanced","NoBuild","FabAdvBuild","Factory","Structure","Land"],"/pa/units/sea/frigate/frigate.json":["Naval","Basic","AirDefense","NoBuild","FactoryBuild","Mobile"],"/pa/units/sea/sonar/sonar.json":["Structure","Recon","NoBuild"],"/pa/units/orbital/ion_defense/ion_defense.json":["Defense","Basic","NoBuild","FabBuild","FabAdvBuild","OrbitalDefense","Structure"],"/pa/units/land/avatar_factory/avatar_factory.json":["Tank","Construction","NoBuild","Factory","Structure","Land"]};
	
	var parseColor = function(clr) {
		var m = clr.match(/^rgb\s*\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*\)$/i);
		return [m[1]/255,m[2]/255,m[3]/255];
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
    
	function ArmyUnit(spc) {
		var self = this;
		
		self.spec = ko.observable(spc);
		self.name = ko.computed(function() {return nameForSpec(self.spec());});
		self.icon = ko.computed(function() {return imageBaseUrl +"units/"+ self.name() +".png"});
		self.sicon = ko.computed(function() {return getIconForSpec(self.spec());});
		self.linkToPaDb = ko.computed(function() {
			// this regex was necessary for pamatches, but brian has been slow about updates, so I now use my own pa-db
			// var link = /([^\/]+?\/[^\/]+?\/)(?=[^\/]*\.json)/.exec(self.spec());
			return "http://www.nanodesu.info/pa-db/recent/unit/"+self.name();
		});
		self.count = ko.observable(0);
		self.visible = ko.computed(function() {return self.count() > 0;});
	}
	
	function PlayerArmy(i, n, pClr, sClr, eventLocationsHandler) {
		var self = this;
		self.id = i;
		self.name = ko.observable(n);
		self.primaryColor = ko.observable(pClr);
		self.secondaryColor = ko.observable(sClr);
		self.units = ko.observableArray([]);
		
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
					unt.count(unt.count()+change);
					ux = unt;
				} else { // else is only executed once per unit type that occurs in the entire game
					// so the following code can be slow
					var newUnit = new ArmyUnit(evt.spec);
					newUnit.count(change);
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
			if (eventLocationsHandler && evt.planet_id === 0) {  // TODO support more planets
				eventLocationsHandler(self.primaryColor(), evt, direction);
			}
		}
	}
	
	function ArmyCompositionModel(start, eventLocationsHandler) {
		var self = this;

		self.startTime = ko.observable(start);
		self.endTime = ko.observable(start);
		self.selectedTime = ko.observable(start);
		
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
		}
		
		self.lockWasOnEnd = function() {
			var prevEnd = self.endTime();
			var border = (prevEnd - self.startTime()) * 0.05;
			self.wasOnEnd = prevEnd - self.selectedTime() < border;
		}
		
		self.selectEnd = function() {
			self.selectedTime(self.endTime());
		}
		
		self.maySelectEnd = function() {
			if (self.wasOnEnd) {
				self.selectEnd();
			}
		}
		
		self.players = ko.observableArray([]);
		
		self.visiblePlayers = ko.computed(function() {
			return ko.utils.arrayFilter(self.players(), function(player) {
				return player.hasUnits();
			});
		}, self);
		
		self.addPlayer = function (id, name, pColor, sColor) {
			self.players.push(new PlayerArmy(id, name, pColor, sColor, eventLocationsHandler));
		}
		
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
		}
		
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
		}
		
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
			
			function addBillboard(x, y, z, spec, color, imageIndex, isGhost) {
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
				
				var key = x+"/"+y+"/"+z+"/"+spec+"/"+isGhost;
				var value = self.billboardsMap[key]; 
				if (value === undefined) {
					value = [];
				}
				value.push(handle);
				
				self.billboardsMap[key] = value;
				return handle;
			}
			
			function removeBillboard(x, y, z, spec, isGhost) {
				var key = x+"/"+y+"/"+z+"/"+spec+"/"+isGhost;
				var value = self.billboardsMap[key];
				if (value !== undefined && value.length > 0) {
					var handle = value[value.length-1];
					self.billboards.remove(handle);
					if (handle.extra) {
						self.billboards.remove(handle.extra);
					}
					value.length = value.length -1;
					self.billboardsMap[key] = value;
				}
			}
			
			function handleNewImageCase(x, y, z, spec, parsedColor, imgPath, isGhost) {
				var billBoard = addBillboard(x, y, z, spec, parsedColor, undefined, isGhost);
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
			
			var changeCnt = evt.change * direction;
			
			var imgPath = self.getImagePath(evt.spec);
			var imgIndex = self.imagesMap[imgPath];
			var parsedColor = parseColor(pColor);
			var addElseRemove = changeCnt > 0;
			
			changeCnt = Math.abs(changeCnt);
			
			for (var i = 0; i < changeCnt; i++) {
				if (addElseRemove) {
					if (imgIndex) {
						addBillboard(evt.x, evt.y, evt.z, evt.spec, parsedColor, imgIndex, evt.is_ghost);						
					} else {
						handleNewImageCase(evt.x, evt.y, evt.z, evt.spec, parsedColor, imgPath, evt.is_ghost);
					}
				} else {
					removeBillboard(evt.x, evt.y, evt.z, evt.spec, evt.is_ghost);
				}
			}
		};

		
	}
	
	var cometInfo = $("#armyDataSource").data("comet-info");
	
	var globeModel = new GlobeViewModel(cometInfo.planets);
	var armyModel = new ArmyCompositionModel(cometInfo.gameStart, globeModel.eventsHandler);
	
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