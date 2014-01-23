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
		var metalIncome = makeDefStat("metalIncome", "Metal income gross");
		var energyIncome = makeDefStat("energyIncome", "Energy income gross");
		var metalIncomeNet = makeDefStat("metalIncomeNet", "Metal income net");
		var energyIncomeNet = makeDefStat("energyIncomeNet", "Energy income net");
		var metalSpending = makeDefStat("metalSpending", "Metal spending");
		var energySpending = makeDefStat("energySpending", "Energy spending");
		
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
		
		var armyCount = makeDefStat("armyCount", "Units alive");
		var metalStored = makeDefStat("metalStored", "Metal stored");
		var energyStored = makeDefStat("energyStored", "Energy stored");
		var metalProduced = makeDefStat("metalProduced", "Metal produced", true);
		var metalWasted = makeDefStat("metalWasted", "Metal wasted", true);
		var energyProduced = makeDefStat("energyProduced", "Energy produced", true);
		var energyWasted = makeDefStat("energyWasted", "Energy wasted", true);
		
		self.sts = [ armyCount, buildSpeed, buildSpeedByMetal, buildSpeedByEnergy, metalIncome, metalSpending,
				metalIncomeNet, metalWasted, metalStored, metalProduced,
				energyIncome, energySpending, energyIncomeNet, energyWasted,
				energyStored, energyProduced, apm ]
		
		var firstTime = undefined;
		var lastTime = undefined;

		var xTimeFormat = function(n) {
			return fmtTime(firstTime, n);
		};
		
		var graphConf = {
				element : document.querySelector("#chartcontainer"),
				width : 700,
				height : 450,
				min : 'auto',
				interpolation : 'step-after',
				renderer : 'line'
		};
		
		var xAxisConf = {
			pixelsPerTick : 50,
			element : document.querySelector("#xaxis"),
			height : 20,
			tickFormat : xTimeFormat
		};
		
		var yAxisConf = {
			pixelsPerTick : 50,
			tickFormat : Rickshaw.Fixtures.Number.formatKMBT,
		};
		
		var hoverConf = {
			xFormatter: xTimeFormat,	
		};
		
		var sliderConf = {
			element: $("#slider")	
		};
		
		self.basicChart = new BasicChart({
			stats: self.sts,
			graph: graphConf,
			xAxis: xAxisConf,
			yAxis: yAxisConf,
			hover: hoverConf,
			slider: sliderConf
		});
		
		var showingLiveNote = false;
		
		self.currentData = {
			playerTimeData: {},
			playerInfo: {}
		};
		
		self.processTime = function() {
			
			var timePointData = self.currentData.playerTimeData;
			firstTime = undefined;
			lastTime = undefined;
			// data is always sorted before calling this function
			for ( var playerName in timePointData) {
				if (firstTime === undefined
						|| firstTime > timePointData[playerName][0].timepoint) {
					firstTime = timePointData[playerName][0].timepoint;
				}
				if (lastTime === undefined
						|| lastTime < timePointData[playerName][timePointData[playerName].length - 1].timepoint) {
					lastTime = timePointData[playerName][timePointData[playerName].length - 1].timepoint;
				}
			}
		}
		
		var lastUpdated = 0;
		
		self.setLastUpdate = function(v) {
			lastUpdated = v;
		}
		
		self.checkLiveProc = function() {
			var mightStillBeRunning = new Date().getTime() - lastUpdated < gameIsLiveOffsetGuess;
			if (mightStillBeRunning) {
				if (!showingLiveNote) {
					$("#livenote").show("slow");
					showingLiveNote = true;
				}
			} else {
				if (showingLiveNote) {
					$("#livenote").hide("slow");
					showingLiveNote = false;
				}
			}
			window.setTimeout(self.checkLiveProc, 500);
		}
		
		self.updateByCurrentData = function() {
			self.currentData.info = self.currentData.playerInfo;
			self.currentData.timeData = self.currentData.playerTimeData;
			self.basicChart.updateData(self.currentData);
		}
		
		self.addPlayer = function(data) {
			for (var i = 0; i < data.value.length; i++) {
				var player = data.value[i];
				if (self.currentData.playerInfo[player.id] === undefined) {
					self.currentData.playerInfo[player.id] = player.data;
				}
			}
			self.updateByCurrentData();
		}
		
		self.addData = function(data) {
			for (var i = 0; i < data.value.length; i++) {
				var playerPack = data.value[i];
				if (self.currentData.playerTimeData[playerPack.playerId] === undefined) {
					self.currentData.playerTimeData[playerPack.playerId] = [];
				}
				self.currentData.playerTimeData[playerPack.playerId].push(playerPack.data);
				self.currentData.playerTimeData[playerPack.playerId].sort(function(a, b) {
					return a.timepoint - b.timepoint;
				});
			}
			self.processTime();
			self.updateByCurrentData();
		}
	}

	var viewModel = new ChartModel();
	viewModel.checkLiveProc();
	
	var baseData = $('#chartDataSource').data("comet-info");
	var hadUpdate = false;
	
	if (baseData.hasComet) {
		$(document).on("new-chart-data", function(event, data) {
			if (hadUpdate) {
				viewModel.setLastUpdate(new Date().getTime());
			}
			hadUpdate = true;
			viewModel.addData(data);
		});
		$(document).on("new-player-data", function(event, data) {
			viewModel.addPlayer(data);
		});
	} else {
		$.getJSON(queryUrl + "?gameId="+$.urlParam("gameId"), function(data) {
			window.setTimeout(function() {
				viewModel.currentData = data;
				viewModel.processTime();
				viewModel.updateByCurrentData();
			}, 250);
		});
	}
	
	ko.applyBindings(viewModel.basicChart, document.getElementById('chartbase'));
});