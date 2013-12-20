$(document).ready(function() {

    var start = /[^\/]*$/;  // ^ : start , \/ : '/', $ : end // as wildcard: /*.json 
    var end = /[.]json$/;
	
	function ArmyUnit(spc) {
		var self = this;
		
		self.spec = ko.observable(spc);
		self.icon = ko.computed(function() {return imageBaseUrl +"units/"+ self.spec().substring(self.spec().search(start), self.spec().search(end))+".png"});
		self.count = ko.observable(1);
	}
	
	function PlayerArmy(i, n, pClr, sClr) {
		var self = this;
		self.id = i;
		self.name = ko.observable(n);
		self.primaryColor = ko.observable(pClr);
		self.secondaryColor = ko.observable(sClr);
		self.units = ko.observableArray([]);
		
		self.resetUnits = function() {
			self.units.removeAll();
		}
		
		self.changeUnitCount = function (spec, changeCnt) {
			var found = false;
			for (var i = 0; i < self.units().length; i++) {
				var unt = self.units()[i];
				if (unt.spec() == spec) {
					var cnt = unt.count();
					unt.count(cnt+changeCnt);
					if (changeCnt < 0 && unt.count() == 0) {
						self.units.splice(i, 1);
					}
					found = true;
					break;
				}
			}
			if (!found && changeCnt > 0) {
				self.units.push(new ArmyUnit(spec));
			}
			
			self.units.sort(function(left, right) {
				return left.spec() == right.spec() ? 0 : (left.spec() < right.spec() ? -1 : 1);
			});
		}
	}
	
	
	function ArmyCompositionModel(start) {
		var self = this;
		
		self.events = ko.observableArray([]);

		self.startTime = ko.observable(start);
		self.endTime = ko.computed(function() {
			var length = self.events().length;
			var foo = length > 0 ? self.events()[length-1].timestamp : self.startTime();
			return foo;
		});
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
			self.events.push({
				player: player,
				spec: spec,
				timestamp: timestamp,
				change: typ == 0 ? +1 : -1,
			});
		}
		
		self.lockForFollowTime = function() {
			self.wasOnEnd = self.endTime() - self.selectedTime() < 5000;
		}
		
		self.mayFollowTime = function() {
			if (self.wasOnEnd) {
				self.selectedTime(self.endTime());
			}
		}
		
		self.players = ko.observableArray([]);
		
		self.addPlayer = function (id, name, pColor, sColor) {
			self.players.push(new PlayerArmy(id, name, pColor, sColor));
		}
		
		self.removeAllUnitsForAllPlayers = function() {
			for (var i = 0; i < self.players().length; i++) {
				self.players()[i].resetUnits();
			}
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
		
		
		self.selectedTime.subscribe(function(newT) {
			// TODO this is a pretty slow way to do this. chrome can deal with it, Firefox is a bit laggy
			self.removeAllUnitsForAllPlayers();
			for (var i = 0; i < self.events().length; i++) {
				var evt = self.events()[i];
				if (evt.timestamp <= newT) {
					self.changeSpecForPlayer(evt.player, evt.spec, evt.change);
				}
			}
		});
	}
	
	var armyBaseData = $("#armyDataSource").data("army-info").value;
	
	var armyModel = new ArmyCompositionModel(armyBaseData.gameStart);

	var playerInfo = armyBaseData.playerInfo;
	
	for (playerId in playerInfo) {
		var player = playerInfo[playerId];
		armyModel.addPlayer(playerId, player.name, player.primaryColor, player.secondaryColor);
	}
	
	var playerEvents = armyBaseData.playerEvents;
	
	armyModel.lockForFollowTime();
	
	for (playerId in playerEvents) {
		var pEvents = playerEvents[playerId];
		for (var i = 0; i < pEvents.length; i++) {
			var evt = pEvents[i];
			armyModel.addEvent(playerId, evt.spec, evt.time, evt.watchType);
		}
	}
	
	armyModel.mayFollowTime();
	
	$(document).on("new-army-events", function(event, data) {
		armyModel.lockForFollowTime();
		for (var i = 0; i < data.value.length; i++) {
			var evt = data.value[i];
			armyModel.addEvent(evt.playerId, evt.spec, evt.time, evt.watchType);
		}
		armyModel.mayFollowTime();
	});	
	
	ko.applyBindings(armyModel, document.getElementById('armycomposition'));
});