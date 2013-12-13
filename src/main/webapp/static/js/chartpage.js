$(function() {
	function ChartModel() {
		var self = this;

		function getEfficiency(inValue, outValue) {
			var speed = undefined;
			if (outValue > 0) {
				speed = Math.min(inValue / outValue, 1);
			} else {
				speed = 1;
			}
			return speed;
		}

		var buildSpeedByMetal = {
			getValue : function(timepoint, i) {
				var metalIn = timepoint[i]['metalIncome'];
				var metalOut = timepoint[i]['metalSpending'];
				var metalStored = timepoint[i]['metalStored'];
				return metalStored <= 0 ? getEfficiency(metalIn, metalOut) : 1;
			},
			text : "Build efficiency by metal",
			accumulates : false
		}

		var buildSpeedByEnergy = {
			getValue : function(timepoint, i) {
				var energyIn = timepoint[i]['energyIncome'];
				var energyOut = timepoint[i]['energySpending'];
				var energyStored = timepoint[i]['energyStored'];
				return energyStored <= 0 ? getEfficiency(energyIn, energyOut) : 1;
			},
			text : "Build efficiency by energy",
			accumulates : false
		}

		var buildSpeed = {
			getValue : function(timepoint, i) {
				return Math.min(buildSpeedByMetal.getValue(timepoint, i),
						buildSpeedByEnergy.getValue(timepoint, i));
			},
			text : 'Build efficiency',
			accumulates : false
		}
		var metalIncome = {
			getValue : function(timepoint, i) {
				return timepoint[i]['metalIncome'];
			},
			text : 'Metal income gross',
			accumulates : false
		};
		var energyIncome = {
			getValue : function(timepoint, i) {
				return timepoint[i]['energyIncome'];
			},
			text : 'Energy income gross',
			accumulates : false
		};
		var metalIncomeNet = {
			getValue : function(tp, i) {
				return tp[i]['metalIncomeNet'];
			},
			text : 'Metal income net',
			accumulates : false,
		}
		var energyIncomeNet = {
			getValue : function(tp, i) {
				return tp[i]['energyIncomeNet'];
			},
			text : 'Energy income net',
			accumulates : false,
		}
		var metalSpending = {
			getValue : function(tp, i) {
				return tp[i]['metalSpending'];
			},
			text : 'Metal spending',
			accumulates : false,
		}
		var energySpending = {
			getValue : function(tp, i) {
				return tp[i]['energySpending'];
			},
			text : 'Energy spending',
			accumulates : false,
		}
		var apm = {
				getValue : function(tp, i) {
					if (tp.length < 2) {
						return 0;
					}
					
					var averageSeconds = 60*1000;
					var maxPoints = 100;
					
					var timeFound = 0;
					var startPoint = i;
					var sum = 0;
					for (var j = 1; j < maxPoints; j++) {
						if (i - j >= 0 && timeFound < averageSeconds) {
							sum = sum + tp[i-j]['apm'];
							timeFound += tp[i-j+1].timepoint - tp[i-j].timepoint;
						} else {
							break;
						}
					}
					// use recursion to fix the first point of the table by displaying the same value as the 2nd point has
					return timeFound == 0 ? apm.getValue(tp, i+1) : sum / (timeFound / 60000);
				},			text : 'APM',
			accumulates : false,
		}
		var armyCount = {
			getValue : function(tp, i) {
				return tp[i]['armyCount'];
			},
			text : 'Units alive',
			accumulates : false
		};
		var metalStored = {
			getValue : function(tp, i) {
				return tp[i]['metalStored'];
			},
			text : 'Metal stored',
			accumulates : false
		};
		var energyStored = {
			getValue : function(tp, i) {
				return tp[i]['energyStored'];
			},
			text : 'Energy stored',
			accumulates : false
		};
		var metalProduced = {
			getValue : function(tp, i) {
				return tp[i]['metalProduced'];
			},
			text : 'Metal produced',
			accumulates : true
		};
		var metalWasted = {
			getValue : function(tp, i) {
				return tp[i]['metalWasted'];
			},
			text : 'Metal wasted',
			accumulates : true
		};
		var energyProduced = {
			getValue : function(tp, i) {
				return tp[i]['energyProduced'];
			},
			text : 'Energy produced',
			accumulates : true
		};
		var energyWasted = {
			getValue : function(tp, i) {
				return tp[i]['energyWasted'];
			},
			text : 'Energy wasted',
			accumulates : true
		};

		self.selectedStat = ko.observable(armyCount);

		self.stats = [ armyCount, buildSpeed, buildSpeedByMetal, buildSpeedByEnergy, metalIncome, metalSpending,
				metalIncomeNet, metalWasted, metalStored, metalProduced,
				energyIncome, energySpending, energyIncomeNet, energyWasted,
				energyStored, energyProduced, apm ]

		self.selectStat = function(data) {
			self.selectedStat(data);
			updateForCurrentData();
		};

		self.loadData = function() {
			var gameId = $.urlParam("gameId");

			// queryUrl is injected from the HeadInjection snippet
			if (gameId === undefined) {
				$.get(queryUrl + "?gameIdent=" + $.urlParam('gameIdent'),
						populateChart);
			} else {
				$.get(queryUrl + "?gameId=" + gameId, populateChart);
			}
		}

		var firstTime = undefined;
		var lastTime = undefined;
		var tableData = [ {
			data : [ {
				x : 0,
				y : 0
			} ]
		} ];
		var graph = new Rickshaw.Graph({
			element : document.querySelector("#chartcontainer"),
			width : 700,
			height : 450,
			min : 'auto',
			interpolation : 'step-after',
			renderer : 'line',
			series : tableData,
		});

		var xTimeFormat = function(n) {
			var secs = (n - firstTime) / 1000;

			if (secs < 0) {
				return "";
			}

			var min = Math.floor(secs / 60);
			var sec = Math.floor(secs % 60);
			return zeroFill(min, 2) + ":" + zeroFill(sec, 2);
		};

		var x_axis = new Rickshaw.Graph.Axis.X({
			graph : graph,
			pixelsPerTick : 50,
			element : document.querySelector("#xaxis"),
			height : 20,
			tickFormat : xTimeFormat
		});

		// hacky solution the the height of the axis growing and growing and
		// growing due to a bug in rickshaw (?) ...
		var oldSetSize = x_axis.setSize;
		x_axis.setSize = function(a) {
			if (oldSetSize != undefined)
				oldSetSize(a);
			oldSetSize = undefined;
		}

		var y_axis = new Rickshaw.Graph.Axis.Y({
			graph : graph,
			pixelsPerTick : 50,
			tickFormat : Rickshaw.Fixtures.Number.formatKMBT,
		});

		graph.render();

		var hoverDetail = new Rickshaw.Graph.HoverDetail({
			graph : graph,
			xFormatter : xTimeFormat,
		});

		var slider = new Rickshaw.Graph.RangeSlider({
			graph : graph,
			element : $('#slider')
		});

		var currentData = undefined;
		var showingLiveNote = false;

		function updateForCurrentData() {
			if (currentData === undefined) {
				return;
			}
			var timePointData = currentData.playerTimeData;

			var palette = new Rickshaw.Color.Palette();

			firstTime = undefined;
			lastTime = undefined;
			
			for ( var playerName in timePointData) {
				// access the first or last element, since the objects are given
				// to us sorted by the webservice
				if (firstTime === undefined
						|| firstTime > timePointData[playerName][0].timepoint) {
					firstTime = timePointData[playerName][0].timepoint;
				}
				if (lastTime === undefined
						|| lastTime < timePointData[playerName][timePointData[playerName].length - 1].timepoint) {
					lastTime = timePointData[playerName][timePointData[playerName].length - 1].timepoint;
				}
			}

			var selected = self.selectedStat();

			tableData.length = 0;
			
			var playerInfo = currentData.playerInfo
			
			for ( var playerName in timePointData) {
				var accumulator = 0;
				var playerData = [];
				for ( var i = 0; i < timePointData[playerName].length; i++) {
					var p = timePointData[playerName][i];
					var val = undefined;
					if (selected.accumulates) {
						accumulator += selected.getValue(timePointData[playerName], i);
						val = accumulator;
					} else {
						val = selected.getValue(timePointData[playerName], i);
					}
					playerData.push({
						x : p.timepoint,
						y : val
					});
				}
				var teamColor = palette.color();
				if (playerInfo[playerName].color) {
					teamColor = playerInfo[playerName].color;
				}
				tableData.push({
					name : playerInfo[playerName].name,
					data : playerData,
					color : teamColor,
				});
			}

			graph.update();
		}
		function populateChart(data) {
			currentData = data;
			updateForCurrentData();
			
			// dont trust the user's time for this, all game data points are supposed to be server time
			$.get(queryUrl + "/time", function(timeMs) {
				var mightStillBeRunning = timeMs.ms - lastTime < gameIsLiveOffsetGuess;
				if (mightStillBeRunning) {
					window.setTimeout(self.loadData, 5000);
					if (!showingLiveNote) {
						$("#livenote").show("slide", {direction: "right" }, "slow");
						showingLiveNote = true;
					}
				} else {
					if (showingLiveNote) {
						$("#livenote").hide("slide", {direction: "right" }, "slow");
						showingLiveNote = false;
					}
				}
			});
		}
	}

	var viewModel = new ChartModel();
	viewModel.loadData();

	ko.applyBindings(viewModel);
});