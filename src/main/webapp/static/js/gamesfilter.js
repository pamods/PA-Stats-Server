(function() {
	function FilterModel() {
		var self = this;
		self.playerIdMap = ko.observable({
		});
		self.players = ko.computed(function() {
			var result = [];
			var map = self.playerIdMap();
			for (var p in map) {
				if (map.hasOwnProperty(p)) {
					result.push(p);
				}
			}
			return result.sort();
		});
		self.playerFilter = ko.observable();
		self.playerId = ko.computed(function() {
			return self.playerIdMap()[self.playerFilter()];
		});
		
		self.systems = ko.observable([]);
		self.systemFilter = ko.observable();
		
		var trim = function(s) {
			return s ? s : "";
		};
		self.navigate = function() {
			window.location.href = window.location.pathname + "?" + "player=" + trim(self.playerId()) + "&system=" + trim(self.systemFilter());
		};
		
		self.clear = function() {
			self.playerFilter("");
			self.systemFilter("");
			self.navigate();
		};
		
		self.initAutoCompletePlayers = function() {
			$.getJSON("/report/withcache/playermap", function(mapping) {
				self.playerIdMap(mapping);
				$('#playerFilter').autocomplete({
					minLength: 2,
					source: self.players(),
					select: function(e, ui) {
						if (ui.item) {
							self.playerFilter(ui.item.value);
						}
					}
				});
			});
		};
		
		self.initAutoCompleteSystems = function() {
			$.getJSON("/report/withcache/systems", function(set) {
				self.systems(set.sort());
				$('#systemFilter').autocomplete({
					minLength: 2,
					source: self.systems(),
					select: function(e, ui) {
						if (ui.item) {
							self.systemFilter(ui.item.value);
						}
					}
				});
			});
		};
		
		self.initAutoCompletePlayers();
		self.initAutoCompleteSystems();
	};
	
	model = new FilterModel();
	ko.applyBindings(model, $('#gamesfilter').get(0));
	
	
}());