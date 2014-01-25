$(document).ready(function() {

    var start = /[^\/]*$/;  // ^ : start , \/ : '/', $ : end // as wildcard: /*.json 
    var end = /[.]json$/;
    
	function ArmyUnit(spc, cnt) {
		var self = this;
		
		self.spec = ko.observable(spc);
		self.name = ko.computed(function() {return self.spec().substring(self.spec().search(start), self.spec().search(end))});
		self.icon = ko.computed(function() {return imageBaseUrl +"units/"+ self.name() +".png"});
		self.linkToPaDb = ko.computed(function() {
			var link = /([^\/]+?\/[^\/]+?\/)(?=[^\/]*\.json)/.exec(self.spec());
			return "http://pamatches.com/units/"+link[0];
		});
		self.count = ko.observable(cnt);
		self.visible = ko.computed(function() {return self.count() > 0;});
	}
	
	function PlayerArmy(i, n, pClr, sClr) {
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
		
		self.changeUnitCount = function (spec, changeCnt) {
			var index = self.unitSpecIndex[spec];
			var found = index != undefined;
			if (found) {// found is executed once per event in the game
				// so it needs to be fast
				var unt = self.units()[index];
				unt.count(unt.count()+changeCnt);
			} else { // else is only executed once per unit type that occurs in the entire game
				// so the following code can be slow
				var newUnit = new ArmyUnit(spec, changeCnt);
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
	}
	
	
	function ArmyCompositionModel(start) {
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
		
		self.addEvent = function(player, spec, timestamp, typ) {
			var evt = {
				player: player,
				spec: spec,
				timestamp: timestamp,
				change: typ == 0 ? +1 : -1,
			};
			if (self.endTime() < timestamp) {
				self.endTime(timestamp);
			}
			self.indexEvent(evt);
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
			self.players.push(new PlayerArmy(id, name, pColor, sColor));
		}
		
		self.changeSpecForPlayer = function(playerId, spec, addElseRemove) {
			var player = undefined;
			for (var i = 0; i < self.players().length; i++) {
				if (self.players()[i].id == playerId) {
					player = self.players()[i];
					break;
				}
			}
			
			player.changeUnitCount(spec, addElseRemove);
		}
		
		
		self.timeBefore = self.selectedTime();
		
		self.eventIndex = {};
		
		var hashBucketSize = 1000;
		
		self.indexEvent = function(event) {
			var hashBucket = Math.floor(event.timestamp / hashBucketSize);
			if (self.eventIndex[hashBucket] === undefined) {
				self.eventIndex[hashBucket] = [];
			}
			self.eventIndex[hashBucket].push(event);
		}
		
		self.selectedTime.subscribe(function(newT) {
			var direction = newT > self.timeBefore ? 1 : -1;
			
			var a = self.timeBefore > newT ? newT : self.timeBefore;
			var b = self.timeBefore > newT ? self.timeBefore : newT;
			
			var firstBucket = Math.floor(a / hashBucketSize);
			var lastBucket = Math.floor(b / hashBucketSize);
			for (var i = firstBucket; i <= lastBucket; i++) {
				if (self.eventIndex[i] != undefined) {
					for (var j = 0; j < self.eventIndex[i].length; j++) {
						var evt = self.eventIndex[i][j];
						if (evt.timestamp > a && evt.timestamp <= b) {
							self.changeSpecForPlayer(evt.player, evt.spec, evt.change * direction);
						}
					}
				}
			}
			self.timeBefore = newT;
		});
	}
	
	var cometInfo = $("#armyDataSource").data("comet-info");
	var armyModel = new ArmyCompositionModel(cometInfo.gameStart);
	
	if (cometInfo.hasComet) {
		var nothingReceived = true;
		$(document).on("new-players", function(event, data) {
			ko.tasks.processImmediate(function() {
				for (var i = 0; i < data.value.length; i++) {
					var p = data.value[i];
					armyModel.addPlayer(p.playerId, p.name, p.pColor, p.sColor);
				}
			});
			armyModel.selectEnd();
		});
		
		$(document).on("new-army-events", function(event, data) {
			armyModel.lockWasOnEnd();
			ko.tasks.processImmediate(function() {
				for (var i = 0; i < data.value.length; i++) {
					var evt = data.value[i];
					armyModel.addEvent(evt.playerId, evt.spec, evt.time, evt.watchType);
				}
			});
			if (nothingReceived) {
				armyModel.selectEnd();
				nothingReceived = false;
			} else {
				armyModel.maySelectEnd();
			}
		});
	} else {
		$.getJSON(queryUrl + "/events?gameId="+$.urlParam("gameId"), function(armyBaseData) {
			var playerInfo = armyBaseData.playerInfo;
			
			ko.tasks.processImmediate(function() {
				for (playerId in playerInfo) {
					var player = playerInfo[playerId];
					armyModel.addPlayer(playerId, player.name, player.primaryColor, player.secondaryColor);
				}
			});
			
			var playerEvents = armyBaseData.playerEvents;

			for (playerId in playerEvents) {
				var pEvents = playerEvents[playerId];
				ko.tasks.processImmediate(function() {
					for (var i = 0; i < pEvents.length; i++) {
						var evt = pEvents[i];
						armyModel.addEvent(playerId, evt.spec, evt.time, evt.watchType);
					}
				});
				armyModel.maySelectEnd();
			}
		});
	}
	
	console.log("bind army composition!");
	ko.applyBindings(armyModel, document.getElementById('armycomposition'));
});