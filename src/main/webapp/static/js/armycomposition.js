$(document).ready(function() {

    var start = /[^\/]*$/;  // ^ : start , \/ : '/', $ : end // as wildcard: /*.json 
    var end = /[.]json$/;
	
	function ArmyUnit(spc, cnt) {
		var self = this;
		
		self.spec = ko.observable(spc);
		self.icon = ko.computed(function() {return imageBaseUrl +"units/"+ self.spec().substring(self.spec().search(start), self.spec().search(end))+".png"});
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
		}, self);
		
		self.resetUnits = function() {
			self.units.removeAll();
		}
		
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