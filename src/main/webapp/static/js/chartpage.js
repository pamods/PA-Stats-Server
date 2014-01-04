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
		
		self.currentData = undefined;
		
		function processTime() {
			var timePointData = self.currentData.playerTimeData;
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
			
			// dont trust the user's time for this, all game data points are supposed to be server time
			$.get(queryUrl + "/time", function(timeMs) {
				var mightStillBeRunning = timeMs.ms - lastTime < gameIsLiveOffsetGuess;
				if (mightStillBeRunning) {
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
				window.setTimeout(function() {processTime();}, 3000);
			});
		}
		
		function updateByCurrentData() {
			self.currentData.info = self.currentData.playerInfo;
			self.currentData.timeData = self.currentData.playerTimeData;
			self.basicChart.updateData(self.currentData);
		}
		
		self.populateChart = function (data) {
			//console.log("populated");
			self.currentData = data;
			processTime();
			updateByCurrentData();
		}
		
		self.addData = function(data) {
			for (var i = 0; i < data.value.length; i++) {
				var playerPack = data.value[i];
				self.currentData.playerTimeData[playerPack.playerId].push(playerPack.data);
			}
			updateByCurrentData();
		}
	}

	var viewModel = new ChartModel();

	
	$(document).on("new-chart-data", function(event, data) {
		viewModel.addData(data);
	});
	
	ko.applyBindings(viewModel.basicChart, document.getElementById('chartbase'));

	var baseData = $('#chartDataSource').data("chart-info");
	window.setTimeout(function() {viewModel.populateChart(baseData);}, 500);
});